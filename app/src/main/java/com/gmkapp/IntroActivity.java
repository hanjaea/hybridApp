package com.gmkapp;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);

        context = this;
        try {
            // 앱 버전 가져오기
            APP_VERSION = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(">>> 1 IntroActivity onCreate ", "onCreate");
        Toast.makeText(this,"IntroActivity", Toast.LENGTH_SHORT);

        //versionCheck();
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (startCnt != 0) {
            versionCheck();
        }
        startCnt++;
        Log.d(">>> 1 IntroActivity onResume ", "onResume");

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
                finish(); // 액티비티 종료
            }
        };
        handler.sendEmptyMessageDelayed(0, 2000); // ms, 2초후 종료시킴
        Log.d(">>> 1 IntroActivity initialize ", "initialize");
    }


    private class VersionCheckTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {

            HashMap<String, String> param = new HashMap<String, String>();
            param.put("osTypeCd", "ANDRD");

            String resp = HttpConnectionUtil.postRequest(URLConstants.URL_DOMAIN + URLConstants.VERSION_URL, param);

            return resp;
        }


        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            String newAppVer = "";

            try {
                JSONObject jobj = new JSONObject(result);
                jobj = jobj.getJSONObject("data");

                newAppVer = jobj.getString("appVer");


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
                }


                if (newVerNum > nowVerNum) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder
                            .setMessage("버전이 낮은 앱을 사용할 수 없습니다.")
                            .setCancelable(false)
                            .setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=kr.co.flagone"));
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
