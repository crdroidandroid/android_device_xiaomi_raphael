/**
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

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;
import org.lineageos.settings.widget.SeekBarPreference;

public class TouchSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mSharedPrefs;
    private SeekBarPreference mTouchSensitivity;
    private SeekBarPreference mTouchResponse;
    private SeekBarPreference mTouchResistant;
    private SwitchPreference mGameMode;

    private String packageName = "";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.touch_settings);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        Bundle bundle = getArguments();
        String appName = "";
        if (bundle != null) {
            appName = bundle.getString("appName", "");
            packageName = bundle.getString("packageName", "");
        }

        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(appName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mGameMode = (SwitchPreference) findPreference(Constants.PREF_TOUCH_GAME_MODE);
        mTouchResistant = (SeekBarPreference) findPreference(Constants.PREF_TOUCH_RESISTANT);
        mTouchResponse = (SeekBarPreference) findPreference(Constants.PREF_TOUCH_RESPONSE);
        mTouchSensitivity = (SeekBarPreference) findPreference(Constants.PREF_TOUCH_SENSITIVITY);
        updateDefaults();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
        if (Constants.PREF_TOUCH_GAME_MODE.equals(key)) {
            updateTouchModes(sharedPrefs.getBoolean(key, false) ? 1 : 0,
                    Constants.TOUCH_GAME_MODE);
        } else if (Constants.PREF_TOUCH_RESPONSE.equals(key)) {
            updateTouchModes(sharedPrefs.getInt(key, 0), Constants.TOUCH_RESPONSE);
        } else if (Constants.PREF_TOUCH_SENSITIVITY.equals(key)) {
            updateTouchModes(sharedPrefs.getInt(key, 0), Constants.TOUCH_SENSITIVITY);
        } else if (Constants.PREF_TOUCH_RESISTANT.equals(key)) {
            updateTouchModes(sharedPrefs.getInt(key, 0), Constants.TOUCH_RESISTANT);
        }
    }

    private void updateDefaults() {
        String[] values = getTouchValues().split(",");
        mGameMode.setChecked(Integer.parseInt(values[Constants.TOUCH_GAME_MODE]) == 1);
        mTouchResponse.setProgress(Integer.parseInt(values[Constants.TOUCH_RESPONSE]));
        mTouchSensitivity.setProgress(Integer.parseInt(values[Constants.TOUCH_SENSITIVITY]));
        mTouchResistant.setProgress(Integer.parseInt(values[Constants.TOUCH_RESISTANT]));
    }

    private void writeTouchValues(String modes) {
        mSharedPrefs.edit().putString(packageName, modes).apply();
    }

    public String getTouchValues() {
        String values = mSharedPrefs.getString(packageName, null);
        if (values == null || values.isEmpty()) {
            values = "0,0,0,0";
        }
        writeTouchValues(values);
        return values;
    }

    public void updateTouchModes(int value, int mode) {
        String[] values = getTouchValues().split(",");
        values[mode] = String.valueOf(value);
        String finalValues = values[Constants.TOUCH_GAME_MODE] + "," + values[Constants.TOUCH_RESPONSE] + ","
                + values[Constants.TOUCH_SENSITIVITY] + "," + values[Constants.TOUCH_RESISTANT];
        writeTouchValues(finalValues);
    }
}

