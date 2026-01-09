##################################################################
####################### METRICS SERVER ###########################
##################################################################

data "http" "metrics_server_yaml" {
  count = terraform.workspace == "dev" ? 1 : 0
  url   = "https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"
}

data "kubectl_file_documents" "docs" {
  count   = terraform.workspace == "dev" ? 1 : 0
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

# Gera os manifestos com kubectl kustomize inline
data "external" "kustomize_manifests" {
  program = [
    "${path.module}/scripts/update_kustomize.sh",
    "develop-${var.commit_sha}",
    local.kustomize_path
  ]
}

# A saída do script agora é YAML puro, não precisa mais de base64
locals {
  kustomize_yaml = data.external.kustomize_manifests.result.stdout
}

# Lê os manifestos decodificados
data "kubectl_file_documents" "kustomization" {
  content = local.kustomize_yaml
}

resource "kubectl_manifest" "kustomization" {
  for_each = data.kubectl_file_documents.kustomization.manifests

  yaml_body = each.value

  # Usa server-side apply para evitar conflitos com recursos do Terraform
  server_side_apply = true
  force_conflicts   = true  # Permite sobrescrever campos gerenciados por outros controllers (ex: HPA)
  wait              = true

  depends_on = [
    kubernetes_namespace.oficina,
    kubernetes_secret_v1.jwt_secrets,
    kubernetes_secret_v1.notification_secrets,
    kubernetes_secret_v1.postgres_credentials
  ]
}