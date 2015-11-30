package com.github.neiplz.pedometer.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.github.neiplz.pedometer.AppConfig;
import com.github.neiplz.pedometer.R;
import com.github.neiplz.pedometer.models.Pref;
import com.github.neiplz.pedometer.persistence.DatabaseHelper;
import com.github.neiplz.pedometer.utils.Constants;

import java.util.List;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String LOG_TAG = "SettingsActivity";
    private static AppConfig mAppConfig;
    static DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mAppConfig = AppConfig.getAppConfig(getApplicationContext());
        mDatabaseHelper = DatabaseHelper.getInstance(getApplicationContext());
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            // 决定左上角图标的右侧是否有向左的小箭头, true 表示有小箭头，并且图标可以点击
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     *
     * 响应向左箭头点击事件
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        String action = getIntent().getAction();

//        PreferenceFragment

//        Log.d(LOG_TAG, "action = " + action);
//        FragmentManager fragmentManager = getFragmentManager();
//        if(null != fragmentManager){
//            Log.d(LOG_TAG, "clazz = " + fragmentManager.getClass().getName());
//        } else {
//            Log.d(LOG_TAG, "null == fragmentManager");
//        }


        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                Log.d(LOG_TAG, "...onOptionsItemSelected()...");
//                Intent parentIntent = NavUtils.getParentActivityIntent(this);
//                if (NavUtils.shouldUpRecreateTask(this, parentIntent)) {
//                    Log.d(LOG_TAG, "====if====");
//                    TaskStackBuilder.create(this)
//                            .addNextIntentWithParentStack(parentIntent)
//                            .startActivities();
//                } else {
//                    Log.d(LOG_TAG, "====else====");
//                    parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    NavUtils.navigateUpTo(this, parentIntent);
//                }
                this.finish();
                return true;
//            default:
//                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            boolean shouldSync = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                    .getBoolean(Constants.KEY_PREF_SYNC, true);

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(String.valueOf(value));

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
//            else if (preference instanceof SwitchPreference){
//
//            }
            else if (preference instanceof EditTextPreference){
                Log.d(LOG_TAG,"...EditTextPreference...called");

                if(Boolean.valueOf(mAppConfig.getProperties(Constants.KEY_HAS_LOGGED_IN)) && shouldSync){
                    String email = mAppConfig.getProperties(Constants.KEY_USER_EMAIL);
                    if(!TextUtils.isEmpty(email)){
                        String key = preference.getKey();
                        if(Constants.KEY_PREF_GOAL.equals(key)){
                            Log.d(LOG_TAG, "...goal...called");
                            Pref pref = new Pref();
                            pref.setEmail(email);
                            pref.setGoal(Integer.parseInt(stringValue));
                            pref.setSync(1);
                            mDatabaseHelper.updatePreference(pref);
                        } else if(Constants.KEY_PREF_STRIDE.equals(key)){
                            Log.d(LOG_TAG,"...stride...called");
                            Pref pref = new Pref();
                            pref.setEmail(email);
                            pref.setStride(Integer.parseInt(stringValue));
                            pref.setSync(1);
                            mDatabaseHelper.updatePreference(pref);
                        } else if(Constants.KEY_PREF_SENSITIVITY.equals(key)){
                            Log.d(LOG_TAG,"...SENSITIVITY...called");
                            Pref pref = new Pref();
                            pref.setEmail(email);
                            pref.setSensitivity(Float.parseFloat(stringValue));
                            pref.setSync(1);
                            mDatabaseHelper.updatePreference(pref);
                        }
                    }
                }
                preference.setSummary(stringValue);
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("sync"));
            bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_GOAL));
            bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_STRIDE));
            bindPreferenceSummaryToValue(findPreference(Constants.KEY_PREF_SENSITIVITY));
        }

        /**
         * 返回箭头(Up button)点击事件
         * @param item
         * @return
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.d(LOG_TAG,"...GeneralPreferenceFragment.onOptionsItemSelected(MenuItem item)..."+id);
            if (id == android.R.id.home) {
                Log.d(LOG_TAG,"返回箭头点击了啊1");
                startActivity(new Intent(getActivity(), SettingsActivity.class));

//                Activity activity = getActivity();
//
//                Intent upIntent = NavUtils.getParentActivityIntent(activity);
//                if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
//                    TaskStackBuilder.create(activity)
//                            .addNextIntentWithParentStack(upIntent)
//                            .startActivities();
//                } else {
//                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    NavUtils.navigateUpTo(activity, upIntent);
//                }

                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.d(LOG_TAG,"....DataSyncPreferenceFragment.onOptionsItemSelected(MenuItem item)..."+id);
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                Log.d(LOG_TAG, "返回箭头点击了啊2");
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


}
