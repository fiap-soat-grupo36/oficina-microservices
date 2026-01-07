region    = "us-east-2"

# RDS Configuration
rds_identifier    = "fiap-rds"
rds_database_name =  "fiapdb-dev"

# AWS Secrets Manager Configuration
# Descomente e configure com os nomes dos seus secrets
# secrets_manager_db_secret_name    = "oficina-mecanica-dev/database"
secrets_manager_jwt_secret_name   = "jwt_secret"
secrets_manager_email_secret_name = "email_credentials"