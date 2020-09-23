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

#include <android-base/logging.h>
#include <android-base/properties.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>
#include <sys/sysinfo.h>

#include "vendor_init.h"

using ::android::base::SetProperty;

void property_override(char const prop[], char const value[]) {

    prop_info *pi;

    pi = (prop_info*) __system_property_find(prop);
    if (pi)
        __system_property_update(pi, value, strlen(value));
    else
        __system_property_add(prop, strlen(prop), value, strlen(value));
}

void load_dalvik_properties() {
    struct sysinfo sys;

    sysinfo(&sys);
    if (sys.totalram < 6144ull * 1024 * 1024) {
        // from - phone-xhdpi-6144-dalvik-heap.mk
        property_override("dalvik.vm.heapstartsize", "16m");
        property_override("dalvik.vm.heapgrowthlimit", "256m");
        property_override("dalvik.vm.heapsize", "512m");
        property_override("dalvik.vm.heapmaxfree", "32m");
    } else {
        // 8GB & 12GB RAM
        property_override("dalvik.vm.heapstartsize", "32m");
        property_override("dalvik.vm.heapgrowthlimit", "512m");
        property_override("dalvik.vm.heapsize", "768m");
        property_override("dalvik.vm.heapmaxfree", "64m");
    }

    property_override("dalvik.vm.heaptargetutilization", "0.5");
    property_override("dalvik.vm.heapminfree", "8m");
}

void load_raphaelglobal() {
    property_override("ro.product.model", "Mi 9T Pro");
    property_override("ro.build.product", "raphael");
    property_override("ro.product.device", "raphael");
    property_override("ro.build.description", "raphael-user 9 PKQ1.181121.001 V10.3.1.0.PFKEUXM release-keys");
    property_override("ro.build.fingerprint", "google/walleye/walleye:8.1.0/OPM1.171019.011/4448085:user/release-keys");
}

void load_raphaelin() {
    property_override("ro.product.model", "Redmi K20 Pro");
    property_override("ro.build.product", "raphaelin");
    property_override("ro.product.device", "raphaelin");
    property_override("ro.build.description", "raphaelin-user 9 PKQ1.181121.001 V10.3.3.0.PFKINXM release-keys");
    property_override("ro.build.fingerprint", "google/walleye/walleye:8.1.0/OPM1.171019.011/4448085:user/release-keys");
}

void load_raphael() {
    property_override("ro.product.model", "Redmi K20 Pro");
    property_override("ro.build.product", "raphael");
    property_override("ro.product.device", "raphael");
    property_override("ro.build.description", "raphael-user 9 PKQ1.181121.001 V10.3.12.0.PFKCNXM release-keys");
    property_override("ro.build.fingerprint", "google/walleye/walleye:8.1.0/OPM1.171019.011/4448085:user/release-keys");
}


void vendor_load_properties() {
    std::string region = android::base::GetProperty("ro.boot.hwc", "");

    if (region.find("CN") != std::string::npos) {
        load_raphael();
    } else if (region.find("INDIA") != std::string::npos) {
        load_raphaelin();
    } else if (region.find("GLOBAL") != std::string::npos) {
        load_raphaelglobal();
    } else {
        LOG(ERROR) << __func__ << ": unexcepted region!";
    }

    load_dalvik_properties();
}
