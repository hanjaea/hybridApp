package com.gmkapp.settings;

import android.content.Context;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import com.gmkapp.util.CPreferences;


/**
 * 커스텀로딩 클래스
 * @author YT
 */


public class WebViewSetting {

	
	
	public static Map<String, String> setHeader(Context context) {



		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("MOB_PUSH_TOKEN", CPreferences.getPreferences(context, "tokenId"));
		extraHeaders.put("MOB_TRMNL_OS_VER", CPreferences.getPreferences(context, "dVersion"));
		extraHeaders.put("MOB_TRMNL_OS_TYPE", "Android");
		extraHeaders.put("MOB_IDTF_CHAR", CPreferences.getPreferences(context, "MOB_IDTF_CHAR"));
		extraHeaders.put("MOB_TRMNL_MODEL_NAME", Build.MODEL);



		return extraHeaders;
	}

}