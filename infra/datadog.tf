# Namespace para Datadog Agent (apenas dev - prod usa namespace do infra-kubernetes)
resource "kubernetes_namespace" "datadog" {
  count = local.environment == "dev" ? 1 : 0

  metadata {
    name = "datadog-agent"
  }
}

# Secret com credenciais do Datadog
resource "kubernetes_secret" "datadog" {
  count = local.environment == "dev" ? 1 : 0

  metadata {
    name      = "datadog"
    namespace = local.environment == "dev" ? kubernetes_namespace.datadog[0].metadata[0].name : "datadog-agent"
  }

  data = {
    "api-key" = var.datadog_api_key
    "app-key" = var.datadog_app_key
  }

  depends_on = [kubernetes_namespace.datadog]
}

# DatadogAgent CRD - só aplica se o Datadog Operator (do infra-kubernetes) já instalou o CRD
data "kubectl_path_documents" "dd_agent" {
  pattern = "../k8s/datadog/datadog-agent.yaml"
}

resource "kubectl_manifest" "dd_agent_manifest" {
  for_each  = local.environment == "dev" ? data.kubectl_path_documents.dd_agent.manifests : {}
  yaml_body = each.value

  # Usar server-side apply para evitar conflitos
  server_side_apply = true

  # Aguardar o recurso ser criado completamente
  wait = true

  depends_on = [
    kubernetes_namespace.datadog,
    kubernetes_secret.datadog
  ]
}