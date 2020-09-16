/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jira;

import com.nokia.as.manager.data.GaugeMetric;

import java.util.ArrayList;
import java.util.List;

public class JiraFilter {

    private String key;
    private String jql;
    private String fields;
    private IssueList issueList;
    private Integer nbIssues;
    private GaugeMetric nbIssuesGaugeMetric;
    private Integer nbTodayIssues;
    private GaugeMetric nbTodayIssuesGaugeMetric;
    private List<User> assignees;
    private Integer retryCount;

    public JiraFilter(String key, String jql, String fields) {
        this.key = key;
        this.jql = jql;
        this.fields = fields;
        this.nbIssues = 0;
        this.nbTodayIssues = 0;
        this.assignees = new ArrayList<>();
        this.retryCount = 3;
    }

    public String getKey() {
        return key;
    }

    public String getJql() {
        return jql;
    }

    public String getFields() {
        return fields;
    }

    public IssueList getIssueList() {
        return issueList;
    }

    public void setIssueList(IssueList issueList) {
        this.issueList = issueList;
    }

    public Integer getNbIssues() {
        return nbIssues;
    }

    public void setNbIssues(Integer nbIssues) {
        this.nbIssues = nbIssues;
    }

    public GaugeMetric getNbIssuesGaugeMetric() {
        return nbIssuesGaugeMetric;
    }

    public void setNbIssuesGaugeMetric(GaugeMetric nbIssuesGaugeMetric) {
        this.nbIssuesGaugeMetric = nbIssuesGaugeMetric;
    }

    public Integer getNbTodayIssues() {
        return nbTodayIssues;
    }

    public void setNbTodayIssues(Integer nbTodayIssues) {
        this.nbTodayIssues = nbTodayIssues;
    }

    public GaugeMetric getNbTodayIssuesGaugeMetric() {
        return nbTodayIssuesGaugeMetric;
    }

    public void setNbTodayIssuesGaugeMetric(GaugeMetric nbTodayIssuesGaugeMetric) {
        this.nbTodayIssuesGaugeMetric = nbTodayIssuesGaugeMetric;
    }

    public List<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<User> assignees) {
        this.assignees = assignees;
    }

    public void incrementNbTodayIssues() {
        this.nbTodayIssues++;
    }

    public void resetNbTodayIssues() {
        this.nbTodayIssues = 0;
    }

    public User getAssignee(String username) {
        return assignees.stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElse(null);
    }

    public Issue getIssue(String issueId) {
        if (issueList == null) {
            return null;
        }
        return issueList.stream()
                .filter(issue -> issueId.equals(issue.getId()))
                .findFirst()
                .orElse(null);
    }

    public Integer getRetryCount() {
        return retryCount;
    }
}
