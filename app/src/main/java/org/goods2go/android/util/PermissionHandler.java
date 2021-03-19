package org.goods2go.android.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionHandler {

    public static final int LOCATION_REQUEST = 514;
    public static final int CAMERA_REQUEST = 284;

    public static void requestLocationPermission(Activity activity){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_REQUEST);
    }


    public static void requestCameraPermission(Activity activity){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_REQUEST);
    }

    public static int handleRequestResult(int requestCode,
                                              String[] permissions,
                                              int[] grantResults){
        if (requestCode == LOCATION_REQUEST) {
            if((permissions.length == 1
                    && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                return 0;
            }
            return requestCode;
        }
        if(requestCode == CAMERA_REQUEST){
            if((permissions.length == 1
                    && permissions[0].equals(Manifest.permission.CAMERA)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                return 0;
            }
            return requestCode;
        }
        return -1;
    }
}
