/*
 * Copyright (C) 2021-2025 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <libvariant.h>

#define FINGERPRINT "Xiaomi/raphael/raphael:11/RKQ1.200826.002/V12.5.2.0.RFKMIXM:user/release-keys"

static const variant_info raphael_global_info = {
    .hwc_value = "GLOBAL",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphael",
    .marketname = "",
    .model = "Mi 9T Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = true,
};

static const variant_info raphaelin_info = {
    .hwc_value = "INDIA",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphael",
    .marketname = "",
    .model = "Redmi K20 Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = false,
};

static const variant_info raphael_info = {
    .hwc_value = "",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphael",
    .marketname = "",
    .model = "Redmi K20 Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = true,
};

const std::vector<variant_info> variants = {
    raphael_global_info,
    raphaelin_info,
    raphael_info,
};
