include INFO

.PHONY: build push release delete

build:
	docker build $(DOCKER_BUILD_OPTS) -t $(FINAL_IMAGE_NAME) .

run:
	docker run --name $(CONTAINER) $(CMONITORING_DOCKER_OPTS) $(FINAL_IMAGE_NAME)
	docker run --name $(PROM_CONTAINER_NAME) $(PROM_DOCKER_OPTS) $(PROM_IMAGE_NAME)
	docker run --name $(GRAFANA_CONTAINER_NAME) $(GRAFANA_DOCKER_OPTS) $(GRAFANA_IMAGE_NAME)
	sleep $(TIMEOUT)

delete:
	docker rm -f $(CONTAINER) $(PROM_CONTAINER_NAME) $(GRAFANA_CONTAINER_NAME)

#push:
#	docker push $(FINAL_IMAGE_NAME)

release: build push

default: build
