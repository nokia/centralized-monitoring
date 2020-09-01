/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.nokia.as.connector.ConnectionSettings;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.connector.data.jenkins.JenkinsDirectory;
import com.nokia.as.connector.data.jenkins.JobList;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class JenkinsConnectorTest {

    @Test
    void createJobList() {
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
        JenkinsConnector connector = (JenkinsConnector) monitoringManager.getConnectorList(ConnectorType.JENKINS).get(0);

        for (JenkinsDirectory directory : connector.getDirectories()) {
            JobList jobList = connector.createJobList(directory, directory.isDetailed());
            assertTrue(jobList.size() > 0);
        }
    }

    @Test
    void registerJobList() {
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
        JenkinsConnector connector = (JenkinsConnector) monitoringManager.getConnectorList(ConnectorType.JENKINS)
                .get(0);

        for (JenkinsDirectory directory : connector.getDirectories()) {
            connector.createAndRegisterJobs(directory);
        }
    }

}