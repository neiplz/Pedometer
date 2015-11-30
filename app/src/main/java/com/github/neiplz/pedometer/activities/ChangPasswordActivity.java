package com.github.neiplz.pedometer.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.neiplz.pedometer.R;
import com.github.neiplz.pedometer.utils.Constants;
import com.github.neiplz.pedometer.utils.EncryptionUtils;
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
import java.util.Properties;

public class ChangPasswordActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ChangPasswordActivity";

    private String mEmail;
    private ChangePasswordTask mChangePwdTask = null;
    private EditText mOldPasswordView;
    private EditText mNewPasswordView;
    private EditText mNewPasswordAgainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chang_password);

        //使用Intent对象得到FirstActivity传递来的参数
        mEmail = getIntent().getStringExtra("email");

        mOldPasswordView = (EditText) findViewById(R.id.old_password);
        mNewPasswordView = (EditText) findViewById(R.id.new_password);
        mNewPasswordAgainView = (EditText) findViewById(R.id.new_password_again);

        Button btnChangePwd = (Button) findViewById(R.id.btn_change_password);
        btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChange();
//                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//                startActivityForResult(intent,5);
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ChangPasswordActivity.this.finish();
            }
        });
    }

    private void attemptChange() {
        if (null != mChangePwdTask) {
            return;
        }

        int networkType = NetworkUtils.getNetworkType(ChangPasswordActivity.this);
        if(0 == networkType){
            Toast.makeText(this, getString(R.string.error_network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset errors.
        mOldPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mNewPasswordAgainView.setError(null);

        String oldPassword = mOldPasswordView.getText().toString();
        String newPassword = mNewPasswordView.getText().toString();
        String newPasswordAgain = mNewPasswordAgainView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(oldPassword)) {
            mOldPasswordView.setError(getString(R.string.error_field_required));
            focusView = mOldPasswordView;
            cancel = true;
        } else if(!StringUtils.isPasswordValid(oldPassword)) {
            mOldPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mOldPasswordView;
            cancel = true;
        }

        if (!cancel && TextUtils.isEmpty(newPassword)) {
            mNewPasswordView.setError(getString(R.string.error_field_required));
            focusView = mNewPasswordView;
            cancel = true;
        } else if(!StringUtils.isPasswordValid(newPassword)) {
            mNewPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mNewPasswordView;
            cancel = true;
        }

        if (!cancel && TextUtils.isEmpty(newPasswordAgain)) {
            mNewPasswordAgainView.setError(getString(R.string.error_field_required));
            focusView = mNewPasswordAgainView;
            cancel = true;
        } else if(!StringUtils.isPasswordValid(newPasswordAgain)) {
            mNewPasswordAgainView.setError(getString(R.string.error_invalid_password));
            focusView = mNewPasswordAgainView;
            cancel = true;
        }

        if (!cancel && newPassword.equals(oldPassword)) {
            mNewPasswordView.setError(getString(R.string.error_password_diff));
            focusView = mNewPasswordView;
            cancel = true;
        }

        if (!cancel && !newPassword.equals(newPasswordAgain)) {
            mNewPasswordAgainView.setError(getString(R.string.error_confirm_password_failed));
            focusView = mNewPasswordAgainView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            try {
                oldPassword = EncryptionUtils.encryptDES(oldPassword, Constants.ENCRYPTION_KEY);
                newPassword = EncryptionUtils.encryptDES(newPassword, Constants.ENCRYPTION_KEY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mChangePwdTask = new ChangePasswordTask(mEmail, oldPassword, newPassword);
            mChangePwdTask.execute((Void) null);
        }

    }


    public class ChangePasswordTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mEmail;
        private final String mOldPassword;
        private final String mNewPassword;

        public ChangePasswordTask(String email,String oldPassword,String newPassword) {
            mEmail = email;
            mOldPassword = oldPassword;
            mNewPassword = newPassword;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OutputStream os = null;
            InputStream is = null;
            try {
                URL url = new URL(Constants.URL_UPDATE_PASSWORD);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod(Constants.REQUEST_METHOD_POST);
                httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
                httpURLConnection.setReadTimeout(Constants.READ_TIMEOUT);

                //参数
//                String param = "email=" + mEmail + "&name=" + mName+ "&password=" + mPassword;
                String param = "email=" + mEmail + "&oldPwd=" + URLEncoder.encode(mOldPassword, "UTF-8") + "&newPwd=" + URLEncoder.encode(mNewPassword, Constants.CHARSET_UTF8);
                //添加post请求的两行属性
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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

                if(0 == resultCode){
                    Toast.makeText(ChangPasswordActivity.this, getString(R.string.prompt_password_change_succeed), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChangPasswordActivity.this,getString(R.string.error_password_change_failed),Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChangPasswordActivity.this,getString(R.string.error_network_fail),Toast.LENGTH_LONG).show();
            }

            mChangePwdTask = null;
        }
    }


}
