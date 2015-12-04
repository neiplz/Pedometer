package com.github.neiplz.pedometer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    private static final String LOG_TAG = "WelcomeActivity";

    TextView tvVersion;
    TextView tvProgress;// 下载进度展示

    // 服务器返回的信息
    private String mVersionName;// 版本名
    private int mVersionCode;// 版本号
    private String mDesc;// 版本描述
    private String mDownloadUrl;// 下载地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 加载启动页面
        final View view = View.inflate(this, R.layout.activity_welcome,null);
        setContentView(view);

        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvProgress = (TextView) findViewById(R.id.tv_progress);

        try {
            tvVersion.setText("当前版本：" + getVersionName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 渐变展示启动屏
        AlphaAnimation alpha = new AlphaAnimation(0.5f,1.0f);
        alpha.setDuration(5000);

        view.startAnimation(alpha);
        alpha.setAnimationListener(new WelcomeAnimationListener());

    }

    public String getVersionName() throws Exception{
        PackageManager packageManager = getPackageManager();
        //0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
        String version = packInfo.versionName;
        return version;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    class WelcomeAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            swichToActivity();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * 跳转至主页面
     */
    private void swichToActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
