package com.gmkapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.gmkapp.util.CPreferences;
import com.gmkapp.util.SystemUtil;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;

import com.gmkapp.util.HttpConnectionUtil;
import com.gmkapp.util.URLConstants;

/**
 * 애플리케이션 메인 웹뷰 초기화면...
 * 개발환경 : Android 5.1 ~ 7.0
 * 테스트 디바이스 : Samsung Galaxy S4 (Android 5.0.1) ~ S7 (Android 7.0)
 */

public class IntroActivity extends AppCompatActivity {
    private String APP_VERSION = "";
    private Context context = null;
    private int startCnt = 0;
    protected AQuery aq;
    private String TAG = IntroActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        context = this;
        aq = new AQuery(IntroActivity.this);
        try {
            // 앱 버전 가져오기
            APP_VERSION = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (startCnt == 0) {
            versionCheck();
        }else{
            initialize();
        }
        startCnt++;
    }

    /**
     * 앱 버젼 체크
     */
    private void versionCheck() {
        new Thread() {
            public void run() {
                //AsyncTask 호출
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) //3.0
                    new VersionCheckTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    new VersionCheckTask().execute();

            }
        }.start();

    }

    /**
     * Firebase 초기화 및 화면 3초 지연 처리
     */
    private void initialize() {
        FirebaseInstanceId.getInstance().getToken();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish(); // 액티비티 종료
            }
        };
        handler.sendEmptyMessageDelayed(0, 2000); // ms, 2초후 종료시킴
    }


    private int[] getSpltVersion(String newAppVer){
        int[] returnInt = new int[2];
        String[] newVerArr = newAppVer.split("\\.");
        String[] nowVerArr = APP_VERSION.split("\\.");

        int newVerNum = 0;

        for (int i = 0; i < newVerArr.length; i++) {
            if (i == 0) {
                newVerNum += (Integer.parseInt(newVerArr[i]) * 10000);
            } else if (i == 1) {
                newVerNum += (Integer.parseInt(newVerArr[i]) * 100);
            } else if (i == 2) {
                newVerNum += (Integer.parseInt(newVerArr[i]) * 1);
            }
            returnInt[0] = newVerNum;
        }
        int nowVerNum = 0;

        for (int i = 0; i < nowVerArr.length; i++) {
            if (i == 0) {
                nowVerNum += (Integer.parseInt(nowVerArr[i]) * 10000);
            } else if (i == 1) {
                nowVerNum += (Integer.parseInt(nowVerArr[i]) * 100);
            } else if (i == 2) {
                nowVerNum += (Integer.parseInt(nowVerArr[i]) * 1);
            }
            returnInt[1] = nowVerNum;
        }
        return returnInt;
    }


    /**
     * POST 방식으로 서버에 호출
     */
    private class VersionCheckTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {

            HashMap<String, String> param = new HashMap<String, String>();
            param.put(URLConstants.NET_PARAMS.OS_POST_TYPE, "ANDROID");

            String resp = HttpConnectionUtil.postRequest(URLConstants.URL_DOMAIN + URLConstants.VERSION_POST_URL, param);
            //String resp  = HttpConnectionUtil.postRequest(URLConstants.versionUrl,param);

            return resp;
        }


        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                String newAppVer = null;
                // json 으로 리턴 받았을 때 사용 하는 방식
                JSONObject jobj = new JSONObject(result);
                jobj = jobj.getJSONObject("data");

                newAppVer = jobj.getString("appVersion");

                int[] resVal = getSpltVersion(newAppVer);

                Log.d(">>>> VersionCheckTask","newVerNum : " + Integer.toString(resVal[0]) + " nowVerNum :" + Integer.toString(resVal[1]));
                //if (newVerNum > nowVerNum) {
                 if (resVal[0] > resVal[1]) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder
                            .setMessage("새로운 앱이 존재합니다. \n마켓으로 이동합니다.")
                            .setCancelable(false)
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Write your code here to execute after dialog
                                            Toast.makeText(getApplicationContext(),
                                                    "앱 다운로드를 취소합니다.", Toast.LENGTH_SHORT)
                                                    .show();
                                            dialog.cancel();

                                            // 사용자가 앱 업데이트를 하지 않았을 경우 해당 화면이후 접근 금지 유무 정책이 필요
                                            if(true){
                                                CPreferences.setPreferences(context,"IsFirst","false");
                                                ActivityCompat.finishAffinity(MainActivity.mMainactivity);
                                                System.runFinalizersOnExit(true);
                                                System.exit(0);
                                            }else{
                                                initialize();
                                            }

                                        }
                                    })
                            .setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.sirma.mobile.bible.android")); // com.gmkapp
                                            startActivity(i);
                                        }
                                    });
                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // 다이얼로그 보여주기
                    alertDialog.show();


                } else {
                    initialize();
                }



            } catch (Exception e) {
                e.printStackTrace();
                initialize();
            }
        }
    }
}
