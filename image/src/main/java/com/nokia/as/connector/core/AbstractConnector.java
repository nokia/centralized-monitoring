/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.google.gson.annotations.Expose;
import com.nokia.as.connector.ConnectorStatus;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.manager.data.GaugeMetric;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class AbstractConnector {
    static final Logger logger = LoggerFactory.getLogger(AbstractConnector.class);

    @Expose
    String id;
    @Expose
    String serverAddress;
    @Expose
    Boolean ssl;
    String protocol;
    URI uri;
    @Expose
    Integer timeoutMs;
    @Expose
    Integer nbSteps;
    Integer currentStep;
    @Expose
    ConnectorStatus status;
    @Expose
    Integer httpStatusCode;
    HttpConnector httpClient;
    HttpRequest request;
    HttpResponse<String> response;
    private GaugeMetric gaugeMetric;
    JSONObject config;
    MonitoringManager monitoringManager;

    public AbstractConnector(HttpConnector httpClient,
                             String id,
                             String serverAddress,
                             Boolean ssl,
                             Integer timeoutMs,
                             Integer nbSteps,
                             JSONObject config,
                             MonitoringManager monitoringManager) {
        this.id = id;
        this.serverAddress = serverAddress;
        this.ssl = ssl;
        this.protocol = ssl ? "https" : "http";
        this.timeoutMs = timeoutMs;
        this.nbSteps = nbSteps;
        this.currentStep = 0;
        this.status = ConnectorStatus.DOWN;
        this.httpClient = httpClient;
        this.config = config;
        this.monitoringManager = monitoringManager;
        try {
            this.uri = new URI(this.protocol + "://" + this.serverAddress);
            this.request = HttpRequest.newBuilder().GET().uri(this.uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public abstract ConnectorType getType();

    public abstract ConnectorStatus getStatus();

    public abstract ConnectorStatus updateStatus(HttpResponse<String> response);

    public String getId() {
        return this.id;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public URI getUri() {
        return this.uri;
    }

    public GaugeMetric getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(GaugeMetric gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public Integer getTimeoutMs() {
        return this.timeoutMs;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void addStep() {
        this.currentStep++;
        if (this.currentStep > this.nbSteps) {
            this.currentStep = 1;
        }
    }

    boolean checkResponse(HttpResponse<String> response) {
        String output = "Check " + this.getType() + " - " + this.id + " - " + this.uri.toString() + " - ";
        if (response != null && response.statusCode() == HttpURLConnection.HTTP_OK) {
            logger.info(output + "OK (" + response.statusCode() + ")");
            return true;
        } else if (response != null) {
            logger.error(output + "NOK (" + response.statusCode() + ")");
        } else {
            logger.error(output + "NOK (No answer)");
        }
        return false;
    }

    boolean checkStep() {
        return this.currentStep == 0 || this.currentStep.equals(this.nbSteps);
    }

    public static AbstractConnector build(HttpConnector httpClient,
                                          ConnectorType type,
                                          String id,
                                          String serverAddress,
                                          Boolean ssl,
                                          Integer timeoutMs,
                                          Integer nbSteps,
                                          JSONObject config,
                                          MonitoringManager monitoringManager)
            throws ClassNotFoundException {
        switch (type) {
            case GITLAB:
                return new GitlabConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case JENKINS:
                return new JenkinsConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case GERRIT:
                return new GerritConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case CONFLUENCE:
                return new ConfluenceConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case JIRA:
                return new JiraConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case ARTIFACTORY:
                return new ArtifactoryConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            case OPENSTACK:
                return new OpenstackConnector(httpClient,
                        id, serverAddress, ssl, timeoutMs, nbSteps, config, monitoringManager);
            default:
                throw new ClassNotFoundException("Collector not found " + type);
        }
    }

    public static AbstractConnector build(HttpConnector httpClient,
                                          ConnectorType type,
                                          String id,
                                          String serverAddress,
                                          Boolean ssl,
                                          JSONObject config,
                                          MonitoringManager collectorRegistry)
            throws ClassNotFoundException {
        return AbstractConnector.build(httpClient, type, id, serverAddress, ssl, 0, 0, config, collectorRegistry);
    }
}
