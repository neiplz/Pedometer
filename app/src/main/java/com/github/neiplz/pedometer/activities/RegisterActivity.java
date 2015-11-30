package com.github.neiplz.pedometer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.neiplz.pedometer.AppConfig;
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

public class RegisterActivity extends AppCompatActivity {


    private UserRegisterTask mRegTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mNameView;
    private EditText mPasswordView;
    private EditText mPasswordAgainView;
    private View mProgressView;
    private View mRegisterFormView;

    private static AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        populateAutoComplete();

        mNameView = (EditText) findViewById(R.id.name);

        mPasswordView = (EditText) findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                //后者 判断软键盘选择的内容
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
        mPasswordAgainView = (EditText) findViewById(R.id.password_again);

        Button btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
//                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//                startActivityForResult(intent,5);
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        appConfig = AppConfig.getAppConfig(this);
    }

    private void populateAutoComplete() {
//        getLoaderManager().initLoader(0, null, this);
    }

    private void attemptRegister() {
        if (null != mRegTask) {
            return;
        }

        int networkType = NetworkUtils.getNetworkType(RegisterActivity.this);
        if(0 == networkType){
            Toast.makeText(this, getString(R.string.error_network_not_available), Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordAgain = mPasswordAgainView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!StringUtils.isEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !StringUtils.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (!TextUtils.isEmpty(passwordAgain) && !StringUtils.isPasswordValid(passwordAgain)) {
            mPasswordAgainView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordAgainView;
            cancel = true;
        } else if(!passwordAgain.equals(password)){
            mPasswordAgainView.setError(getString(R.string.error_confirm_password_failed));
            focusView = mPasswordAgainView;
            cancel = true;
        }




        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            try {
                password = EncryptionUtils.encryptDES(password, Constants.ENCRYPTION_KEY);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mRegTask = new UserRegisterTask(email, name, password);
            mRegTask.execute((Void) null);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mEmail;
        private final String mName;
        private final String mPassword;

        UserRegisterTask(String email, String name, String password) {
            mEmail = email;
            mName = name;
            mPassword = password;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
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
                URL url = new URL(Constants.URL_USER_REGISTER);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod(Constants.REQUEST_METHOD_POST);
                httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
                httpURLConnection.setReadTimeout(Constants.READ_TIMEOUT);

                //参数
//                String param = "email=" + mEmail + "&name=" + mName+ "&password=" + mPassword;
                String param = "email=" + mEmail + "&name=" + mName  + "&password=" + URLEncoder.encode(mPassword, Constants.CHARSET_UTF8);
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
//                    Log.d(LOG_TAG,response);
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
            mRegTask = null;
            showProgress(false);

            if(null != json){
                int resultCode = -1;

                try {
                    resultCode = json.getInt("resultCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(0 == resultCode){
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();

                    Intent data = new Intent();
//                    data.putExtra(BUNDLE_KEY_REQUEST_CODE, requestCode);
                    data.putExtra("email", mEmail);
                    data.putExtra("name", mName);
                    data.putExtra("password", mPassword);
                    setResult(RESULT_OK, data);
                    RegisterActivity.this.sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGIN));

                    // 保存登录信息
                    Properties props = new Properties();
                    props.setProperty(Constants.KEY_USER_EMAIL, mEmail);
                    props.setProperty(Constants.KEY_USER_NAME, mName);
//                    props.setProperty(Constants.KEY_USER_PASSWORD, mPassword);
                    props.setProperty(Constants.KEY_IS_REMEMBER_ME, String.valueOf(true));

                    appConfig.set(props);

                    finish();
                } else if (2 == resultCode) {
                    mEmailView.setError(getString(R.string.error_mail_been_taken));
                    mEmailView.requestFocus();
                } else {
                    Toast.makeText(RegisterActivity.this,getString(R.string.error_registration_failed),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this,getString(R.string.error_network_fail),Toast.LENGTH_LONG).show();
            }
        }

        //取消验证
        @Override
        protected void onCancelled() {
            mRegTask = null;
            showProgress(false);
        }
    }
}
