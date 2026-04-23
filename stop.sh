#!/bin/bash

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESOURCE_DIR="$ROOT_DIR/resource-serv"
REGISTRATION_DIR="$ROOT_DIR/registration-serv"

kill_by_pid() {
  local name="$1"
  local pid_file="$2"

  if [ -f "$pid_file" ]; then
    local pid
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      echo "==> Stopping $name (PID $pid)..."
      kill "$pid"
      rm -f "$pid_file"
    else
      echo "    $name is not running (stale PID $pid), removing PID file."
      rm -f "$pid_file"
    fi
  else
    echo "    No PID file for $name, skipping."
  fi
}

kill_by_pid "resource-serv" "$RESOURCE_DIR/resource.pid"
kill_by_pid "registration-serv" "$REGISTRATION_DIR/registration.pid"

echo "==> Stopping PostgreSQL..."
docker compose -f "$RESOURCE_DIR/docker-compose.yml" down

echo ""
echo "All services stopped."
