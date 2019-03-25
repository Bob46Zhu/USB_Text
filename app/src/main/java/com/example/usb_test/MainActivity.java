package com.example.usb_test;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {


    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private TextView mTextMessage;
    private EditText Send_edt;
    private Button openBtn;
    private Button sendBtn;
    private TextView receive_textview;
    private TextView log_textview;
    private Context mContext;

    private String mystring=new String();
    private static final String TAG = "YGKJ";

    UsbManager manager;
    private UsbDevice device=null;
    UsbInterface[] usbinterface=null;
    UsbEndpoint[][] endpoint=new UsbEndpoint[5][5];
    UsbDeviceConnection connection=null;
    byte[] mybuffer=new byte[1024];
    private int myvid=1155,mypid=22336;

    DataTransfer mydatatransfer=new DataTransfer(1024);
    ConnectedThread mconnectedthread=null;
    //DrawContralThread mdrawcontralthread=null;
    boolean threadcontrol_ct=false;
    boolean threadcontrol_mt=false;
    boolean threadsenddata=false;
    boolean flag_rec = false;

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {

            switch(msg.what){
                case 1:
                    int datalen=mydatatransfer.GetDataLen();
                    byte[] mtmpbyte = new byte[datalen];
                    log_textview.append("datalen="+datalen+"\n");
                    if(mystring.length()>2048){
                        mystring=mystring.substring(datalen,mystring.length());
                    }
                    mydatatransfer.ReadMultiData(mtmpbyte,datalen);
                    //mystring = String.valueOf(mtmpbyte);
                    mystring = new String(mtmpbyte);
                    log_textview.append(mystring+"\n");

                    break;
                case 2:
                    receive_textview.append(mystring);
                    showToast(mystring);
                    log_textview.append("getBytes Lens="+mystring.getBytes().length+"\n");
                    log_textview.append("String.valueOf('5')= "+String.valueOf('5')+"\n");
                    log_textview.append("mystring lens="+mystring.length()+"\n");
                    log_textview.append("mystring :"+mystring+"\n");
                    //log_textview.append("getBytes ="+);
                    if(mystring == String.valueOf('5')){
                        log_textview.append("Target Connected\n");
                    }
                default:
                    break;
            }
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            finish();
        };
    };
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        threadcontrol_ct=false;
        threadcontrol_mt=false;
        showToast("USB设备拔出");
        super.onDestroy();
    }
//    private byte[] senddata = new byte[65503];
//    private byte[] receivedata = new byte[1024] ;

//    private UsbManager mUsbManager;
//    private UsbDevice mUsbDevice;
//    private PendingIntent mPendingIntent;
//    private UsbEndpoint mUsbEndpoinIn;
//    private UsbEndpoint mUsbEndpoinOut;
//    private UsbEndpoint mUsbEndpoinCTR;
//    private UsbEndpoint mIntUsbEndpointIn;
//    private UsbEndpoint mIntUsbEndpointOut;
//    private UsbInterface mUsbInterface1;
//    private UsbInterface mUsbInterface2;
//    private UsbDeviceConnection mUsbDeviceConnection;
//    private String sendMessage = "abcdefg";
//    private byte[] sendbyte = {22,3,3,44,55,66};


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UI_Init();
        log_textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = Send_edt.getText().toString();
                String filedetail = Send_edt.getText().toString();
                ExFileHelper exFileHelper = new ExFileHelper(mContext);
                try {
                    exFileHelper.saveFileToExStorage(filename,filedetail);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threadsenddata = true;
                showToast("usb已连接");
            }
        });

        IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");
        registerReceiver(mUsbReceiver, filter);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Log.e(TAG, "get device list  = " + deviceList.size());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            Log.i(TAG, "vid: " + device.getVendorId() + "\t pid: " + device.getProductId());
            if(device.getVendorId()==myvid&&device.getProductId()==mypid){
                break;
            }
        }
        if(device!=null&&device.getVendorId()==myvid&&device.getProductId()==mypid){
            log_textview.setText("找到设备: VID:"+ device.getVendorId() +
                    "\n pid: " + device.getProductId()+"\n");
        }
        else{
            log_textview.setText("找不到USB设备\n");
            finish();
            return;
        }

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if(manager.hasPermission(device)){
        }
        else{
            manager.requestPermission(device, pi);
        }

        if(manager.hasPermission(device)){
            log_textview.append("拥有访问权限\n");
        }
        else{
            log_textview.append("无访问权限\n");
        }

        log_textview.append("设备名："+device.getDeviceName()+"\n");

        log_textview.append("接口数为："+device.getInterfaceCount()+"\n");

        usbinterface=new UsbInterface[device.getInterfaceCount()];
        for(int i=0;i<device.getInterfaceCount();i++){
            usbinterface[i]=device.getInterface(i);
            log_textview.append("接口"+i+"的端点数为："+usbinterface[i].getEndpointCount()+"\n");
            for(int j=0;j<usbinterface[i].getEndpointCount();j++){
                endpoint[i][j]=usbinterface[i].getEndpoint(j);
                if(endpoint[i][j].getDirection()==0 ){
                    log_textview.append("端点"+j+"的数据方向为输入\n");
                }
                else{
                    log_textview.append("端点"+j+"的数据方向为输出\n");
                }
            }
        }

        threadcontrol_ct=true;
        threadcontrol_mt=true;
        if(mconnectedthread!=null){
            mconnectedthread=null;
        }
        mconnectedthread= new ConnectedThread();
        mconnectedthread.start();

//        if(mdrawcontralthread!=null){
//            mdrawcontralthread=null;
//        }
//
//        mdrawcontralthread=new DrawContralThread();
//        mdrawcontralthread.start();




//        OpenBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //枚举设备
//                enumerateDevice(mUsbManager);
//                //查找设备接口
//                getDeviceInterface();
//                //获取设备endpoint
//                assignEndpoint(mUsbInterface2);
//                //打开conn连接通道
//                openDevice(mUsbInterface2);
//            }
//        });
//
//        Send_Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //sendMessageToPoint(toByteArray(sendMessage));
//               // receiverMessageFromPoint();
//                senddata = Send_edt.getText().toString().getBytes();
//                sendMessageToPoint(senddata);
//
//
//            }
//        });




//        OpenBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                HashMap<String,UsbDevice> UsbDeviceList = mUsbManager.getDeviceList();
//                Iterator<UsbDevice> UsbDeviceIterator = UsbDeviceList.values().iterator();
//                while(UsbDeviceIterator.hasNext()){
//                    mUsbDevice = UsbDeviceIterator.next();
//                    if(mUsbManager.hasPermission(mUsbDevice)){
//                        log_text.append("PID:"+mUsbDevice.getProductId()+"\n");
//                        log_text.append("VID:"+mUsbDevice.getVendorId()+"\n");
//                        log_text.append("Device Name:"+mUsbDevice.getDeviceName()+"\n");
//                        log_text.append("DeviceID::"+mUsbDevice.getDeviceId()+"\n");
//                    }
//                    else {
//                        mUsbManager.requestPermission(mUsbDevice,amPendingIntent);
//                    }
//                }
//            }
//        });

//        Send_Btn.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            initCommunication(mUsbDevice);
//                                            sendToUsb(sendMessage);
//                                        }
//                                    });
//                Thread mUsbthread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (true){
//                            synchronized (this){
//                                byte [] bytes =new byte[mUsbEndpoinIn.getMaxPacketSize()];
//                                int ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpoinIn,bytes,bytes.length,5000);
//                                if (ret > 0){
//                                    StringBuilder stringBuilder = new StringBuilder(bytes.length);
//                                    for (byte b : bytes){
//                                        if (b != 0){
//                                            if (b == 2 ){
//                                                stringBuilder.append("receive a message!\n");
//                                            }
//                                            stringBuilder.append(Integer.toHexString(b));
//                                        }
//                                    }
//                                    log_text.append(stringBuilder.toString());
//                                }
//                            }
//                        }
//                        //mUsbDeviceConnection.close();
//                    }
//                });
//                mUsbthread.start();
//
//            }
//        });

                BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            }

            public void UI_Init(){
                log_textview = findViewById(R.id.log_text);
                receive_textview = findViewById(R.id.receive_msg);
                Send_edt = findViewById(R.id.send_EDT);
                openBtn = findViewById(R.id.OpenBtn);
                sendBtn = findViewById(R.id.Send_Btn);
            }

    //            网上DEMO
            //枚举USB设备
//            private void enumerateDevice(UsbManager usbManager){
//                    log_text.append("开始进行枚举设备！\n");
//                    if(usbManager == null){
//                        log_text.append("创建UsbManager失败，请重新启动应用!\n");
//                        return;
//                    }else {
//                        HashMap<String,UsbDevice> usbDeviceList = mUsbManager.getDeviceList();
//                        if (!(usbDeviceList.isEmpty())){
//                            //usbDeviceList不为空
//                            log_text.append("设备列表不为空\n");
//                            Iterator<UsbDevice> usbdeviceIterator = usbDeviceList.values().iterator();
//                            while(usbdeviceIterator.hasNext()){
//                                UsbDevice device = usbdeviceIterator.next();
//                                //输出设备信息
//                                log_text.append("设备信息:"+device.getVendorId()+","+device.getProductId()+"\n");
//                                if (device.getProductId() == 22336 && device.getVendorId() == 1155){
//                                    mUsbDevice = device;//获取UsbDevice
//                                    log_text.append("发现待匹配设备："+ device.getVendorId()+","+device.getProductId()+"\n");
//
//                                }
//                            }
//                        }else {
//                            log_text.append("请接入USB设备\n");
//                        }
//                    }
//            }

            //寻找设备接口
//    private void getDeviceInterface(){
//        if (mUsbDevice != null){
//            log_text.append("interfaceCounts:"+mUsbDevice.getInterfaceCount()+"\n");
//            for (int i = 0; i < mUsbDevice.getInterfaceCount();i++){
//                UsbInterface intf = mUsbDevice.getInterface(i);
//                if (i == 0){
//                    mUsbInterface1 = intf;//保存设备接口
//                    log_text.append("成功获得接口:"+mUsbInterface1.getId()+"\n");
//                }
//                if (i == 1){
//                    mUsbInterface2 = intf;
//                    log_text.append("成功获得接口："+mUsbInterface2.getId()+"\n");
//                }
//            }
//        }else {
//            log_text.append("设备为空！\n");
//        }
//    }


    //分配端点，IN\OUT 即输入输出，可以通过判断
//    private UsbEndpoint assignEndpoint(UsbInterface mInterface){
//
//        for (int i = 0; i < mInterface.getEndpointCount();i++){
//            UsbEndpoint ep = mInterface.getEndpoint(i);
//            //查找批量传输接口
//            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
//                if (ep.getDirection() == UsbConstants.USB_DIR_OUT){
//                    mUsbEndpoinOut = ep;
//                    log_text.append("Find the BulkEndpointOut,"+"Index:"+i+","+"使用端点号："+mUsbEndpoinOut.getEndpointNumber()+"\n");
//                }else{
//                    mUsbEndpoinIn = ep;
//                    log_text.append("Find the BulkEnpointIn:"+"Index:"+i+","+"使用端点号："+mUsbEndpoinIn.getEndpointNumber()+"\n");
//                }
//            }
//            //查找控制传输接口
//            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL){
//                mUsbEndpoinCTR = ep;
//                log_text.append("find the ControlEndpoint:"+"index"+i+","+mUsbEndpoinCTR.getEndpointNumber()+"\n");
//            }
//            //查找中断传输端口
//            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT){
//                if (ep.getDirection() == UsbConstants.USB_DIR_OUT){
//                    mIntUsbEndpointOut = ep;
//                    log_text.append("Find the InterruptEndpointOut:"+"index:"+i+","+mIntUsbEndpointOut.getEndpointNumber()+"\n");
//
//                }
//                if(ep.getDirection() == UsbConstants.USB_DIR_IN){
//                    mIntUsbEndpointIn = ep;
//                    log_text.append("find the InterruptEndpointIn:"+ "index:"+i+","+mIntUsbEndpointIn.getEndpointNumber()+"\n");
//
//                }
//            }
//        }
//        if (mUsbEndpoinOut == null &&mUsbEndpoinIn == null && mUsbEndpoinCTR == null && mIntUsbEndpointIn == null
//            && mIntUsbEndpointOut == null){
//            throw new IllegalArgumentException("not endpoint is founded!");
//        }
//        return mIntUsbEndpointIn;
//    }

            //打开设备
//    private void openDevice(UsbInterface mInterface){
//        if(mInterface != null)
//        {
//            UsbDeviceConnection conn = null;
//            //在open前判断是否有连接权限，对于连接权限可以静态分配，也可以动态分配权限
//            if(mUsbManager.hasPermission(mUsbDevice)){
//                conn = mUsbManager.openDevice(mUsbDevice);
//                log_text.append("设备有权限,打开成功\n");
//            }
//            else {
//                mUsbManager.requestPermission(mUsbDevice,mPendingIntent);
//            }
//            if(conn == null)
//            {
//                log_text.append("连接通道为空！\n");
//                return;
//            }
//            if(conn.claimInterface(mInterface,true)){
//                mUsbDeviceConnection = conn;
//                if(mUsbDeviceConnection != null)//到此应该已经连接上前面板了
//                    log_text.append("open设备成功!\n");
//                final String mySerial = mUsbDeviceConnection.getSerial();
//                log_text.append("设备serial number:"+ mySerial+"\n");
//            }else {
//                log_text.append("无法打开连接通道!\n");
//                conn.close();
//            }
//        }
//    }


    //发送数据
//    private void sendMessageToPoint(byte[] buffer){
//        //bulkOut 传输
//        if(mUsbDeviceConnection.bulkTransfer(mUsbEndpoinOut,buffer,buffer.length,0)<0)
//        {
//            log_text.append("BulkOut返回输出为负数\n");
//        }else {
//            log_text.append("send Message Succese!\n发送内容为："+"\n");
//        }
//    }

//    //从设备接收数据 bulkIn
//    private byte[] receiverMessageFromPoint(){
//        byte[] buffer = new byte[1024];
//        if(mUsbDeviceConnection.bulkTransfer(mUsbEndpoinIn,buffer,buffer.length,2000)<0)
//            log_text.append("bulkIn返回输出为 负数\n");
//        else {
//            log_text.append("Receive Message Succese\n");
//            log_text.append("收到的数据："+buffer.toString()+"\n");
//        }
//        return buffer;
//    }
//            private void initCommunication(UsbDevice usbDev) {
//                log_text.append("initCommunication\n");
//                if (usbDev.getProductId() == 22336 && usbDev.getVendorId() == 1155) {
//                    log_text.append("连接设备VID:" + usbDev.getVendorId());
//                    int interfaceCount = usbDev.getInterfaceCount();
//
//                        UsbInterface usbInterface = usbDev.getInterface(0);
//                        if ((UsbConstants.USB_CLASS_CDC_DATA != usbInterface.getInterfaceClass())
//                                && (UsbConstants.USB_CLASS_COMM != usbInterface.getInterfaceClass())) {
//
//                        }
//                        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
//                            UsbEndpoint ep = usbInterface.getEndpoint(i);
//                            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
//                                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
//                                    mUsbEndpoinIn = ep;
//                                } else {
//                                    mUsbEndpoinOut = ep;
//                                }
//                            }
//                        }
//                        if (mUsbEndpoinOut == null || mUsbEndpoinIn == null) {
//                            log_text.append("endpoint is null\n");
//                            mUsbEndpoinIn = null;
//                            mUsbEndpoinOut = null;
//                            mUsbInterface = null;
//                        } else {
//                            log_text.append("\nendpoint out:" + mUsbEndpoinOut + ",endpoint in:" + mUsbEndpoinIn.getAddress() + "\n");
//                            mUsbInterface = usbInterface;
//                            mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
//                        }
//
//                }
//            }
//
//            private void sendToUsb(String str) {
//                senddata = str.getBytes();
//                log_text.append("senddata:"+senddata+"\n");
//                log_text.append("context:"+str+"\n");
//                int ret = -1;
//                //发送准备命令
//                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpoinOut, senddata, senddata.length, 5000);
//                log_text.append("数据已发送\n"+"ret值为："+ret+"\n");
//                log_text.append("发送的数据长度："+senddata.toString().length()+"\n");
//                log_text.append(senddata+"\n");
//                //接收发送成功信息（相当于读取设备数据）
//                receivedata = new byte[128]; //根据设备实际情况写数据大小
//                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpoinIn, receivedata, receivedata.length, 10000);
//                Toast.makeText(MainActivity.this, String.valueOf(ret), Toast.LENGTH_SHORT).show();
//                log_text.append("接收的数据："+receivedata+"\n");
//                log_text.append("接收的数据长度："+receivedata.toString().length()+"\n");
//                log_text.append(String.valueOf(ret)+"\n");
//            }


    //字符串转数组
//    private String toHexString(byte[] arg, int length) {
//        String result = new String();
//        if (arg != null) {
//            for (int i = 0; i < length; i++) {
//                result = result
//                        + (Integer.toHexString(
//                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
//                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
//                        : arg[i])
//                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
//                        : arg[i])) + " ";
//            }
//            return result;
//        }
//        return "";
//    }




    //字符串转数组
//private byte[] toByteArray(String arg) {
//    if (arg != null) {
//        char[] NewArray = new char[1000];
//        char[] array = arg.toCharArray();
//        int length = 0;
//        for (int i = 0; i < array.length; i++) {
//            if (array[i] != ' ') {
//                NewArray[length] = array[i];
//                length++;
//            }
//        }
//        int EvenLength = (length % 2 == 0) ? length : length + 1;
//        if (EvenLength != 0) {
//            int[] data = new int[EvenLength];
//            data[EvenLength - 1] = 0;
//            for (int i = 0; i < length; i++) {
//                if (NewArray[i] >= '0' && NewArray[i] <= '9') {
//                    data[i] = NewArray[i] - '0';
//                } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
//                    data[i] = NewArray[i] - 'a' + 10;
//                } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
//                    data[i] = NewArray[i] - 'A' + 10;
//                }
//            }
//            byte[] byteArray = new byte[EvenLength / 2];
//            for (int i = 0; i < EvenLength / 2; i++) {
//                byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
//            }
//            return byteArray;
//        }
//    }
//    return new byte[] {};
//}


//  字符串转数组
//    private byte[] toByteArray2(String arg) {
//        if (arg != null) {
//            char[] NewArray = new char[1000];
//            char[] array = arg.toCharArray();
//            int length = 0;
//            for (int i = 0; i < array.length; i++) {
//                if (array[i] != ' ') {
//                    NewArray[length] = array[i];
//                    length++;
//                }
//            }
//            NewArray[length] = 0x0D;
//            NewArray[length + 1] = 0x0A;
//            length += 2;
//            byte[] byteArray = new byte[length];
//            for (int i = 0; i < length; i++) {
//                byteArray[i] = (byte)NewArray[i];
//            }
//            return byteArray;
//        }
//        return new byte[] {};
//    }

    class ConnectedThread extends Thread{
        @Override
        public void destroy() {
            // TODO Auto-generated method stub
            super.destroy();
        }
        public ConnectedThread(){
            if(connection!=null){
                connection.close();
            }
            connection = manager.openDevice(device);
            connection.claimInterface(usbinterface[1], true);
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            int datalength;
            while(threadcontrol_ct){
                if(threadsenddata){
                    threadsenddata=false;
                    byte[] mytmpbyte=Send_edt.getText().toString().getBytes();
                    connection.bulkTransfer(endpoint[1][0], mytmpbyte, mytmpbyte.length, 30);
                }
                datalength=connection.bulkTransfer(endpoint[1][1], mybuffer, 1024, 30);
                if(datalength>=0){
                    mHandler.obtainMessage(1).sendToTarget();
                    mHandler.obtainMessage(2).sendToTarget();
                }
                        //flag_rec = true;
                mydatatransfer.AddData(mybuffer, datalength);
                        //mybuffer = null;
            }
        }
    }
//    class DrawContralThread extends Thread{
//        public DrawContralThread(){
//
//        }
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            while(threadcontrol_mt){
//                try {
//                    sleep(200);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                if (flag_rec) {
//                    mHandler.obtainMessage(2).sendToTarget();
//                    flag_rec = false;
//                }
//            }
//        }
//
//    }

    public  void showToast(String str){
        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
    }




}