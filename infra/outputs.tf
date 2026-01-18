# Outputs do API Gateway e Infraestrutura

# VPC Link
output "vpc_link_id" {
  description = "VPC Link ID para usar no API Gateway"
  value       = aws_apigatewayv2_vpc_link.api.id
}

output "vpc_link_arn" {
  description = "VPC Link ARN"
  value       = aws_apigatewayv2_vpc_link.api.arn
}

# Environment
output "environment" {
  description = "Environment atual (dev, prod, etc)"
  value       = local.environment
}

# API Gateway Outputs
output "api_gateway_id" {
  description = "ID do API Gateway HTTP API"
  value       = aws_apigatewayv2_api.api.id
}

output "api_gateway_endpoint" {
  description = "Endpoint do API Gateway"
  value       = aws_apigatewayv2_api.api.api_endpoint
}

output "api_gateway_invoke_url" {
  description = "URL de invocação do API Gateway"
  value       = aws_apigatewayv2_stage.default.invoke_url
}

output "api_gateway_url_with_env" {
  description = "URL completa do API Gateway com prefixo do environment"
  value       = "${aws_apigatewayv2_stage.default.invoke_url}/${local.environment}"
}
