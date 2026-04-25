output "region" {
  value = var.region
}

output "cluster_name" {
  value = module.eks.cluster_name
}

output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "oidc_provider" {
  value = module.eks.oidc_provider
}

output "oidc_provider_arn" {
  value = module.eks.oidc_provider_arn
}

output "ecr_registry" {
  value = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
}

output "ecr_backend_url" {
  value = aws_ecr_repository.this["futsal-backend"].repository_url
}

output "ecr_frontend_url" {
  value = aws_ecr_repository.this["futsal-frontend"].repository_url
}

output "secret_arn_db" {
  value = aws_secretsmanager_secret.db.arn
}

output "secret_arn_jwt" {
  value = aws_secretsmanager_secret.jwt.arn
}

output "secret_arn_smtp" {
  value = aws_secretsmanager_secret.smtp.arn
}

output "secret_arn_grafana" {
  value = aws_secretsmanager_secret.grafana.arn
}

output "secret_name_db" { value = aws_secretsmanager_secret.db.name }
output "secret_name_jwt" { value = aws_secretsmanager_secret.jwt.name }
output "secret_name_smtp" { value = aws_secretsmanager_secret.smtp.name }
output "secret_name_grafana" { value = aws_secretsmanager_secret.grafana.name }

output "eso_role_arn" {
  value = aws_iam_role.eso.arn
}
