variable "build_timestamp" {
  type    = string
  default = "manual-build"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "ami_name" {
  type    = string
  default = "webapp"
}

packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  ami_name      = "${var.ami_name}-${var.build_timestamp}"
  instance_type = "t3.medium"
  region        = var.aws_region

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"]
  }

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }

  ssh_username = "ubuntu"
  ssh_timeout  = "30m"
}

build {
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "file" {
    source      = "target/webapp-1.0.0.jar"
    destination = "/tmp/webapp.jar"
  }

  provisioner "file" {
    source      = "csye6225.service"
    destination = "/tmp/csye6225.service"
  }

  provisioner "shell" {
    script = "update-system.sh"
  }

  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i amazon-cloudwatch-agent.deb",
      "sudo apt-get install -f -y",
      "sudo systemctl enable amazon-cloudwatch-agent"
    ]
  }

  provisioner "shell" {
    script = "java-setup.sh"
  }

  provisioner "shell" {
    inline = [
      "sudo groupadd -r csye6225 || true",
      "sudo useradd -r -g csye6225 -s /usr/sbin/nologin csye6225 || true"
    ]
  }

  provisioner "shell" {
    script = "app-setup.sh"
  }

  provisioner "shell" {
    script = "service-setup.sh"
  }
}
