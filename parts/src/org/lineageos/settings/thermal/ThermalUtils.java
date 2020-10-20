/*
 * Copyright (C) 2020 The LineageOS Project
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

package org.lineageos.settings.thermal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

import vendor.xiaomi.hardware.touchfeature.V1_0.ITouchFeature;

public final class ThermalUtils {

    private static final String THERMAL_CONTROL = "thermal_control";

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_BENCHMARK = 1;
    protected static final int STATE_BROWSER = 2;
    protected static final int STATE_CAMERA = 3;
    protected static final int STATE_DIALER = 4;
    protected static final int STATE_GAMING = 5;
    protected static final int STATE_YOUTUBE = 6;

    private static final String THERMAL_STATE_DEFAULT = "0";
    private static final String THERMAL_STATE_BENCHMARK = "10";
    private static final String THERMAL_STATE_BROWSER = "11";
    private static final String THERMAL_STATE_CAMERA = "12";
    private static final String THERMAL_STATE_DIALER = "8";
    private static final String THERMAL_STATE_GAMING = "13";
    private static final String THERMAL_STATE_YOUTUBE = "14";

    private static final String THERMAL_BENCHMARK = "thermal.benchmark=";
    private static final String THERMAL_BROWSER = "thermal.browser=";
    private static final String THERMAL_CAMERA = "thermal.camera=";
    private static final String THERMAL_DIALER = "thermal.dialer=";
    private static final String THERMAL_GAMING = "thermal.gaming=";
    private static final String THERMAL_YOUTUBE = "thermal.youtube=";

    private static final String THERMAL_SCONFIG = "/sys/class/thermal/thermal_message/sconfig";

    private boolean mTouchModeChanged;

    private Display mDisplay;
    private ITouchFeature mTouchFeature = null;
    private SharedPreferences mSharedPrefs;

    protected ThermalUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        WindowManager mWindowManager = context.getSystemService(WindowManager.class);
        mDisplay = mWindowManager.getDefaultDisplay();

        try {
            mTouchFeature = ITouchFeature.getService();
        } catch (RemoteException e) {
            // Do nothing
        }

    }

    public static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, ThermalService.class),
                UserHandle.CURRENT);
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(THERMAL_CONTROL, profiles).apply();
    }

    private String getValue() {
        String value = mSharedPrefs.getString(THERMAL_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = THERMAL_BENCHMARK + ":" + THERMAL_BROWSER + ":" + THERMAL_CAMERA + ":" +
                    THERMAL_DIALER + ":" + THERMAL_GAMING + ":" + THERMAL_YOUTUBE;
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_BENCHMARK:
                modes[0] = modes[0] + packageName + ",";
                break;
            case STATE_BROWSER:
                modes[1] = modes[1] + packageName + ",";
                break;
            case STATE_CAMERA:
                modes[2] = modes[2] + packageName + ",";
                break;
            case STATE_DIALER:
                modes[3] = modes[3] + packageName + ",";
                break;
            case STATE_GAMING:
                modes[4] = modes[4] + packageName + ",";
                break;
            case STATE_YOUTUBE:
                modes[5] = modes[5] + packageName + ",";
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3] + ":" +
                modes[4] + ":" + modes[5];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_BENCHMARK;
        } else if (modes[1].contains(packageName + ",")) {
            state = STATE_BROWSER;
        } else if (modes[2].contains(packageName + ",")) {
            state = STATE_CAMERA;
        } else if (modes[3].contains(packageName + ",")) {
            state = STATE_DIALER;
        } else if (modes[4].contains(packageName + ",")) {
            state = STATE_GAMING;
        } else if (modes[5].contains(packageName + ",")) {
            state = STATE_YOUTUBE;
        }

        return state;
    }

    protected void setDefaultThermalProfile() {
        FileUtils.writeLine(THERMAL_SCONFIG, THERMAL_STATE_DEFAULT);
    }

    protected void setThermalProfile(String packageName) {
        String value = getValue();
        String modes[];
        String state = THERMAL_STATE_DEFAULT;

        if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                state = THERMAL_STATE_BENCHMARK;
            } else if (modes[1].contains(packageName + ",")) {
                state = THERMAL_STATE_BROWSER;
            } else if (modes[2].contains(packageName + ",")) {
                state = THERMAL_STATE_CAMERA;
            } else if (modes[3].contains(packageName + ",")) {
                state = THERMAL_STATE_DIALER;
            } else if (modes[4].contains(packageName + ",")) {
                state = THERMAL_STATE_GAMING;
            } else if (modes[5].contains(packageName + ",")) {
                state = THERMAL_STATE_YOUTUBE;
            }
        }
        FileUtils.writeLine(THERMAL_SCONFIG, state);

        if (state == THERMAL_STATE_BENCHMARK || state == THERMAL_STATE_GAMING) {
            updateTouchModes(packageName);
        } else if (mTouchModeChanged) {
            resetTouchModes();
        }
    }

    private void updateTouchModes(String packageName) {
        String values = mSharedPrefs.getString(packageName, null);
        resetTouchModes();

        if (values == null || values.isEmpty()) {
            return;
        }

        String[] value = values.split(",");
        int gameMode = Integer.parseInt(value[Constants.TOUCH_GAME_MODE]);
        int touchResponse = Integer.parseInt(value[Constants.TOUCH_RESPONSE]);
        int touchSensitivity = Integer.parseInt(value[Constants.TOUCH_SENSITIVITY]);
        int touchResistant = Integer.parseInt(value[Constants.TOUCH_RESISTANT]);
        int touchActiveMode = (touchResponse != 0 && touchSensitivity != 0 && touchResistant != 0)
                ? 1 : 0;
        try {
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_TOLERANCE, touchSensitivity);
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_UP_THRESHOLD, touchResponse);
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_EDGE_FILTER, touchResistant);
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_GAME_MODE, gameMode);
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_ACTIVE_MODE, touchActiveMode);
        } catch (RemoteException e) {
            // Do nothing
        }

        mTouchModeChanged = true;
        updateTouchRotation();
    }

    protected void resetTouchModes() {
        if (!mTouchModeChanged) {
            return;
        }

        try {
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_GAME_MODE);
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_ACTIVE_MODE);
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_UP_THRESHOLD);
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_TOLERANCE);
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_EDGE_FILTER);
            mTouchFeature.resetTouchMode(Constants.MODE_TOUCH_ROTATION);
        } catch (RemoteException e) {
            // Do nothing
        }

        mTouchModeChanged = false;
    }

    protected void updateTouchRotation() {
        if (!mTouchModeChanged) {
            return;
        }

        int touchRotation = 0;
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                touchRotation = 0;
                break;
            case Surface.ROTATION_90:
                touchRotation = 1;
                break;
            case Surface.ROTATION_180:
                touchRotation = 2;
                break;
            case Surface.ROTATION_270:
                touchRotation = 3;
                break;
        }

        try {
            mTouchFeature.setTouchMode(Constants.MODE_TOUCH_ROTATION, touchRotation);
        } catch (RemoteException e) {
            // Do nothing
        }
    }
}
