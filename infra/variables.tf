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