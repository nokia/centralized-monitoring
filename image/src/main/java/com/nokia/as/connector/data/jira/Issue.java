/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jira;

import com.google.gson.annotations.Expose;
import com.nokia.as.manager.data.GaugeMetric;
import com.nokia.as.util.DateUtil;

import java.util.Date;

public class Issue {

    @Expose
    private String id;
    @Expose
    private String key;
    @Expose
    private String reporter;
    @Expose
    private String assignee;
    @Expose
    private IssueStatus status;
    private String createdString;
    @Expose
    private Date created;
    private String updatedString;
    @Expose
    private Date updated;
    @Expose
    private String summary;
    @Expose
    String issueType;
    String severity;
    private GaugeMetric gaugeMetric;

    public Issue(String id,
                 String key,
                 String reporter,
                 String assignee,
                 IssueStatus status,
                 String createdString,
                 String updatedString,
                 String summary,
                 String issueType) {
        this.id = id;
        this.key = key;
        this.reporter = reporter;
        this.assignee = assignee;
        this.status = status;
        this.createdString = createdString;
        this.created = DateUtil.getJiraDate(this.createdString);
        this.updatedString = updatedString;
        this.updated = DateUtil.getJiraDate(this.updatedString);
        this.summary = summary;
        this.issueType = issueType;
    }

    public Issue(String id,
                 String key,
                 String reporter,
                 String assignee,
                 IssueStatus status,
                 String createdString,
                 String updatedString,
                 String summary,
                 String issueType,
                 GaugeMetric gaugeMetric) {
        this(id, key, reporter, assignee, status, createdString, updatedString, summary, issueType);
        this.gaugeMetric = gaugeMetric;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getReporter() {
        return reporter;
    }

    public String getAssignee() {
        return assignee;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public String getCreatedString() {
        return createdString;
    }

    public Date getCreated() {
        return created;
    }

    public String getUpdatedString() {
        return updatedString;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getSummary() {
        return summary;
    }

    public String getIssueType() {
        return issueType;
    }

    public GaugeMetric getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(GaugeMetric gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public String getSummary(boolean condensed) {
        return this.summary.length() > 100 && condensed ? this.summary.substring(0, 99) + "..." : summary;
    }
}
