#!/usr/bin/env bash
# Baixa o modelo de embedding (ONNX, ~250 MB) para a pasta modelo/. Rodar UMA vez, com internet.
set -euo pipefail
cd "$(dirname "$0")"
BASE="https://huggingface.co/Qdrant/paraphrase-multilingual-MiniLM-L12-v2-onnx-Q/resolve/main"
mkdir -p modelo
for arquivo in model_optimized.onnx tokenizer.json; do
  if [ -s "modelo/$arquivo" ]; then
    echo "modelo/$arquivo já existe, pulando."
  else
    echo "Baixando $arquivo..."
    curl -fL --progress-bar -o "modelo/$arquivo" "$BASE/$arquivo"
  fi
done
echo "Modelo pronto em modelo/"
