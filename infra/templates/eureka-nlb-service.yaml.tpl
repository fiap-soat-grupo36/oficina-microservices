---
# Service interno para comunicação dentro do cluster
apiVersion: v1
kind: Service
metadata:
  name: eureka-server-internal
  namespace: ${namespace}
  labels:
    app: eureka-server
    managed-by: terraform
spec:
  type: ClusterIP
  selector:
    app: eureka-server
  ports:
    - name: http
      port: 8761
      targetPort: 8761
---
# Service externo usando Target Group EXISTENTE do NLB
# Gerenciado pelo Terraform - ARN injetado automaticamente
apiVersion: v1
kind: Service
metadata:
  name: eureka-server-nlb
  namespace: ${namespace}
  labels:
    app: eureka-server
    managed-by: terraform
  annotations:
    # ARN do Target Group existente (injetado pelo Terraform)
    service.beta.kubernetes.io/aws-load-balancer-target-group-arn: "${target_group_arn}"
    
    # Tipo de target (IP mode para pods direto)
    service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: "ip"
    
    # Metadata do NLB existente
    terraform.io/nlb-dns: "${nlb_dns}"
spec:
  type: LoadBalancer
  selector:
    app: eureka-server
  ports:
    - name: http
      port: 8761
      targetPort: 8761
      protocol: TCP
