data "aws_eks_cluster" "oficina" {
  name = "eks-fiap-oficina-mecanica"
}

data "aws_iam_user" "deploy_user" {
  user_name = "terraform-user"
}

data "aws_eks_cluster_auth" "oficina" {
  name = data.aws_eks_cluster.oficina.name
}

data "aws_vpc" "main" {
  filter {
    name   = "tag:Name"
    values = ["fiap-oficina-mecanica"]
  }
}

data "aws_lb" "nlb" {
  name = "oficina-mecanica"
}

data "aws_instances" "node_group_instances" {
  filter {
    name   = "tag:eks:cluster-name"
    values = ["eks-fiap-oficina-mecanica"]
  }
}

data "aws_lb_target_group" "nlb_tg" {
  name = "oficina-mecanica-tg"
}