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

# Processa os manifestos Kustomize do overlay específico do ambiente
data "external" "kustomize_build" {
  program = ["bash", "-c", <<-EOT
    cd ${local.kustomize_path}
    kubectl kustomize . | jq -Rs '{"manifests": .}'
  EOT
  ]
}

data "kubectl_file_documents" "kustomization" {
  content = data.external.kustomize_build.result.manifests
}

resource "kubectl_manifest" "kustomization" {
  for_each   = data.kubectl_file_documents.kustomization.manifests
  yaml_body  = each.value
  depends_on = [kubernetes_namespace.oficina]
}