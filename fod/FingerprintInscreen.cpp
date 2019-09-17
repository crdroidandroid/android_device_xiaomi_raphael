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

#define LOG_TAG "FingerprintInscreenService"

#include "FingerprintInscreen.h"

#include <android-base/logging.h>
#include <cmath>
#include <fstream>

#define FINGERPRINT_ACQUIRED_VENDOR 6

#define COMMAND_NIT 10
#define PARAM_NIT_630_FOD 1
#define PARAM_NIT_300_FOD 4
#define PARAM_NIT_NONE 0

#define FOD_STATUS_PATH "/sys/devices/virtual/touch/tp_dev/fod_status"
#define FOD_STATUS_ON 1
#define FOD_STATUS_OFF 0

#define FOD_SENSOR_X 445
#define FOD_SENSOR_Y 1931
#define FOD_SENSOR_SIZE 190

#define BRIGHTNESS_PATH "/sys/class/backlight/panel0-backlight/brightness_clone"

namespace {
template <typename T>
static T get(const std::string& path, const T& def) {
    std::ifstream file(path);
    T result;

    file >> result;
    return file.fail() ? def : result;
}

template <typename T>
static void set(const std::string& path, const T& value) {
    std::ofstream file(path);
    file << value;
}

} // anonymous namespace

namespace vendor {
namespace lineage {
namespace biometrics {
namespace fingerprint {
namespace inscreen {
namespace V1_0 {
namespace implementation {

FingerprintInscreen::FingerprintInscreen() {
    xiaomiDisplayFeatureService = IDisplayFeature::getService();
    xiaomiFingerprintService = IXiaomiFingerprint::getService();
}

Return<int32_t> FingerprintInscreen::getPositionX() {
    return FOD_SENSOR_X;
}

Return<int32_t> FingerprintInscreen::getPositionY() {
    return FOD_SENSOR_Y;
}

Return<int32_t> FingerprintInscreen::getSize() {
    return FOD_SENSOR_SIZE;
}

Return<void> FingerprintInscreen::onStartEnroll() {
    return Void();
}

Return<void> FingerprintInscreen::onFinishEnroll() {
    return Void();
}

Return<void> FingerprintInscreen::onPress(int32_t ambientLight) {
    xiaomiDisplayFeatureService->setFeature(0, 11, 1, 4);
    if (get(BRIGHTNESS_PATH, 0) != 0 && ambientLight < 12) {
        xiaomiDisplayFeatureService->setFeature(0, 11, 1, 5);
        xiaomiFingerprintService->extCmd(COMMAND_NIT, PARAM_NIT_300_FOD);
    } else {
        xiaomiDisplayFeatureService->setFeature(0, 11, 1, 3);
        xiaomiFingerprintService->extCmd(COMMAND_NIT, PARAM_NIT_630_FOD);
    }
    return Void();
}

Return<void> FingerprintInscreen::onRelease() {
    xiaomiDisplayFeatureService->setFeature(0, 11, 1, 4);
    if (get(BRIGHTNESS_PATH, 0) != 0) {
        xiaomiDisplayFeatureService->setFeature(0, 11, 0, 5);
        xiaomiDisplayFeatureService->setFeature(0, 11, 0, 3);
    }
    xiaomiFingerprintService->extCmd(COMMAND_NIT, PARAM_NIT_NONE);
    return Void();
}

Return<void> FingerprintInscreen::onShowFODView() {
    set(FOD_STATUS_PATH, FOD_STATUS_ON);
    return Void();
}

Return<void> FingerprintInscreen::onHideFODView() {
    set(FOD_STATUS_PATH, FOD_STATUS_OFF);
    xiaomiDisplayFeatureService->setFeature(0, 11, 1, 4);
    if (get(BRIGHTNESS_PATH, 0) != 0) {
        xiaomiDisplayFeatureService->setFeature(0, 11, 0, 5);
        xiaomiDisplayFeatureService->setFeature(0, 11, 0, 3);
    }
    xiaomiFingerprintService->extCmd(COMMAND_NIT, PARAM_NIT_NONE);
    return Void();
}

Return<bool> FingerprintInscreen::handleAcquired(int32_t acquiredInfo, int32_t vendorCode) {
    LOG(ERROR) << "acquiredInfo: " << acquiredInfo << ", vendorCode: " << vendorCode << "\n";
    return false;
}

Return<bool> FingerprintInscreen::handleError(int32_t error, int32_t vendorCode) {
    LOG(ERROR) << "error: " << error << ", vendorCode: " << vendorCode << "\n";
    return false;
}

Return<void> FingerprintInscreen::setLongPressEnabled(bool) {
    return Void();
}

Return<int32_t> FingerprintInscreen::getDimAmount(int32_t brightness) {
    float alpha;
    (void) brightness;
    int realBrightness = get(BRIGHTNESS_PATH, 0);

    alpha = 1.0 - pow(realBrightness / 1680.0, 0.455);

    if (realBrightness > 500) {
        alpha = 1.0 - pow(realBrightness / 2047.0 * 430.0 / 600.0, 0.455);
    }

    return 255 * alpha;
}

Return<bool> FingerprintInscreen::shouldBoostBrightness() {
    return false;
}

Return<void> FingerprintInscreen::setCallback(const sp<IFingerprintInscreenCallback>& callback) {
    (void) callback;
    return Void();
}

}  // namespace implementation
}  // namespace V1_0
}  // namespace inscreen
}  // namespace fingerprint
}  // namespace biometrics
}  // namespace lineage
}  // namespace vendor
