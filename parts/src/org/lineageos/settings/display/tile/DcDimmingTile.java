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

package org.lineageos.settings.display.tile;

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.R;

import vendor.xiaomi.hardware.displayfeature.V1_0.IDisplayFeature;

public class DcDimmingTile extends TileService {
    private static final String DC_DIMMING_KEY = "dc_dimming_enable";
    private static final boolean DC_DIMMING_DEFAULT_VALUE = false;

    @Override
    public void onStartListening() {
        super.onStartListening();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPrefs.getBoolean(DC_DIMMING_KEY, DC_DIMMING_DEFAULT_VALUE)) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_dimming_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_dimming_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        switchDcDimming(!(getQsTile().getState() == Tile.STATE_ACTIVE));
    }

    private void switchDcDimming(boolean enable) {
        try {
            IDisplayFeature mDisplayFeature = IDisplayFeature.getService();
            mDisplayFeature.setFeature(0, 20, enable ? 1 : 0, 255);
        } catch (Exception e) {
            // Do nothing
        }

        if (enable) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_dimming_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_dimming_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefs.edit().putBoolean(DC_DIMMING_KEY, enable).apply();
    }
}
