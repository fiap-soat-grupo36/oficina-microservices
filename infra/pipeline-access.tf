##################################################################
####################### EKS RBAC FOR PIPELINE #####################
##################################################################

resource "kubernetes_cluster_role" "namespace_manager" {
  metadata {
    name = "namespace-manager-${local.environment}"
  }

  rule {
    api_groups = [""]
    resources  = ["namespaces"]
    verbs      = ["get", "list", "watch", "create", "patch", "update", "delete"]
  }
}

resource "kubernetes_cluster_role_binding" "pipeline_namespace_manager" {
  count = local.environment == "dev" ? 1 : 0

  metadata {
    name = "pipeline-namespace-manager-binding"
  }

  subject {
    kind      = "Group"
    name      = "grupo-soat-oficina"
    api_group = "rbac.authorization.k8s.io"
  }

  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = kubernetes_cluster_role.namespace_manager.metadata[0].name
  }
}

##################################################################
####################### IAM POLICY FOR PIPELINE ###################
##################################################################

resource "aws_iam_policy" "pipeline_eks_describe" {
  name        = "pipeline-${local.environment}-eks-describe"
  description = "Allow EKS describe operations needed to build kubeconfig"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "eks:DescribeCluster",
          "eks:ListClusters"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_user_policy_attachment" "deploy_user_eks_describe" {
  user       = data.aws_iam_user.deploy_user.user_name
  policy_arn = aws_iam_policy.pipeline_eks_describe.arn
}
