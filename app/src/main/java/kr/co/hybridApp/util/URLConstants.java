package kr.co.hybridapp.util;

public class URLConstants {

    //public static final String URL_DOMAIN = "http://center.ohnaebus.com";
    //public static final String SPLASH_URL = "http://bus.ohnaebus.com/main/splash.do";

    //private final String URL_DOMAIN = "https://www.flagone.co.kr/";
    public static final String URL_DOMAIN = "http://www.gmkapp.com";

    public static final String MAIN_URL = "/mobile/main/main.do";
    public static final String LOGIN_URL = "/mobile/login.do";

    public static class NET {
        public static final String APP_VERSION = URL_DOMAIN + "";
        public static final String BUS_GPS_DATA = URL_DOMAIN + "/busTracker/busGpsData.json";               // 버스 Gps정보
        public static final String BUS_STATE_DATA = URL_DOMAIN + "/busTracker/busStateData.json";           // 버스 상태정보
        public static final String BUS_CHILD_DATA = URL_DOMAIN + "/busTracker/childrenStateData.json";     // 원아정보
        public static final String BUS_ROUTE_STATE_DATA = URL_DOMAIN + "/busTracker/routeStateData.json";   // 운행정보
        public static final String SELECT_BUS_DATA_INFS = URL_DOMAIN + "/busTracker/selectBusDataInfs.json";   // 버스앱(정보기관정보,정류장정보,원아정보)

    }


}
