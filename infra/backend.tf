terraform {
  backend "s3" {
    bucket = "projeto-oficina-terraform"
    key    = "services/terraform.tfstate"
    region = "us-east-2"
  }
}
