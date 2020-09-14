/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.manager;

import com.nokia.as.connector.ConnectionSettings;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.util.FileUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MonitoringManagerTest {

    @DisplayName("Test MonitoringManager.build")
    @Test
    void testBuild() {
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
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.JENKINS));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.GERRIT));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.GITLAB));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.CONFLUENCE));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.JIRA));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.ARTIFACTORY));
        assertNotNull(monitoringManager.getConnectorList(ConnectorType.OPENSTACK));
    }

}
