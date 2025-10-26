#!/usr/bin/env bash
set -euo pipefail

cd /home/ubuntu/app

echo ">>> docker compose down"
docker compose down || true

echo ">>> docker compose build & up"
docker compose up -d --build

echo ">>> status"
docker compose ps

# 디스크 여유 확보
docker image prune -af || true
