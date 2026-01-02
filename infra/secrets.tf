##################################################################
####################### SECRETS OVERRIDE #########################
##################################################################

# Locals para processar JSON dos secrets
locals {
  db_endpoint = var.rds_identifier != "" ? data.aws_rds_cluster.cluster.endpoint : "postgres:5432"
  db_url      = "jdbc:postgresql://${local.db_endpoint}/${var.rds_database_name}"

  # Parse JSON dos secrets do Secrets Manager
  db_credentials    = jsondecode(data.aws_secretsmanager_secret_version.db_password.secret_string)
  jwt_secret        = var.secrets_manager_jwt_secret_name != "" ? jsondecode(data.aws_secretsmanager_secret_version.jwt_secret[0].secret_string) : {}
  email_credentials = var.secrets_manager_email_secret_name != "" ? jsondecode(data.aws_secretsmanager_secret_version.email_credentials[0].secret_string) : {}
}

# Secret: postgres-credentials
resource "kubernetes_secret_v1" "postgres_credentials" {

  metadata {
    name      = "postgres-credentials"
    namespace = local.namespace
  }

  data = {
    POSTGRES_USER     = base64encode(lookup(local.db_credentials, "username", "postgres"))
    POSTGRES_PASSWORD = base64encode(lookup(local.db_credentials, "password", ""))
    DB_USERNAME       = base64encode(lookup(local.db_credentials, "username", "postgres"))
    DB_PASSWORD       = base64encode(lookup(local.db_credentials, "password", ""))
  }

  type = "Opaque"

  depends_on = [
    kubectl_manifest.kustomization
  ]
}

# Secret: jwt-secrets
resource "kubernetes_secret_v1" "jwt_secrets" {
  count = var.secrets_manager_jwt_secret_name != "" && terraform.workspace != "default" ? 1 : 0

  metadata {
    name      = "jwt-secrets"
    namespace = local.namespace
  }

  data = {
    JWT_SECRET = base64encode(lookup(local.jwt_secret, "secret", ""))
  }

  type = "Opaque"

  depends_on = [
    kubectl_manifest.kustomization
  ]
}

# Secret: notification-service-secrets
resource "kubernetes_secret_v1" "notification_secrets" {
  count = var.secrets_manager_email_secret_name != "" && terraform.workspace != "default" ? 1 : 0

  metadata {
    name      = "notification-service-secrets"
    namespace = local.namespace
  }

  data = {
    EMAIL_HOST     = base64encode(lookup(local.email_credentials, "host", "smtp.gmail.com"))
    EMAIL_PORT     = base64encode(lookup(local.email_credentials, "port", "587"))
    EMAIL_USERNAME = base64encode(lookup(local.email_credentials, "username", ""))
    EMAIL_PASSWORD = base64encode(lookup(local.email_credentials, "password", ""))
    EMAIL_FROM     = base64encode(lookup(local.email_credentials, "from", ""))
  }

  type = "Opaque"

  depends_on = [
    kubectl_manifest.kustomization
  ]
}
