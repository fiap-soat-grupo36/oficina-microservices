##################################################################
###################### CONFIGMAP OVERRIDE ########################
##################################################################

# Criar o ConfigMap base primeiro
resource "kubernetes_config_map_v1" "oficina_shared" {
  metadata {
    name      = "oficina-shared-config"
    namespace = local.namespace
  }

  data = {
    SPRING_PROFILES_ACTIVE = "k8s"
    EUREKA_URL = "http://eureka-server:8761/eureka/"
    DB_URL = "jdbc:postgresql://postgres:5432/oficina-db"
    DB_NAME = "oficina-db"
    SERVER_PORT_AUTH = "8082"
    SERVER_PORT_CUSTOMER = "8081"
    SERVER_PORT_CATALOG = "8083"
    SERVER_PORT_INVENTORY = "8084"
    SERVER_PORT_BUDGET = "8085"
    SERVER_PORT_WORK_ORDER = "8086"
    SERVER_PORT_NOTIFICATION = "8087"
  }

  depends_on = [
    kubernetes_namespace.oficina
  ]
}

# ConfigMap para sobrescrever DB_URL com endpoint real do RDS
resource "kubernetes_config_map_v1_data" "db_override" {
  count = var.rds_identifier != "" && terraform.workspace != "default" ? 1 : 0

  metadata {
    name      = "oficina-shared-config"
    namespace = local.namespace
  }

  data = {
    DB_URL = "jdbc:postgresql://${data.aws_rds_cluster.cluster.endpoint}/${var.rds_database_name}"
    DB_NAME = var.rds_database_name
  }

  force = true

  depends_on = [
    kubernetes_config_map_v1.oficina_shared,
    kubectl_manifest.kustomization
  ]
}