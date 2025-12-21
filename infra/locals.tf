locals {
  workspace = terraform.workspace
  environment = terraform.workspace == "default" ? "dev" : terraform.workspace
}