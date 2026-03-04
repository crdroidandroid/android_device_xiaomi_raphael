#
# Copyright (C) 2021-2022 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# ART / Dexpreopt
PRODUCT_USES_DEFAULT_ART_CONFIG := true

# Enable dexpreopt
WITH_DEXPREOPT := true
WITH_DEXPREOPT_DEBUG_INFO := false
DEX_PREOPT_DEFAULT := speed-profile
PRODUCT_SYSTEM_SERVER_COMPILER_FILTER := speed-profile

TARGET_HAS_UDFPS := true
TARGET_IS_LEGACY := true
TARGET_HAS_FM := true

# Inherit from sm8150-common
$(call inherit-product, device/xiaomi/sm8150-common/msmnile.mk)

# AAPT
PRODUCT_AAPT_CONFIG := normal
PRODUCT_AAPT_PREF_CONFIG := xxhdpi

# Audio configs
PRODUCT_COPY_FILES += \
    $(call find-copy-subdir-files,*,$(LOCAL_PATH)/audio/,$(TARGET_COPY_OUT_VENDOR)/etc)

# Boot animation
TARGET_SCREEN_HEIGHT := 2340
TARGET_SCREEN_WIDTH := 1080

# Camera
$(call inherit-product-if-exists, device/xiaomi/miuicamera/device.mk)

PRODUCT_PACKAGES += \
    libMegviiFacepp-0.5.2 \
    libmegface \
    libpiex_shim

# Camera motor
PRODUCT_PACKAGES += \
    vendor.xiaomi.hardware.motor@1.0-service.xml

PRODUCT_PACKAGES += \
    vendor.xiaomi.hardware.motor@1.0.vendor

# Init
$(call soong_config_set,xiaomi_msmnile,variant_lib,//$(LOCAL_PATH):libvariant_xiaomi_raphael)

# Overlays
PRODUCT_PACKAGES += \
    ApertureOverlayDevice \
    FrameworkResOverlayDevice \
    LineageSDKOverlayDevice \
    LineageSystemUIOverlayDevice \
    SettingsOverlayDevice \
    SystemUIOverlayDevice

# Power
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/etc/powerhint.json:$(TARGET_COPY_OUT_VENDOR)/etc/powerhint.json

# Sensors
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/sensors/hals.conf:$(TARGET_COPY_OUT_VENDOR)/etc/sensors/hals.conf

# Shipping API level
PRODUCT_SHIPPING_API_LEVEL := 28

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Inherit from vendor blobs
$(call inherit-product, vendor/xiaomi/raphael/raphael-vendor.mk)
