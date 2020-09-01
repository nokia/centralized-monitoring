include INFO

.PHONY: build

build:
	docker build $(DOCKER_BUILD_OPTS) -t $(FINAL_IMAGE_NAME) .

run:
	docker run --name $(CONTAINER) $(DOCKER_OPTS) $(FINAL_IMAGE_NAME)
	sleep $(TIMEOUT)

default: build
