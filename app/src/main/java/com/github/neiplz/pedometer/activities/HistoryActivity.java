package com.github.neiplz.pedometer.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.neiplz.pedometer.R;
import com.github.neiplz.pedometer.persistence.DatabaseHelper;
import com.github.neiplz.pedometer.utils.DateUtils;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HistoryActivity extends AppCompatActivity {

    private static final String LOG_TAG = "HistoryActivity";

    BarChart mBarChart;
    private DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "只能来点随机数据了", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateChart();
            }
        });

        /**
         * 数据库实例
         */
        mDatabaseHelper = DatabaseHelper.getInstance(this);

        mBarChart = (BarChart) findViewById(R.id.chart_bar);
        if (mBarChart.getData().size() > 0){
            mBarChart.clearChart();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        updateChart();
    }

    private void updateChart() {

        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(DateUtils.getToday());
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (mBarChart.getData().size() > 0){
            mBarChart.clearChart();
        }

        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        yesterday.add(Calendar.DAY_OF_YEAR, -6);

        int steps;
        int goal = 10000;
        boolean showSteps = true;

        mBarChart.setShowDecimal(true); // show decimal in distance view only

        BarModel bm;
        for (int i = 0; i < 7; i++) {
//            steps = mDatabaseHelper.getSteps(yesterday.getTimeInMillis());
            steps = new Random().nextInt(11000);
            if (steps > 0) {
                bm = new BarModel(df.format(new Date(yesterday.getTimeInMillis())), 0,
                        steps > goal ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
                if (showSteps) {
                    bm.setValue(steps);
                } else {
                    bm.setValue(steps);
                }
                mBarChart.addBar(bm);
            }
            yesterday.add(Calendar.DAY_OF_YEAR, 1);
        }
        if (mBarChart.getData().size() > 0) {
//            mBarChart.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(final View v) {
////                    Dialog_Statistics.getDialog(getActivity(), since_boot).show();
//                }
//            });
            mBarChart.startAnimation();
        } else {
            mBarChart.setVisibility(View.GONE);
        }
//        db.close();
    }


}
