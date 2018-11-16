package com.gmkapp.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.gmkapp.MainActivity;
import com.gmkapp.R;

import java.io.File;
import java.io.IOException;


/**
 * 웹뷰크롬클라이언트 클래스
 * 주유기능 : Javascript ConsoleMessage, Alert dialog, Confirm dialog, 위치정보 설정 dialog
 */

public class MyWebChromeClient extends WebChromeClient {

	Context context = null;
	MainActivity mainActivity = null;
	
	//생성자 추가
	public MyWebChromeClient(Context context, MainActivity activity){
		this.context = context;
		this.mainActivity = activity;
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

	// input type = file 관련 추가 사항
	// For Android Version < 3.0
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		//System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
		mainActivity.mUploadMessage = uploadMsg;
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType(mainActivity.TYPE_IMAGE);
		mainActivity.startActivityForResult(intent, mainActivity.INPUT_FILE_REQUEST_CODE);
	}

	// For 3.0 <= Android Version < 4.1
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		//System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
		openFileChooser(uploadMsg, acceptType, "");
	}

	// For 4.1 <= Android Version < 5.0
	public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
		Log.d(getClass().getName(), "openFileChooser : "+acceptType+"/"+capture);
		mainActivity.mUploadMessage = uploadFile;
		imageChooser();
	}

	// For Android Version 5.0+
	// Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
	public boolean onShowFileChooser(WebView webView,
									 ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
		System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
		if (mainActivity.mFilePathCallback != null) {
			mainActivity.mFilePathCallback.onReceiveValue(null);
		}
		mainActivity.mFilePathCallback = filePathCallback;
		imageChooser();
		return true;
	}


	private void imageChooser() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = mainActivity.createImageFile();
				takePictureIntent.putExtra("PhotoPath", mainActivity.mCameraPhotoPath);
			} catch (IOException ex) {
				// Error occurred while creating the File
				Log.e(getClass().getName(), "Unable to create Image File", ex);
			}

			// Continue only if the File was successfully created
			if (photoFile != null) {
				mainActivity.mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
			} else {
				takePictureIntent = null;
			}
		}

		//Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
		//contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
		//contentSelectionIntent.setType(mainActivity.TYPE_IMAGE);

		//Intent[] intentArray;
		//if (takePictureIntent != null) {
		//	intentArray = new Intent[]{takePictureIntent};
		//} else {
		//	intentArray = new Intent[0];
		//}

		//Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
		//chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
		//chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
		//chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

		//startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		mainActivity.startActivityForResult(intent, mainActivity.INPUT_FILE_REQUEST_CODE);
	}

}
