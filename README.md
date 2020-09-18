# Centralised Monitoring

## Description

This is an external aggregated dashboard that can provide operation and dev team with a simple view of the health of the various environments and also allow us to better troubleshoot issues and reduce TCO. \
Every monitored system is clickable so it can be used as bookmarks. \
This can also be used as a radiator on multiple screen.

### System to Monitor

##### Generic monitoring

Jenkins (up/down) \
Gerrit (up/down) \
Gitlab (up/down) \
Confluence (up/down) \
Jira (up/down) \
Artifactory (up/down) \
OpenStack (up/down)

##### Specific monitoring

Jenkins directory(ies) / pods(s) / build(s) \
Jira filter(s) / issue(s)

## Getting started

### Cloud

##### Preliminary information
The Centralized Monitoring runtime is designed to be deployed in a cloud, kubernetes-based infrastructure, using the 
helm package manager for an easier and faster configuration.

Currently, the helm chart is available for kubernetes/helm3 configurations, but we plan to deploy it also on AWS, Azure 
and GCP.

##### Deployment

First, clone this repository.

*NB: centralized-monitoring image is not in a docker hub yet, so you will have to build it yourself, as follows:*

```
cd image
make build
```
It is recommended to have the image on a hub, as it is easier to access for a kubernetes cluster.

```
docker tag cmonitoring:0.1.1 <your_hub>/cmonitoring:0.1.1
docker push <your_hub>/cmonitoring:0.1.1 
```

Then go into the chart/centralized-monitoring directory, and edit the values.yaml file to match your needs. 
[A guideline](https://github.com/nokia/centralized-monitoring/blob/master/chart/centralized-monitoring/README.md) is in 
progress to help you define your configuration, specifying each entry of the values file.

You may also want to add a configuration from the beginning. For this step, edit the config.json in the *files* 
directory following the logic detailed in the **configuration** paragraph.  

When your values.yaml and config.json are ready, simply deploy the chart using helm commands:

```
cd chart/centralized-monitoring
helm install -f values.yaml <your_release_name> .
```

You will now have to expose the necessary services to access the monitoring from outside of the cluster. You can, in 
this case, either port-forward the services or expose them using an ingress controller.

Then jump to the **import Grafana dashboards** paragraph to finalize the monitoring.

### Deploy locally or in your own VM

First, clone this repository.

Then fill the connection settings with your proxy, and your Jira / Jenkins credentials. \
File location: image/resources/config/connectionSettings.json

Build the multi-connector daemon:
```
cd image
make build
```

Start prometheus, grafana and the multi-connector daemon newly built:
```
make run
```

### Configuration

The multi-connector deamon won't do anything without a configuration. \
It's possible to send a JSON configuration via a POST request to /api/set-config:

```
{
  "interval": 60,    --> Number of seconds between each set of connections (can't be set below 60)
  "connectorGroups": [    --> List of connectors grouped by types
    {
      "type": "...",    --> Connector type (gitlab/gerrit/artifactory/jenkins/confluence/jira/openstack)
      "connectors": [    --> Connector list
        {
          "id": "...",    --> Connector ID (e.g: my-jenkins-01)
          "address": "...",    --> URI to reach in order to test the service
          "ssl": true/false,    --> true for https, false for http
          "timeoutMs": 5000,    --> Connection timeout
          "nbSteps": 1    --> Number of intervals between each conection (e.g: with an interval of 60 seconds and a nbSteps set to 60, will do a connection each hour)
        },
        ...
      ]
    },
    {
      "type": "jenkins",
      "connectors": [
        {
          "id": "...",
          "address": "...",
          "ssl": true,
          "timeoutMs": 10000,
          "nbSteps": 60,
          "directories": [    --> Custom parameter only available for jenkins connectors. Will check every Jenkins directories to get jobs
            {
              "key": "my_directory",    --> This key will be used to select the directories in the dashboard
              "path": "<jenkins_path>",    --> The directory path
              "detailed": true    --> false to get only the job status, true to get more data (last successful build, last failure, last duration)
            },
            ...
          ]
        }
      ]
    },
    {
      "type": "jira",
      "connectors": [
        {
          "id": "...",
          "address": "...",
          "ssl": true,
          "timeoutMs": 5000,
          "nbSteps": 60,
          "filters": [    --> Custom parameter only available for jira connectors. Will check every Jira filters to get issues
            {
              "key": "my_filter",    --> This key will be used to select the filters in the dashboard
              "jql": "<jql_query>",    --> JQL query
              "fields": "id,key,reporter,assignee,status,created,updated,summary"    --> The fields that will be retrieved by the filter
            },
            ...
          ]
        }
      ]
    }
  ]
}
```

An example configuration file is available in image/resources/config/config.json

### Import Grafana dashboards

The templates dashboards JSON files are available in grafana/dashboards \
It's very simple to import a dashboard to Grafana. \
Please refer to the [official documentation](https://grafana.com/docs/grafana/latest/reference/export_import/#importing-a-dashboard).

## REST API

### Set a new configuration

| Method          | Path                        | Description   |
| :-------------: | :-------------------------: | :-----------: |
|  POST            | /api/set-config   | Will set the configuration for the deamon. Any new configuration file erase the previous one. <br> Will be rejected if the JSON schema isn't valid (see "Configuration" above) |

### Get the current configuration

| Method          | Path                        | Description   |
| :-------------: | :-------------------------: | :-----------: |
|  GET            | /api/get-config   | Get the current JSON configuration |

### Get the global monitoring

| Method          | Path                        | Description   |
| :-------------: | :-------------------------: | :-----------: |
|  GET            | /api/global-monitoring   | Get a JSON output corresponding to all of your exposed metrics <br> that you can see in your dashboard |

### Get Jenkins jobs

| Method          | Path                        | Description   |
| :-------------: | :-------------------------: | :-----------: |
|  GET            | /api/jenkins-get-jobs/{instance}   | Get jobs from a Jenkins instance and directory |

| Parameter       | Required | Description |
| :-------------: | :-: | :-----------: |
|  directory-key          | YES | The directory set up in the configiration file |

### Get Jira issues

| Method          | Path                        | Description   |
| :-------------: | :-------------------------: | :-----------: |
|  GET            | /api/jira-get-issues/{instance}   | Get issues from a Jira instance and filter |

| Parameter       | Required | Description |
| :-------------: | :-: | :-----------: |
|  filter-key          | YES | The filter set up in the configiration file |

## Dashboard

Here's the current version of the dashboard with, for instance, a default configuration:

[Screenshot incoming]

## Architecture

Here's the current architecture schema of this project:

[Schema incoming]
