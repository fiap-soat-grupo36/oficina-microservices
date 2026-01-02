# Outputs do NLB e Target Group Existentes

# ARN do Target Group criado pelo Terraform (usado pelo Kubernetes Service)
output "nlb_target_group_arn" {
  description = "ARN do Target Group do NLB para uso no Kubernetes"
  value       = aws_lb_target_group.eureka.arn
}

# DNS do NLB criado pelo Terraform
output "nlb_dns_name" {
  description = "DNS do Network Load Balancer"
  value       = aws_lb.eureka.dns_name
}

# ARN do NLB criado pelo Terraform
output "nlb_arn" {
  description = "ARN do Network Load Balancer"
  value       = aws_lb.eureka.arn
}

# IDs das instâncias do Node Group (para registro manual se necessário)
output "node_group_instance_ids" {
  description = "IDs das instâncias EC2 do node group do EKS"
  value       = data.aws_instances.node_group_instances.ids
}

output "created_nlb_arn" {
  description = "ARN do NLB criado pelo Terraform"
  value       = aws_lb.eureka.arn
}

output "created_nlb_dns" {
  description = "DNS do NLB criado pelo Terraform"
  value       = aws_lb.eureka.dns_name
}

output "created_target_group_arn" {
  description = "ARN do Target Group criado pelo Terraform"
  value       = aws_lb_target_group.eureka.arn
}

output "vpc_link_id" {
  description = "VPC Link ID para usar no API Gateway"
  value       = aws_apigatewayv2_vpc_link.eureka.id
}

output "vpc_link_arn" {
  description = "VPC Link ARN"
  value       = aws_apigatewayv2_vpc_link.eureka.arn
}
