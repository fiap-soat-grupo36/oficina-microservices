##################################################################
######################## K8S NAMESPACE ############################
##################################################################

resource "kubernetes_namespace" "oficina" {
  metadata {
    name = "oficina-mecanica-${var.environment}"
    labels = {
      name        = "oficina-${var.environment}"
      environment = var.environment
    }
  }

  lifecycle {
    prevent_destroy = false
  }
}
