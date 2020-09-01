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

    private String jenkinsLogin;
    private String jenkinsToken;

    private String jiraLogin;
    private String jiraToken;

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

        JSONObject jsonJenkins = jsonObj.getJSONObject("jenkins");
        String jenkinsLogin = jsonJenkins.getString("login");
        String jenkinsToken = jsonJenkins.getString("token");

        JSONObject jsonJira = jsonObj.getJSONObject("jira");
        String jiraLogin = jsonJira.getString("login");
        String jiraToken = jsonJira.getString("token");

        return new ConnectionSettings(
                isProxyEnabled,
                proxyIp,
                proxyPort,
                jenkinsLogin,
                jenkinsToken,
                jiraLogin,
                jiraToken);
    }

    private ConnectionSettings(boolean isProxyEnabled,
                               String proxyIp,
                               int proxyPort,
                               String jenkinsLogin,
                               String jenkinsToken,
                               String jiraLogin,
                               String jiraToken) {
        this.isProxyEnabled = isProxyEnabled;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.jenkinsLogin = jenkinsLogin;
        this.jenkinsToken = jenkinsToken;
        this.jiraLogin = jiraLogin;
        this.jiraToken = jiraToken;
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

    public String getJenkinsLogin() {
        return jenkinsLogin;
    }

    public String getJenkinsToken() {
        return jenkinsToken;
    }

    public String getJiraLogin() {
        return jiraLogin;
    }

    public String getJiraToken() {
        return jiraToken;
    }
}