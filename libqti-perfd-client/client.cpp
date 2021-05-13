/*
 * Copyright (C) 2021 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "libqti-perfd-client"

#include <stdint.h>
#include <log/log.h>

extern "C" void perf_get_feedback() {}
extern "C" void perf_hint() {}
extern "C" int perf_lock_acq(int handle, int duration, int arg3[], int arg4) {
    ALOGI("perf_lock_acq: handle: %d, duration: %d, arg3[0]: %d, arg4: %d",
            handle, duration, arg3[0], arg4);
    if (handle > 0)
        return handle;

    return 233;
}
extern "C" void perf_lock_cmd() {}
extern "C" int perf_lock_rel(int handle) {
    ALOGI("perf_lock_rel: handle: %d", handle);
    if (handle > 0)
        return handle;

    return 233;
}
extern "C" void perf_lock_use_profile() {}
