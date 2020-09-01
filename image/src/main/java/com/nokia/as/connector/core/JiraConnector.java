/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.nokia.as.connector.ConnectorStatus;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.connector.data.jira.*;
import com.nokia.as.main.jetty.App;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.manager.data.GaugeMetric;
import com.nokia.as.manager.data.Label;
import com.nokia.as.util.DateUtil;
import com.nokia.as.util.JSONUtil;
import com.nokia.as.util.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraConnector extends AbstractConnector {

    private final int MIN_INTERVAL = 30;

    private List<JiraFilter> filters;

    public JiraConnector(HttpConnector httpClient,
                         String id,
                         String serverAddress,
                         Boolean ssl,
                         Integer timeoutMs,
                         Integer nbSteps,
                         JSONObject config,
                         JSONObject connectionSettings,
                         MonitoringManager monitoringManager) {
        super(httpClient, id, serverAddress, ssl, timeoutMs, nbSteps, config, connectionSettings, monitoringManager);
        this.nbSteps = nbSteps < MIN_INTERVAL ? MIN_INTERVAL : nbSteps;
        JSONObject jsonJira = connectionSettings.getJSONObject("jira");
        this.login = jsonJira.getString("login");
        this.pwd = jsonJira.getString("token");
        this.request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Authorization", HttpConnector.basicAuth(this.login, this.pwd))
                .build();

        this.filters = config.has("filters") ?
                buildFilters(config.getJSONArray("filters")) : new ArrayList<>();
    }

    public List<JiraFilter> buildFilters(JSONArray filtersJson) {
        List<JiraFilter> filters = new ArrayList<>();
        for (int i = 0; i < filtersJson.length(); i++) {
            JSONObject directoryJson = filtersJson.getJSONObject(i);
            JiraFilter filter = new JiraFilter(
                    directoryJson.getString("key"),
                    directoryJson.getString("jql"),
                    directoryJson.getString("fields"));
            filters.add(filter);
        }
        return filters;
    }

    public ConnectorType getType() {
        return ConnectorType.JIRA;
    }

    public JiraFilter getFilter(String key) {
        return filters.stream()
                .filter(user -> key.equals(user.getKey()))
                .findFirst()
                .orElse(null);
    }

    public ConnectorStatus updateStatus(HttpResponse<String> response) {
        if (!checkStep()) {
            return status;
        }
        if (checkResponse(response)) {
            status = ConnectorStatus.READY;
        } else {
            status = ConnectorStatus.ERROR;
        }
        if (response != null) {
            httpStatusCode = response.statusCode();
        }

        postCheck();

        return status;
    }

    private void postCheck() {
        for (JiraFilter filter : filters) {
            App.executorService.submit(() -> createAndRegisterIssues(filter));
        }
    }

    private void createAndRegisterIssues(JiraFilter filter) {
        createIssueList(filter);
        updateNbIssues(filter);
        registerIssues(filter);
        registerNbIssues(filter);
        registerAssigneesIssues(filter);
    }

    public void registerIssues(JiraFilter filter) {
        for (Issue issue : filter.getIssueList()) {
            if (issue.getGaugeMetric() == null) {
                GaugeMetric gaugeMetric = new GaugeMetric(
                        "jira_issue_" + issue.getKey() + id + "_" + filter.getKey(),
                        new ArrayList<>(Arrays.asList(
                                new Label("component_type", "issue"),
                                new Label("source", id),
                                new Label("filter", filter.getKey()),
                                new Label("metric", "status"),
                                new Label("id", issue.getId()),
                                new Label("a_key", issue.getKey()),
                                new Label("b_summary", issue.getSummary(true), true),
                                new Label("c_assignee", issue.getAssignee(), true),
                                new Label("d_issuetype", issue.getIssueType(), true),
                                new Label("e_created",
                                        new SimpleDateFormat("yyyy/MM/dd").format(issue.getCreated())),
                                new Label("all", "all")
                        )));

                monitoringManager.getMetricRegistry().register(gaugeMetric);
                issue.setGaugeMetric(gaugeMetric);
            } else {
                issue.getGaugeMetric().checkLabel("a_summary", issue.getSummary(true));
                issue.getGaugeMetric().checkLabel("c_assignee", issue.getAssignee());
                issue.getGaugeMetric().checkLabel("d_issuetype", issue.getIssueType());
            }
            issue.getGaugeMetric().set(issue.getStatus().getGaugeValue());
        }
    }

    public void registerNbIssues(JiraFilter filter) {
        if (filter.getNbIssuesGaugeMetric() == null) {
            GaugeMetric gaugeMetric = new GaugeMetric(
                    "jira_nb_issues_" + id + "_" + filter.getKey(),
                    new ArrayList<>(Arrays.asList(
                            new Label("component_type", "jira_nb_issues"),
                            new Label("source", id),
                            new Label("filter", filter.getKey()),
                            new Label("assignee", "all")
                    )));

            filter.setNbIssuesGaugeMetric(gaugeMetric);
            monitoringManager.getMetricRegistry().register(filter.getNbIssuesGaugeMetric());
        }

        if (filter.getNbTodayIssuesGaugeMetric() == null) {
            GaugeMetric gaugeMetric = new GaugeMetric(
                    "jira_nb_today_issues_" + id + "_" + filter.getKey(),
                    new ArrayList<>(Arrays.asList(
                            new Label("component_type", "jira_nb_today_issues"),
                            new Label("source", id),
                            new Label("filter", filter.getKey()),
                            new Label("assignee", "all")
                    )));

            filter.setNbTodayIssuesGaugeMetric(gaugeMetric);
            monitoringManager.getMetricRegistry().register(filter.getNbTodayIssuesGaugeMetric());
        }

        filter.getNbIssuesGaugeMetric().set(filter.getNbIssues());

        filter.getNbTodayIssuesGaugeMetric().set(filter.getNbTodayIssues());
    }

    public void registerAssigneesIssues(JiraFilter filter) {
        for (User assignee : filter.getAssignees()) {
            if (assignee.getNbIssuesGaugeMetric() == null) {
                GaugeMetric gaugeMetric = new GaugeMetric(
                        "assignee_nb_issues_" + id + "_" + assignee.getCsl(),
                        new ArrayList<>(Arrays.asList(
                                new Label("component_type", "jira_nb_issues"),
                                new Label("source", id),
                                new Label("filter", filter.getKey()),
                                new Label("assignee", assignee.getCsl())
                        )));

                monitoringManager.getMetricRegistry().register(gaugeMetric);
                assignee.setNbIssuesGauge(gaugeMetric);
            }

            if (assignee.getNbTodayIssuesGaugeMetric() == null) {

                GaugeMetric gaugeMetric = new GaugeMetric(
                        "jira_nb_today_issues_" + id + "_" + assignee.getCsl(),
                        new ArrayList<>(Arrays.asList(
                                new Label("component_type", "jira_nb_today_issues"),
                                new Label("source", id),
                                new Label("filter", filter.getKey()),
                                new Label("assignee", assignee.getCsl())
                        )));

                monitoringManager.getMetricRegistry().register(gaugeMetric);
                assignee.setNbTodayIssuesGaugeMetric(gaugeMetric);
            }

            assignee.getNbIssuesGaugeMetric().set(assignee.getNbIssues());

            assignee.getNbTodayIssuesGaugeMetric().set(assignee.getNbTodayIssues());
        }
    }

    public IssueList createIssueList(JiraFilter filter) {
        IssueList issueList = new IssueList(filter.getJql());

        try {
            String jqlFilter = URLEncoder.encodeParam(filter.getJql());
            String filterService = "/rest/api/2/search?jql=" + jqlFilter + "&fields=" + filter.getFields();
            String issuesJsonObj = "";
            int nbTries = 0;
            boolean isFilterSuccess = false;
            while (!isFilterSuccess && nbTries < filter.getRetryCount()) {
                issuesJsonObj = httpClient.getAsync(
                        new URI(protocol + "://" + serverAddress + filterService),
                        timeoutMs / 1000,
                        login,
                        pwd)
                        .body();

                isFilterSuccess = JSONUtil.isValidJSON(issuesJsonObj) &&
                        !(new JSONObject(issuesJsonObj).has("errorMessages"));
                nbTries++;
            }

            if (!isFilterSuccess) {
                return null;
            }

            JSONArray issuesJson = new JSONObject(issuesJsonObj).getJSONArray("issues");

            for (int i = 0; i < issuesJson.length(); i++) {
                JSONObject issueJson = issuesJson.getJSONObject(i);
                String id = issueJson.getString("id");
                String key = issueJson.getString("key");
                JSONObject fieldsJson = issueJson.getJSONObject("fields");
                String summary = fieldsJson.getString("summary");
                String createdString = fieldsJson.getString("created");
                String updatedString = fieldsJson.getString("updated");
                String reporter = fieldsJson.getJSONObject("reporter").getString("key");
                String assignee = fieldsJson.has("assignee") && !fieldsJson.isNull("assignee") ?
                        fieldsJson.getJSONObject("assignee").getString("key") : "";
                IssueStatus status = IssueStatus.fromId(
                        Integer.parseInt(fieldsJson.getJSONObject("status").getString("id")));
                String issueType = fieldsJson.has("issuetype") ?
                        fieldsJson.getJSONObject("issuetype").getString("name") : "";

                Issue existingIssue = filter.getIssue(id);
                if (existingIssue == null) {
                    Issue issue = new Issue(id, key, reporter, assignee, status, createdString,
                            updatedString, summary, issueType);
                    issueList.add(issue);
                } else {
                    Issue issue = new Issue(id, key, reporter, assignee, status, createdString,
                            updatedString, summary, issueType, existingIssue.getGaugeMetric());
                    issueList.add(issue);
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        filter.setIssueList(issueList);

        return issueList;
    }

    public void updateNbIssues(JiraFilter filter) {
        filter.setNbIssues(filter.getIssueList().size());
        Date startOfToday = DateUtil.getStartOfDay();
        filter.resetNbTodayIssues();
        for (User assignee : filter.getAssignees()) {
            assignee.resetNbIssues();
            assignee.resetNbTodayIssues();
        }
        for (Issue issue : filter.getIssueList()) {
            User assignee = filter.getAssignee(issue.getAssignee());
            if (assignee == null) {
                assignee = new User(issue.getAssignee());
                filter.getAssignees().add(assignee);
            }
            assignee.incrementNbIssues();
            if (issue.getCreated().after(startOfToday) || issue.getCreated().equals(startOfToday)) {
                filter.incrementNbTodayIssues();
                assignee.incrementNbTodayIssues();
            }
        }
    }

    public List<JiraFilter> getFilters() {
        return filters;
    }

    public ConnectorStatus getStatus() {
        return status;
    }

}
