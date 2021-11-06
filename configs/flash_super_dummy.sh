#!/system/bin/sh
#
# Copyright (C) 2021 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

SUPER="/dev/block/by-name/system"

mkdir /tmp/super-mnt

mount $SUPER /tmp/super-mnt 2>/dev/null

if [ "$?" = "0" ]; then
    echo "Detected stock /system in super partition, flashing super_dummy.img!"
    umount /tmp/super-mnt
    dd if=/tmp/super_dummy.img of=$SUPER
fi

rmdir /tmp/super-mnt
