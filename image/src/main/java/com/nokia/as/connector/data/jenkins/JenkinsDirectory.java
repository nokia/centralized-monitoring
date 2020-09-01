/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jenkins;

public class JenkinsDirectory {

    private String key;
    private String path;
    private boolean detailed;
    private JobList jobList;
    private Integer retryCount;

    public JenkinsDirectory(String key, String path, boolean detailed) {
        this.key = key;
        this.path = path;
        this.detailed = detailed;
        this.retryCount = 5;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public JobList getJobList() {
        return jobList;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public void setJobList(JobList jobList) {
        this.jobList = jobList;
    }

    public Job getJob(String jobName) {
        if (jobList == null) {
            return null;
        }
        return jobList.stream()
                .filter(job -> jobName.equals(job.getName()))
                .findFirst()
                .orElse(null);
    }

    public Integer getRetryCount() {
        return retryCount;
    }
}
