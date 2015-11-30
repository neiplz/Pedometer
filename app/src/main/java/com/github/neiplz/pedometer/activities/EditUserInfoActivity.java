package com.github.neiplz.pedometer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.net.URLEncoder;

public class EditUserInfoActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EditUserInfoActivity";

    private EditUserInfoTask mEditUserInfoTask = null;
    private static AppConfig mAppConfig;

//    private static TextView mTvEmail;
    private String mEmail;
    private String mName;
    private String mHeight;
    private String mWeight;
    private String mGender;

    private TextView mTvEmail;
    private EditText mEtName;
    private EditText mEtHeight;
    private EditText mEtWeight;
    private RadioGroup mRgGender;
    private RadioButton mRbMale;
    private RadioButton mRbFemale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        mAppConfig = AppConfig.getAppConfig(this);

        //使用Intent对象得到FirstActivity传递来的参数
        mEmail = getIntent().getStringExtra("email");
        mName = getIntent().getStringExtra("name");
        mHeight = getIntent().getStringExtra("height");
        mWeight = getIntent().getStringExtra("weight");
        mGender = getIntent().getStringExtra("gender");

        mTvEmail = (TextView) findViewById(R.id.email);
        mEtName = (EditText) findViewById(R.id.name);
        mEtHeight = (EditText) findViewById(R.id.height);
        mEtWeight = (EditText) findViewById(R.id.weight);

        mRgGender = (RadioGroup) findViewById(R.id.gender);
        mRbMale = (RadioButton) findViewById(R.id.male);
        mRbFemale = (RadioButton) findViewById(R.id.female);
        mRgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.male:
                        Log.d(LOG_TAG, "男");
                        mGender = "0";
                        break;
                    case R.id.female:
                        Log.d(LOG_TAG, "女");
                        mGender = "1";
                        break;
                }
            }
        });

        Button btnCommit = (Button) findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmit();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void attemptSubmit() {
        if (null != mEditUserInfoTask) {
            return;
        }

        int networkType = NetworkUtils.getNetworkType(EditUserInfoActivity.this);
        if(0 == networkType){
            Toast.makeText(this, getString(R.string.error_network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        mEditUserInfoTask = new EditUserInfoTask(mEmail, mEtName.getText().toString(), mEtHeight.getText().toString(), mEtWeight.getText().toString(), mGender);
        mEditUserInfoTask.execute((Void) null);
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
//        mTvEmail.setText(mAppConfig.getProperties(Constants.KEY_USER_EMAIL));

        mTvEmail.setText(mEmail);
        mEtName.setText(mName);
        mEtHeight.setText(mHeight);
        mEtWeight.setText(mWeight);

        mGender = getIntent().getStringExtra("gender");
        if(null != mGender){
            if("0".equals(mGender)){
                mRbMale.setChecked(true);
            } else if("1".equals(mGender)){
                mRbFemale.setChecked(true);
            }
        }


    }

    public class EditUserInfoTask extends AsyncTask<Void, Void, JSONObject> {

        private String email;
        private String name;
        private String height;
        private String weight;
        private String gender;

        public EditUserInfoTask(String email, String name, String height, String weight, String gender) {
            this.email = email;
            this.name = name;
            this.height = height;
            this.weight = weight;
            this.gender = gender;
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
                URL url = new URL(Constants.URL_UPDATE_USER_INFO);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod(Constants.REQUEST_METHOD_POST);
                httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
                httpURLConnection.setReadTimeout(Constants.READ_TIMEOUT);

                //参数
                String param = "email=" + email + "&height=" + height + "&weight=" + weight + "&gender=" + gender + "&name=" + URLEncoder.encode(name, Constants.CHARSET_UTF8);

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
                    Log.d(LOG_TAG, response);
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
//            super.onPostExecute(jsonObject);
            mEditUserInfoTask = null;

            if(null != jsonObject){
                int resultCode = -1;

                try {
                    resultCode = jsonObject.getInt("resultCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(0 == resultCode){
                    Toast.makeText(EditUserInfoActivity.this, getString(R.string.user_info_edit_succeed), Toast.LENGTH_SHORT).show();

                    //数据是使用Intent返回
                    Intent intent = new Intent();
                    //把返回数据存入Intent
                    intent.putExtra("email", email);
                    intent.putExtra("name", name);
                    intent.putExtra("height", height);
                    intent.putExtra("weight", weight);
                    intent.putExtra("gender", gender);
                    //设置返回数据
                    EditUserInfoActivity.this.setResult(RESULT_OK, intent);
                    //关闭Activity
                    EditUserInfoActivity.this.finish();

                } else {
                    Toast.makeText(EditUserInfoActivity.this, getString(R.string.user_info_edit_failed), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditUserInfoActivity.this,getString(R.string.error_network_fail),Toast.LENGTH_LONG).show();
            }
        }
    }
}
