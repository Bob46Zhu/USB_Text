package com.example.usb_test;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private EditText Send_edt;
    private Button OpenBtn;
    private Button Send_Btn;

    private byte[] senddata = new byte[65503];
    private byte[] receivedata = new byte[1024] ;

    private TextView log_text;
    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private PendingIntent mPendingIntent;
    private UsbEndpoint mUsbEndpoinIn;
    private UsbEndpoint mUsbEndpoinOut;
    private UsbEndpoint mUsbEndpoinCTR;
    private UsbEndpoint mIntUsbEndpointIn;
    private UsbEndpoint mIntUsbEndpointOut;
    private UsbInterface mUsbInterface1;
    private UsbInterface mUsbInterface2;
    private UsbDeviceConnection mUsbDeviceConnection;
    private String sendMessage = "abcdefg";
    private byte[] sendbyte = {22,3,3,44,55,66};


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

        mTextMessage = (TextView) findViewById(R.id.message);
        OpenBtn = findViewById(R.id.OpenBtn);
        log_text = findViewById(R.id.log_text);
        Send_Btn = findViewById(R.id.Send_Btn);
        Send_edt = findViewById(R.id.send_EDT);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        OpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //枚举设备
                enumerateDevice(mUsbManager);
                //查找设备接口
                getDeviceInterface();
                //获取设备endpoint
                assignEndpoint(mUsbInterface2);
                //打开conn连接通道
                openDevice(mUsbInterface2);
            }
        });

        Send_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessageToPoint(toByteArray(sendMessage));
               // receiverMessageFromPoint();
                senddata = Send_edt.getText().toString().getBytes();
                sendMessageToPoint(senddata);


            }
        });




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


//            网上DEMO
            //枚举USB设备
            private void enumerateDevice(UsbManager usbManager){
                    log_text.append("开始进行枚举设备！\n");
                    if(usbManager == null){
                        log_text.append("创建UsbManager失败，请重新启动应用!\n");
                        return;
                    }else {
                        HashMap<String,UsbDevice> usbDeviceList = mUsbManager.getDeviceList();
                        if (!(usbDeviceList.isEmpty())){
                            //usbDeviceList不为空
                            log_text.append("设备列表不为空\n");
                            Iterator<UsbDevice> usbdeviceIterator = usbDeviceList.values().iterator();
                            while(usbdeviceIterator.hasNext()){
                                UsbDevice device = usbdeviceIterator.next();
                                //输出设备信息
                                log_text.append("设备信息:"+device.getVendorId()+","+device.getProductId()+"\n");
                                if (device.getProductId() == 22336 && device.getVendorId() == 1155){
                                    mUsbDevice = device;//获取UsbDevice
                                    log_text.append("发现待匹配设备："+ device.getVendorId()+","+device.getProductId()+"\n");

                                }
                            }
                        }else {
                            log_text.append("请接入USB设备\n");
                        }
                    }
            }

            //寻找设备接口
    private void getDeviceInterface(){
        if (mUsbDevice != null){
            log_text.append("interfaceCounts:"+mUsbDevice.getInterfaceCount()+"\n");
            for (int i = 0; i < mUsbDevice.getInterfaceCount();i++){
                UsbInterface intf = mUsbDevice.getInterface(i);
                if (i == 0){
                    mUsbInterface1 = intf;//保存设备接口
                    log_text.append("成功获得接口:"+mUsbInterface1.getId()+"\n");
                }
                if (i == 1){
                    mUsbInterface2 = intf;
                    log_text.append("成功获得接口："+mUsbInterface2.getId()+"\n");
                }
            }
        }else {
            log_text.append("设备为空！\n");
        }
    }


    //分配端点，IN\OUT 即输入输出，可以通过判断
    private UsbEndpoint assignEndpoint(UsbInterface mInterface){

        for (int i = 0; i < mInterface.getEndpointCount();i++){
            UsbEndpoint ep = mInterface.getEndpoint(i);
            //查找批量传输接口
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK){
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT){
                    mUsbEndpoinOut = ep;
                    log_text.append("Find the BulkEndpointOut,"+"Index:"+i+","+"使用端点号："+mUsbEndpoinOut.getEndpointNumber()+"\n");
                }else{
                    mUsbEndpoinIn = ep;
                    log_text.append("Find the BulkEnpointIn:"+"Index:"+i+","+"使用端点号："+mUsbEndpoinIn.getEndpointNumber()+"\n");
                }
            }
            //查找控制传输接口
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_CONTROL){
                mUsbEndpoinCTR = ep;
                log_text.append("find the ControlEndpoint:"+"index"+i+","+mUsbEndpoinCTR.getEndpointNumber()+"\n");
            }
            //查找中断传输端口
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT){
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT){
                    mIntUsbEndpointOut = ep;
                    log_text.append("Find the InterruptEndpointOut:"+"index:"+i+","+mIntUsbEndpointOut.getEndpointNumber()+"\n");

                }
                if(ep.getDirection() == UsbConstants.USB_DIR_IN){
                    mIntUsbEndpointIn = ep;
                    log_text.append("find the InterruptEndpointIn:"+ "index:"+i+","+mIntUsbEndpointIn.getEndpointNumber()+"\n");

                }
            }
        }
        if (mUsbEndpoinOut == null &&mUsbEndpoinIn == null && mUsbEndpoinCTR == null && mIntUsbEndpointIn == null
            && mIntUsbEndpointOut == null){
            throw new IllegalArgumentException("not endpoint is founded!");
        }
        return mIntUsbEndpointIn;
    }

            //打开设备
    private void openDevice(UsbInterface mInterface){
        if(mInterface != null)
        {
            UsbDeviceConnection conn = null;
            //在open前判断是否有连接权限，对于连接权限可以静态分配，也可以动态分配权限
            if(mUsbManager.hasPermission(mUsbDevice)){
                conn = mUsbManager.openDevice(mUsbDevice);
                log_text.append("设备有权限,打开成功\n");
            }
            else {
                mUsbManager.requestPermission(mUsbDevice,mPendingIntent);
            }
            if(conn == null)
            {
                log_text.append("连接通道为空！\n");
                return;
            }
            if(conn.claimInterface(mInterface,true)){
                mUsbDeviceConnection = conn;
                if(mUsbDeviceConnection != null)//到此应该已经连接上前面板了
                    log_text.append("open设备成功!\n");
                final String mySerial = mUsbDeviceConnection.getSerial();
                log_text.append("设备serial number:"+ mySerial+"\n");
            }else {
                log_text.append("无法打开连接通道!\n");
                conn.close();
            }
        }
    }


    //发送数据
    private void sendMessageToPoint(byte[] buffer){
        //bulkOut 传输
        if(mUsbDeviceConnection.bulkTransfer(mUsbEndpoinOut,buffer,buffer.length,0)<0)
        {
            log_text.append("BulkOut返回输出为负数\n");
        }else {
            log_text.append("send Message Succese!\n发送内容为："+"\n");
        }
    }

    //从设备接收数据 bulkIn
    private byte[] receiverMessageFromPoint(){
        byte[] buffer = new byte[1024];
        if(mUsbDeviceConnection.bulkTransfer(mUsbEndpoinIn,buffer,buffer.length,2000)<0)
            log_text.append("bulkIn返回输出为 负数\n");
        else {
            log_text.append("Receive Message Succese\n");
            log_text.append("收到的数据："+buffer.toString()+"\n");
        }
        return buffer;
    }
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
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }




    //字符串转数组
private byte[] toByteArray(String arg) {
    if (arg != null) {
        char[] NewArray = new char[1000];
        char[] array = arg.toCharArray();
        int length = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != ' ') {
                NewArray[length] = array[i];
                length++;
            }
        }
        int EvenLength = (length % 2 == 0) ? length : length + 1;
        if (EvenLength != 0) {
            int[] data = new int[EvenLength];
            data[EvenLength - 1] = 0;
            for (int i = 0; i < length; i++) {
                if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                    data[i] = NewArray[i] - '0';
                } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                    data[i] = NewArray[i] - 'a' + 10;
                } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                    data[i] = NewArray[i] - 'A' + 10;
                }
            }
            byte[] byteArray = new byte[EvenLength / 2];
            for (int i = 0; i < EvenLength / 2; i++) {
                byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
            }
            return byteArray;
        }
    }
    return new byte[] {};
}


//  字符串转数组
    private byte[] toByteArray2(String arg) {
        if (arg != null) {
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            NewArray[length] = 0x0D;
            NewArray[length + 1] = 0x0A;
            length += 2;
            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)NewArray[i];
            }
            return byteArray;
        }
        return new byte[] {};
    }



}