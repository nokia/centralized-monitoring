/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.main.jetty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nokia.as.connector.ConnectorType;
import com.nokia.as.connector.core.JenkinsConnector;
import com.nokia.as.connector.core.JiraConnector;
import com.nokia.as.connector.data.jenkins.JobList;
import com.nokia.as.connector.data.jira.IssueList;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Entry points for the Jetty server
 */
@Path(App.API_ROOT)
public class EntryPoint {

    @POST
    @Path("set-config")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String setConfig(InputStream data) {
        String newConfig = App.monitoringManager.setConfig(data);
        App.monitoringManager.init();
        return newConfig;
    }

    @GET
    @Path("get-config")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConfig(InputStream data) {
        return App.monitoringManager.getConfig();
    }

    @GET
    @Path("global-monitoring")
    @Produces(MediaType.APPLICATION_JSON)
    public String globalMonitoring() {
        return App.monitoringManager.toString();
    }

    @GET
    @Path("jenkins-get-jobs/{instance}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getJobs(@PathParam("instance") String instance,
                          @QueryParam("directory-key") String directoryKey) {

        JenkinsConnector jenkinsConnector = (JenkinsConnector)
                App.monitoringManager.getConnectorList(ConnectorType.JENKINS).getConnector(instance);

        JobList jobList = jenkinsConnector.getDirectory(directoryKey).getJobList();

        if (jobList == null) {
            return "[]";
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(jobList);
    }

    @GET
    @Path("jira-get-issues/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIssues(@PathParam("id") String instance,
                            @QueryParam("filter-key") String filterKey) {

        JiraConnector jiraConnector = (JiraConnector)
                App.monitoringManager.getGlobalConnectorList().getConnector("jiradc2");

        IssueList issueList = jiraConnector.getFilter(filterKey).getIssueList();

        if (issueList == null) {
            return "[]";
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(issueList);
    }

}