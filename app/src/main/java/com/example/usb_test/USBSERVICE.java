package com.example.usb_test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class USBSERVICE extends Service {
    public USBSERVICE() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
