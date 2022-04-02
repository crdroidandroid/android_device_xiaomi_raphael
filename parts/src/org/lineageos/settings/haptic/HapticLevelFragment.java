/*
 * Copyright (C) 2022 The LineageOS Project
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
 * limitations under the License
 */

package org.lineageos.settings.haptic;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.FileUtils;
import org.lineageos.settings.widget.SeekBarPreference;

public class HapticLevelFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    private Vibrator mVibrator;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.haptic_settings);

        final SeekBarPreference mHapticLevel = (SeekBarPreference) findPreference(HapticUtils.PREF_LEVEL);
        if (FileUtils.fileExists(HapticUtils.PATH_LEVEL)) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (mVibrator == null || !mVibrator.hasVibrator()) {
                mVibrator = null;
            }
            mHapticLevel.setEnabled(true);
            mHapticLevel.setOnPreferenceChangeListener(this);
        } else {
            mHapticLevel.setSummary(R.string.haptic_level_summary_incompatible);
            mHapticLevel.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.haptic, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (HapticUtils.PREF_LEVEL.equals(preference.getKey())) {
            HapticUtils.applyLevel(getContext(), (int) newValue, true);
            doHapticFeedback();
        }

        return true;
    }

    private void doHapticFeedback() {
        if (mVibrator == null) {
            return;
        }
        mVibrator.vibrate(VibrationEffect.createOneShot(500,
                VibrationEffect.DEFAULT_AMPLITUDE));
    }
}
