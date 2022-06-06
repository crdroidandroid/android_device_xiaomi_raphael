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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import java.lang.Math;

import org.lineageos.settings.utils.FileUtils;

public final class HapticUtils {

    final static String PREF_LEVEL = "haptic_level_pref";
    final static String PATH_LEVEL = "/sys/module/qti_haptics/parameters/vmax_mv_override";

    final static int MIN_LEVEL = 1;
    final static int MAX_LEVEL = 3700;

    public static void applyLevel(Context context, int value, boolean test) {
        if (FileUtils.fileExists(PATH_LEVEL)) {
            double level = value / 100.0 * (MAX_LEVEL - MIN_LEVEL) + MIN_LEVEL;
            int newValue = (int) Math.round(level);
            FileUtils.writeLine(PATH_LEVEL, String.valueOf(newValue));

            if (test) {
                Vibrator dev = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (dev.hasVibrator()) {
                    dev.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        }
    }

    public static void restoreLevel(Context context) {
        applyLevel(context, Settings.Secure.getInt(
            context.getContentResolver(), PREF_LEVEL, 80), false);
    }
}
