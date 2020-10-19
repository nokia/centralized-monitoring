# centralized-monitoring helm chart

This is an helm chart for the centralized monitoring runtime, including 
the prometheus and grafana implementations needed to expose the collected 
metrics. 

### Prerequisites

Before installing this helm chart, this few steps are important:
- fetch prometheus-11.12.0 and grafana-5.5.6 helm charts into a **charts** directory at the chart root
- properly configure your runtime and your metrics, following the next chapter

### Runtime & metrics configuration

##### Connection Settings

*WiP*

##### Metrics Configuration

*WiP*

### Values definition
The purpose of this part is to define the important value definitions for 
centralized-monitoring, prometheus and grafana in this release. The values 
that aren't detailed here will work with no modifications. Keep in mind
that the prometheus and grafana helm chart used here are the official 
prometheus-11.12.0 and grafana-5.5.6 versions, which are defined 
[here for prometheus](https://github.com/helm/charts/tree/master/stable/prometheus) 
and [here for grafana](https://github.com/helm/charts/tree/master/stable/grafana).

#### cmonitoring

| Parameter        | Description           | Default  |
| ------------- | ------------- | -----:|
| replicaCount      | Number of centralized monitoring pods | 1 |
| nameOverride | Expanded name of the chart, used for the kubernetes service and configMap names. This entry isn't mandatory, as it has a default-generated value. | <chart_name> |
| fullNameOverride | Fully qualified app name for deployment and persistent volume claim. This entry isn't mandatory either, as it has a default-generated value. | <release_name-chart_name> |
| image.repository | Docker image for the runtime, in the format <repo>/<image_name> | cmonitoring |
| image.tag | Docker image tag | 0.1.1 |
| image.pullPolicy | Pull policy for the docker image | IfNotPresent |
| config.config.enabled     | Enables centralized monitoring user configuration from the deployment, from files/config.json | true |
| config.connectionSettings.enabled | Uses user connection settings configuration from files/connectionSettings | true |
| securityContext | Docker container security context, to make sure the image runs as the configured user 5555 | enabled |
| resources | Requests and limits for the deployment in cpu and memory. | enabled |
| persistence.enabled | Enables configuration persistence inside the cluster if the application dies. | true |
| persistence.storageClass.enabled | Enables user-defined storageClass for persistence | true |
| persistence.storageClass.name | User-defined storageClass name | hostpath |
| persistence.size | Storage allocated to store the persistent configuration | 1Gi |
| persistence.accessModes | accessModes for the persistent volume. ReadOnly not recommended, as you can change the configuration via the API | ReadWriteOnce |

#### prometheus

The important parameter for this release is the extraScrapeConfigs. This 
adds by default the correct metrics to the prometheus runtime.

| Parameter        | Description           | Default  |
| ------------- | ------------- | -----:|
| extraScrapeConfigs.job_name | Prometheus job name for centralized monitoring | cmonitoring |
| extraScrapeConfigs.metrics_path | The path where centralized monitoring metrics are exposed by the centralized monitoring pod | /api/metrics |
| extraScrapeConfigs.scrape_interval | Metrics interval for centralized monitoring | 60s |
| static_configs.targets | Centralized monitoring service url | centralized-monitoring |

The complete configuration can be found [here](https://github.com/helm/charts/tree/master/stable/prometheus)

#### grafana

In Grafana, it is important to configure the prometheus datasource in the 
datasource.yaml. It is also advised to configure admin user. 

| Parameter        | Description           | Default  |
| ------------- | ------------- | -----:|
| adminUser | Grafana admin user | admin |
| adminPassword | Grafana admin password | admin |
| datasources.datasource.yaml.datasources.name | Prometheus datasource name | Prometheus |
| datasources.datasource.yaml.datasources.type | Prometheus datasource type (to be set as prometheus) | prometheus |
| datasources.datasource.yaml.datasources.url | Prometheus datasource url, which is the kubernetes service name of the prometheus runtime | http://cmonitoring-prometheus-server |
| datasources.datasource.yaml.datasources.access | Prometheus datasource access type (proxy has to be set as kubernetes doesn't expose services) | proxy |
| datasources.datasource.yaml.datasources.isDefault | Set prometheus datasource as default | true |

The complete configuration can be found [here](https://github.com/helm/charts/tree/master/stable/grafana).
