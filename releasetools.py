# Copyright (C) 2009 The Android Open Source Project
# Copyright (C) 2019 The Mokee Open Source Project
# Copyright (C) 2019 The LineageOS Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import common
import re

def FullOTA_Assertions(info):
  AddTrustZoneAssertion(info, info.input_zip)
  AddVendorAssertion(info, info.input_zip)

def IncrementalOTA_Assertions(info):
  AddTrustZoneAssertion(info, info.target_zip)
  AddVendorAssertion(info, info.target_zip)

def FullOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def IncrementalOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def AddImage(info, basename, dest):
  name = basename
  data = info.input_zip.read("IMAGES/" + basename)
  common.ZipWriteStr(info.output_zip, name, data)
  info.script.AppendExtra('package_extract_file("%s", "%s");' % (name, dest))

def OTA_InstallEnd(info):
  info.script.Print("Patching firmware images...")
  AddImage(info, "dtbo.img", "/dev/block/bootdevice/by-name/dtbo")
  AddImage(info, "vbmeta.img", "/dev/block/bootdevice/by-name/vbmeta")
  return

def AddTrustZoneAssertion(info, input_zip):
  android_info = info.input_zip.read("OTA/android-info.txt")
  m = re.search(r'require\s+version-tz\s*=\s*(\S+)', android_info)
  if m:
    versions = m.group(1).split('|')
    if len(versions) and '*' not in versions:
      cmd = 'assert(raphael.verify_trustzone(' + ','.join(['"%s"' % tz for tz in versions]) + ') == "1" || abort("ERROR: This package requires firmware from an Android 10 based MIUI build. Please upgrade firmware and retry!"););'
      info.script.AppendExtra(cmd)

def AddVendorAssertion(info, input_zip):
  android_info = info.input_zip.read("OTA/android-info.txt")
  variants = []
  for variant in ('in', 'cn', 'eea'):
    variants.append(re.search(r'require\s+version-{}\s*=\s*(\S+)'.format(variant), android_info).group(1).split(','))
  cmd = 'assert(getprop("ro.boot.hwc") == "{0}" && (raphael.verify_vendor("{2}", "{1}") == "1" || abort("ERROR: This package requires vendor from atleast {2}. Please upgrade firmware and retry!");) || true);' 
  for variant in variants:
    info.script.AppendExtra(cmd.format(*variant))
