package kr.co.hybridapp.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * 웹뷰클라이언트 클래스
 * 주유기능 : url,로드 시작 종료 시점,에러사항 캐치
 *
 * @author YT
 */

public class MyWebViewClient extends WebViewClient {

    Context context = null;
    String activityName = null;



    //생성자 추가
    public MyWebViewClient(Context context, String activityName) {
        this.context = context;
        this.activityName = activityName;
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        /*
        boolean value = true;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType = mime.getMimeTypeFromExtension(extension);
            if (mimeType != null) {
                if (mimeType.toLowerCase().contains("video")
                        || extension.toLowerCase().contains("mov")
                        || extension.toLowerCase().contains("mp3")
                        || extension.toLowerCase().contains("mp4")) {
                    DownloadManager mdDownloadManager = (DownloadManager) context
                            .getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));
                    File destinationFile = new File(
                            Environment.getExternalStorageDirectory(),
                            getFileName(url));
                    request.setDescription("파일 다운로드 중입니다..");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationUri(Uri.fromFile(destinationFile));
                    mdDownloadManager.enqueue(request);
                    value = false;
                }
            }
            if (value) {
                view.loadUrl(url);
            }
            return true;
        }
        */

        if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(tel);
            return true;
        }

        /*
        if(url.endsWith(".png")){
            Uri source = Uri.parse(url);
            // Make a new request pointing to the .apk url
            DownloadManager.Request request = new DownloadManager.Request(source);
            // appears the same in Notification bar while downloading
            request.setDescription("Description for the DownloadManager Bar");
            request.setTitle("YourApp.png");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            // save the file in the "Downloads" folder of SDCARD
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SmartPigs.apk");
            // get download service and enqueue file
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
            return true;
        }

        if(url.endsWith(".mp4")){
            view.loadUrl(url);
            return true;
        }
        */

//        Log.e("----" , "URL---" + url);
//        Log.e("----" , "MOB_PUSH_TOKEN---" + WebViewSetting.setHeader(context).get("MOB_PUSH_TOKEN"));
//        Log.e("----" , "MOB_TRMNL_OS_VER---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_OS_VER"));
//        Log.e("----" , "MOB_TRMNL_OS_TYPE---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_OS_TYPE"));
//        Log.e("----" , "MOB_IDTF_CHAR---" + WebViewSetting.setHeader(context).get("MOB_IDTF_CHAR"));
//        Log.e("----" , "MOB_TRMNL_MODEL_NAME---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_MODEL_NAME"));


        view.loadUrl(url, WebViewSetting.setHeader(context));

        return false;
        //return true;
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        /*
        boolean value = true;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType = mime.getMimeTypeFromExtension(extension);
            if (mimeType != null) {
                if (mimeType.toLowerCase().contains("video")
                        || extension.toLowerCase().contains("mov")
                        || extension.toLowerCase().contains("mp3")
                        || extension.toLowerCase().contains("mp4")) {
                    DownloadManager mdDownloadManager = (DownloadManager) context
                            .getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request req = new DownloadManager.Request(
                            Uri.parse(url));
                    File destinationFile = new File(
                            Environment.getExternalStorageDirectory(),
                            getFileName(url));
                    req.setDescription("파일 다운로드 중입니다..");
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    req.setDestinationUri(Uri.fromFile(destinationFile));
                    mdDownloadManager.enqueue(req);
                    value = false;
                }
            }
            if (value) {
                view.loadUrl(url);
            }
            return true;
        }
        */

        if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(tel);
            return true;
        }

//        Log.e("----" , "URL---" + url);
//        Log.e("----" , "MOB_PUSH_TOKEN---" + WebViewSetting.setHeader(context).get("MOB_PUSH_TOKEN"));
//        Log.e("----" , "MOB_TRMNL_OS_VER---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_OS_VER"));
//        Log.e("----" , "MOB_TRMNL_OS_TYPE---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_OS_TYPE"));
//        Log.e("----" , "MOB_IDTF_CHAR---" + WebViewSetting.setHeader(context).get("MOB_IDTF_CHAR"));
//        Log.e("----" , "MOB_TRMNL_MODEL_NAME---" + WebViewSetting.setHeader(context).get("MOB_TRMNL_MODEL_NAME"));

        view.loadUrl(url, WebViewSetting.setHeader(context));



        return false;
    }


    @Override
    public void onPageStarted(WebView view, String url,
                              android.graphics.Bitmap favicon) {



    }

    @Override
    public void onPageFinished(WebView view, String url) {

        if(view.getVisibility()!= View.VISIBLE){
            view.setVisibility(View.VISIBLE);

        }


    }
//    @Override
//    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//        handler.proceed();  //SSL 에러가 발생해도 계속 진행!
//    }
    @Override
    public void onReceivedError(final WebView view, int errorCode, String description, String failingUrl) {

        switch (errorCode) {
            case ERROR_AUTHENTICATION:               // 서버에서 사용자 인증 실패
            case ERROR_BAD_URL:                            // 잘못된 URL
            case ERROR_CONNECT:                           // 서버로 연결 실패
            case ERROR_FAILED_SSL_HANDSHAKE:     // SSL handshake 수행 실패
            case ERROR_FILE:                                   // 일반 파일 오류
            case ERROR_FILE_NOT_FOUND:                // 파일을 찾을 수 없습니다
            case ERROR_HOST_LOOKUP:            // 서버 또는 프록시 호스트 이름 조회 실패
            case ERROR_IO:                               // 서버에서 읽거나 서버로 쓰기 실패
            case ERROR_PROXY_AUTHENTICATION:    // 프록시에서 사용자 인증 실패
            case ERROR_REDIRECT_LOOP:                // 너무 많은 리디렉션
            case ERROR_TIMEOUT:                          // 연결 시간 초과
            case ERROR_TOO_MANY_REQUESTS:            // 페이지 로드중 너무 많은 요청 발생
            case ERROR_UNKNOWN:                         // 일반 오류
            case ERROR_UNSUPPORTED_AUTH_SCHEME:  // 지원되지 않는 인증 체계
            case ERROR_UNSUPPORTED_SCHEME:


                //view.loadUrl("file:///android_asset/error.html");    //에러페이지 출력
                //view.clearHistory();


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        view.reload();

                    }
                });
                builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                builder.show();

                break;
        }
    }
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//
//            Map<String, String> extraHeaders = WebViewSetting.setHeader(context);
//
//            request.getRequestHeaders().put("MOB_PUSH_TOKEN", extraHeaders.get("MOB_PUSH_TOKEN"));
//            request.getRequestHeaders().put("MOB_TRMNL_OS_VER", extraHeaders.get("MOB_TRMNL_OS_VER"));
//            request.getRequestHeaders().put("MOB_TRMNL_OS_TYPE", extraHeaders.get("MOB_TRMNL_OS_TYPE"));
//            request.getRequestHeaders().put("MOB_IDTF_CHAR", extraHeaders.get("MOB_IDTF_CHAR"));
//            request.getRequestHeaders().put("MOB_TRMNL_MODEL_NAME", extraHeaders.get("MOB_TRMNL_MODEL_NAME"));
//        }
//
//        return super.shouldInterceptRequest(view, request);
//    }
//
//    @Override
//    public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
//        // I need to updated the header here
//
//        return super.shouldInterceptRequest(view, url);
//    }


    /**
     * File name from URL
     *
     * @param url
     * @return
     */
    public String getFileName(String url) {
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
