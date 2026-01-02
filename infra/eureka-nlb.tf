##################################################################
##################### EUREKA NLB SERVICE #########################
##################################################################

locals {
  eureka_service_yaml = templatefile("${path.module}/templates/eureka-nlb-service.yaml.tpl", {
    namespace        = local.namespace
    target_group_arn = aws_lb_target_group.eureka.arn
    nlb_dns          = aws_lb.eureka.dns_name
  })
}

resource "kubectl_manifest" "eureka_nlb_service" {
  yaml_body = local.eureka_service_yaml

  depends_on = [
    kubernetes_namespace.oficina,
    helm_release.aws_load_balancer_controller,
    aws_lb.eureka,
    aws_lb_target_group.eureka
  ]
}
