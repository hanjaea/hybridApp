package com.gmkapp.util;

public class URLConstants {

    public static final String URL_DOMAIN = "https://www.gmkapp.com";
    //public static final String URL_DOMAIN = "http://192.168.0.3:8080";
    public static final String MAIN_URL = "/mobile/main/main.do";
    public static final String LOGIN_URL = "/mobile/login.do";
    public static final String VERSION_POST_URL = "/api/mobile/auth/selectAppVersion.json";
    public static final String VERSION_GET_URL = "/api/mobile/auth/selectAppVersion.json?";


    /*
     *   net PARAMS
     */
    public static class NET_PARAMS {

        public static final String OS_GET_TYPE ="osTypeCd=";                              //os type (android : 0, is = 1
        public static final String OS_POST_TYPE ="osTypeCd";
    }

    /*
     *   OS 설정
     */
    public static class PARAMS_OS_TYPE {
        public static final int ANDROID 	= 0;        //안드로이드
        public static final int IOS= 1;                 // IOS
    }


    /*
     *   net 결과 기본 데이터
     */
    public static class NET_RESULT {
        public static final String CODE ="code";       // 수신 결과  0:fail 1: success
        public static final String DATA ="data";       // 수신 데이터

    }

    /*
     *   net 결과 코드
     */
    public static class NET_RESULT_CODE {
        public static final int FAIL = 0;
        public static final int SUCCESS = 1;

    }

}
