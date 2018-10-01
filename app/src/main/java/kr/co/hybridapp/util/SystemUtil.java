package kr.co.hybridapp.util;

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
     * 비콘 거리 계산 시 사용 할 함수
     *
     * @param txPower
     * @param rssi
     * @return
     */
    public static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        Log.d("SystemUtil ", "calculating accuracy based on rssi of " + rssi);


        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            Log.d("SystemUtil ", " avg rssi: " + rssi + " accuracy: " + accuracy);
            return accuracy;
        }
    }

    /**
     * 해당 Mac address 정보 가져오기
     * @param context
     * @return
     */
    /*
    public static String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "device has no macAddress wifi";//device has no macaddress or wifi is disabled
        }
        return macAddress;
    }
    */


    /**
     * IOT 서버로 던지기 후 리턴 받은 json 결과값 확인 용
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }




    /*
      *  파일 스트링으로 읽기
      *  @param filePath
      */
    public static String loadFileAsString(String filePath) throws IOException {
        StringBuffer data = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            data.append(readData);
        }
        reader.close();
        return data.toString();
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


    /*
     *  문자열 반대 수서로 변환하기
     *
     * @param  txt 문자
     */
    public static String getReverseString(String txt) {
        if(isNull(txt))
            return "";
        return ( new StringBuffer(txt) ).reverse().toString();
    }


    /*
   * 파일 유무 확인
    */
    public static boolean isFile(Context context, String fileName){
        File file= new  File(Environment.getExternalStoragePublicDirectory("/passengerGuideBS"), fileName);
        if (file.exists())
            return true;
        else
            return false;
    }
    /*
   * 파일 삭제
    */
    public static void setFileDel(Context context, String fileName){
        File file= new  File(Environment.getExternalStoragePublicDirectory("/passengerGuideBS"), fileName);
        file.delete();
    }

    public static File getFile(Context context, String fileName){
        return new  File(Environment.getExternalStoragePublicDirectory("/passengerGuideBS"), fileName);
    }

    public static File getDocumentsFile(Context context, String fileName){
        return new  File(Environment.getExternalStoragePublicDirectory("/passengerGuideBS"), fileName);
    }


    public static void removeDir(String fileDir){
        File file= new  File(fileDir);
        File[] childFileList = file.listFiles();

        for(File childFile : childFileList)
        {
            if(childFile.isDirectory()) {
                removeDir(childFile.getAbsolutePath());    //하위 디렉토리
            }
            else {
                childFile.delete();    //하위 파일
            }
        }
        file.delete();    //root 삭제
    }

    /** 사용가능한 내장 메모리 크기를 가져온다 */
    public static long getInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long size = availableBlocks * blockSize;
        if(size >= 1024){
            size /= 1024;
            if(size >= 1024){
                size /=1024;
            }
            else {
                size = 0;
            }
        }
        else {
            size =0;
        }
        return size;
    }



    public static void backupDatabase(Context context) throws IOException {
        String DB_FILEPATH = "/data/user/0/com.bluenmobile.holabusTracker/databases/Hola_Bus_Tracker.db";
        //String DB_FILEPATH = "/storage/emulated/0/bluenmobile/databases/Hola_Bus.db";
        if (isSDCardWriteable(context)) {
            // Open your local db as the input stream
            String inFileName = DB_FILEPATH;
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = Environment.getExternalStorageDirectory() + "/Hola_Bus";
            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();
        }
    }

    public static void sqliteExport(Context context){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/storage/emulated/0/bluenmobile/databases/Hola_Bus_Tracker.db";
                String backupDBPath = "Hola_Bus";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                if(backupDB.exists()){
                    Toast.makeText(context, "DB Export Complete!!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
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
     * DB 파일 저장
     *
     * @param context
     * @param is
     */
    public static void saveDBFile(Context context, InputStream is) throws Exception {
        int BUFFER_SIZE = 1024 * 2;
        FileOutputStream fos = null;
        ZipInputStream zis = null;
        ZipEntry zentry = null;
        if (is == null)
            return;


        try {
            zis = new ZipInputStream(is);
            zentry = zis.getNextEntry();
            while ((zentry = zis.getNextEntry()) != null) {
                String fileNameToUnzip = zentry.getName();
                File databaseFile = context.getDatabasePath(fileNameToUnzip);
                if (databaseFile.exists())
                    databaseFile.delete();
                else
                    databaseFile.getParentFile().mkdir();
                fos = new FileOutputStream(databaseFile);

                byte[] buffer = new byte[BUFFER_SIZE];
                int len = 0;
                zis.getNextEntry();
                while ((len = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }

            zis.closeEntry();
        } finally {

            if (zis != null)
                zis.close();
            if (fos != null) {
                fos.close();
            }
        }
    }


    /**
     * 로컬 DB  SDcard 로 export
     * @param context
     */
    public static void exportDB(Context context, String dbpath){
        final String DB_NAME = "Hola_Bus.db";
        //File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        //File data = Environment.getExternalStorageDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        //String currentDBPath = "/data/"+ "com.bluenmobile.ohnaebus" +"/databases/"+DB_NAME;
        String sd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/holabus/";
        String currentDBPath = dbpath;
        String backupDBPath = DB_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(context, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    public static void sqliteExport(Context context, String dbpath){
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = dbpath;
                String backupDBPath = "HolaBus.sqlite";
                String local = Environment.getExternalStorageDirectory().getAbsolutePath() + "/holabus/";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(local, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                if(backupDB.exists()){
                    Toast.makeText(context, "DB Export Complete!!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
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

    // 현재 로컬 db에 들어 있는 값이 앱 실행 시 받아온 값보다 예전 일자인지 확인
    public static boolean compareData(String d1, String d2){
        boolean retflag = true;
        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date1 = null;
        try {
            date1 = dateFormat.parse(d1);
            Date date2 = dateFormat.parse(d2);
            retflag = date1.after(date2);
            //date1이 date2보다 이후 일때 true, 아니면 false
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retflag;
    }

    /**
     * 년월일 시분초
     * @return
     */
    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * 년월일 시분초
     * @return
     */
    public static String getDateTimeFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "YYYYMMddHHmm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean compareTime(String d1, String d2){
        boolean retflag = true;
        SimpleDateFormat dateFormat = new  SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date1 = null;
        try {
            date1 = dateFormat.parse(d1);
            Date date2 = dateFormat.parse(d2);
            retflag = date1.after(date2);
            //date1이 date2보다 이후 일때 true, 아니면 false
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retflag;
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
