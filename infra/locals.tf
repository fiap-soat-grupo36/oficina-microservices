locals {
  workspace    = terraform.workspace
  environment  = terraform.workspace == "default" ? "dev" : terraform.workspace
  project_name = "oficina-mecanica"
  kustomize_overlay = terraform.workspace == "default" ? "dev" : terraform.workspace
  kustomize_path    = "${path.module}/../k8s/overlays/${local.kustomize_overlay}"
}