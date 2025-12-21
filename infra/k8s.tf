##################################################################
####################### METRICS SERVER ###########################
##################################################################

data "http" "metrics_server_yaml" {
  count = terraform.workspace == "dev" ? 1 : 0
  url   = "https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
}

data "kubectl_file_documents" "docs" {
  count   = terraform.workspace  == "dev" ? 1 : 0
  content = data.http.metrics_server_yaml[0].response_body
}

resource "kubectl_manifest" "metrics" {
  for_each = (
    terraform.workspace == "dev"
    ? data.kubectl_file_documents.docs[0].manifests
    : {}
  )
  yaml_body  = each.value
  depends_on = [kubernetes_namespace.oficina]
}

##################################################################
######################### APPLICATION ############################
##################################################################

data "kubectl_path_documents" "dd_agent" {
  pattern = "../../k8s/datadog/datadog-agent.yaml"
}

resource "kubectl_manifest" "dd_agent_manifest" {
  for_each   = data.kubectl_path_documents.dd_agent.manifests
  yaml_body  = each.value
}

