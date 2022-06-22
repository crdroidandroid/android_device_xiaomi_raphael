#
# Copyright (C) 2021-2022 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from sm8150-common
include device/xiaomi/sm8150-common/BoardConfigCommon.mk

DEVICE_PATH := device/xiaomi/raphael

BUILD_BROKEN_DUP_RULES := true

# Assert
TARGET_OTA_ASSERT_DEVICE := raphael,raphaelin

# Display
TARGET_SCREEN_DENSITY := 440

# Init
TARGET_INIT_VENDOR_LIB := //$(DEVICE_PATH):init_xiaomi_raphael
TARGET_RECOVERY_DEVICE_MODULES := init_xiaomi_raphael

# Kernel
TARGET_KERNEL_CONFIG += vendor/xiaomi/raphael.config

# Inherit from the proprietary version
include vendor/xiaomi/raphael/BoardConfigVendor.mk
