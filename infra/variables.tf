variable "region" {
  description = "AWS region for the provider and resources"
  type        = string
  default     = "us-east-2"
}

variable "datadog_api_key" {
  description = "Datadog API key (prefer to pass via environment or secret)"
  type        = string
  default     = ""
}

variable "datadog_app_key" {
  description = "Datadog Application key (prefer to pass via environment or secret)"
  type        = string
  default     = ""
}

variable "rds_identifier" {
  description = "RDS instance identifier to use for the database connection. Leave empty to use default placeholder."
  type        = string
  default     = "fiap-rds"
}

variable "rds_database_name" {
  description = "Database name in the RDS instance"
  type        = string
  default     = "oficina-db"
}

variable "secrets_manager_jwt_secret_name" {
  description = "Name of the AWS Secrets Manager secret containing JWT secret key. Leave empty to use Kustomize defaults."
  type        = string
  default     = ""
}

variable "secrets_manager_email_secret_name" {
  description = "Name of the AWS Secrets Manager secret containing email credentials (JSON with host, port, username, password, from keys). Leave empty to use Kustomize defaults."
  type        = string
  default     = ""
}

variable "image_tag" {
  description = "The Docker image tag to deploy, passed from the CI/CD pipeline."
  type        = string
  default     = "develop"
}