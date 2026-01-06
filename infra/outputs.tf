# IDs das instâncias do Node Group
output "node_group_instance_ids" {
  description = "IDs das instâncias EC2 do node group do EKS"
  value       = data.aws_instances.node_group_instances.ids
}

output "vpc_link_id" {
  description = "VPC Link ID para usar no API Gateway"
  value       = aws_apigatewayv2_vpc_link.eureka.id
}

output "vpc_link_arn" {
  description = "VPC Link ARN"
  value       = aws_apigatewayv2_vpc_link.eureka.arn
}
