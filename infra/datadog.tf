module "datadog" {
  source  = "c0x12c/helm-datadog/aws"
  version = "~> 0.8.0"

  environment  = local.environment
  cluster_name = data.aws_eks_cluster.oficina.name

  datadog_site    = "https://us5.datadoghq.com/"
  datadog_api_key = var.datadog_api_key
  datadog_app_key = var.datadog_app_key

  enabled_agent                      = true
  enabled_cluster_agent              = true
  enabled_cluster_check              = true
  enabled_container_collect_all_logs = true
  enabled_logs                       = true
  enabled_metric_provider            = true

  namespace = kubernetes_namespace.oficina.metadata[0].name
  datadog_envs = [{
    name  = "DD_EKS_FARGATE"
    value = "true"
  }]

  #node_selector = {
  #  "service-type" = "backbone"
  #}

  tolerations = [
    #{
    #  key      = "service-type"
    #  operator = "Equal"
    #  value    = "backbone"
    #  effect   = "NoSchedule"
    #},
    {
      key      = "eks.amazonaws.com/compute-type"
      operator = "Equal"
      value    = "fargate"
      effect   = "NoSchedule"
    }
  ]

  depends_on = [kubernetes_namespace.oficina]

}

