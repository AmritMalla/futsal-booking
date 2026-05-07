variable "region" {
  description = "AWS region for the sandbox deployment."
  type        = string
  default     = "us-east-1"
}

variable "cluster_version" {
  description = "EKS control plane version."
  type        = string
  default     = "1.30"
}

variable "node_instance_type" {
  description = "EC2 instance type for the managed node group."
  type        = string
  default     = "t3.medium"
}

variable "node_desired_size" {
  description = "Desired number of worker nodes."
  type        = number
  default     = 2
}

variable "owner_tag" {
  description = "Human-readable owner tag for created resources."
  type        = string
  default     = "amt"
}

variable "letsencrypt_email" {
  description = "Email address used for Let's Encrypt account registration. Read by bootstrap.sh, not by Terraform directly."
  type        = string
}
