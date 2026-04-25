resource "aws_secretsmanager_secret" "db" {
  name                    = "/futsal/sandbox/db"
  description             = "PostgreSQL credentials for the sandbox cluster"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "jwt" {
  name                    = "/futsal/sandbox/jwt"
  description             = "JWT signing secret for the sandbox backend"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "smtp" {
  name                    = "/futsal/sandbox/smtp"
  description             = "Optional SMTP credentials for the sandbox backend"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret" "grafana" {
  name                    = "/futsal/sandbox/grafana"
  description             = "Grafana admin credentials"
  recovery_window_in_days = 0
}
