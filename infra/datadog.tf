# Datadog Agent gerenciado pelo infra-kubernetes via EKS Addon
# Este código foi comentado para evitar conflitos e dependências circulares
# O Datadog Operator é instalado via EKS addon no projeto infra-kubernetes

# # Namespace para Datadog Agent
# resource "kubernetes_namespace" "datadog" {
#   metadata {
#     name = "datadog-agent"
#   }
# }
#
# resource "kubernetes_secret" "datadog" {
#   metadata {
#     name      = "datadog"
#     namespace = kubernetes_namespace.datadog.metadata[0].name
#   }
#
#   data = {
#     "api-key" = var.datadog_api_key
#     "app-key" = var.datadog_app_key
#   }
#
#   depends_on = [kubernetes_namespace.datadog]
# }
#
# data "kubectl_path_documents" "dd_agent" {
#   pattern = "../k8s/datadog/datadog-agent.yaml"
# }
#
# resource "kubectl_manifest" "dd_agent_manifest" {
#   for_each  = data.kubectl_path_documents.dd_agent.manifests
#   yaml_body = each.value
#
#   depends_on = [
#     kubernetes_namespace.datadog,
#     kubernetes_secret.datadog
#   ]
# }