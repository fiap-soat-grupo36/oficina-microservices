##################################################################
######################## K8S NAMESPACE ############################
##################################################################

resource "kubernetes_namespace" "oficina" {
  metadata {
    name = "oficina-mecanica-${local.environment}"
    labels = {
      name        = "oficina-${local.environment}"
      environment = local.environment
    }
  }

  lifecycle {
    prevent_destroy = false
  }
}
