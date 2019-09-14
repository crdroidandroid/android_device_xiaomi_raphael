/*
 * Copyright (C) 2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.popupcamera;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;

import java.util.List;

import org.lineageos.internal.util.FileUtils;
import vendor.xiaomi.hardware.motor.V1_0.IMotor;

public class PopupCameraService extends Service {

    private static final String TAG = "PopupCameraService";
    private static final boolean DEBUG = false;
    private static final String closeCameraState = "0";
    private static final String openCameraState = "1";
    private static String mCameraState = "-1";

    private IMotor mMotor = null;

    private SensorManager mSensorManager;
    private Sensor mFreeFallSensor;
    private static final int FREE_FALL_SENSOR_ID = 33171042;

    private static final String GREEN_LED_PATH = "/sys/class/leds/green/brightness";
    private static final String BLUE_LED_PATH = "/sys/class/leds/blue/brightness";

    private static Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        mSensorManager = this.getSystemService(SensorManager.class);
        mFreeFallSensor = mSensorManager.getDefaultSensor(FREE_FALL_SENSOR_ID);
        registerReceiver();
        try {
            mMotor = IMotor.getService();
        } catch(Exception e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        this.unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("lineageos.intent.action.CAMERA_STATUS_CHANGED");
        filter.addAction("lineageos.intent.action.ACTIVE_PACKAGE_CHANGED");
        this.registerReceiver(mIntentReceiver, filter);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (lineageos.content.Intent.ACTION_CAMERA_STATUS_CHANGED.equals(action)) {
               mCameraState = intent.getExtras().getString(lineageos.content.Intent.EXTRA_CAMERA_STATE);
               updateMotor(mCameraState);
            }
        }
    };

    private void updateMotor(String cameraState) {
        if (mMotor == null) return;
        try {
           if (cameraState.equals(openCameraState) && mMotor.getMotorStatus() == 13) {
               lightUp();
               mMotor.popupMotor(1);
               mSensorManager.registerListener(mFreeFallListener, mFreeFallSensor, SensorManager.SENSOR_DELAY_NORMAL);
           } else if (cameraState.equals(closeCameraState) && mMotor.getMotorStatus() == 11) {
               lightUp();
               mMotor.takebackMotor(1);
               mSensorManager.unregisterListener(mFreeFallListener, mFreeFallSensor);
           }
        } catch(Exception e) {
        }
    }

    private void lightUp() {
        FileUtils.writeLine(GREEN_LED_PATH, "255");
        FileUtils.writeLine(BLUE_LED_PATH, "255");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FileUtils.writeLine(GREEN_LED_PATH, "0");
                FileUtils.writeLine(BLUE_LED_PATH, "0");
            }
        }, 1200);
    }

    private SensorEventListener mFreeFallListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == FREE_FALL_SENSOR_ID && event.values[0] == 2.0f) {
               updateMotor(closeCameraState);
               goBackHome();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void goBackHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}
