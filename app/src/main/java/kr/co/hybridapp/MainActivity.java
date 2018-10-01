package kr.co.hybridapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.app.DownloadManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.hybridapp.receiver.DownloadReceiver;
import kr.co.hybridapp.util.CPreferences;
import kr.co.hybridapp.settings.WebViewSetting;
import kr.co.hybridapp.settings.MyWebViewClient;
import kr.co.hybridapp.settings.MyWebChromeClient;
import kr.co.hybridapp.util.SystemUtil;
import kr.co.hybridapp.util.URLConstants;

/**
 * 애플리케이션 메인 웹뷰 초기화면...
 * 개발환경 : Android 5.1 ~ 7.0
 * 테스트 디바이스 : Samsung Galaxy S4 (Android 5.0.1) ~ S7 (Android 7.0)
 */

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {
    public final int REQUEST_CODE = 0;

    private final String TAG = "MainActivity";
    private String activityName = "MainActivity";
    private String APP_VERSION = "";
    private WebView webview;
    private Context context;
    private boolean isSecond = false;  // 하드웨어 Back Key를 두번 눌렀는지 체크
    private Timer timer; //하드웨어 Back Key를 두번 누르는 사이에 2초가 지났는지 체크

    private String url = null;
    private boolean mResume = false;
    private boolean mFlag = false;
    private DownloadReceiver mOnComplete = new DownloadReceiver();
    private String URL_DOMAIN = URLConstants.URL_DOMAIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(">>> 1 MainActivity onCreate ", "onCreate");
        context = this;

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
        /*
        if(!isStoragePermissionGranted()){
            moveTaskToBack(true);
            CPreferences.setPreferences(context,"IsFirst","false");
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        */

        Log.d(">>>> isStoragePermissionGranted", String.valueOf(mFlag));

    }

    /**
     * 안드로이드 파일 관련 퍼미션 체크 성공 후 처리 할 로직
     */
    private void PermissionSuccess(){
        if(mFlag) {
            // 인트로 화면으로 바로 이동 처리 시킨다
            String IsFirst = CPreferences.getPreferences(context, "IsFirst");
            Log.d(">>> 1 MainActivity IsFirst : ", IsFirst);
            if (IsFirst == null || "false".equals(IsFirst)) {
                CPreferences.setPreferences(context, "IsFirst", "true");
                startActivity(new Intent(context, SplashActivity.class));
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
        Log.d(">>>  onResume AUTO_LOGIN_TOKEN ", AUTO_LOGIN_TOKEN);
        Log.d(">>>  onResume mFlag ", String.valueOf(mFlag));
        Log.d(">>> getPreferences url", CPreferences.getPreferences(context,"url"));
        mResume = true;
        //if(mFlag) {
            Bundle extras = getIntent().getExtras();
            Log.d(">>> 1 MainActivity onResume ", "onResume");
            if (extras != null) {
                Log.d(">>> 1 MainActivity extras != null", extras.toString());
                if (extras.containsKey("url")) {
                    url = extras.getString("url");
                } else {
                    if(CPreferences.getPreferences(context,"url") != null ){
                        url = CPreferences.getPreferences(context,"url");
                    }else {
                        url = null;
                    }
                }
            }
            //url = getIntent().getData().getString("url");
            Log.d(">>> 1 MainActivity URL", url == null ? "null" : url);
            Log.d(">>> 1 MainActivity isPushClick", CPreferences.getPreferences(context, "isPushClick"));
            // 푸시를 통해 들어온 경우 해당 url로 바로 진입 처리 하는 로직
            if (webview != null && (CPreferences.getPreferences(context, "isPushClick").equals("true") || url != null)) {
                CPreferences.setPreferences(context, "isPushClick", "true");
                url = CPreferences.getPreferences(context, "url");
                Log.d(">>> 2 MainActivity URL", url);
                //webview.loadUrl(BASE_URL + subUrl, WebViewSetting.setHeader(context)); //메인페이지 로드
                //webview.loadUrl("file:///android_asset/www/index.html", WebViewSetting.setHeader(context));
                if(url == null || url.isEmpty()){
                    if(CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN") != null && !CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN").isEmpty()){
                        webview.loadUrl(URL_DOMAIN + URLConstants.MAIN_URL, WebViewSetting.setHeader(context)); //메인페이지
                    }else{
                        webview.loadUrl(URL_DOMAIN + URLConstants.LOGIN_URL, WebViewSetting.setHeader(context)); //로그인페이지
                    }
                }else {
                    if(CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN") != null && !CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN").isEmpty()){
                        webview.loadUrl(URL_DOMAIN + URLConstants.MAIN_URL, WebViewSetting.setHeader(context)); //메인페이지
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                webview.loadUrl(URL_DOMAIN + url, WebViewSetting.setHeader(context));
                            }
                        }, 1000);
                    }else {
                        webview.loadUrl(URL_DOMAIN + url, WebViewSetting.setHeader(context));
                    }
                }

                CPreferences.setPreferences(context, "url", "");
                //CPreferences.setPreferences(context, "isPushClick", "false");
                getIntent().removeExtra("url");
                //url = null;
                //mFlag = false;

            //}
        }
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


        String tokenId = CPreferences.getPreferences(context,"tokenId");
        Log.d(">>>> tokenId : ", tokenId);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mResume = false;

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
        Log.d(">>>> ","initWebView()");

        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webview.addJavascriptInterface(new AndroidBridge(), "AppDroid");    //Javascript와 통신을 위한 JavascriptInterface 추가
        webview.setVerticalScrollBarEnabled(false);             //웹뷰 자체 스크롤을 제거
        webview.setHorizontalScrollBarEnabled(false);           //웹뷰 자체 스크롤을 제거
        webview.setNetworkAvailable(true);                      //네트워크 상태 통지

        webview.setDownloadListener(new DownloadListener() {    // 파일다운로드 리스트 등록
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file");
                    String fileName = contentDisposition.replace("inline; filename=", "");
                    fileName = fileName.replaceAll("\"", "");
                    request.setTitle(fileName);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        } else {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        }
                    }
                }
            }
        });

        HttpURLConnection.setFollowRedirects(true);                         // 보안접속 사용설정
        webSettings.setPluginState(WebSettings.PluginState.ON);             // 플러그인 사용 설정
        webSettings.setUserAgentString(webSettings.getUserAgentString()
                .replace("Android", "ShareOfficeApp AppDroid Android")
                .replace("Chrome", ""));                  // 기본 브라우저와 Webview의 UserAgent값을 구분해주기위해 사용


        webSettings.setJavaScriptEnabled(true);                             // Javascript 허용
        webSettings.setBuiltInZoomControls(false);                          // 줌 아이콘 사용 유무
        webSettings.setSupportMultipleWindows(true);                        // 여러개의 윈도우를 사용할 수 있도록 허용
        webSettings.setAppCacheEnabled(true);                               // 앱 캐시 허용
        webSettings.setAllowFileAccess(true);                               // 웹뷰 내에서 파일 접근 가능 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);         // Javascript에서 window.open() 사용 허용
        webSettings.setLoadsImagesAutomatically(true);                      // 웹뷰가 앱에 등록된 이미지 리소스를 자동으로 로드하도록 설정
        webSettings.setUseWideViewPort(false);                              // Wide Viewport 사용 유무
        webSettings.setSavePassword(false);                                 // 패스워드 저장 유무를 묻는창을 띄우지 않음
        //webSettings.setDomStorageEnabled(true);                           // 로컬스토리지 허용
        //webSettings.setGeolocationEnabled(true);                          // 위치정보 사용 유무


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
        webview.setWebChromeClient(new MyWebChromeClient(context));                 // WebChromeClient (javascript alert, dialog 를 네이티브 다이얼로그 방식으로 표현해 준다.)

        Bundle extras = getIntent().getExtras();

        Log.d(">>> AUTO_LOGIN_TOKEN : ", CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN"));
        Log.d(">>> AUTO_LOGIN_TOKEN value of : ", String.valueOf(CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN").isEmpty()));

        // Intent를 통해 Bundle 값이 있을 경우 해당 주소로 이동
        if(CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN") != null && !CPreferences.getPreferences(context, "AUTO_LOGIN_TOKEN").isEmpty()){
            webview.loadUrl(URL_DOMAIN + URLConstants.MAIN_URL, WebViewSetting.setHeader(context)); //메인페이지
        }else{
            webview.loadUrl(URL_DOMAIN + URLConstants.LOGIN_URL, WebViewSetting.setHeader(context)); //로그인페이지
        }
        //webview.loadUrl("file:///android_asset/www/index.html", WebViewSetting.setHeader(context));
        //webview.loadUrl("http://code.google.com/p/gcm/source/browse/");
        //webview.loadUrl("https://www.sample-videos.com/");

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
            }else{
                // 권한 거부 때 처리
                mFlag = false;
                Toast.makeText(getApplicationContext(), "파일쓰기권한을 체크해야 합니다", Toast.LENGTH_LONG).show();
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }



    /**
     * 네이티브 to Javascript 통신 메소드
     * 네이티브에서 호출하는 방법 : webview.loadUrl("javascript:alert(1);");
     *
     */
    private void callJavascript(String script) {
        webview.loadUrl(script);
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                mFlag = true;
                PermissionSuccess();
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                mFlag = false;
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
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
            Log.d(">>> getVariable key : ",CPreferences.getPreferences(context, key));
            return CPreferences.getPreferences(context, key);
        }

        @JavascriptInterface
        public void setVariable(final String key, final String value) {
            CPreferences.setPreferences(context, key, value);
            Log.d(">>> setVariable key : ",key);
            Log.d(">>> setVariable value : ",value);
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
        public void callAnotherView(final String url) {
            try {
                Log.d(">>>>> callAnotherView url : ",url);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("url",url);
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
        public void downloadVideo(final String url) {
            DownloadManager mdDownloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(
                    Uri.parse(url));
            File destinationFile = new File(
                    Environment.getExternalStorageDirectory(),
                    SystemUtil.getFileName(url));
            req.setDescription("파일 다운로드 중입니다..");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationUri(Uri.fromFile(destinationFile));
            mdDownloadManager.enqueue(req);

            registerReceiver(mOnComplete, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));



        }
    }

    //BroadcastReceiver onComplete=new BroadcastReceiver() {
    //    public void onReceive(Context ctxt, Intent intent) {
            //findViewById(R.id.start).setEnabled(true);
    //        Toast.makeText(context, "파일다운로드를 완료하였습니다.", Toast.LENGTH_LONG).show();
    //    }
    //};



}
