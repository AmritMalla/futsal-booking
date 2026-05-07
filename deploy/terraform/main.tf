provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "futsal_arena"
      Environment = "sandbox"
      ManagedBy   = "terraform"
      Owner       = var.owner_tag
    }
  }
}

data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  name = "futsal-sandbox"
  azs  = slice(data.aws_availability_zones.available.names, 0, 2)
}
