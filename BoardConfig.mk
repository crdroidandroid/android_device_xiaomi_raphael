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

# Partitions (reserved size)
BOARD_SYSTEMIMAGE_PARTITION_RESERVED_SIZE := 576716800
BOARD_SYSTEM_EXTIMAGE_PARTITION_RESERVED_SIZE := 498073600

# Sepolicy
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/private
SYSTEM_EXT_PUBLIC_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/public
BOARD_VENDOR_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/vendor

# Inherit from the proprietary version
include vendor/xiaomi/raphael/BoardConfigVendor.mk
