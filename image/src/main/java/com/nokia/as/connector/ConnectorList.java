/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector;

import com.google.gson.annotations.Expose;
import com.nokia.as.connector.core.AbstractConnector;
import com.nokia.as.main.jetty.App;

import java.util.ArrayList;

public class ConnectorList extends ArrayList<AbstractConnector> {
    @Expose
    private ConnectorType type;

    public ConnectorList(ConnectorType type) {
        this.type = type;
    }

    public ConnectorList() {
        this(null);
    }

    public ConnectorType getType() {
        return type;
    }

    public AbstractConnector getConnector(String id) {
        return App.monitoringManager.getGlobalConnectorList().stream().
                filter(e -> id.equals(e.getId())).
                findFirst().
                orElse(null);
    }
}
