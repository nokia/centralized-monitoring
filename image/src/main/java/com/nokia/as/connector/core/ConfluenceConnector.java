/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.nokia.as.connector.ConnectorStatus;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.manager.MonitoringManager;
import org.json.JSONObject;

import java.net.http.HttpResponse;

public class ConfluenceConnector extends AbstractConnector {

    public ConfluenceConnector(HttpConnector httpClient,
                               String id,
                               String serverAddress,
                               Boolean ssl,
                               Integer timeoutMs,
                               Integer nbSteps,
                               JSONObject config,
                               MonitoringManager monitoringManager) {
        super(httpClient, id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
    }

    public ConnectorType getType() {
        return ConnectorType.CONFLUENCE;
    }

    public ConnectorStatus updateStatus(HttpResponse response) {
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
        return status;
    }

    public ConnectorStatus getStatus() {
        return status;
    }

}
