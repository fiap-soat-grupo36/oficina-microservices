##################################################################
####################### METRICS SERVER ###########################
##################################################################

data "http" "metrics_server_yaml" {
  url = "https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
}

data "kubectl_file_documents" "docs" {
  content = data.http.metrics_server_yaml.response_body
}

resource "kubectl_manifest" "metrics" {
  for_each   = data.kubectl_file_documents.docs.manifests
  yaml_body  = each.value
  depends_on = [kubernetes_namespace.oficina]
}

##################################################################
######################### APPLICATION ############################
##################################################################

#data "kubectl_path_documents" "app_docs" {
#  pattern = "../../k8s/app/*.yaml"
#}

#resource "kubectl_manifest" "app_manifest" {
#  for_each   = data.kubectl_path_documents.app_docs.manifests
#  yaml_body  = each.value
#  depends_on = [kubectl_manifest.metrics, kubectl_manifest.db_manifest, kubectl_manifest.namespace_manifest]
#}

