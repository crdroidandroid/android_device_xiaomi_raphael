/*
 * Copyright (C) 2021-2022 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <libinit_dalvik_heap.h>
#include <libinit_variant.h>

#include "vendor_init.h"

#define FINGERPRINT "Xiaomi/raphael/raphael:11/RKQ1.200826.002/V12.5.2.0.RFKMIXM:user/release-keys"

static const variant_info_t raphael_global_info = {
    .hwc_value = "GLOBAL",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphael",
    .marketname = "",
    .model = "Mi 9T Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = NFC_TYPE_NFC_ESE,
};

static const variant_info_t raphaelin_info = {
    .hwc_value = "INDIA",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphaelin",
    .marketname = "",
    .model = "Redmi K20 Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = NFC_TYPE_NONE,
};

static const variant_info_t raphael_info = {
    .hwc_value = "",
    .sku_value = "",

    .brand = "Xiaomi",
    .device = "raphael",
    .marketname = "",
    .model = "Redmi K20 Pro",
    .build_fingerprint = FINGERPRINT,

    .nfc = NFC_TYPE_NFC_ESE,
};

static const std::vector<variant_info_t> variants = {
    raphael_global_info,
    raphaelin_info,
    raphael_info,
};

void vendor_load_properties() {
    set_dalvik_heap();
    search_variant(variants);
}
