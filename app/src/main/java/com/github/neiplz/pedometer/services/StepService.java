package com.github.neiplz.pedometer.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.github.neiplz.pedometer.listeners.StepDetectorListener;

/**
 * 计步服务
 */
public class StepService extends Service {

	private static final String LOG_TAG = "StepService";

	public static Boolean mFlag = false;
	private SensorManager mSensorManager;
	private StepDetectorListener mStepDetectorListener;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		new Thread(new Runnable() {
			public void run() {
				startStepDetector();
			}
		}).start();

	}

	private void startStepDetector() {
//        Log.d(LOG_TAG,"StepService started");
		mFlag = true;
		mStepDetectorListener = new StepDetectorListener(this);
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);//获取传感器管理器的实例
		Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//获得传感器的类型，这里获得的类型是加速度传感器
		//此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
		mSensorManager.registerListener(mStepDetectorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFlag = false;

        // 注销传感器监听事件
		if (mStepDetectorListener != null) {
			mSensorManager.unregisterListener(mStepDetectorListener);
		}
	}
}
