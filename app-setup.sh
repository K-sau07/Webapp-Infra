#!/bin/bash
set -e

echo "=== Setting up the application ==="

sudo mkdir -p /opt/app
sudo cp /tmp/webapp.jar /opt/app/webapp.jar

sudo mkdir -p /home/csye6225
sudo chown -R csye6225:csye6225 /home/csye6225
sudo chown -R csye6225:csye6225 /opt/app

sudo touch /var/log/webapp.log
sudo chown csye6225:csye6225 /var/log/webapp.log

echo "=== Application setup complete ==="
