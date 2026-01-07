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

# ConfigMap do Eureka com DNS do NLB - COMENTADO
# IMPORTANTE: Os serviços INTERNOS devem usar eureka-server-internal (ClusterIP)
# O NLB é apenas para acesso EXTERNO ao Eureka
#
# resource "kubernetes_config_map_v1_data" "eureka_nlb_config" {
#   metadata {
#     name      = "oficina-shared-config"
#     namespace = local.namespace
#   }
#
#   data = {
#     EUREKA_HOSTNAME                      = aws_lb.eureka.dns_name
#     EUREKA_URL                           = "http://${aws_lb.eureka.dns_name}:8761/eureka/"
#     EUREKA_CLIENT_SERVICEURL_DEFAULTZONE = "http://${aws_lb.eureka.dns_name}:8761/eureka/"
#   }
#
#   force = true
#
#   depends_on = [
#     kubernetes_config_map_v1.oficina_shared,
#     aws_lb.eureka
#   ]
#
#   # kubernetes_config_map_v1_data does not expose metadata labels/annotations.
# }
