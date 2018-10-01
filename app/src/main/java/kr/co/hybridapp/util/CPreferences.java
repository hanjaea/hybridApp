package kr.co.hybridapp.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * preferencesUtil 클래스
 * 파일 형태의 저장소 (Key, Value 형태의 Editing)
 * @author YT
 */

@SuppressWarnings("static-access")
public class CPreferences {


    //예약Key : UUID, USER_ID, REG_ID

    /**
     * Preference 세팅
     *
     * @author YT
     */
    public static void setPreferences(Context context, String key, String value) {
        SharedPreferences p = (context.getApplicationContext()).getSharedPreferences("atxpert-bus-pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = p.edit();
        editor.remove(key);
        editor.putString(key, value);
        editor.commit();

    }

    /**
     * Preference 가져오기
     *
     * @author YT
     */
    public static String getPreferences(Context context, String key) {
        SharedPreferences p = (context.getApplicationContext()).getSharedPreferences("atxpert-bus-pref", context.MODE_PRIVATE);
        return p.getString(key, "");

    }

}