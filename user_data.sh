#!/bin/bash
set -e
exec > /var/log/user_data.log 2>&1

sudo apt-get update
sudo apt-get install -y awscli

DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id db_password_new \
  --query SecretString \
  --output text \
  --region "${aws_region}")

cat <<EOF > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
{
  "metrics": {
    "metrics_collected": {
      "statsd": {
        "service_address": ":8125",
        "metrics_collection_interval": 60,
        "metrics_aggregation_interval": 60
      }
    }
  },
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/log/webapp.log",
            "log_group_name": "webapp-logs",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
EOF

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s

sudo touch /var/log/webapp.log
sudo chown csye6225:csye6225 /var/log/webapp.log
sudo chmod 644 /var/log/webapp.log

cat <<EOF > /opt/app/.env
DATABASE_URL=jdbc:postgresql://${rds_endpoint}/${db_name}?user=${db_username}&password=$${DB_PASSWORD}
AWS_REGION=${aws_region}
AWS_S3_BUCKET=${s3_bucket_name}
PORT=8080
EOF

sudo chown csye6225:csye6225 /opt/app/.env
sudo chmod 600 /opt/app/.env

sudo systemctl enable csye6225.service
sudo systemctl start csye6225.service
sudo systemctl restart csye6225.service
