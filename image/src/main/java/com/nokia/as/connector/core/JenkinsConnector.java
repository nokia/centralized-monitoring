/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.core;

import com.nokia.as.connector.ConnectorStatus;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.connector.data.jenkins.BuildStatus;
import com.nokia.as.connector.data.jenkins.JenkinsDirectory;
import com.nokia.as.connector.data.jenkins.Job;
import com.nokia.as.connector.data.jenkins.JobList;
import com.nokia.as.main.jetty.App;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.manager.data.GaugeMetric;
import com.nokia.as.manager.data.Label;
import com.nokia.as.util.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.StreamSupport;

public class JenkinsConnector extends AbstractConnector {

    private final int MIN_INTERVAL = 60;

    private List<JenkinsDirectory> directories;

    private Map<String, String> colorMap = Map.ofEntries(
            Map.entry("blue", "success"),
            Map.entry("red", "failure"),
            Map.entry("yellow", "unstable"),
            Map.entry("blue_anime", "inprogress"),
            Map.entry("red_anime", "inprogress"),
            Map.entry("yellow_anime", "inprogress")
    );

    public JenkinsConnector(HttpConnector httpClient,
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
        JSONObject jsonJenkins = connectionSettings.getJSONObject("jenkins");
        this.login = jsonJenkins.getString("login");
        this.pwd = jsonJenkins.getString("token");
        this.request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Authorization", HttpConnector.basicAuth(this.login, this.pwd))
                .build();

        this.directories = config.has("directories") ?
                buildDirectories(config.getJSONArray("directories")) : new ArrayList<>();
    }

    public List<JenkinsDirectory> buildDirectories(JSONArray directoriesJson) {
        List<JenkinsDirectory> directories = new ArrayList<>();
        for (int i = 0; i < directoriesJson.length(); i++) {
            JSONObject directoryJson = directoriesJson.getJSONObject(i);
            JenkinsDirectory directory = new JenkinsDirectory(
                    directoryJson.getString("key"),
                    directoryJson.getString("path"),
                    directoryJson.getBoolean("detailed"));
            directories.add(directory);
        }
        return directories;
    }

    public ConnectorType getType() {
        return ConnectorType.JENKINS;
    }

    public JenkinsDirectory getDirectory(String key) {
        return directories.stream()
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
        for (JenkinsDirectory directory : directories) {
            App.executorService.submit(() -> createAndRegisterJobs(directory));
        }
    }

    public void createAndRegisterJobs(JenkinsDirectory directory) {
        createJobList(directory, directory.isDetailed());
        registerJobs(directory);
    }

    private void registerJobs(JenkinsDirectory directory) {
        for (Job job : directory.getJobList()) {
            if (job.getGaugeMetric() == null) {
                GaugeMetric gaugeMetric = new GaugeMetric(
                        "jenkins_job_" + job.getName() + "_" + id + "_" + directory.getKey(),
                        new ArrayList<>(Arrays.asList(
                                new Label("component_type", "job"),
                                new Label("source", id),
                                new Label("directory", directory.getKey()),
                                new Label("metric", "status"),
                                new Label("a_name", job.getName()),
                                new Label("b_lastsuccess", job.getLastSuccessDate(), true),
                                new Label("c_lastfailure", job.getLastFailureDate(), true),
                                new Label("d_lastduration", job.getLastDurationString(), true)
                        )));

                monitoringManager.getMetricRegistry().register(gaugeMetric);
                job.setGaugeMetric(gaugeMetric);
            } else {
                job.getGaugeMetric().checkLabel("b_lastsuccess", job.getLastSuccessDate());
                job.getGaugeMetric().checkLabel("c_lastfailure", job.getLastFailureDate());
                job.getGaugeMetric().checkLabel("d_lastduration", job.getLastDurationString());
            }

            job.getGaugeMetric().set(job.getStatus().getGaugeValue());
        }
    }

    public ConnectorStatus getStatus() {
        return status;
    }

    public JobList createJobList(JenkinsDirectory directory, boolean detailed) {
        String directoryPath = directory.getPath();
        JobList jobList = new JobList(directoryPath);

        try {
            // Get job list
            String jobListSerice = URLEncoder.encode("/" + directoryPath + "/api/json?tree=jobs[name,color]");
            HttpResponse<String> jobsResponse = null;
            int nbTries = 0;
            while (jobsResponse == null && nbTries < directory.getRetryCount()) {
                jobsResponse = httpClient.getAsync(
                        new URI(protocol + "://" + serverAddress + jobListSerice),
                        timeoutMs / 1000,
                        login,
                        pwd);
                nbTries++;
            }

            if (jobsResponse == null || jobsResponse.statusCode() != 200) {
                return jobList;
            }

            String jobsJson = jobsResponse.body();

            JSONArray jobsJsonArray = new JSONObject(jobsJson).getJSONArray("jobs");

            for (int i = 0; i < jobsJsonArray.length(); i++) {
                JSONObject jsonObject = jobsJsonArray.getJSONObject(i);

                if (!jsonObject.has("color")) {
                    continue;
                }

                String name = jsonObject.getString("name");
                String color = jsonObject.getString("color");
                String status = colorMap.getOrDefault(color, color);

                Job existingJob = directory.getJob(name);

                if (!detailed) {
                    if (existingJob == null) {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, null);
                        jobList.add(job);
                    } else {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, null,
                                existingJob.getGaugeMetric());
                        jobList.add(job);
                    }
                    continue;
                }

                // Get last duration
                String lastBuildService = URLEncoder.encode("/" + directoryPath + "/job/" + name +
                        "/lastBuild/api/json");
                HttpResponse<String> lastBuildResponse = null;
                nbTries = 0;
                while (lastBuildResponse == null && nbTries < directory.getRetryCount()) {
                    lastBuildResponse = httpClient.getAsync(
                            new URI(protocol + "://" + serverAddress + lastBuildService),
                            timeoutMs / 1000,
                            login,
                            pwd);
                    nbTries++;
                }

                if (lastBuildResponse == null || lastBuildResponse.statusCode() != 200) {
                    if (existingJob == null) {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, null);
                        jobList.add(job);
                    } else {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, null,
                                existingJob.getGaugeMetric());
                        jobList.add(job);
                    }
                    continue;
                }

                String lastBuildJson = lastBuildResponse.body();

                JSONObject lastBuildObj = new JSONObject(lastBuildJson);
                JSONArray actions = lastBuildObj.getJSONArray("actions");

                Long lastDuration = Objects.requireNonNull(StreamSupport.stream(actions.spliterator(), false)
                        .map(e -> (JSONObject) e)
                        .filter(e -> e.has("_class") &&
                                "jenkins.metrics.impl.TimeInQueueAction".equals(e.getString("_class")))
                        .findFirst()
                        .orElse(null))
                        .getLong("buildingDurationMillis");

                // Long lastSuccess = lastBuildObj.getLong("timestamp");

                // Get last success and last failure
                String jobBuildsService =
                        URLEncoder.encode(directoryPath + "/job/" + name +
                                "/api/json?tree=builds[number,status,timestamp,id,result]");
                HttpResponse<String> buildsResponse = null;
                nbTries = 0;
                while (buildsResponse == null && nbTries < directory.getRetryCount()) {
                    buildsResponse = httpClient.getAsync(
                            new URI(protocol + "://" + serverAddress + "/" + jobBuildsService),
                            timeoutMs / 1000,
                            login,
                            pwd);
                    nbTries++;
                }

                if (buildsResponse == null || buildsResponse.statusCode() != 200) {
                    if (existingJob == null) {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, lastDuration);
                        jobList.add(job);
                    } else {
                        Job job = new Job(name, BuildStatus.fromString(status), null, null, lastDuration,
                                existingJob.getGaugeMetric());
                        jobList.add(job);
                    }
                    continue;
                }

                String buildsJson = buildsResponse.body();

                JSONArray builds = new JSONObject(buildsJson).getJSONArray("builds");

                JSONObject lastFailureObj = StreamSupport.stream(builds.spliterator(), false)
                        .map(e -> (JSONObject) e)
                        .sorted((e1, e2) -> Long.compare(e2.getLong("timestamp"), e1.getLong("timestamp")))
                        .filter(e -> e.has("result") && !e.isNull("result") &&
                                "FAILURE".equals(e.getString("result")))
                        .findFirst()
                        .orElse(null);

                Long lastFailure = lastFailureObj == null ? null : lastFailureObj.getLong("timestamp");

                JSONObject lastSuccessObj = StreamSupport.stream(builds.spliterator(), false)
                        .map(e -> (JSONObject) e)
                        .sorted((e1, e2) -> Long.compare(e2.getLong("timestamp"), e1.getLong("timestamp")))
                        .filter(e -> e.has("result") && !e.isNull("result") &&
                                "SUCCESS".equals(e.getString("result")))
                        .findFirst()
                        .orElse(null);

                Long lastSuccess = lastSuccessObj == null ? null : lastSuccessObj.getLong("timestamp");

                if (existingJob == null) {
                    Job job = new Job(name, BuildStatus.fromString(status), lastSuccess, lastFailure, lastDuration);
                    jobList.add(job);
                } else {
                    Job job = new Job(name, BuildStatus.fromString(status), lastSuccess, lastFailure, lastDuration,
                            existingJob.getGaugeMetric());
                    jobList.add(job);
                }

            }


        } catch (URISyntaxException | NotFoundException e) {
            e.printStackTrace();
        }

        directory.setJobList(jobList);

        return jobList;
    }

    private JobList createJobList(JenkinsDirectory directory) {
        return createJobList(directory, true);
    }

    public List<JenkinsDirectory> getDirectories() {
        return directories;
    }
}
