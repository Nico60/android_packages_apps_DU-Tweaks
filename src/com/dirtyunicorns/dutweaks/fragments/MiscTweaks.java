/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.dirtyunicorns.dutweaks.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.dirtyunicorns.dutweaks.Helpers;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.util.du.QSUtils;

public class MiscTweaks extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String SHOW_FOURG = "show_fourg";
    private static final String DISABLE_IMMERSIVE_MESSAGE = "disable_immersive_message";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String FORCE_EXPANDED_NOTIFICATIONS = "force_expanded_notifications";
    private static final String ENABLE_TASK_MANAGER = "enable_task_manager";
    private static final String DISABLE_TORCH_ON_SCREEN_OFF = "disable_torch_on_screen_off";
    private static final String DISABLE_TORCH_ON_SCREEN_OFF_DELAY = "disable_torch_on_screen_off_delay";

    private SwitchPreference mShowFourG;
    private SwitchPreference mDisableIM;
    private SwitchPreference mStatusBarBrightnessControl;
    private SwitchPreference mForceExpanded;
    private SwitchPreference mEnableTaskManager;
    private SwitchPreference mTorchOff;
    private ListPreference mTorchOffDelay;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.misc_tweaks);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Activity activity = getActivity();

        mShowFourG = (SwitchPreference) findPreference(SHOW_FOURG);
        if (Utils.isWifiOnly(getActivity())) {
            prefSet.removePreference(mShowFourG);
        } else {
        mShowFourG.setChecked((Settings.System.getInt(resolver,
                Settings.System.SHOW_FOURG, 0) == 1));
        }

        mDisableIM = (SwitchPreference) findPreference(DISABLE_IMMERSIVE_MESSAGE);
        mDisableIM.setChecked((Settings.System.getInt(resolver,
                Settings.System.DISABLE_IMMERSIVE_MESSAGE, 0) == 1));

	mForceExpanded = (SwitchPreference) findPreference(FORCE_EXPANDED_NOTIFICATIONS);
        mForceExpanded.setChecked((Settings.System.getInt(resolver,
                Settings.System.FORCE_EXPANDED_NOTIFICATIONS, 0) == 1));

        mEnableTaskManager = (SwitchPreference) findPreference(ENABLE_TASK_MANAGER);
        mEnableTaskManager.setChecked((Settings.System.getInt(resolver,
                Settings.System.ENABLE_TASK_MANAGER, 0) == 1));

        mTorchOff = (SwitchPreference) prefSet.findPreference(DISABLE_TORCH_ON_SCREEN_OFF);
        mTorchOffDelay = (ListPreference) prefSet.findPreference(DISABLE_TORCH_ON_SCREEN_OFF_DELAY);
        int torchOffDelay = Settings.System.getInt(resolver,
                Settings.System.DISABLE_TORCH_ON_SCREEN_OFF_DELAY, 10);
        mTorchOffDelay.setValue(String.valueOf(torchOffDelay));
        mTorchOffDelay.setSummary(mTorchOffDelay.getEntry());
        mTorchOffDelay.setOnPreferenceChangeListener(this);
        if (!QSUtils.deviceSupportsFlashLight(activity)) {
            prefSet.removePreference(mTorchOff);
            prefSet.removePreference(mTorchOffDelay);
        }

        mStatusBarBrightnessControl = (SwitchPreference) findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);
        int statusBarBrightnessControl = Settings.System.getInt(getContentResolver(),
                STATUS_BAR_BRIGHTNESS_CONTROL, 0);
        mStatusBarBrightnessControl.setChecked(statusBarBrightnessControl != 0);
        try {
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_brightness_control_info);
            }
        } catch (SettingNotFoundException e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if  (preference == mDisableIM) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DISABLE_IMMERSIVE_MESSAGE, checked ? 1:0);
            return true;
        } else if  (preference == mForceExpanded) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FORCE_EXPANDED_NOTIFICATIONS, checked ? 1:0);
            return true;
        } else if  (preference == mEnableTaskManager) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_TASK_MANAGER, checked ? 1:0);
        }
        if  (preference == mShowFourG) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_FOURG, checked ? 1:0);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mStatusBarBrightnessControl) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), STATUS_BAR_BRIGHTNESS_CONTROL,
                    value ? 1 : 0);
            return true;
        } else if (preference == mTorchOffDelay) {
            int torchOffDelay = Integer.valueOf((String) newValue);
            int index = mTorchOffDelay.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DISABLE_TORCH_ON_SCREEN_OFF_DELAY, torchOffDelay);
            mTorchOffDelay.setSummary(mTorchOffDelay.getEntries()[index]);
            return true;
        }
        return false;
    }
}
