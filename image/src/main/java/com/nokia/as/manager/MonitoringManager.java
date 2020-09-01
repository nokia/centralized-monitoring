/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nokia.as.connector.*;
import com.nokia.as.connector.core.AbstractConnector;
import com.nokia.as.main.jetty.App;
import com.nokia.as.manager.data.GaugeMetric;
import com.nokia.as.manager.data.Label;
import com.nokia.as.manager.data.MetricRegistry;
import com.nokia.as.util.FileUtil;
import com.nokia.as.util.JSONUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MonitoringManager {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringManager.class);

    @Expose
    private Integer interval;
    private final int DEFAULT_INTERVAL = 60;
    private String configFile;
    private String config;
    private String connectionSettings;
    @Expose
    @SerializedName("connectorGroups")
    private List<ConnectorList> connectorLists;
    private ConnectorList globalConnectorList;

    private ScheduledExecutorService scheduler;
    private Runnable step = this::nextStep;

    private MetricRegistry metricRegistry;

    private HttpConnector httpClient;

    public MonitoringManager(HttpConnector httpClient) {
        this(httpClient, App.CONFIG_FILE, App.CONNECTION_CONFIG_FILE);
    }

    public MonitoringManager(HttpConnector httpClient, String configFile, String connectionConfigFile) {
        this.configFile = configFile;
        this.httpClient = httpClient;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.metricRegistry = new MetricRegistry();
        this.connectorLists = new ArrayList<>();
        this.globalConnectorList = new ConnectorList();
        this.interval = DEFAULT_INTERVAL;
        this.config = "";
        this.connectionSettings = FileUtil.readFile(connectionConfigFile);
    }

    public void init() {
        if (build(configFile)) {
            this.metricRegistry.clear();
            registerConnectorGauges();
        }
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public ConnectorList getGlobalConnectorList() {
        return globalConnectorList;
    }

    private void registerConnectorGauges() {
        for (AbstractConnector connector : globalConnectorList) {
            GaugeMetric gaugeMetric = new GaugeMetric(
                    connector.getId(),
                    new ArrayList<>(Collections.singletonList(
                            new Label("component_type", connector.getType().getValue())
                    )));

            metricRegistry.register(gaugeMetric);
            connector.setGaugeMetric(gaugeMetric);
        }
    }

    public void updateGauge(AbstractConnector connector) {
        connector.getGaugeMetric().set(connector.getStatus().getGaugeValue());
    }

    public ConnectorList getConnectorList(ConnectorType type) {
        for (ConnectorList connectorList : this.connectorLists) {
            if (type.equals(connectorList.getType())) {
                return connectorList;
            }
        }
        return null;
    }

    private boolean build(String configFile) {
        if (!FileUtil.fileExists(configFile)) {
            logger.warn("Warning: no config file");
            return false;
        }

        config = FileUtil.readFile(configFile);
        JSONObject jsonConfig = new JSONObject(config);

        this.connectorLists = new ArrayList<>();
        this.globalConnectorList = new ConnectorList();

        if (!jsonConfig.has("interval") || jsonConfig.getInt("interval") < DEFAULT_INTERVAL) {
            this.interval = DEFAULT_INTERVAL;
        } else {
            this.interval = jsonConfig.getInt("interval");
        }

        JSONArray connectorGroups = jsonConfig.getJSONArray("connectorGroups");
        for (int i = 0; i < connectorGroups.length(); i++) {
            JSONObject connectorGroup = connectorGroups.getJSONObject(i);
            String connectorType = connectorGroup.getString("type");
            JSONArray connectors = connectorGroup.getJSONArray("connectors");
            for (int j = 0; j < connectors.length(); j++) {
                JSONObject connectorConfig = connectors.getJSONObject(j);
                addConnector(this.httpClient,
                        ConnectorType.fromString(connectorType),
                        connectorConfig.getString("id"),
                        connectorConfig.getString("address"),
                        connectorConfig.getBoolean("ssl"),
                        connectorConfig.getInt("timeoutMs"),
                        connectorConfig.getInt("nbSteps"),
                        connectorConfig,
                        new JSONObject(connectionSettings),
                        this);
            }
        }
        return true;
    }

    private void addConnector(HttpConnector httpClient,
                              ConnectorType connectorType,
                              String id,
                              String serverAddress,
                              Boolean ssl,
                              Integer timeoutMs,
                              Integer nbSteps,
                              JSONObject config,
                              JSONObject connectionSettings,
                              MonitoringManager monitoringManager) {
        try {
            AbstractConnector connector = AbstractConnector.build(
                    httpClient,
                    connectorType,
                    id,
                    serverAddress,
                    ssl,
                    timeoutMs,
                    nbSteps,
                    config,
                    connectionSettings,
                    monitoringManager);

            ConnectorList connectorList = this.getConnectorList(connectorType);
            if (connectorList == null) {
                connectorList = new ConnectorList(connectorType);
                connectorLists.add(connectorList);
            }

            connectorList.add(connector);
            globalConnectorList.add(connector);

        } catch (ClassNotFoundException e) {
            logger.error("Error: Connector " + connectorType + " not found");
        }
    }

    private void updateStatus() {
        httpClient.checkServices(this);
    }

    private void nextStep() {
        updateStatus();
    }

    public void run() {
        scheduler.scheduleAtFixedRate(step, 0, this.interval, TimeUnit.SECONDS);
    }

    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public boolean isValidJsonConfig(JSONObject jsonConfig) {
        List<String> connectorTypesConfig = new ArrayList<>();
        for (int i = 0; i < jsonConfig.getJSONArray("connectorGroups").length(); i++) {
            JSONObject connectorGroup = jsonConfig.getJSONArray("connectorGroups").getJSONObject(i);

            if (!connectorGroup.has("type") || !connectorGroup.has("connectors")) {
                logger.error("Config file error: type or connectors is missing");
                return false;
            }

            List<String> connectorTypes = Stream.of(ConnectorType.values()).map(ConnectorType::toString)
                    .collect(Collectors.toList());

            String type = connectorGroup.getString("type");
            if (!connectorTypes.contains(type)) {
                logger.error("Config file error: Connector type " + type +
                        " unknown: it must belong to the following types: " +
                        String.join(",", connectorTypes));
                return false;
            } else if (connectorTypesConfig.contains(type)) {
                logger.error("Config file error: the type " + type + " can't be set more than one time");
                return false;
            }
            connectorTypesConfig.add(type);


            Map<String, Class> keys = new HashMap<>() {{
                put("id", String.class);
                put("address", String.class);
                put("ssl", Boolean.class);
                put("timeoutMs", Integer.class);
                put("nbSteps", Integer.class);
            }};

            for (int j = 0; j < connectorGroup.getJSONArray("connectors").length(); j++) {
                JSONObject connector = connectorGroup.getJSONArray("connectors").getJSONObject(j);
                if (!JSONUtil.checkKeys(keys, connector)) {
                    return false;
                }
            }

        }
        return true;
    }

    public String setConfig(InputStream data) {
        String newConfig = "";
        try {
            newConfig = FileUtil.readInputStream(data);

            if (!JSONUtil.isValidJSON(newConfig)) {
                logger.error("Config file error: JSON not valid");
                return "{\"error\": \"JSON not valid\"}";
            } else {
                JSONObject jsonConfig = new JSONObject(newConfig);
                if (!isValidJsonConfig(jsonConfig)) {
                    logger.error("Config file error: validation failed");
                    return "{\"error\": \"configuration file not valid\"}";
                }
            }

            FileUtil.write(App.CONFIG_FILE, newConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newConfig;
    }

    public String getConfig() {
        return config;
    }
}
