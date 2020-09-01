/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jenkins;

import com.google.gson.annotations.Expose;
import com.nokia.as.manager.data.GaugeMetric;
import com.nokia.as.util.DateUtil;

import java.util.Date;

public class Job {

    @Expose
    private String name;
    @Expose
    private BuildStatus status;
    @Expose
    private Long lastSuccess;
    private String lastSuccessDate;
    @Expose
    private Long lastFailure;
    private String lastFailureDate;
    @Expose
    private Long lastDuration;
    private GaugeMetric gaugeMetric;

    public Job(String name, BuildStatus status, Long lastSuccess, Long lastFailure, Long lastDuration) {
        this.name = name;
        this.status = status;
        this.lastSuccess = lastSuccess;
        this.lastFailure = lastFailure;
        this.lastDuration = lastDuration;
        this.lastSuccessDate = this.lastSuccess == null ? "N/A" :
                DateUtil.secondsToString(DateUtil.millisecondsSince(new Date(this.lastSuccess)) / 1000, 2);
        this.lastFailureDate = this.lastFailure == null ? "N/A" :
                DateUtil.secondsToString(DateUtil.millisecondsSince(new Date(this.lastFailure)) / 1000, 2);
    }

    public Job(String name, BuildStatus status, Long lastSuccess, Long lastFailure, Long lastDuration,
               GaugeMetric gaugeMetric) {
        this(name, status, lastSuccess, lastFailure, lastDuration);
        this.gaugeMetric = gaugeMetric;
    }

    public String getName() {
        return name;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public Long getLastSuccess() {
        return lastSuccess;
    }

    public String getLastSuccessDate() {
        return lastSuccessDate;
    }

    public Long getLastFailure() {
        return lastFailure;
    }

    public String getLastFailureDate() {
        return lastFailureDate;
    }

    public Long getLastDuration() {
        return lastDuration;
    }

    public String getLastDurationString() {
        if (lastDuration == null) {
            return "N/A";
        }
        return DateUtil.secondsToString(lastDuration / 1000, 3);
    }

    public GaugeMetric getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(GaugeMetric gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }
}
