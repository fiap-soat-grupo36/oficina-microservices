##################################################################
##################### API GATEWAY HTTP v2 ########################
##################################################################

# Locals para configuração do API Gateway
locals {
  # Define o host do Ingress baseado no environment
  ingress_host = var.ingress_host != "" ? var.ingress_host : (
    local.environment == "prod" ? "oficina-mecanica.com" : "${local.environment}.oficina-mecanica.com"
  )
}

# Data source para encontrar o NLB criado pelo NGINX Ingress Controller
data "aws_lb" "ingress_nginx" {
  tags = {
    "service.k8s.aws/stack" = "ingress-nginx/ingress-nginx-controller"
  }
}

# Data source para obter o listener HTTP (porta 80) do NLB do Ingress
data "aws_lb_listener" "ingress_nginx_http" {
  load_balancer_arn = data.aws_lb.ingress_nginx.arn
  port              = 80
}

# API Gateway HTTP v2
resource "aws_apigatewayv2_api" "api" {
  name          = "${var.project_name}-http-api-${local.environment}"
  description   = "HTTP API para expor os servicos da Oficina (${local.environment}) via NGINX Ingress"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
    allow_headers = ["*"]
    max_age       = 300
  }

  tags = {
    Name        = "${var.project_name}-http-api-${local.environment}"
    Environment = local.environment
    Project     = var.project_name
  }
}

# Security Group para o VPC Link
resource "aws_security_group" "apigw_vpc_link" {
  name_prefix = "apigw-vpc-link-${local.environment}-"
  description = "Security group para o VPC Link do API Gateway - ${local.environment}"
  vpc_id      = data.aws_vpc.main.id

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "apigw-vpc-link-${local.environment}"
    Environment = local.environment
    Project     = var.project_name
  }
}

# VPC Link para conectar API Gateway ao NLB do NGINX Ingress
resource "aws_apigatewayv2_vpc_link" "api" {
  name               = "${var.project_name}-vpc-link-${local.environment}"
  security_group_ids = [aws_security_group.apigw_vpc_link.id]
  subnet_ids         = data.aws_subnets.private.ids

  tags = {
    Name        = "${var.project_name}-vpc-link-${local.environment}"
    Environment = local.environment
    Project     = var.project_name
  }
}

# Integração do API Gateway com o NLB do NGINX Ingress via VPC Link
resource "aws_apigatewayv2_integration" "proxy" {
  api_id                 = aws_apigatewayv2_api.api.id
  connection_id          = aws_apigatewayv2_vpc_link.api.id
  connection_type        = "VPC_LINK"
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = data.aws_lb_listener.ingress_nginx_http.arn
  payload_format_version = "1.0"
  timeout_milliseconds   = 30000

  # Preserva o path original e sobrescreve o Host header para o NGINX Ingress
  request_parameters = {
    "overwrite:path"        = "$request.path"
    "overwrite:header.Host" = local.ingress_host
  }
}

##################################################################
##################### ROTAS COM PREFIXO ENVIRONMENT ##############
##################################################################

# Rota raiz: /{environment}
resource "aws_apigatewayv2_route" "root_env" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "ANY /${local.environment}"
  target    = "integrations/${aws_apigatewayv2_integration.proxy.id}"
}

# Rota proxy: /{environment}/{proxy+}
# Captura todas as requisições como /dev/auth/..., /prod/customer/..., etc.
resource "aws_apigatewayv2_route" "proxy_env" {
  api_id    = aws_apigatewayv2_api.api.id
  route_key = "ANY /${local.environment}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.proxy.id}"
}

# Stage padrão com auto-deploy e logging
resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "$default"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway.arn
    format = jsonencode({
      requestId        = "$context.requestId"
      ip               = "$context.identity.sourceIp"
      requestTime      = "$context.requestTime"
      httpMethod       = "$context.httpMethod"
      routeKey         = "$context.routeKey"
      status           = "$context.status"
      protocol         = "$context.protocol"
      responseLength   = "$context.responseLength"
      integrationError = "$context.integrationErrorMessage"
    })
  }

  default_route_settings {
    throttling_burst_limit = 5000
    throttling_rate_limit  = 10000
  }

  tags = {
    Name        = "${var.project_name}-default-stage-${local.environment}"
    Environment = local.environment
    Project     = var.project_name
  }
}

# CloudWatch Log Group para API Gateway logs
resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/aws/apigateway/${var.project_name}-http-api-${local.environment}"
  retention_in_days = 7

  tags = {
    Name        = "${var.project_name}-api-gateway-logs-${local.environment}"
    Environment = local.environment
    Project     = var.project_name
  }
}
