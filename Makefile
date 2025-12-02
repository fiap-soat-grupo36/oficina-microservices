DOCKER ?= docker
TAG ?= latest
PLATFORMS ?= linux/amd64,linux/arm64
# Defina REGISTRY para push (ex.: seu-usuario no Docker Hub)
REGISTRY ?=

SERVICES := \
	eureka-server \
	auth-service \
	customer-service \
	catalog-service \
	inventory-service \
	budget-service \
	work-order-service \
	notification-service

ifeq ($(REGISTRY),)
IMAGE_PREFIX :=
else
IMAGE_PREFIX := $(REGISTRY)/
endif

image_name = $(IMAGE_PREFIX)oficina-$1:$(TAG)

.PHONY: build push buildx-push ensure-registry clean

build: $(SERVICES:%=build-%)

build-%:
	@svc=$*; \
	img=$(call image_name,$$svc); \
	echo "==> Building $$img"; \
	$(DOCKER) build -t $$img -f ./$$svc/Dockerfile .

push: ensure-registry $(SERVICES:%=push-%)

push-%: build-%
	@svc=$*; \
	img=$(call image_name,$$svc); \
	echo "==> Pushing $$img"; \
	$(DOCKER) push $$img

# Build multi-arch e já faz push (usa buildx)
buildx-push: ensure-registry $(SERVICES:%=buildx-push-%)

buildx-push-%:
	@svc=$*; \
	img=$(call image_name,$$svc); \
	echo "==> Buildx + push $$img (platforms=$(PLATFORMS))"; \
	$(DOCKER) buildx build --platform $(PLATFORMS) --push -t $$img -f ./$$svc/Dockerfile .

ensure-registry:
ifndef REGISTRY
	$(error Defina REGISTRY=<seu-usuario-no-registry> para fazer push)
endif

clean:
	@for svc in $(SERVICES); do \
		img=$(call image_name,$$svc); \
		echo "==> Removendo $$img"; \
		$(DOCKER) rmi $$img 2>/dev/null || true; \
	done
