#!/usr/bin/env bash
# Build both WARs (rva + jwt-tool) and copy them into ./wars/.
# Runs BEFORE `docker compose up --build`.

set -euo pipefail
cd "$(dirname "$0")"

HERE="$(pwd)"
RVA_DIR="$HERE/../app"
TOOL_DIR="$HERE/../attack"

mkdir -p wars

echo "==> Build rva (app/) ..."
(cd "$RVA_DIR" && mvn -q clean package -DskipTests)
RVA_WAR=$(ls "$RVA_DIR/target/"*.war | head -n 1)
cp "$RVA_WAR" "$HERE/wars/rva.war"
echo "   $RVA_WAR -> wars/rva.war"

echo "==> Build jwt-tool (attack/) ..."
(cd "$TOOL_DIR" && mvn -q clean package -DskipTests)
cp "$TOOL_DIR/target/jwt-tool.war" "$HERE/wars/jwt-tool.war"
echo "   target/jwt-tool.war -> wars/jwt-tool.war"

echo ""
echo "==> Done. Run:"
echo "   docker compose up --build"
