package com.github.neiplz.pedometer.utils;

public class Constants {

    public static final String INTENT_ACTION_LOGIN = "com.github.neiplz.action.LOGIN";

    public static final String INTENT_ACTION_LOGOUT = "com.github.neiplz.action.LOGOUT";

    public static final String ENCRYPTION_KEY = "Thinkpad";//长度为8

    public static final int READ_TIMEOUT = 10000;
    public static final int CONNECT_TIMEOUT = 10000;

    public static final String REQUEST_METHOD_POST = "POST";
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static final String KEY_USER_EMAIL = "user.email";
    public static final String KEY_USER_HEIGHT = "user.height";
    public static final String KEY_USER_WEIGHT = "user.weight";
    public static final String KEY_USER_NAME = "user.name";
    public static final String KEY_USER_GENDER = "user.gender";
    public static final String KEY_USER_PASSWORD = "user.password";
    public static final String KEY_IS_REMEMBER_ME = "isRememberMe";
    public static final String KEY_HAS_LOGGED_IN = "hasLoggedIn";
    public static final String KEY_STEP_LENGTH = "stepLength";

    public static final String CHARSET_UTF8 = "UTF-8";

//    public final static int GOAL = 10000;
    public final static String SERVICE_NAME_STEP_SERVICE = "com.github.neiplz.pedometer.services.StepService";
    public final static String PERF_NAME_SHARED_PREFERENCE = "com.github.neiplz.pedometer_preferences";

    public final static String KEY_PREF_STRIDE = "stride";
    public final static String KEY_PREF_GOAL = "goal";
    public final static String KEY_PREF_SENSITIVITY = "sensitivity";
    public final static String KEY_PREF_SYNC = "sync";

    public final static String DEFAULT_STRING_STRIDE = "45";
    public final static String DEFAULT_STRING_SENSITIVITY = "4.5";
    public final static String DEFAULT_STRING_GOAL = "10000";

    public static final String URL_LOGIN_CHECK = "http://192.168.182.250:8080/user/loginCheck";
    public static final String URL_USER_REGISTER = "http://192.168.182.250:8080/user/save";
    public static final String URL_UPDATE_PASSWORD = "http://192.168.182.250:8080/user/updatePwd";
    public static final String URL_LOAD_INFO = "http://192.168.182.250:8080/user/getByEmail";
    public static final String URL_UPDATE_USER_INFO = "http://192.168.182.250:8080/user/updateAppUser";
    public static final String URL_SYNC_PREF = "http://192.168.182.250:8080/pref/sync";

//    public static final String URL_LOGIN_CHECK = "http://192.168.1.100:8080/user/loginCheck";
//    public static final String URL_USER_REGISTER = "http://192.168.1.100:8080/user/save";
//    public static final String URL_UPDATE_PASSWORD = "http://192.168.1.100:8080/user/updatePwd";
//    public static final String URL_LOAD_INFO = "http://192.168.1.100:8080/user/getByEmail";
//    public static final String URL_UPDATE_USER_INFO = "http://192.168.1.100:8080/user/updateAppUser";
//    public static final String URL_SYNC_PREF = "http://192.168.1.100:8080/pref/sync";

//    public static final String URL_LOGIN_CHECK = "http://122.193.135.162:8083/user/loginCheck";
//    public static final String URL_USER_REGISTER = "http://122.193.135.162:8083/user/save";
//    public static final String URL_UPDATE_PASSWORD = "http://122.193.135.162:8083/user/updatePwd";
//    public static final String URL_LOAD_INFO = "http://122.193.135.162:8083/user/getByEmail";
//    public static final String URL_UPDATE_USER_INFO = "http://122.193.135.162:8083/user/updateAppUser";
//    public static final String URL_SYNC_PREF = "http://122.193.135.162:8083/pref/sync";


    public static final int REQUEST_CODE_EDIT_PASSWORD = 91;
    public static final int REQUEST_CODE_EDIT_USER_INFO = 92;


}
