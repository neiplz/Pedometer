package com.github.neiplz.pedometer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    private static final String LOG_TAG = "WelcomeActivity";

//    TextView tvVersion;
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

//        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvProgress = (TextView) findViewById(R.id.tv_progress);

        // 渐变展示启动屏
        AlphaAnimation alpha = new AlphaAnimation(0.5f,1.0f);
        alpha.setDuration(50000);

        view.startAnimation(alpha);
        alpha.setAnimationListener(new WelcomeAnimationListener());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    class WelcomeAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            swichToActivity();
        }

        @Override
        public void onAnimationEnd(Animation animation) {

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
