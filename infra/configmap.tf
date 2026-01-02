##################################################################
###################### CONFIGMAP OVERRIDE ########################
##################################################################

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
    kubernetes_namespace.oficina,
    kubectl_manifest.kustomization
  ]
}