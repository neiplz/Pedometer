package com.github.neiplz.pedometer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.neiplz.pedometer.activities.HistoryActivity;
import com.github.neiplz.pedometer.activities.LoginActivity;
import com.github.neiplz.pedometer.activities.SettingsActivity;
import com.github.neiplz.pedometer.activities.UserInfoActivity;
import com.github.neiplz.pedometer.models.Pref;
import com.github.neiplz.pedometer.models.Step;
import com.github.neiplz.pedometer.persistence.DatabaseHelper;
import com.github.neiplz.pedometer.listeners.StepDetectorListener;
import com.github.neiplz.pedometer.services.StepService;
import com.github.neiplz.pedometer.utils.Constants;
import com.github.neiplz.pedometer.utils.DateUtils;
import com.github.neiplz.pedometer.utils.NetworkUtils;
import com.github.neiplz.pedometer.utils.StringUtils;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity";

    private boolean mHasLoggedIn = false;
    private TextView mTvUserName;
    private static AppConfig mAppConfig;

    private static TextView mTvSteps;
    private static TextView mTvGoal;
    private static TextView mTvDistance;
    private static TextView mTvCalorie;
    private static TextView mTvDuration;
//    private TextView mTvAverage;

    private static PieChart mPieChart;
    private static PieModel mPieModeCurrent;
    private static PieModel mPieModeTotal;
//    private boolean mShowSteps = true;

    private DatabaseHelper mDatabaseHelper;
    private String mEmail;
    private Thread mThread;
    private volatile boolean finished = false;
    private Intent mStepServiceIntent;
    private static int mCurrentStep = 0;

    private static int mCalories = 0;
    private static double mDistance = 0;

    private SharedPreferences mSharedPreferences;
    private static int mStride;//步长
    private static float mWeight = 70;
    private int mGoal;//目标
    private int mSensitivity;//敏感度
    private boolean mPrefSync;//配置是否同步

    SyncPrefTask mSyncPrefTask;

    FloatingActionButton mFabStart;
    FloatingActionButton mFabPause;
    FloatingActionButton mFabStop;

    private static int mStepToday = 0;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.INTENT_ACTION_LOGOUT)) {
                // 用户注销
                mHasLoggedIn = false;
                mAppConfig.set(Constants.KEY_HAS_LOGGED_IN,String.valueOf(mHasLoggedIn));
                mTvUserName.setText(R.string.logged_in_user_name);

            } else if (action.equals(Constants.INTENT_ACTION_LOGIN)) {
                // 用户登录
                mHasLoggedIn = true;
                mAppConfig.set(Constants.KEY_HAS_LOGGED_IN,String.valueOf(mHasLoggedIn));
                Log.d(LOG_TAG, "用户登录");

                String email = mAppConfig.getProperties(Constants.KEY_USER_EMAIL);
                if(!TextUtils.isEmpty(email)){
                    mTvUserName.setText(email);
                    /**
                     * 只在用户登录的时候做同步配置信息
                     */
                    Log.d(LOG_TAG, "用户登录--同步配置");
                    syncPreference(email);
                }
            }
        }
    };


    private static Handler mHandler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mCurrentStep = StepDetectorListener.CURRENT_SETP;

            switch (msg.what){
                case 1:
                    mTvSteps.setText(String.valueOf(mCurrentStep));
                    mPieModeCurrent.setValue(mCurrentStep);
                    mPieChart.update();

                    mDistance = mCurrentStep * mStride * 0.01;
                    mTvDistance.setText(String.valueOf((int)mDistance));

//                    mCalories = (int) (mWeight * mCurrentStep * mDistance *0.001 );
                    mTvCalorie.setText(String.valueOf(mStepToday + mCurrentStep));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppConfig = AppConfig.getAppConfig(this);

        //清除登录记录
//        mAppConfig.remove(Constants.KEY_HAS_LOGGED_IN);
        mHasLoggedIn = false;
        mAppConfig.set(Constants.KEY_HAS_LOGGED_IN, String.valueOf(mHasLoggedIn));

        mStepServiceIntent = new Intent(this, StepService.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //底部按钮
        mFabStart = (FloatingActionButton) findViewById(R.id.fab_start);
        mFabPause = (FloatingActionButton) findViewById(R.id.fab_pause);
        mFabStop = (FloatingActionButton) findViewById(R.id.fab_stop);

        mFabStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, getString(R.string.tip_start), Toast.LENGTH_SHORT).show();

                view.setVisibility(View.INVISIBLE);
                mFabPause.setVisibility(View.VISIBLE);
                mFabStop.setVisibility(View.VISIBLE);

                Log.d(LOG_TAG, "启用StepService");

                StepDetectorListener.SENSITIVITY = Float.valueOf(mSharedPreferences.getString(Constants.KEY_PREF_SENSITIVITY, Constants.DEFAULT_STRING_SENSITIVITY));
                startService(mStepServiceIntent);
                startTask();
            }
        });

        mFabPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, getString(R.string.tip_pause), Toast.LENGTH_SHORT).show();


                mFabStart.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
                mFabStop.setVisibility(View.VISIBLE);

                Log.d(LOG_TAG, "暂停StepService");

                stopTask();
                stopService(mStepServiceIntent);
            }
        });

        mFabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, getString(R.string.tip_sttop), Toast.LENGTH_SHORT).show();

                mFabStart.setVisibility(View.VISIBLE);
                mFabPause.setVisibility(View.INVISIBLE);
                view.setVisibility(View.INVISIBLE);

                Log.d(LOG_TAG, "停止StepService");

                if(isServiceRunning(StepService.class.getName())){
                    Log.d(LOG_TAG, "StepService实际上正在运行");
                    stopTask();
                    stopService(mStepServiceIntent);
                } else {
                    Log.d(LOG_TAG, "StepService实际上已经关闭");
                }

                if(mHasLoggedIn){
                    String email = mAppConfig.getProperties(Constants.KEY_USER_EMAIL);
                    if(!TextUtils.isEmpty(email)){
                        mStepToday = mDatabaseHelper.updateSteps(email, DateUtils.getToday(), StepDetectorListener.CURRENT_SETP, 1);
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_records_save_failed), Toast.LENGTH_SHORT).show();
                }
                StepDetectorListener.CURRENT_SETP = 0;
                mTvSteps.setText("0");
            }
        });

        //侧边栏
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(navigationView.getHeaderCount() > 0) {
            final View header = navigationView.getHeaderView(0);

            //用户名区（即邮件）
            mTvUserName = (TextView) header.findViewById(R.id.tv_user_name);
            mTvUserName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(mHasLoggedIn){
                        naviTo(UserInfoActivity.class);
                    } else {
                        naviTo(LoginActivity.class);
                    }
                }
            });

            //头像区
            final ImageView ivAvatar = (ImageView) header.findViewById(R.id.iv_avatar);
            ivAvatar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(mHasLoggedIn){
                        naviTo(UserInfoActivity.class);
                    } else {
                        naviTo(LoginActivity.class);
                    }
                }
            });
        }

        mTvSteps = (TextView) findViewById(R.id.steps);
        mTvGoal = (TextView) findViewById(R.id.goal);
        mTvCalorie = (TextView) findViewById(R.id.calorie);
        mTvDistance = (TextView) findViewById(R.id.distance);
        mTvDuration = (TextView) findViewById(R.id.duration);


        // 注册BroadcastReceiver
        IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_LOGOUT);
        filter.addAction(Constants.INTENT_ACTION_LOGIN);
        registerReceiver(mBroadcastReceiver, filter);

        /**
         * 数据库实例
         */
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        mSharedPreferences = getSharedPreferences(Constants.PERF_NAME_SHARED_PREFERENCE, MODE_PRIVATE);
        mGoal = Integer.valueOf(mSharedPreferences.getString(Constants.KEY_PREF_GOAL, "10000"));

        /**
         * 初始化饼图
         */
        mPieChart = (PieChart) findViewById(R.id.chart_pie);

        mPieModeCurrent = new PieModel("", 0, Color.parseColor("#00e676"));
        mPieChart.addPieSlice(mPieModeCurrent);

        mPieModeTotal = new PieModel("", mGoal, Color.parseColor("#f44336"));
        mPieChart.addPieSlice(mPieModeTotal);

        mPieChart.setDrawValueInPie(false);
        mPieChart.setUsePieRotation(true);
        mPieChart.startAnimation();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mEmail = mAppConfig.getProperties(Constants.KEY_USER_EMAIL);

        mStride = Integer.valueOf(mSharedPreferences.getString(Constants.KEY_PREF_STRIDE, Constants.DEFAULT_STRING_STRIDE));
//        mSensitivity = Integer.valueOf(mSharedPreferences.getString(Constants.KEY_PREF_SENSITIVITY, Constants.DEFAULT_STRING_SENSITIVITY));
        StepDetectorListener.SENSITIVITY = Float.valueOf(mSharedPreferences.getString(Constants.KEY_PREF_SENSITIVITY, Constants.DEFAULT_STRING_SENSITIVITY));
        mPrefSync = mSharedPreferences.getBoolean(Constants.KEY_PREF_SYNC, true);

        mTvGoal.setText(String.valueOf(mGoal));
        mTvDuration.setText(String.valueOf(mStride));

        if (mHasLoggedIn) {
            Log.d(LOG_TAG, "用户已登录");

            String tpWeight = mAppConfig.getProperties(Constants.KEY_USER_WEIGHT);
            if(!TextUtils.isEmpty(tpWeight)){
                mWeight = Float.valueOf(tpWeight);
            }

            Step initStep = mDatabaseHelper.queryStep(mEmail, DateUtils.getToday());
            if(null != initStep){
                mStepToday = initStep.getStepCount();
            }
        } else {
            Log.d(LOG_TAG, "用户未登录");
            Toast.makeText(this, "用户未登录，计步功能能正常运行，但无法保存记录", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppConfig.set(Constants.KEY_HAS_LOGGED_IN, String.valueOf(false));
        //注销BroadcastReceiver
        unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 右上角按钮点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            // catch event that there's no activity to handle intent
            if (null != intent.resolveActivity(getPackageManager())) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "404!页面不存在!", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 侧边栏顶级菜单点击事件
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            if(mHasLoggedIn){
                naviTo(HistoryActivity.class);
            } else {
                Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_info) {
            if(mHasLoggedIn){
                naviTo(UserInfoActivity.class);
            } else {
                Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_setting) {
            naviTo(SettingsActivity.class);
//            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void startTask(){
        if(null == mThread){
            finished = false;
//            Log.d(LOG_TAG,"it's here in startTask()......");
            mThread = new Thread(new Runnable() {
                public void run() {
                    while (!finished) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                        Log.d(LOG_TAG,"it's here Thread.run()......");
                        if (StepService.mFlag) {
//                            Log.d(LOG_TAG,"StepService.mFlag = true");
                            Message msg = new Message();
                            msg.what = 1;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            });
            mThread.start();
        }
    }

    private void stopTask(){
        if(null != mThread){
            finished = true;
            mThread = null;
        }
    }

    /**
     * Intent跳转
     * @param cls
     */
    private void naviTo(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        if (null != intent.resolveActivity(getPackageManager())) {
            startActivity(intent);
        }
    }


    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
//                Bundle bundle = data.getExtras();
//                mTvUserName.setText(bundle.getString("email"));
                break;
            default:
                break;
        }
    }

    private boolean isServiceRunning(String className) {
        if(!TextUtils.isEmpty(className)){
            ActivityManager myManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServiceList = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(100);
            for (int i = 0; i < runningServiceList.size(); i++) {
                if (runningServiceList.get(i).service.getClassName().toString()
                        .equals(className)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean syncPreference(String email){

        if(0 == NetworkUtils.getNetworkType(MainActivity.this)){
            Log.d(LOG_TAG,"网络不可用，无法同步配置信息");
            return false;
        }

        Pref pref = mDatabaseHelper.queryPref(email);

        if(null != pref){
            Log.d(LOG_TAG,"Pref = " + pref.toString());
            //上传
            int sync = pref.getSync();
            if(sync == 1){
                if (null == mSyncPrefTask) {
                    mSyncPrefTask = new SyncPrefTask(pref, 1);
                    mSyncPrefTask.execute((Void) null);
                    return true;
                }
            }
        } else {
            //下载
            pref = new Pref();
            pref.setEmail(email);
            if (null == mSyncPrefTask) {
                mSyncPrefTask = new SyncPrefTask(pref, 2);
                mSyncPrefTask.execute((Void) null);
                return true;
            }
        }
        return false;
    }


    public class SyncPrefTask extends AsyncTask<Void, Void, JSONObject> {

        private Pref mPref;
        private final int mFlag;//1:upload; 2:download

        public SyncPrefTask(Pref pref, int flag) {
            mPref = pref;
            mFlag = flag;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OutputStream os = null;
            InputStream is = null;
            try {
                URL url = new URL(Constants.URL_SYNC_PREF);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod(Constants.REQUEST_METHOD_POST);
                httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
                httpURLConnection.setReadTimeout(Constants.READ_TIMEOUT);

                StringBuffer sb = new StringBuffer();
                sb.append("flag=").append(mFlag);
                if(null != mPref){
                    if(1 == mFlag){
                        String email = mPref.getEmail();
                        sb.append("&email=");
                        if(!TextUtils.isEmpty(email)){
                            sb.append(email);
                        }

                        int goal = mPref.getGoal();
                        sb.append("&goal=");
                        if (goal > 0) {
                            sb.append(goal);
                        }

                        int stride = mPref.getStride();
                        sb.append("&stride=");
                        if (stride > 0) {
                            sb.append(stride);
                        }
                        float sensitivity = mPref.getSensitivity();
                        sb.append("&sensitivity=");
                        if (sensitivity > 0.0f) {
                            sb.append(sensitivity);
                        }
                    } else if(2 == mFlag){
                        String email = mPref.getEmail();
                        sb.append("&email=");
                        if(!TextUtils.isEmpty(email)){
                            sb.append(email);
                        }
                    }

                }
                //添加post请求的两行属性
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Content-Length", sb.length() + "");

                //设置打开输出流
                httpURLConnection.setDoOutput(true);
                //拿到输出流
                os = httpURLConnection.getOutputStream();
                //使用输出流往服务器提交数据
                os.write(sb.toString().getBytes());

                if(HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()){
                    is = httpURLConnection.getInputStream();
                    String response = NetworkUtils.getStringFromStream(is);
                    Log.d(LOG_TAG,response);
                    return StringUtils.toJson(response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }  catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(null != os){
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(null != is){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        //线程结束后的ui处理
        @Override
        protected void onPostExecute(final JSONObject json) {

            if(null != json){
                int resultCode = -1;

                try {
                    resultCode = json.getInt("resultCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(1 == mFlag){
                    boolean res = false;
                    if(1 == resultCode){
                        res = mDatabaseHelper.setPrefFlag(mPref.getEmail(),0);
                    }
                    if(res){
                        Toast.makeText(MainActivity.this, "设置信息上传成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "设置信息上传失败！", Toast.LENGTH_SHORT).show();

                    }
                } else if(2 == mFlag){

                    if(2 == resultCode){
                        JSONObject obj = null;
                        String goal = null;
                        String stride = null;
                        String sensitivity = null;
                        Pref pref = new Pref();

                        try {
                            //获取对象中的对象
                            obj = json.getJSONObject("data");

                            goal = obj.getString(Constants.KEY_PREF_GOAL);
                            stride = obj.getString(Constants.KEY_PREF_STRIDE);
                            sensitivity = obj.getString(Constants.KEY_PREF_SENSITIVITY);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                        if(null != goal){
                            edit.putString(Constants.KEY_PREF_GOAL, goal);
                            pref.setGoal(Integer.parseInt(goal));
                        }
                        if(null != stride){
                            edit.putString(Constants.KEY_PREF_STRIDE, stride);
                            pref.setStride(Integer.parseInt(stride));
                        }
                        if(null != sensitivity){
                            edit.putString(Constants.KEY_PREF_SENSITIVITY, sensitivity);
                            pref.setSensitivity(Float.parseFloat(sensitivity));
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                            edit.apply();
                        } else {
                            edit.commit();
                        }

                        pref.setSync(0);
                        pref.setEmail(mPref.getEmail());
                        //更新到本地数据库，并将sync置为0
                        mDatabaseHelper.updatePreference(pref);
                        Toast.makeText(MainActivity.this, "设置信息下载成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "设置信息下载失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            mSyncPrefTask = null;
        }
    }

}
