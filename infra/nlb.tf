##################################################################
##################### NLB POR AMBIENTE ###########################
##################################################################

# Este arquivo cria NLBs e Target Groups específicos por ambiente
# Cada workspace (dev/prod) terá seu próprio NLB e Target Group

# NLB por Ambiente
resource "aws_lb" "eureka" {
  name = "oficina-mecanica-${local.environment}"
  # Internal: true = Privado (apenas VPC), false = Publico (internet)
  internal           = false
  load_balancer_type = "network"
  # Usa apenas subnets únicas por AZ (NLB não aceita múltiplas subnets na mesma AZ)
  subnets            = local.nlb_subnets

  enable_deletion_protection       = local.environment == "prod" ? true : false
  enable_cross_zone_load_balancing = true

  tags = {
    Name        = "oficina-mecanica-${local.environment}"
    Environment = local.environment
    Project     = local.project_name
    Service     = "eureka-server"
  }
}

# Target Group por Ambiente
resource "aws_lb_target_group" "eureka" {
  name        = "oficina-mecanica-tg-${local.environment}"
  port        = 8761
  protocol    = "TCP"
  target_type = "ip"
  vpc_id      = data.aws_vpc.main.id

  health_check {
    enabled             = true
    protocol            = "HTTP"
    path                = "/actuator/health"
    port                = "8761"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    interval            = 10
  }

  tags = {
    Name        = "oficina-mecanica-tg-${local.environment}"
    Environment = local.environment
    Project     = local.project_name
    Service     = "eureka-server"
  }
}

# Listener do NLB
resource "aws_lb_listener" "eureka" {
  load_balancer_arn = aws_lb.eureka.arn
  port              = "8761"
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.eureka.arn
  }
}
