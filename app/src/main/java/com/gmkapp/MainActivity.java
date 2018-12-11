package com.gmkapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.app.DownloadManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.gmkapp.receiver.DownloadReceiver;
import com.gmkapp.util.CPreferences;
import com.gmkapp.settings.WebViewSetting;
import com.gmkapp.settings.MyWebViewClient;
import com.gmkapp.settings.MyWebChromeClient;
import com.gmkapp.util.RealPathUtil;
import com.gmkapp.util.SystemUtil;
import com.gmkapp.util.URLConstants;

/**
 * 애플리케이션 메인 웹뷰 초기화면...
 * 개발환경 : Android 5.1 ~ 7.0 test
 * 테스트 디바이스 : Samsung Galaxy S4 (Android 5.0.1) ~ S7 (Android 7.0)
 */

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {
    public final int REQUEST_CODE = 0;

    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final int INTRO_RESULTCODE = 1;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mFilePathCallback;
    public static String mCameraPhotoPath;
    public static final String TYPE_IMAGE = "image/*";
    private String activityName = "MainActivity";
    private String APP_VERSION = "";
    public static WebView webview;
    private Context context;
    private boolean isSecond = false;  // 하드웨어 Back Key를 두번 눌렀는지 체크
    private Timer timer; //하드웨어 Back Key를 두번 누르는 사이에 2초가 지났는지 체크
    public static String istrue = "false";

    private String url = null;
    private static boolean mResume = false;
    public static boolean mPause = false;
    private boolean mFlag = false;
    private DownloadReceiver mOnComplete = new DownloadReceiver();
    private String URL_DOMAIN = URLConstants.URL_DOMAIN;
    public static MainActivity mMainactivity;
    private WebSettings webSettings;
    private Map<String, String> mHeader = new HashMap<String, String>();
    private boolean goUrl = false;

    public Uri mImageCaptureUri = null;
    public String elementId = null;
    public final int PICK_FROM_GALLERY = 50001;
    public final int PICK_FROM_CAMERA = 50002;
    public final int CROP_FROM_CAMERA = 50003;

    public String callBase64ImageString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mMainactivity = this;

        webview = (WebView) findViewById(R.id.webview);
        webSettings = webview.getSettings();
        mHeader = WebViewSetting.setHeader(context);
        String tokenId = CPreferences.getPreferences(context,"tokenId");

        // 앱 루팅 체크로직
        if(isRooted()){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder
                    .setMessage("루트권한을 가진 디바이스에서는 실행할 수 없습니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {

                                    moveTaskToBack(true);
                                    finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());

                                }
                            });
            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();

            return;
        }
        isStoragePermissionGranted();
    }

    /**
     * 안드로이드 파일 관련 퍼미션 체크 성공 후 처리 할 로직
     */
    private void PermissionSuccess(){
        if(mFlag) {
            Intent i = new Intent(this, IntroActivity.class);
            startActivityForResult(i, INTRO_RESULTCODE);

            // 인트로 화면으로 바로 이동 처리 시킨다
            String IsFirst = CPreferences.getPreferences(context, "IsFirst");
            Log.d("MainActivity",">>> PermissionSuccess 1 MainActivity IsFirst : "+ IsFirst);
            if (IsFirst == null || "false".equals(IsFirst)) {
                CPreferences.setPreferences(context, "IsFirst", "true");
            }

            // 사용자 폰이 모델정보를 저장한다.
            CPreferences.setPreferences(context, "dModel", Build.MODEL);

            // 앱 버전 가져오기
            try {
                APP_VERSION = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                CPreferences.setPreferences(context, "dVersion", APP_VERSION);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 웹뷰 초기 세팅
            initWebView();

            // onResume 에서 호출할 함수 호출
            if(!mResume) {
                funcResume();
            }
        }
    }

    /**
     * onResume에서 호출될 함수를 별도 함수로 만들어 퍼미션 체크 후 호출 할 수 있도록 하였다.
     */
    private void funcResume(){
        String AUTO_LOGIN_TOKEN = CPreferences.getPreferences(context,"AUTO_LOGIN_TOKEN");
        mResume = true;
        // 푸시를 통해 들어온 경우 해당 url로 바로 진입 처리 하는 로직
        webview.loadUrl("file:///android_asset/www/index.html",WebViewSetting.setHeader(context));
        /*
        if (webview != null && goUrl) {
            if(CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN") != null && !CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN").isEmpty()){
                webview.loadUrl(URL_DOMAIN + URLConstants.MAIN_URL, WebViewSetting.setHeader(context)); //메인페이지
            }else{
                webview.loadUrl(URL_DOMAIN + URLConstants.LOGIN_URL, WebViewSetting.setHeader(context)); //로그인페이지
            }
        }
        */

    }

    /**
     * 루팅체크 함수
     * @return
     */
    private boolean isRooted() {
        boolean runtimeFlag = false;
        try{
            Runtime.getRuntime().exec("su");
            runtimeFlag = true;
        }catch(Exception e){
            runtimeFlag = false;
        }

        if(findBinary("su") || runtimeFlag){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 중요체크로직 체크 함수
     * @param binaryName
     * @return
     */
    public boolean findBinary(String binaryName) {
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

    @Override
    protected void onStart(){
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /**
        * 앱 최초 집입시에는 mFlag 가 false 이므로 호출되지 않을 것이지만 퍼미션 다이얼로그 박스 호출 뒤 사용자가
        * 퍼미션 수락 후 PermissionSuccess() 함수가 호출되면 아래 함수도 함께 호출 된다.
        **/
        if(!mResume) {
            funcResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 다운로드 리시버 풀기
        unregisterReceiver(mOnComplete);
        // 웹뷰 캐쉬 정리
        webViewClearCache();

    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 웹뷰 초기세팅
     */
    @SuppressWarnings("deprecation")
    public void initWebView() {

        webview.addJavascriptInterface(new AndroidBridge(), "AppDroid");    //Javascript와 통신을 위한 JavascriptInterface 추가
        webview.setVerticalScrollBarEnabled(false);             //웹뷰 자체 스크롤을 제거
        webview.setHorizontalScrollBarEnabled(false);           //웹뷰 자체 스크롤을 제거
        webview.setNetworkAvailable(true);                      //네트워크 상태 통지

        if(Build.VERSION.SDK_INT >= 21){
            webSettings.setMixedContentMode(0);
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        HttpURLConnection.setFollowRedirects(true);                         // 보안접속 사용설정
        webSettings.setPluginState(WebSettings.PluginState.ON);             // 플러그인 사용 설정
        webSettings.setUserAgentString(webSettings.getUserAgentString()
                .replace("Android", "gmkApp AppDroid Android")
                .replace("Chrome", ""));                  // 기본 브라우저와 Webview의 UserAgent값을 구분해주기위해 사용


        webSettings.setJavaScriptEnabled(true);                             // Javascript 허용
        webSettings.setBuiltInZoomControls(true);                          // 줌 아이콘 사용 유무
        webSettings.setSupportZoom(true);                                   // 줌 사용여부 체크
        webSettings.setSupportMultipleWindows(true);                        // 여러개의 윈도우를 사용할 수 있도록 허용
        webSettings.setAppCacheEnabled(true);                               // 앱 캐시 허용
        webSettings.setAllowFileAccess(true);                               // 웹뷰 내에서 파일 접근 가능 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);         // Javascript에서 window.open() 사용 허용
        webSettings.setLoadsImagesAutomatically(true);                      // 웹뷰가 앱에 등록된 이미지 리소스를 자동으로 로드하도록 설정
        webSettings.setUseWideViewPort(true);                              // Wide Viewport 사용 유무
        webSettings.setSavePassword(false);                                 // 패스워드 저장 유무를 묻는창을 띄우지 않음
        webSettings.setDomStorageEnabled(true);                           // 로컬스토리지 허용
        //webSettings.setGeolocationEnabled(true);                          // 위치정보 사용 유무
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);

        // front 에서 아래 meta tag를 삽입 해 주어야 한다. (아이폰은 상관없지만 안드로이드 때문에 해 줘야 한다)
        // <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=2.0, minimum-scale=1.0, user-scalable=yes,target-densitydpi=medium-dpi">


        // 사용자 폰 버젼이 21 Android 5.0(LOLLIPOP) 버젼이상일 경우
        // 혼합된 컨텐츠와 서드파티 쿠키가 설정에 따라 Webview 에서 Block 시키는 게 기본으로 바뀌어 아래 코드를 적용 시켜줘야 함
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); //항상허용

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webview, true);
        }

        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.setWebViewClient(new MyWebViewClient(context, activityName));       // WebViewClient (front 에서 오고가는 http url 을 인터셉터 하여 네이티브에서 가공처리 할 수 있다)
        webview.setWebChromeClient(new MyWebChromeClient(context, mMainactivity));                 // WebChromeClient (javascript alert, dialog 를 네이티브 다이얼로그 방식으로 표현해 준다.)

    }


    /**
     * 하드웨어 Back Key 입력시 이벤트
     * 웹뷰의 히스토리가 있으면 뒤로가기 없으면 두번 누르면 종료
     *
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (webview.canGoBack() && !webview.getUrl().contains("/mobile/main/")) {
                webview.goBack();
                return true;
            }

            if (isSecond == false) { // 첫번째인 경우
                Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_LONG).show();
                isSecond = true;
                // Back키가 2초내에 두번 눌렸는지 감지
                TimerTask second = new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        timer = null;
                        isSecond = false;
                    }
                };
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer();
                timer.schedule(second, 2000);
            } else {
                moveTaskToBack(true);
                CPreferences.setPreferences(context,"IsFirst","false");
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        return true;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    /**
     * 권한체크시 반드시 Override 하여 사용 해야 한다.
     * 해당 함수를 통해 사용자가 퍼미션 확인/취소 버튼 이벤트값이 넘어오기 때문이다
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
                // 허가받았을 때 처리
                mFlag = true;
                PermissionSuccess();
                goUrl = true;
            }else{
                // 권한 거부 때 처리
                mFlag = false;
                Toast.makeText(getApplicationContext(), "파일쓰기권한을 체크해야 합니다", Toast.LENGTH_LONG).show();
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mImageCaptureUri = FileProvider.getUriForFile(context, "com.gmkapp.fileprovider", new File(Environment.getExternalStorageDirectory(), url));
            }else{
                mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            }


            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
            dialog.dismiss();
        }
    };

    DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_GALLERY);
            dialog.dismiss();
        }
    };

    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();
        }
    };

    /**
     * 네이티브 to Javascript 통신 메소드
     * 네이티브에서 호출하는 방법 : webview.loadUrl("javascript:alert(1);");
     *
     */
    public static void callJavascript(final String script) {
        webview.post(new Runnable() {
            public void run() {
                webview.loadUrl(script);
            }
        });
    }

    /**
     * 액티비티 내 웹뷰가 아닌 외부 브라우저로 호출
     *
     */
    public void callOutBrowser(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 웹뷰 캐시 삭제
     *
     */
    @SuppressWarnings("deprecation")
    public void webViewClearCache() {
        webview.clearHistory();
        webview.clearCache(true);

        CookieSyncManager cookieSyncManager = CookieSyncManager
                .createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieSyncManager.sync();

        MainActivity.this.deleteDatabase("webview.db");
        MainActivity.this.deleteDatabase("webviewCache.db");

        clearCacheFolder(context.getCacheDir());
    }

    /**
     * 웹뷰 캐시 청소
     * @param dir
     * @return
     */
    public int clearCacheFolder(final File dir){
        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child);
                    }

                    if (child.delete()) {
                        deletedFiles++;
                    }
                }
            }
            catch(Exception e) {

            }
        }
        return deletedFiles;
    }


    /**
     * 로컬스토로지 권한 요청
     * @return
     */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mFlag = true;
                PermissionSuccess();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE);
                mFlag = false;
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            mFlag = true;
            return true;
        }
    }

    /**
     * Javascript to 네이티브 통신
     * Javascript에서 호출하는 방법 : window.SampleApp.getData("Call Android!!");
     * JavascriptInterface를 추가할 때 네이밍과 AndroidBridge 클래스 내 메소드 이름을 조합해서 호출
     *
     */

    private class AndroidBridge {

        @JavascriptInterface
        public String getVariable(final String key) {
            return CPreferences.getPreferences(context, key);
        }

        @JavascriptInterface
        public void setVariable(final String key, final String value) {
            CPreferences.setPreferences(context, key, value);
        }
        @JavascriptInterface
        public void callOutBrowser(final String url) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void setWebviewUrl(final String url) {
            webview.loadUrl(url,WebViewSetting.setHeader(context));
        }

        @JavascriptInterface
        public void getCamera(final String retFunc) {
            callBase64ImageString = "javascript:eval(\"var func = " + retFunc.replaceAll("\"","'") + "\");";
            cameraPermissionCheck();
        }

        @JavascriptInterface
        public void downloadVideo(final String url, final String ext) {
            mMainactivity.istrue = "false";
            DownloadManager mdDownloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(
                    Uri.parse(url));
            File destinationFile = new File(
                    Environment.getExternalStorageDirectory(),
                    SystemUtil.getFileName(url, ext));
            req.setDescription("파일 다운로드 중입니다..");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationUri(Uri.fromFile(destinationFile));
            mdDownloadManager.enqueue(req);

            registerReceiver(mOnComplete, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        }

        @JavascriptInterface
        public void fileDownload(final String url, final String ext) {
            mMainactivity.istrue = "false";
            DownloadManager mdDownloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(
                    Uri.parse(url));
            File destinationFile = new File(
                    Environment.getExternalStorageDirectory(),
                    SystemUtil.getFileName(url, ext));
            req.setDescription("파일 다운로드 중입니다..");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationUri(Uri.fromFile(destinationFile));
            mdDownloadManager.enqueue(req);

            registerReceiver(mOnComplete, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        }

    }

    /*
     카메라 기능 추가 건으로 기존 함수를 막고 아래 함수를 사용 한다. 해당 함수 역할을 확인 할 필요 있음 사제 하지 말 것
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri result = getResultUri(data);

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if(requestCode == INTRO_RESULTCODE && resultCode == RESULT_CANCELED) {
            // webview url 호출 함수
            goUrl = true;
            funcResume();

        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    */

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode != RESULT_OK)

            return;


        switch (requestCode) {

            case PICK_FROM_GALLERY:
            {

                mImageCaptureUri = data.getData();

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                intent.putExtra("outputX", 320);
                intent.putExtra("outputY", 320);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("crop", "true");
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_CAMERA);

                break;
            }
            case PICK_FROM_CAMERA: {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    this.grantUriPermission("com.android.camera", mImageCaptureUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                //List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    grantUriPermission("com.android.camera.action.CROP", mImageCaptureUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

                intent.putExtra("outputX", 320);
                intent.putExtra("outputY", 320);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                intent.putExtra("return-data", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                Intent i = new Intent(intent);


                startActivityForResult(i, CROP_FROM_CAMERA);



                break;

            }


            case CROP_FROM_CAMERA:
            {

                final Bundle extras = data.getExtras();
                Bitmap photo = null;
                if(extras != null)
                {
                    photo = extras.getParcelable("data");
                }else{
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);


                        photo = getCroppedBitmap(bitmap);




                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String base64image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);



                webview.loadUrl(callBase64ImageString + "func('"+base64image+"');");



                File file = new File(mImageCaptureUri.getPath());
                if (file.exists()) {
                    file.delete();
                }


            }

        }
    }
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public void chooseImage(){
        new AlertDialog.Builder(this)
                .setTitle("선택")
                .setPositiveButton("카메라", cameraListener)
                .setNeutralButton("갤러리", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();
    }

    public void cameraPermissionCheck(){

        if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }else{

            chooseImage();

        }

    }

}
