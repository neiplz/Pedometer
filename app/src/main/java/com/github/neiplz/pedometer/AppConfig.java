package com.github.neiplz.pedometer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 应用程序配置类：用于保存用户相关信息及设置
 * 
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @created 2014年9月25日 下午5:29:00
 * 
 */
public class AppConfig {

    private static final String LOG_TAG = "AppConfig";

    private final static String APP_CONFIG = "config";

    public final static String CONF_COOKIE = "cookie";

    public final static String CONF_APP_UNIQUEID = "APP_UNIQUEID";

    // 默认应用根目录
    public final static String APP_CONF_FOLDER = "Pedometer";
    public static final String KEY_FRITST_START = "KEY_FRIST_START";

    private static boolean unlessThenGingerBread;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            unlessThenGingerBread = true;
        }
    }


    private Context mContext;
    private static AppConfig appConfig;

    public static AppConfig getAppConfig(Context context) {
        if (null == appConfig) {
            appConfig = new AppConfig();
            appConfig.mContext = context;
        }
        return appConfig;
    }

    /**
     * 获取Preference设置
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static void apply(SharedPreferences.Editor editor) {
        if(unlessThenGingerBread){
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public String getProperties(String key) {
        Properties properties = getProperties();
        if(null != properties){
            return properties.getProperty(key);
        }
        return null;
    }

    public Properties getProperties() {
        FileInputStream fis = null;
        Properties props = new Properties();
        try {
            // 读取files目录下的config
            // fis = activity.openFileInput(APP_CONFIG);

            // 读取app_config目录下的config
            File dir = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            fis = new FileInputStream(dir.getPath() + File.separator + APP_CONFIG);

            props.load(fis);
        } catch (Exception e) {
        } finally {
            try {
                if(null != fis){
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    private void setProperties(Properties p) {
        FileOutputStream fos = null;
        try {
            // 把config建在files目录下
            // fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);

            // 把config建在(自定义)app_config的目录下
            File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            File conf = new File(dirConf, APP_CONFIG);
            fos = new FileOutputStream(conf);

            p.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(null != fos){
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void set(Properties props) {
        Properties properties = getProperties();
        properties.putAll(props);
        setProperties(properties);
    }

    public void set(String key, String value) {
        Properties props = getProperties();
        props.setProperty(key, value);
        setProperties(props);
    }

    public void remove(String... key) {
        Properties props = getProperties();
        for (String k : key)
            props.remove(k);
        setProperties(props);
    }
}
