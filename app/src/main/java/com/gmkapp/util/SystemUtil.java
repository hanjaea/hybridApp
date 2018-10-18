package com.gmkapp.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by bluemobile on 2018. 4. 10..
 */

public class SystemUtil {

    /**
     * Handler 처리
     *
     * @param hd  : 핸들러 객체
     * @param key : 구분
     */
    public static void setHandler(Handler hd, int key) {

        Message msg = new Message();
        Bundle bundle = new Bundle();

        bundle.putInt("STAGE", key);

        msg.setData(bundle);
        hd.sendMessage(msg);

    }

    /**
     * String null 체크
     *
     * @param txt
     */
    public static boolean isNull(String txt) {
        if (TextUtils.isEmpty(txt)) {
            return true;
        } else {
            if (txt == null || txt.equals("") || txt.equals("null") || txt.equals(" "))
                return true;
            else if (txt.trim().length() <= 0)
                return true;
            else return false;
        }
    }


    /**
     * App Version 가져오기
     *
     * @param context
     */
    public static String getAppVersion(Context context) {
        String appVersion = null;

        try {

            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            appVersion = i.versionName;

        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVersion;
    }

    public static String getAppVersionStr(Context context) {
        String appVersion = null;

        try {

            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            appVersion = i.versionName;
            appVersion = appVersion.replaceAll("\\.","");

        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVersion;
    }


    public static int getAppVersionInt(Context context) {
        int appVersion = 0;

        try {

            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = i.versionName;
            String conver = "0";
            if(version != null){
                conver = version.replaceAll("\\.","");
                appVersion = Integer.parseInt(conver);
            }
            //appVersion = i.versionName;

        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVersion;
    }


    /**
     * app 버젼정보 확인용
     * @param context
     * @return
     */
    public static String getAppVersionString(Context context) {
        String appVersion = "";

        try {

            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = i.versionName;
            String conver = "0";
            if(version != null){
                //conver = version.replaceAll("\\.","");
                //appVersion = Integer.parseInt(conver);
                appVersion = version;
            }
            //appVersion = i.versionName;

        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVersion;
    }


    private static boolean isSDCardWriteable(Context context) {
        boolean rc = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            rc = true;
        }
        return rc;
    }


    /**
     * 루팅된 폰인지 확
     * @return
     */
    public static boolean isRooted() {
        return findBinary("su");
    }


    /**
     * device에 저장된 계정 binary 확인
     * @param binaryName
     * @return
     */
    public static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                    "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if ( new File( where + binaryName ).exists() ) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }


    /**
     * File name from URL
     *
     * @param url
     * @return
     */
    public static String getFileName(String url) {
        String filenameWithoutExtension = "";
        String fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
        String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));


        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cDate);

        //System.currentTimeMillis()
        Log.d(">>>> MyWebViewClient fileName : ", fileName);
        Log.d(">>>> MyWebViewClient fileNameWithoutExtn : ", fileNameWithoutExtn);
        //filenameWithoutExtension = String.valueOf(fDate+"_"+fileName);
        filenameWithoutExtension = String.valueOf(fDate+ ".mp4");
        return filenameWithoutExtension;
    }


}
