#!/usr/bin/env bash

# The original postStartCommand.sh was taken from
#   https://github.com/dotnet/runtime/blob/b135297d08f8ee87c12272e68686e831af34d70b/.devcontainer/scripts/postCreateCommand.sh
#
# SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
# SPDX-License-Identifier: CC0-1.0

set -e

opt=$1
case "$opt" in
    android)
        # Create the Android emulator.
        ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools/bin/avdmanager -s create avd --name ${EMULATOR_NAME_X64} --package "system-images;android-${SDK_API_LEVEL};default;x86_64"
    ;;
esac

# reset the repo to the commit hash that was used to build the prebuilt Codespace
git reset --hard $(cat ./artifacts/prebuild.sha)