resource "kubernetes_secret" "datadog" {
  metadata {
    name      = "datadog"
    namespace = "datadog-agent"
  }

  data = {
    "api-key" = var.datadog_api_key
    "app-key" = var.datadog_app_key
  }
}

data "kubectl_path_documents" "dd_agent" {
  pattern = "../k8s/datadog/datadog-agent.yaml"
}

resource "kubectl_manifest" "dd_agent_manifest" {
  for_each  = data.kubectl_path_documents.dd_agent.manifests
  yaml_body = each.value
}