##################################################################
###################### CONFIGMAP OVERRIDE ########################
##################################################################

# ConfigMap unificado com informações corretas desde o início
resource "kubernetes_config_map_v1" "oficina_shared" {
  metadata {
    name      = "oficina-shared-config"
    namespace = local.namespace
  }

  data = {
    SPRING_PROFILES_ACTIVE = "k8s"
    # URLs do Eureka para os microservices (usa service interno)
    EUREKA_URL                           = "http://eureka-server-internal:8761/eureka/"
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE = "http://eureka-server-internal:8761/eureka/"
    # Database 
    DB_URL  = "jdbc:postgresql://${data.aws_rds_cluster.cluster.endpoint}/${var.rds_database_name}"
    DB_NAME = var.rds_database_name
    # Database initialization
    # DEV: create-drop recria schema toda vez, seed sempre roda
    # PROD: update preserva dados, seed desabilitado
    DDL_AUTO      = local.environment == "dev" ? "create-drop" : "update"
    SQL_INIT_MODE = local.environment == "dev" ? "always" : "never"
    SHOW_SQL      = "false"
    # Logging configuration
    LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB = local.environment == "dev" ? "DEBUG" : "INFO"
    LOGGING_LEVEL_BR_COM_FIAP_OFICINA     = local.environment == "dev" ? "DEBUG" : "INFO"
    # Service Ports
    SERVER_PORT_AUTH         = "8082"
    SERVER_PORT_CUSTOMER     = "8081"
    SERVER_PORT_CATALOG      = "8083"
    SERVER_PORT_INVENTORY    = "8084"
    SERVER_PORT_BUDGET       = "8085"
    SERVER_PORT_WORK_ORDER   = "8086"
    SERVER_PORT_NOTIFICATION = "8087"
  }

  depends_on = [
    kubernetes_namespace.oficina
  ]
}
