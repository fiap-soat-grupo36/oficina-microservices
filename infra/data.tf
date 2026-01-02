data "aws_eks_cluster" "oficina" {
  name = "eks-fiap-oficina-mecanica"
}

data "aws_caller_identity" "current" {}

data "aws_iam_user" "deploy_user" {
  user_name = "terraform-user"
}

data "aws_eks_cluster_auth" "oficina" {
  name = data.aws_eks_cluster.oficina.name
}

# Busca o OIDC Provider do cluster EKS
data "aws_iam_openid_connect_provider" "eks" {
  count = terraform.workspace != "default" ? 1 : 0
  # Extrai a URL do OIDC issuer do cluster
  url = data.aws_eks_cluster.oficina.identity[0].oidc[0].issuer
}

data "aws_vpc" "main" {
  filter {
    name   = "tag:Name"
    values = ["fiap-oficina-mecanica"]
  }
}
# Busca todas as subnets privadas da VPC
data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }

  filter {
    name   = "tag:kubernetes.io/role/internal-elb"
    values = ["1"]
  }
}

# Busca informa\u00e7\u00f5es detalhadas de cada subnet para filtrar por AZ
data "aws_subnet" "private" {
  for_each = toset(data.aws_subnets.private.ids)
  id       = each.value
}
# NLB e Target Group agora são CRIADOS pelo Terraform
# Ver arquivo: nlb-per-environment.tf

data "aws_instances" "node_group_instances" {
  filter {
    name   = "tag:eks:cluster-name"
    values = ["eks-fiap-oficina-mecanica"]
  }
}

# Busca o cluster RDS já criado
data "aws_rds_cluster" "cluster" {
  cluster_identifier = var.rds_identifier
}

# Recupera o secret com a senha do master user
data "aws_secretsmanager_secret" "db_password" {
  arn = data.aws_rds_cluster.cluster.master_user_secret[0].secret_arn
}

data "aws_secretsmanager_secret_version" "db_password" {
  secret_id = data.aws_secretsmanager_secret.db_password.id
}

data "aws_secretsmanager_secret" "jwt_secret" {
  count = var.secrets_manager_jwt_secret_name != "" ? 1 : 0
  name  = var.secrets_manager_jwt_secret_name
}

data "aws_secretsmanager_secret_version" "jwt_secret" {
  count     = var.secrets_manager_jwt_secret_name != "" ? 1 : 0
  secret_id = data.aws_secretsmanager_secret.jwt_secret[0].id
}

data "aws_secretsmanager_secret" "email_credentials" {
  count = var.secrets_manager_email_secret_name != "" ? 1 : 0
  name  = var.secrets_manager_email_secret_name
}

data "aws_secretsmanager_secret_version" "email_credentials" {
  count     = var.secrets_manager_email_secret_name != "" ? 1 : 0
  secret_id = data.aws_secretsmanager_secret.email_credentials[0].id
}