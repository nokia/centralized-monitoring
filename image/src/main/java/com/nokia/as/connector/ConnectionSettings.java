/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector;

import org.json.JSONObject;

public class ConnectionSettings {

    private boolean isProxyEnabled;
    private String proxyIp;
    private int proxyPort;

    public static ConnectionSettings build(String jsonConfig) {
        JSONObject jsonObj = new JSONObject(jsonConfig);

        String proxyIp = "";
        int proxyPort = 0;
        boolean isProxyEnabled = false;

        if (jsonObj.has("proxy")) {
            JSONObject jsonProxy = jsonObj.getJSONObject("proxy");
            proxyIp = jsonProxy.getString("ip");
            proxyPort = jsonProxy.getInt("port");
            isProxyEnabled = true;
        }

        return new ConnectionSettings(
                isProxyEnabled,
                proxyIp,
                proxyPort);
    }

    private ConnectionSettings(boolean isProxyEnabled,
                               String proxyIp,
                               int proxyPort) {
        this.isProxyEnabled = isProxyEnabled;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
    }

    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    public String getProxyIp() {
        return proxyIp;
    }

    public int getProxyPort() {
        return proxyPort;
    }
}