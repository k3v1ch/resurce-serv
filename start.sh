#!/bin/bash

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESOURCE_DIR="$ROOT_DIR/resource-serv"
REGISTRATION_DIR="$ROOT_DIR/registration-serv"

echo "==> Starting PostgreSQL..."
docker compose -f "$RESOURCE_DIR/docker-compose.yml" up -d

echo "==> Waiting for PostgreSQL to be ready..."
until docker exec resource-serv pg_isready -U postgres -q 2>/dev/null; do
  sleep 1
done
echo "    PostgreSQL is ready."

echo "==> Building resource-serv..."
cd "$RESOURCE_DIR"
mvn -q package -DskipTests

echo "==> Starting resource-serv on port 8081..."
nohup java -jar "$RESOURCE_DIR/target/resource-serv-0.0.1-SNAPSHOT.jar" \
  > "$RESOURCE_DIR/resource.log" 2>&1 &
echo $! > "$RESOURCE_DIR/resource.pid"
echo "    PID: $(cat "$RESOURCE_DIR/resource.pid")"

echo "==> Building registration-serv..."
cd "$REGISTRATION_DIR"
mvn -q package -DskipTests

echo "==> Starting registration-serv on port 8082..."
nohup java -jar "$REGISTRATION_DIR/target/registration-serv-0.0.1-SNAPSHOT.jar" \
  > "$REGISTRATION_DIR/registration.log" 2>&1 &
echo $! > "$REGISTRATION_DIR/registration.pid"
echo "    PID: $(cat "$REGISTRATION_DIR/registration.pid")"

echo ""
echo "Services started:"
echo "  PostgreSQL        -> localhost:5432"
echo "  resource-serv     -> http://localhost:8081"
echo "  registration-serv -> http://localhost:8082"
echo ""
echo "Logs:"
echo "  $RESOURCE_DIR/resource.log"
echo "  $REGISTRATION_DIR/registration.log"
