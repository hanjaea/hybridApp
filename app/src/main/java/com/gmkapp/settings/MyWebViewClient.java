package com.gmkapp.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * 웹뷰클라이언트 클래스
 * 주유기능 : url,로드 시작 종료 시점,에러사항 캐치
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

        if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(tel);
            return true;
        }

        view.loadUrl(url, WebViewSetting.setHeader(context));

        return false;
        //return true;
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(tel);
            return true;
        }

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

}
