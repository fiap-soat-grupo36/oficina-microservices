locals {
  workspace    = terraform.workspace
  environment  = terraform.workspace == "default" ? "dev" : terraform.workspace
  project_name = "oficina-mecanica"
}