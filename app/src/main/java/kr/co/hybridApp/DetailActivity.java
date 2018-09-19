package kr.co.hybridApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.hybridApp.receiver.DownloadReceiver;
import kr.co.hybridApp.settings.MyWebChromeClient;
import kr.co.hybridApp.settings.MyWebViewClient;
import kr.co.hybridApp.settings.WebViewSetting;
import kr.co.hybridApp.util.CPreferences;
import kr.co.hybridApp.util.SystemUtil;

/**
 * 애플리케이션 상세 웹뷰 화면
 * 개발환경 : Android 5.1 ~ 7.0
 * 테스트 디바이스 : Samsung Galaxy S4 (Android 5.0.1) ~ S7 (Android 7.0)
 */

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class DetailActivity extends AppCompatActivity {
    public final int REQUEST_CODE = 0;

    private final String BASE_URL = "https://www.flagone.co.kr/";
    //private final String BASE_URL = "http://10.59.35.70/";

    private final String MAIN_URL = "mobile/mobileMain.do";

    private String activityName = "DetailActivity";
    private String LinkUrl = "";
    private WebView webview;
    private Context context;
    private boolean isSecond = false;  // 하드웨어 Back Key를 두번 눌렀는지 체크
    private Timer timer; //하드웨어 Back Key를 두번 누르는 사이에 2초가 지났는지 체크
    private DownloadReceiver mOnComplete = new DownloadReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main);

        context = this;
        // 웹뷰 초기 세팅
        initWebView();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.containsKey("url")){
                LinkUrl = extras.getString("url");
            }else{
                LinkUrl = null;
            }
        }

        // 푸시를 통해 들어온 경우 해당 url로 바로 진입 처리 하는 로직
        if(webview!=null){
            String isPushClick = CPreferences.getPreferences(context,"isPushClick");
            Log.d(">> DetailActivity isPushClick : ", isPushClick);

            //webview.loadUrl(BASE_URL + subUrl, WebViewSetting.setHeader(context)); //메인페이지 로드
            webview.loadUrl("file:///android_asset/www/detail.html", WebViewSetting.setHeader(context));
            Log.d("DetailActivity LinkUrl", LinkUrl==null?"null":LinkUrl);
            //webview.loadUrl(LinkUrl, WebViewSetting.setHeader(context));
            // 푸시로 상세화면 접근 시 리스트화면 접근 시 인트로 화면보여지는 것을 막는다
            //CPreferences.setPreferences(context,"IsFirst","true");
            CPreferences.setPreferences(context,"isPushClick","false");
            CPreferences.setPreferences(context,"url","");
            getIntent().removeExtra("url");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webview.addJavascriptInterface(new AndroidBridge(), "AppDroid");  // Javascript와 통신을 위한 JavascriptInterface 추가
        webview.setVerticalScrollBarEnabled(false);                             // 웹뷰 자체 스크롤을 제거
        webview.setHorizontalScrollBarEnabled(false);                           // 웹뷰 자체 스크롤을 제거
        webview.setNetworkAvailable(true);                                      // 네트워크 상태 통지
        HttpURLConnection.setFollowRedirects(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);                 // 플러그인 사용 설정
        webSettings.setUserAgentString(webSettings.getUserAgentString()
                .replace("Android", "ShareOfficeApp AppDroid Android")
                .replace("Chrome", ""));                      //기본 브라우저와 Webview의 UserAgent값을 구분해주기위해 사용


        webSettings.setJavaScriptEnabled(true);                                 // Javascript 허용
        webSettings.setBuiltInZoomControls(false);                              // 줌 아이콘 사용 유무
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);                            // 여러개의 윈도우를 사용할 수 있도록 허용
        webSettings.setAppCacheEnabled(true);                                   // 앱 캐시 허용
        webSettings.setAllowFileAccess(true);                                   // 웹뷰 내에서 파일 접근 가능 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);             // Javascript에서 window.open() 사용 허용
        webSettings.setLoadsImagesAutomatically(true);                          // 웹뷰가 앱에 등록된 이미지 리소스를 자동으로 로드하도록 설정
        webSettings.setUseWideViewPort(false);                                  // Wide Viewport 사용 유무
        webSettings.setSavePassword(false);                                     // 패스워드 저장 유무를 묻는창을 띄우지 않음
        webSettings.setDomStorageEnabled(true);                                 // 로컬스토리지 허용
        webSettings.setGeolocationEnabled(true);                                // 위치정보 사용 유무
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);


        // 사용자 폰 버젼이 21 Android 5.0(LOLLIPOP) 버젼이상일 경우
        // 혼합된 컨텐츠와 서드파티 쿠키가 설정에 따라 Webview 에서 Block 시키는 게 기본으로 바뀌어 아래 코드를 적용 시켜줘야 함
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); //항상허용

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webview, true);
        }

        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.setWebViewClient(new MyWebViewClient(context, activityName));   //WebViewClient
        webview.setWebChromeClient(new MyWebChromeClient(context));             //WebChromeClient


        String subUrl = MAIN_URL;

        Bundle extras = getIntent().getExtras();

        //webview.loadUrl(BASE_URL + subUrl, WebViewSetting.setHeader(context)); //메인페이지 로드
        webview.loadUrl("file:///android_asset/www/detail.html", WebViewSetting.setHeader(context));
        Log.d(">>>>> URL", LinkUrl);
        //webview.loadUrl(LinkUrl, WebViewSetting.setHeader(context));

    }


    /**
     * 하드웨어 Back Key 입력시 이벤트
     * 웹뷰의 히스토리가 있으면 뒤로가기 없으면 두번 누르면 종료
     *
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (webview.canGoBack() && !webview.getUrl().contains("/mobileMain/")) {
                webview.goBack();
                return true;
            }else {
               finish();
            }
        }
        return true;
    }


    /**
     * 네이티브 to Javascript 통신 메소드
     * 네이티브에서 호출하는 방법 : webview.loadUrl("javascript:alert(1);");
     *
     */
    private void callJavascript(final String script) {
        //webview.loadUrl(script);
        webview.post(new Runnable() {
            public void run() {
                try {
                    webview.loadUrl(script);

                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        DetailActivity.this.deleteDatabase("webview.db");
        DetailActivity.this.deleteDatabase("webviewCache.db");


        clearCacheFolder(context.getCacheDir());


    }

    /**
     * 웹뷰 캐시 클리어 함수(해당 화면에서는 사용하지 않는다.)
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
            Log.d(">>> DetailActivity setWebviewUrl url : ", url);
            //webview.loadUrl(url);
            //webview.loadUrl(url,WebViewSetting.setHeader(context));
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // 작업 처리
                    //String jscript = "javascript:" + functionStr;
                    callJavascript(url);
                }
            });

        }

        @JavascriptInterface
        public void closePopup(){
            finish();
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
