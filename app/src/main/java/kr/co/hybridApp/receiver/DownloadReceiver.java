package kr.co.hybridApp.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    @Override public void onReceive(Context context, Intent intent) {

        //String name = intent.getAction(); // Intent SendBroadCast로 보낸 action TAG 이름으로 필요한 방송을 찾는다.
        //if(name.equals("kr.co.hybridApp.SEND_BROAD_CAST ")){
        //    Log.d(TAG, "BroadcastReceiver :: com.dwfox.myapplication.SEND_BROAD_CAST :: " + intent.getStringExtra("sendString")); // putExtra를 이용한 String전달
        //    }
        Toast.makeText(context, "파일다운로드를 완료하였습니다.", Toast.LENGTH_LONG).show();

    }

}
