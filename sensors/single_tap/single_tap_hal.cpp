/*
 * Copyright (C) 2022 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "sensors.raphael_single_tap"

#include <errno.h>
#include <fcntl.h>
#include <hardware/sensors.h>
#include <log/log.h>
#include <poll.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <utils/SystemClock.h>

static const char *single_tap_pressed_path = "/sys/devices/platform/goodix_ts.0/single_tap_pressed";
static const char *single_tap_enabled_path = "/sys/devices/platform/goodix_ts.0/single_tap_enabled";

static struct sensor_t single_tap_sensor = {
        .name = "st2w Sensor",
        .vendor = "The LineageOS Project",
        .version = 1,
        .handle = 0,
        .type = SENSOR_TYPE_DEVICE_PRIVATE_BASE + 1,
        .maxRange = 2048.0f,
        .resolution = 1.0f,
        .power = 0,
        .minDelay = -1,
        .fifoReservedEventCount = 0,
        .fifoMaxEventCount = 0,
        .stringType = "org.lineage.sensor.single_tap",
        .requiredPermission = "",
        .maxDelay = 0,
        .flags = SENSOR_FLAG_ONE_SHOT_MODE | SENSOR_FLAG_WAKE_UP,
        .reserved = {},
};

struct single_tap_context_t {
    sensors_poll_device_1_t device;
    int fd, fd_enable;
};

static int single_tap_read_line(int fd, char* buf, size_t len) {
    int rc;

    rc = lseek(fd, 0, SEEK_SET);
    if (rc < 0) {
        ALOGE("Failed to seek: %d", -errno);
        return rc;
    }

    rc = read(fd, buf, len);
    if (rc < 0) {
        ALOGE("Failed to read: %d", -errno);
        return rc;
    }

    return rc;
}

static int single_tap_read_state(int fd) {
    int rc, state = 0;
    char buf[64];

    rc = single_tap_read_line(fd, buf, sizeof(buf));
    if (rc > 0) {
        rc = sscanf(buf, "%d", &state);
        if (rc != 1) {
            ALOGE("Failed to parse single_tap_pressed: %d", rc);
            state = 0;
        }
    }

    return state;
}

static int single_tap_wait_event(int fd, int timeout) {
    struct pollfd fds = {
            .fd = fd,
            .events = POLLERR | POLLPRI,
            .revents = 0,
    };
    int rc;

    do {
        rc = poll(&fds, 1, timeout);
    } while (rc < 0 && errno == EINTR);

    return rc;
}

static void single_tap_flush_events(int fd) {
    char buf[64];

    while (single_tap_wait_event(fd, 0) > 0) {
        single_tap_read_line(fd, buf, sizeof(buf));
    }
}

static int single_tap_close(struct hw_device_t* dev) {
    single_tap_context_t* ctx = reinterpret_cast<single_tap_context_t*>(dev);

    if (ctx) {
        close(ctx->fd);
        close(ctx->fd_enable);
        delete ctx;
    }

    return 0;
}

static int single_tap_activate(struct sensors_poll_device_t* dev, int handle, int enabled) {
    single_tap_context_t* ctx = reinterpret_cast<single_tap_context_t*>(dev);

    if (!ctx || handle) {
        return -EINVAL;
    }

    write(ctx->fd_enable, enabled ? "1" : "0", 1);

    // Flush any pending events
    if (enabled) single_tap_flush_events(ctx->fd);

    return 0;
}

static int single_tap_setDelay(struct sensors_poll_device_t* dev, int handle, int64_t /* ns */) {
    single_tap_context_t* ctx = reinterpret_cast<single_tap_context_t*>(dev);

    if (!ctx || handle) {
        return -EINVAL;
    }

    return 0;
}

static int single_tap_poll(struct sensors_poll_device_t* dev, sensors_event_t* data, int /* count */) {
    single_tap_context_t* ctx = reinterpret_cast<single_tap_context_t*>(dev);

    if (!ctx) {
        return -EINVAL;
    }

    int single_tap_state = 0;

    do {
        int rc = single_tap_wait_event(ctx->fd, -1);
        if (rc < 0) {
            ALOGE("Failed to poll single_tap_pressed: %d", -errno);
            return -errno;
        } else if (rc > 0) {
            single_tap_state = single_tap_read_state(ctx->fd);
        }
    } while (!single_tap_state);

    memset(data, 0, sizeof(sensors_event_t));
    data->version = sizeof(sensors_event_t);
    data->sensor = single_tap_sensor.handle;
    data->type = single_tap_sensor.type;
    data->timestamp = ::android::elapsedRealtimeNano();

    return 1;
}

static int single_tap_batch(struct sensors_poll_device_1* /* dev */, int /* handle */, int /* flags */,
                       int64_t /* period_ns */, int64_t /* max_ns */) {
    return 0;
}

static int single_tap_flush(struct sensors_poll_device_1* /* dev */, int /* handle */) {
    return -EINVAL;
}

static int open_sensors(const struct hw_module_t* module, const char* /* name */,
                        struct hw_device_t** device) {
    single_tap_context_t* ctx = new single_tap_context_t();

    memset(ctx, 0, sizeof(single_tap_context_t));
    ctx->device.common.tag = HARDWARE_DEVICE_TAG;
    ctx->device.common.version = SENSORS_DEVICE_API_VERSION_1_3;
    ctx->device.common.module = const_cast<hw_module_t*>(module);
    ctx->device.common.close = single_tap_close;
    ctx->device.activate = single_tap_activate;
    ctx->device.setDelay = single_tap_setDelay;
    ctx->device.poll = single_tap_poll;
    ctx->device.batch = single_tap_batch;
    ctx->device.flush = single_tap_flush;

    int retries = 0;

    while (retries < 5) {
        sleep(1);
        retries++;
        ctx->fd = open(single_tap_pressed_path, O_RDONLY);
        if (ctx->fd >= 0) {
            ALOGI("Success open single_tap_pressed state after %d retries", retries);
            break;
        }
    }

    if (ctx->fd < 0) {
        ALOGE("Failed to open single_tap_pressed state after %d retries: %d", retries, -errno);
        delete ctx;

        return -ENODEV;
    }

    ctx->fd_enable = open(single_tap_enabled_path, O_WRONLY);
    if (ctx->fd_enable < 0) {
        ALOGE("Failed to open single_tap_enable: %d", -errno);
        delete ctx;
        return -ENODEV;
    } else {
        ALOGI("Success open single_tap_enable");
    }

    *device = &ctx->device.common;

    return 0;
}

static struct hw_module_methods_t single_tap_module_methods = {
        .open = open_sensors,
};

static int single_tap_get_sensors_list(struct sensors_module_t*, struct sensor_t const** list) {
    *list = &single_tap_sensor;

    return 1;
}

static int single_tap_set_operation_mode(unsigned int mode) {
    return !mode ? 0 : -EINVAL;
}

struct sensors_module_t HAL_MODULE_INFO_SYM = {
        .common = {.tag = HARDWARE_MODULE_TAG,
                   .version_major = 1,
                   .version_minor = 0,
                   .id = SENSORS_HARDWARE_MODULE_ID,
                   .name = "st2w Sensor module",
                   .author = "Ivan Vecera",
                   .methods = &single_tap_module_methods,
                   .dso = NULL,
                   .reserved = {0}},
        .get_sensors_list = single_tap_get_sensors_list,
        .set_operation_mode = single_tap_set_operation_mode,
};
