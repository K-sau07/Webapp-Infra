#!/bin/bash
set -e
echo "=== Setting up systemd service ==="
sudo chown csye6225: /tmp/csye6225.service
sudo mv /tmp/csye6225.service /etc/systemd/system/csye6225.service
sudo systemctl daemon-reload
sudo systemctl enable csye6225.service
sudo systemctl start csye6225.service
