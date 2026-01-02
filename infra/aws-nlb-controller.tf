##################################################################
################ AWS LOAD BALANCER CONTROLLER ####################
##################################################################

# Service Account para o AWS Load Balancer Controller
resource "kubernetes_service_account" "aws_load_balancer_controller" {
  count = terraform.workspace != "default" ? 1 : 0

  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    labels = {
      "app.kubernetes.io/name"      = "aws-load-balancer-controller"
      "app.kubernetes.io/component" = "controller"
    }
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.aws_load_balancer_controller[0].arn
    }
  }

  depends_on = [data.aws_eks_cluster.oficina]
}

# IAM Role para o Load Balancer Controller
resource "aws_iam_role" "aws_load_balancer_controller" {
  count = terraform.workspace != "default" ? 1 : 0

  name = "eks-aws-load-balancer-controller-${local.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = data.aws_iam_openid_connect_provider.eks[0].arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${replace(data.aws_iam_openid_connect_provider.eks[0].url, "https://", "")}:sub" = "system:serviceaccount:kube-system:aws-load-balancer-controller"
            "${replace(data.aws_iam_openid_connect_provider.eks[0].url, "https://", "")}:aud" = "sts.amazonaws.com"
          }
        }
      }
    ]
  })

  tags = {
    Environment = local.environment
    Project     = local.project_name
  }
}

# Attach IAM Policy para o Load Balancer Controller
resource "aws_iam_role_policy_attachment" "aws_load_balancer_controller" {
  count = terraform.workspace != "default" ? 1 : 0

  policy_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:policy/AWSLoadBalancerControllerIAMPolicy"
  role       = aws_iam_role.aws_load_balancer_controller[0].name
}

# Helm Release para o AWS Load Balancer Controller
resource "helm_release" "aws_load_balancer_controller" {
  count = terraform.workspace != "default" ? 1 : 0

  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.8.1" # Verificar última versão em: https://github.com/aws/eks-charts

  set {
    name  = "clusterName"
    value = data.aws_eks_cluster.oficina.name
  }

  set {
    name  = "serviceAccount.create"
    value = "false"
  }

  set {
    name  = "serviceAccount.name"
    value = kubernetes_service_account.aws_load_balancer_controller[0].metadata[0].name
  }

  set {
    name  = "region"
    value = var.region
  }

  set {
    name  = "vpcId"
    value = data.aws_vpc.main.id
  }

  # Habilitar logs para troubleshooting
  set {
    name  = "enableShield"
    value = "false"
  }

  set {
    name  = "enableWaf"
    value = "false"
  }

  set {
    name  = "enableWafv2"
    value = "false"
  }

  depends_on = [
    kubernetes_service_account.aws_load_balancer_controller,
    aws_iam_role_policy_attachment.aws_load_balancer_controller
  ]
}

