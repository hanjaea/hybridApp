package com.gmkapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gmkapp.MainActivity;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    @Override public void onReceive(Context context, Intent intent) {

        //String name = intent.getAction(); // Intent SendBroadCast로 보낸 action TAG 이름으로 필요한 방송을 찾는다.
        //if(name.equals("kr.co.hybridApp.SEND_BROAD_CAST ")){
        //    Log.d(TAG, "BroadcastReceiver :: com.dwfox.myapplication.SEND_BROAD_CAST :: " + intent.getStringExtra("sendString")); // putExtra를 이용한 String전달
        //    }
        Toast.makeText(context, "파일다운로드를 완료하였습니다.", Toast.LENGTH_LONG).show();

        // front에서 리턴 값 사용유무값을 string으로 true를 던졌을 때
        String istrue = MainActivity.istrue;
        if(istrue != null && !istrue.isEmpty() && istrue.equals("true")){
            final String script = "javascript:callbackFunc('다운로드가 완료되었습니다.');";
            MainActivity.callJavascript(script);
            MainActivity.istrue = "";
        }


    }

}
