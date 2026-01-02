##################################################################
##################### VPC LINK PARA API GATEWAY ##################
##################################################################

resource "aws_security_group" "vpc_link" {
  name_prefix = "vpc-link-${local.environment}-"
  description = "Security Group for VPC Link to access internal NLB"
  vpc_id      = data.aws_vpc.main.id

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "vpc-link-${local.environment}"
    Environment = local.environment
    Project     = local.project_name
  }
}

# VPC Link para conectar API Gateway ao NLB interno
resource "aws_apigatewayv2_vpc_link" "eureka" {
  name               = "eureka-vpc-link-${local.environment}"
  security_group_ids = [aws_security_group.vpc_link.id]
  subnet_ids         = data.aws_eks_cluster.oficina.vpc_config[0].subnet_ids

  tags = {
    Name        = "eureka-vpc-link-${local.environment}"
    Environment = local.environment
    Project     = local.project_name
    Service     = "eureka-server"
  }
}