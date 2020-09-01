/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.nokia.as.connector.ConnectionSettings;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.connector.data.jira.IssueList;
import com.nokia.as.connector.data.jira.JiraFilter;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class JiraConnectorTest {

    @Test
    void createIssueList() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ConnectionSettings connectionSettings = ConnectionSettings.build(
                FileUtil.readFile("src/test/resources/connectionSettings.json"));
        HttpConnector httpClient = connectionSettings.isProxyEnabled() ?
                new HttpConnector(executorService,
                        connectionSettings.getProxyIp(),
                        connectionSettings.getProxyPort()) :
                new HttpConnector(executorService);
        MonitoringManager monitoringManager = new MonitoringManager(
                httpClient,
                "src/test/resources/config/config.json",
                "src/test/resources/connectionSettings.json"
        );
        monitoringManager.init();
        JiraConnector connector = (JiraConnector) monitoringManager.getConnectorList(ConnectorType.JIRA).get(0);

        for (JiraFilter filter : connector.getFilters()) {
            IssueList issueList = connector.createIssueList(filter);
            assertTrue(issueList.size() > 0);
        }
    }

    @Test
    void registerNbIssues() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ConnectionSettings connectionSettings = ConnectionSettings.build(
                FileUtil.readFile("src/test/resources/connectionSettings.json"));
        HttpConnector httpClient = connectionSettings.isProxyEnabled() ?
                new HttpConnector(executorService,
                        connectionSettings.getProxyIp(),
                        connectionSettings.getProxyPort()) :
                new HttpConnector(executorService);
        MonitoringManager monitoringManager = new MonitoringManager(
                httpClient,
                "src/test/resources/config/config.json",
                "src/test/resources/connectionSettings.json"
        );
        monitoringManager.init();
        JiraConnector connector = (JiraConnector) monitoringManager.getConnectorList(ConnectorType.JIRA).get(0);

        for (JiraFilter filter : connector.getFilters()) {
            connector.createIssueList(filter);
            connector.updateNbIssues(filter);
            connector.registerNbIssues(filter);
        }
    }

    @Test
    void registerAssigneesIssues() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ConnectionSettings connectionSettings = ConnectionSettings.build(
                FileUtil.readFile("src/test/resources/connectionSettings.json"));
        HttpConnector httpClient = connectionSettings.isProxyEnabled() ?
                new HttpConnector(executorService,
                        connectionSettings.getProxyIp(),
                        connectionSettings.getProxyPort()) :
                new HttpConnector(executorService);
        MonitoringManager monitoringManager = new MonitoringManager(
                httpClient,
                "src/test/resources/config/config.json",
                "src/test/resources/connectionSettings.json"
        );
        monitoringManager.init();
        JiraConnector connector = (JiraConnector) monitoringManager.getConnectorList(ConnectorType.JIRA).get(0);

        for (JiraFilter filter : connector.getFilters()) {
            connector.registerAssigneesIssues(filter);
        }
    }

    @Test
    void registerIssues() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        ConnectionSettings connectionSettings = ConnectionSettings.build(
                FileUtil.readFile("src/test/resources/connectionSettings.json"));
        HttpConnector httpClient = connectionSettings.isProxyEnabled() ?
                new HttpConnector(executorService,
                        connectionSettings.getProxyIp(),
                        connectionSettings.getProxyPort()) :
                new HttpConnector(executorService);
        MonitoringManager monitoringManager = new MonitoringManager(
                httpClient,
                "src/test/resources/config/config.json",
                "src/test/resources/connectionSettings.json"
        );
        monitoringManager.init();
        JiraConnector connector = (JiraConnector) monitoringManager.getConnectorList(ConnectorType.JIRA).get(0);

        for (JiraFilter filter : connector.getFilters()) {
            IssueList issueList = connector.createIssueList(filter);
            connector.registerIssues(filter);
        }

    }
}