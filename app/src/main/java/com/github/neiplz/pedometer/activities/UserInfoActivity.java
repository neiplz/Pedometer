package com.github.neiplz.pedometer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.neiplz.pedometer.AppConfig;
import com.github.neiplz.pedometer.R;
import com.github.neiplz.pedometer.utils.Constants;
import com.github.neiplz.pedometer.utils.NetworkUtils;
import com.github.neiplz.pedometer.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "UserInfoActivity";

    private static AppConfig mAppConfig;

    private LoadInfoTask mLoadInfoTask = null;

    private String mEmail;
    static boolean mHasLoaded = false;

//    TextView mTvEmail;
    TextView mTvChangePassword;
    Button btnLogout;
    static TextView mTvEmail;
    static TextView mTvName;
    static TextView mTvHeight;
    static TextView mTvWeight;
    static TextView mTvGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mAppConfig = AppConfig.getAppConfig(this);
        mEmail = mAppConfig.getProperties("user.email");

        btnLogout = (Button) findViewById(R.id.btn_logout);
        mTvEmail = (TextView) findViewById(R.id.email);
        mTvName = (TextView) findViewById(R.id.name);
        mTvHeight = (TextView) findViewById(R.id.height);
        mTvWeight = (TextView) findViewById(R.id.weight);
        mTvGender = (TextView) findViewById(R.id.gender);

//        mTvEmail = (TextView) findViewById(R.id.email);

        /**
         * 设置点击事件侦听
         */
        findViewById(R.id.ll_change_password).setOnClickListener(this);
        findViewById(R.id.edit_user_detail).setOnClickListener(this);

        btnLogout.setOnClickListener(this);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();

//        if(!mHasLoaded){
            attemptLoadInfo();
//        } else {
//
//        }

    }

    private void attemptLoadInfo() {
        if (null != mLoadInfoTask) {
            return;
        }

        int networkType = NetworkUtils.getNetworkType(UserInfoActivity.this);
        if(0 == networkType){
            Toast.makeText(this, getString(R.string.error_network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        mLoadInfoTask = new LoadInfoTask(mEmail);
        mLoadInfoTask.execute((Void) null);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG,"......onClick......");
        switch (v.getId()) {
            case R.id.btn_logout:
                //注销
                Intent intentLogout = new Intent(Constants.INTENT_ACTION_LOGOUT);
                sendBroadcast(intentLogout);
                finish();
                break;
            case R.id.ll_change_password:
                //修改密码
                Log.d(LOG_TAG, "修改密码");
                Intent intentEditPassword = new Intent(this,ChangPasswordActivity.class);
                intentEditPassword.putExtra("email", mAppConfig.getProperties("user.email"));
                startActivity(intentEditPassword);
                break;
            case R.id.edit_user_detail:
                //编辑用户信息
                Log.d(LOG_TAG, "编辑用户信息");
                Intent intentEditUserInfo = new Intent(this,EditUserInfoActivity.class);
                intentEditUserInfo.putExtra("email", mAppConfig.getProperties(Constants.KEY_USER_EMAIL));
                intentEditUserInfo.putExtra("name", mAppConfig.getProperties(Constants.KEY_USER_NAME));
                intentEditUserInfo.putExtra("height", mAppConfig.getProperties(Constants.KEY_USER_HEIGHT));
                intentEditUserInfo.putExtra("weight", mAppConfig.getProperties(Constants.KEY_USER_WEIGHT));
                intentEditUserInfo.putExtra("gender", mAppConfig.getProperties(Constants.KEY_USER_GENDER));
//                startActivity(intentEditUserInfo);
                startActivityForResult(intentEditUserInfo, Constants.REQUEST_CODE_EDIT_USER_INFO);
                break;
            default:
                break;
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
                if(Constants.REQUEST_CODE_EDIT_USER_INFO == requestCode){

                    Bundle bundle = data.getExtras();

                    mTvEmail.setText(bundle.getString("email"));//email可以不更新
                    mTvName.setText(bundle.getString("name"));
                    mTvHeight.setText(bundle.getString("height"));
                    mTvWeight.setText(bundle.getString("weight"));
                    mTvGender.setText(bundle.getString("gender"));
                }
                break;
            default:
                break;
        }


    }

    public class LoadInfoTask extends AsyncTask<Void, Void, JSONObject> {

        private String mEmail;

        public LoadInfoTask(String mEmail) {
            this.mEmail = mEmail;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected JSONObject doInBackground(Void... params) {
            OutputStream os = null;
            InputStream is = null;
            try {
                URL url = new URL(Constants.URL_LOAD_INFO);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod(Constants.REQUEST_METHOD_POST);
                httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
                httpURLConnection.setReadTimeout(Constants.READ_TIMEOUT);

                //参数
//                String param = "email=" + mEmail + "&name=" + mName+ "&password=" + mPassword;
                String param = "email=" + mEmail;
                //添加post请求的两行属性
                httpURLConnection.setRequestProperty("Content-Type", Constants.CONTENT_TYPE);
                httpURLConnection.setRequestProperty("Content-Length", param.length() + "");

                //设置打开输出流
                httpURLConnection.setDoOutput(true);
                //拿到输出流
                os = httpURLConnection.getOutputStream();
                //使用输出流往服务器提交数据
                os.write(param.getBytes());

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

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param jsonObject The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mLoadInfoTask = null;

            if(null != jsonObject){
                int resultCode = -1;


                try {
                    resultCode = jsonObject.getInt("resultCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(0 == resultCode){
//                    Toast.makeText(UserInfoActivity.this, "成功", Toast.LENGTH_SHORT).show();
                    JSONObject userInfo = null;
                    try {
                        //获取对象中的对象
                        userInfo = jsonObject.getJSONObject("data");


                        String email = userInfo.getString("email");
                        String height = userInfo.getString("height");
                        String weight = userInfo.getString("weight");
                        String name = userInfo.getString("name");
                        String gender = userInfo.getString("gender");

                        UserInfoActivity.mTvEmail.setText(email);
//                        UserInfoActivity.mTvHeight.setText(height + getString(R.string.measurement_unit_height));
                        UserInfoActivity.mTvHeight.setText(height);
//                        UserInfoActivity.mTvWeight.setText(weight + getString(R.string.measurement_unit_weight));
                        UserInfoActivity.mTvWeight.setText(weight);
                        UserInfoActivity.mTvName.setText(name);

                        if(null != gender){
                            if("0".equals(gender)){
                                UserInfoActivity.mTvGender.setText(getString(R.string.gender_male));
                            } else if("1".equals(gender)){
                                UserInfoActivity.mTvGender.setText(getString(R.string.gender_female));
                            }
                        }

                        Properties props = new Properties();
                        props.setProperty(Constants.KEY_USER_GENDER, gender);
                        props.setProperty(Constants.KEY_USER_HEIGHT, height);
                        props.setProperty(Constants.KEY_USER_WEIGHT, weight);
                        props.setProperty(Constants.KEY_USER_NAME, name);

                        UserInfoActivity.mAppConfig.set(props);

                        UserInfoActivity.mHasLoaded = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(UserInfoActivity.this,getString(R.string.load_failed),Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UserInfoActivity.this,getString(R.string.error_network_fail),Toast.LENGTH_LONG).show();
            }
        }
    }
}
