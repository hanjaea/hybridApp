package com.gmkapp.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.gmkapp.R;


/**
 * 웹뷰크롬클라이언트 클래스
 * 주유기능 : Javascript ConsoleMessage, Alert dialog, Confirm dialog, 위치정보 설정 dialog
 * @author YT
 */

public class MyWebChromeClient extends WebChromeClient {

	Context context = null;
	
	//생성자 추가
	public MyWebChromeClient(Context context){
		this.context = context;
	}
	@Override
	public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
		Log.d("Webview ConsoleMessage",
				consoleMessage.message() + '\n'
				+ consoleMessage.messageLevel() + '\n'
				+ consoleMessage.sourceId());
		
		return super.onConsoleMessage(consoleMessage);
	}
	@Override
	public void onProgressChanged(WebView view, int newProgress) {

	}
	@Override
	public boolean onJsAlert(WebView view, String url, String message,
			final android.webkit.JsResult result) {
		new AlertDialog.Builder(context)
				.setTitle(context.getText(R.string.app_name))
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}

						})

				.setCancelable(false).create().show();
		return true;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url,
			String message, final android.webkit.JsResult result) {
		new AlertDialog.Builder(context)
				.setTitle(context.getText(R.string.app_name))
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						})
				.setCancelable(false)
				.create().show();

		return true;
	}

	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
		// Should implement this function.
		final String myOrigin = origin;
		final Callback myCallback = callback;
		AlertDialog.Builder builder = new AlertDialog.Builder(
				context);

		builder.setTitle(context.getText(R.string.app_name));
		builder.setMessage("해당 기능을 사용하기위해서는 위치정보가 필요합니다 허용하시겠습니까?");
		builder.setPositiveButton("확인",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						myCallback.invoke(myOrigin, true, false);
					}
				});

		builder.setNegativeButton("취소",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						myCallback.invoke(myOrigin, false, false);
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}
}
