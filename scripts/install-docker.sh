#!/usr/bin/env bash
set -euo pipefail

if command -v docker >/dev/null 2>&1; then
  echo "Docker already installed: $(docker --version)"
  exit 0
fi

if [ "$(id -u)" -ne 0 ]; then
  SUDO=sudo
else
  SUDO=
fi

if command -v apt-get >/dev/null 2>&1; then
  echo "Installing Docker on Debian/Ubuntu..."
  $SUDO apt-get update
  $SUDO apt-get install -y ca-certificates curl gnupg lsb-release
  mkdir -p /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/$(. /etc/os-release && echo "$ID")/gpg | $SUDO gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/$(. /etc/os-release && echo "$ID") $(lsb_release -cs) stable" | $SUDO tee /etc/apt/sources.list.d/docker.list > /dev/null
  $SUDO apt-get update
  $SUDO apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
elif command -v yum >/dev/null 2>&1 || command -v dnf >/dev/null 2>&1; then
  echo "Installing Docker on RHEL/CentOS/AlmaLinux/Amazon Linux..."
  if command -v amazon-linux-extras >/dev/null 2>&1; then
    $SUDO amazon-linux-extras install docker -y
  else
    $SUDO yum install -y yum-utils
    $SUDO yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    $SUDO yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
  fi
else
  echo "Unsupported OS. Please install Docker manually using https://docs.docker.com/engine/install/." >&2
  exit 1
fi

echo "Enabling and starting Docker service..."
$SUDO systemctl enable docker --now

echo "Docker installation complete."
