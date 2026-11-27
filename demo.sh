#!/usr/bin/env bash
# Roteiro da demonstração ao vivo (5 minutos). Pressione ENTER para avançar cada passo.
set -euo pipefail
cd "$(dirname "$0")"
[ -f target/qdrant-lab.jar ] || mvn -q -B package
java -jar target/qdrant-lab.jar demo
