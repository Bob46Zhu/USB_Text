package com.example.usb_test;

import android.content.Context;
import android.os.Environment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExFileHelper {

    private Context context;
    public ExFileHelper(){}

    public ExFileHelper(Context context){
        super();
        context = context;
    }

    //向外部存储器写入文件的方法
    public void saveFileToExStorage(String filename,String filecontent) throws IOException {
        filename = Environment.getExternalStorageDirectory().getAbsolutePath()+filename;
        FileOutputStream outputStream = new FileOutputStream(filename);
        outputStream.write(filecontent.getBytes());
        //将string 字符串以字节流的形式写入到输出 流中
        outputStream.close();
    }

    public String readFromExStorage(String filename) throws IOException{
        StringBuilder sb = new StringBuilder("");
        filename = Environment.getExternalStorageDirectory().getCanonicalPath()+filename;
        //打开文件输入流
        FileInputStream inputStream = new FileInputStream(filename);
        byte[] temp = new byte[2048];
        int len = 0;
        //读取文件内容：
        while((len = inputStream.read(temp)) > 0){
            sb.append(new String(temp,0,len));
        }
        inputStream.close();
        return sb.toString();
    }


}
