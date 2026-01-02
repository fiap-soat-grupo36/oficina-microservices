locals {
  workspace         = terraform.workspace
  environment       = terraform.workspace == "default" ? "dev" : terraform.workspace
  project_name      = "oficina-mecanica"
  kustomize_overlay = terraform.workspace == "default" ? "dev" : terraform.workspace
  kustomize_path    = "${path.module}/../k8s/overlays/${local.kustomize_overlay}"
  namespace         = kubernetes_namespace.oficina.metadata[0].name
  
  # Agrupa subnets por AZ e seleciona apenas uma por AZ para o NLB
  subnets_by_az = {
    for subnet_id, subnet in data.aws_subnet.private :
    subnet.availability_zone => subnet_id...
  }
  
  # Pega a primeira subnet de cada AZ
  nlb_subnets = [
    for az, subnet_ids in local.subnets_by_az :
    subnet_ids[0]
  ]
}