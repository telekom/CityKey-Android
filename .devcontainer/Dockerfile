# The original DOCKERFILE was taken from
#   https://github.com/dotnet/runtime/blob/2ac0591de5e95f6e98b28b7525b712ed09c73c39/.devcontainer/android/Dockerfile
# 
# SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
# SPDX-License-Identifier: CC0-1.0

# Pinning the image digest ensures reproducibility and security by preventing unintentional updates.
# The digest corresponds to the .NET 8.0-noble version at the time of writing.
ARG VARIANT="8.0-noble@sha256:78e4d6aa58fbc816a2a656768b0bda653780710c0dadab1b2c9e53e1c747aaf3"
FROM mcr.microsoft.com/devcontainers/dotnet:${VARIANT}

# Set up machine requirements to build the repo
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        build-essential \
        clang \
        cmake \
        cpio \
        curl \
        gettext \
        git \
        lldb \
        llvm \
        liblldb-dev \
        libunwind8 \
        libunwind8-dev \
        libicu-dev \
        liblttng-ust-dev \
        libssl-dev \
        libkrb5-dev \
        ninja-build \
        openjdk-17-jdk \
        python3 \
        pulseaudio \
        zlib1g-dev 

SHELL ["/bin/bash", "-c"]

ENV NDK_VER=r26c
ENV SDK_VER=9123335_latest
ENV SDK_API_LEVEL=35
ENV SDK_BUILD_TOOLS=34.0.0
ENV HOST_OS=linux
ENV HOST_OS_SHORT=linux
ENV ANDROID_NDK_ROOT=/android/android-ndk-${NDK_VER}
ENV ANDROID_SDK_ROOT=/android/android-sdk
ENV EMULATOR_NAME_X64=citykey-android-emulator

# Download Android NDK and SDK
RUN curl -sSL --tlsv1.2 https://dl.google.com/android/repository/android-ndk-${NDK_VER}-${HOST_OS}.zip -L --output /tmp/andk.zip && \
    curl -sSL --tlsv1.2 https://dl.google.com/android/repository/commandlinetools-${HOST_OS_SHORT}-${SDK_VER}.zip -L --output /tmp/asdk.zip

# Check hashes of downloads
RUN echo "7faebe2ebd3590518f326c82992603170f07c96e /tmp/andk.zip" | cat | sha1sum -c
RUN echo "0bebf59339eaa534f4217f8aa0972d14dc49e7207be225511073c661ae01da0a /tmp/asdk.zip" | cat | sha256sum -c

# Unpack the NDK and SDK
RUN mkdir -p ${ANDROID_NDK_ROOT} && unzip /tmp/andk.zip -d $(dirname ${ANDROID_NDK_ROOT}) && rm -f /tmp/andk.zip && \
    mkdir -p ${ANDROID_SDK_ROOT} && unzip /tmp/asdk.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && rm -f /tmp/asdk.zip

# Setup the SDK
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses && \
    ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} "platform-tools" "platforms;android-${SDK_API_LEVEL}" "build-tools;${SDK_BUILD_TOOLS}"

# Install an x86_64 emulator
RUN ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_SDK_ROOT} "system-images;android-${SDK_API_LEVEL};default;x86_64"

# Setup kvm group. We need the group ID to be GID owner of /dev/kvm.
# Then add the vscode user to the kvm group.
RUN groupadd -r -g 109 kvm && \
    gpasswd -a vscode kvm