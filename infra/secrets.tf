##################################################################
####################### SECRETS OVERRIDE #########################
##################################################################

# Locals para processar JSON dos secrets
locals {
  db_endpoint = var.rds_identifier != "" ? data.aws_rds_cluster.cluster.endpoint : "postgres:5432"
  db_url      = "jdbc:postgresql://${local.db_endpoint}/${var.rds_database_name}"

  # Parse JSON dos secrets do Secrets Manager
  db_credentials    = jsondecode(data.aws_secretsmanager_secret_version.db_password.secret_string)
  jwt_secret        = var.secrets_manager_jwt_secret_name != "" ? data.aws_secretsmanager_secret_version.jwt_secret.secret_string : "dev-jwt-secret-key-minimum-256-bits-for-hs256-algorithm-change-me"
  email_credentials = var.secrets_manager_email_secret_name != "" ? jsondecode(data.aws_secretsmanager_secret_version.email_credentials.secret_string) : {}
}

# Secret: postgres-credentials
resource "kubernetes_secret_v1" "postgres_credentials" {

  metadata {
    name      = "postgres-credentials"
    namespace = local.namespace
  }

  data = {
    DB_USERNAME       = lookup(local.db_credentials, "username", "postgres")
    DB_PASSWORD       = lookup(local.db_credentials, "password", "")
    POSTGRES_USER     = lookup(local.db_credentials, "username", "postgres")
    POSTGRES_PASSWORD = lookup(local.db_credentials, "password", "")
  }

  type = "Opaque"
}

# Secret: jwt-secrets
resource "kubernetes_secret_v1" "jwt_secrets" {
  metadata {
    name      = "jwt-secrets"
    namespace = local.namespace
  }

  data = {
    JWT_SECRET     = local.jwt_secret
    JWT_EXPIRATION = "14400000"
  }

  type = "Opaque"

  depends_on = [
    kubernetes_namespace.oficina
  ]
}

# Secret: notification-service-secrets
resource "kubernetes_secret_v1" "notification_secrets" {
  metadata {
    name      = "notification-service-secrets"
    namespace = local.namespace
  }

  data = {
    MAIL_HOST      = lookup(local.email_credentials, "host", "smtp.gmail.com")
    MAIL_PORT      = lookup(local.email_credentials, "port", "587")
    EMAIL_USERNAME = lookup(local.email_credentials, "username", "")
    EMAIL_PASSWORD = lookup(local.email_credentials, "password", "")
    EMAIL_SENDER   = lookup(local.email_credentials, "sender", "")
  }

  type = "Opaque"

  depends_on = [
    kubernetes_namespace.oficina
  ]
}
