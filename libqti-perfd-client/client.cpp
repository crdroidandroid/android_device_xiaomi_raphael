/*
 * Copyright (C) 2021 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <stdint.h>

namespace android {
    extern "C" void perf_get_feedback() {}
    extern "C" void perf_hint() {}
    extern "C" void perf_lock_acq() {}
    extern "C" void perf_lock_cmd() {}
    extern "C" void perf_lock_rel() {}
    extern "C" void perf_lock_use_profile() {}
}
