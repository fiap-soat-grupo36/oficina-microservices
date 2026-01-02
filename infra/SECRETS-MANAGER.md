# Exemplo de Secrets no AWS Secrets Manager

Este documento mostra o formato JSON esperado para os secrets que serão criados no AWS Secrets Manager.

## 1. Database Credentials

**Nome sugerido**: `oficina-mecanica-dev/database` ou `oficina-mecanica-prod/database`

```json
{
  "username": "postgres",
  "password": "sua-senha-segura-aqui"
}
```

**Criar via AWS CLI**:
```bash
aws secretsmanager create-secret \
  --name oficina-mecanica-dev/database \
  --description "Database credentials for Oficina Mecânica DEV" \
  --secret-string '{"username":"postgres","password":"SuaSenhaSegura123!"}'
```

## 2. JWT Secret

**Nome sugerido**: `oficina-mecanica-dev/jwt` ou `oficina-mecanica-prod/jwt`

```json
{
  "secret": "sua-chave-jwt-minimo-256-bits-para-hs256"
}
```

**Criar via AWS CLI**:
```bash
# Gerar uma chave aleatória segura
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')

aws secretsmanager create-secret \
  --name oficina-mecanica-dev/jwt \
  --description "JWT secret key for Oficina Mecânica DEV" \
  --secret-string "{\"secret\":\"$JWT_SECRET\"}"
```

## 3. Email Credentials

**Nome sugerido**: `oficina-mecanica-dev/email` ou `oficina-mecanica-prod/email`

```json
{
  "host": "smtp.gmail.com",
  "port": "587",
  "username": "seu-email@example.com",
  "password": "sua-senha-app-gmail",
  "from": "oficina-mecanica@example.com"
}
```

**Criar via AWS CLI**:
```bash
aws secretsmanager create-secret \
  --name oficina-mecanica-dev/email \
  --description "Email credentials for Oficina Mecânica DEV" \
  --secret-string '{"host":"smtp.gmail.com","port":"587","username":"seu-email@gmail.com","password":"sua-app-password","from":"oficina@example.com"}'
```

## Configuração no Terraform

Após criar os secrets, descomente as linhas no `environments/dev.tfvars` ou `environments/prod.tfvars`:

```hcl
secrets_manager_db_secret_name    = "oficina-mecanica-dev/database"
secrets_manager_jwt_secret_name   = "oficina-mecanica-dev/jwt"
secrets_manager_email_secret_name = "oficina-mecanica-dev/email"
```

## Verificar Secrets Criados

```bash
# Listar secrets
aws secretsmanager list-secrets

# Ver o valor de um secret
aws secretsmanager get-secret-value --secret-id oficina-mecanica-dev/database
```

## Rotação de Secrets

Para produção, considere habilitar rotação automática:

```bash
aws secretsmanager rotate-secret \
  --secret-id oficina-mecanica-prod/database \
  --rotation-lambda-arn arn:aws:lambda:us-east-2:123456789012:function:SecretsManagerRotation
```
