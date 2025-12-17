##################################################################
######################## K8S NAMESPACE ############################
##################################################################

resource "kubernetes_namespace" "oficina" {
  metadata {
    name = "oficina-mecanica-${locals.environment}"
    labels = {
      name        = "oficina-${locals.environment}"
      environment = locals.environment
    }
  }

  lifecycle {
    prevent_destroy = false
  }
}
