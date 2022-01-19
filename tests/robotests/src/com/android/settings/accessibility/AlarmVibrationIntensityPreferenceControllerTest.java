/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.settings.accessibility;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.VibrationAttributes;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.preference.PreferenceScreen;
import androidx.test.core.app.ApplicationProvider;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AlarmVibrationIntensityPreferenceControllerTest {

    private static final String PREFERENCE_KEY = "preference_key";

    @Mock private PreferenceScreen mScreen;

    private Lifecycle mLifecycle;
    private Context mContext;
    private Vibrator mVibrator;
    private AlarmVibrationIntensityPreferenceController mController;
    private SeekBarPreference mPreference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mLifecycle = new Lifecycle(() -> mLifecycle);
        mContext = ApplicationProvider.getApplicationContext();
        mVibrator = mContext.getSystemService(Vibrator.class);
        mController = new AlarmVibrationIntensityPreferenceController(mContext, PREFERENCE_KEY,
                Vibrator.VIBRATION_INTENSITY_HIGH);
        mLifecycle.addObserver(mController);
        mPreference = new SeekBarPreference(mContext);
        mPreference.setSummary("Test summary");
        when(mScreen.findPreference(mController.getPreferenceKey())).thenReturn(mPreference);
        mController.displayPreference(mScreen);
    }

    @Test
    public void verifyConstants() {
        assertThat(mController.getPreferenceKey()).isEqualTo(PREFERENCE_KEY);
        assertThat(mController.getAvailabilityStatus())
                .isEqualTo(BasePreferenceController.AVAILABLE);
        assertThat(mController.getMin()).isEqualTo(Vibrator.VIBRATION_INTENSITY_OFF);
        assertThat(mController.getMax()).isEqualTo(Vibrator.VIBRATION_INTENSITY_HIGH);
    }

    @Test
    public void missingSetting_shouldReturnDefault() {
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.ALARM_VIBRATION_INTENSITY, /* value= */ null);

        mController.updateState(mPreference);

        assertThat(mPreference.getProgress()).isEqualTo(
                mVibrator.getDefaultVibrationIntensity(VibrationAttributes.USAGE_ALARM));
    }

    @Test
    public void updateState_shouldDisplayIntensityInSliderPosition() {
        updateSetting(Settings.System.ALARM_VIBRATION_INTENSITY, Vibrator.VIBRATION_INTENSITY_HIGH);
        mController.updateState(mPreference);
        assertThat(mPreference.getProgress()).isEqualTo(Vibrator.VIBRATION_INTENSITY_HIGH);

        updateSetting(Settings.System.ALARM_VIBRATION_INTENSITY,
                Vibrator.VIBRATION_INTENSITY_MEDIUM);
        mController.updateState(mPreference);
        assertThat(mPreference.getProgress()).isEqualTo(Vibrator.VIBRATION_INTENSITY_MEDIUM);

        updateSetting(Settings.System.ALARM_VIBRATION_INTENSITY, Vibrator.VIBRATION_INTENSITY_LOW);
        mController.updateState(mPreference);
        assertThat(mPreference.getProgress()).isEqualTo(Vibrator.VIBRATION_INTENSITY_LOW);

        updateSetting(Settings.System.ALARM_VIBRATION_INTENSITY, Vibrator.VIBRATION_INTENSITY_OFF);
        mController.updateState(mPreference);
        assertThat(mPreference.getProgress()).isEqualTo(Vibrator.VIBRATION_INTENSITY_OFF);
    }


    @Test
    public void setProgress_updatesIntensitySetting() throws Exception {
        mController.setSliderPosition(Vibrator.VIBRATION_INTENSITY_OFF);
        assertThat(readSetting(Settings.System.ALARM_VIBRATION_INTENSITY))
                .isEqualTo(Vibrator.VIBRATION_INTENSITY_OFF);

        mController.setSliderPosition(Vibrator.VIBRATION_INTENSITY_LOW);
        assertThat(readSetting(Settings.System.ALARM_VIBRATION_INTENSITY))
                .isEqualTo(Vibrator.VIBRATION_INTENSITY_LOW);

        mController.setSliderPosition(Vibrator.VIBRATION_INTENSITY_MEDIUM);
        assertThat(readSetting(Settings.System.ALARM_VIBRATION_INTENSITY))
                .isEqualTo(Vibrator.VIBRATION_INTENSITY_MEDIUM);

        mController.setSliderPosition(Vibrator.VIBRATION_INTENSITY_HIGH);
        assertThat(readSetting(Settings.System.ALARM_VIBRATION_INTENSITY))
                .isEqualTo(Vibrator.VIBRATION_INTENSITY_HIGH);
    }

    private void updateSetting(String key, int value) {
        Settings.System.putInt(mContext.getContentResolver(), key, value);
    }

    private int readSetting(String settingKey) throws Settings.SettingNotFoundException {
        return Settings.System.getInt(mContext.getContentResolver(), settingKey);
    }
}