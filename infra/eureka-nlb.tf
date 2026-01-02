##################################################################
##################### EUREKA NLB SERVICE #########################
##################################################################

# Template do Service do Eureka com Target Group criado pelo Terraform
locals {
  eureka_service_yaml = templatefile("${path.module}/templates/eureka-nlb-service.yaml.tpl", {
    namespace        = local.namespace
    target_group_arn = aws_lb_target_group.eureka.arn
    nlb_dns          = aws_lb.eureka.dns_name
  })
}

# Cria o Service do Eureka via Terraform (gerenciado como código)
resource "kubectl_manifest" "eureka_nlb_service" {
  yaml_body = local.eureka_service_yaml

  depends_on = [
    kubernetes_namespace.oficina,
    helm_release.aws_load_balancer_controller,
    aws_lb.eureka,
    aws_lb_target_group.eureka
  ]
}

# ConfigMap do Eureka com DNS do NLB
resource "kubernetes_config_map" "eureka_config" {
  metadata {
    name      = "eureka-server-config"
    namespace = local.namespace
  }

  data = {
    EUREKA_HOSTNAME                      = aws_lb.eureka.dns_name
    SERVER_PORT                          = "8761"
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE = "http://${aws_lb.eureka.dns_name}:8761/eureka/"
  }

  depends_on = [
    kubernetes_namespace.oficina,
    aws_lb.eureka
  ]

  lifecycle {
    ignore_changes = [
      metadata[0].annotations,
      metadata[0].labels
    ]
  }
}
