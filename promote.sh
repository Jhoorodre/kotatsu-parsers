#!/bin/bash

# Script para promover um parser de "Em Planejamento" para "Ativas", commitar e subir

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "❌ Erro: Faltaram argumentos."
  echo "📖 Uso: ./promote.sh \"Nome do Site\" \"NomeDaClasse.kt\""
  echo "Exemplo: ./promote.sh \"Nexus Toons\" \"NexusToons.kt\""
  exit 1
fi

NAME=$1
CLASS_FILE=$2

echo "✨ Promovendo '$NAME' de Planejamento para Ativas ✨"

for REPO in . ../kotatsu-dl; do
  DOC_PATH="$REPO/docs/Sources.md"
  if [ ! -f "$DOC_PATH" ]; then continue; fi

  python3 -c "
import sys
name = sys.argv[1]
class_file = sys.argv[2]
path = sys.argv[3]
with open(path, 'r') as f:
    lines = f.readlines()
out_lines = []
target_line = ''
for line in lines:
    if '| ' + name + ' |' in line and '\`-\`' in line:
        target_line = line
    else:
        out_lines.append(line)
if target_line:
    parts = target_line.split('|')
    domain_part = parts[2]
    if 'kotatsu-dl' in path:
        new_line = f'| {name} |{domain_part}|\n'
    else:
        new_line = f'| {name} |{domain_part}| [\`{class_file}\`](../src/main/kotlin/org/koitharu/kotatsu/parsers/site/pt/{class_file}) |\n'
    ativas_idx = -1
    for i, l in enumerate(out_lines):
        if '## 🟢 Ativas' in l:
            ativas_idx = i
            break
    if ativas_idx != -1:
        insert_idx = ativas_idx + 4
        while insert_idx < len(out_lines) and out_lines[insert_idx].startswith('|'):
            row_name = out_lines[insert_idx].split('|')[1].strip().lower()
            if row_name > name.lower():
                break
            insert_idx += 1
        out_lines.insert(insert_idx, new_line)
with open(path, 'w') as f:
    f.writelines(out_lines)
" "$NAME" "$CLASS_FILE" "$DOC_PATH"

  echo "✅ Atualizado: $DOC_PATH"
done

echo "-----------------------------------------------"
read -p "Deseja commitar e enviar (push) essas alterações para o GitHub agora? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  
  # Commita no Parsers (diretorio atual)
  git add "src/main/kotlin/org/koitharu/kotatsu/parsers/site/pt/$CLASS_FILE"
  git add docs/Sources.md
  git commit -m "feat: Add $NAME parser port e promover para ativos"
  BRANCH_PARSERS=$(git rev-parse --abbrev-ref HEAD)
  git push origin "$BRANCH_PARSERS"
  echo "🚀 Push concluído no kotatsu-parsers!"
  
  # Commita no DL
  cd ../kotatsu-dl
  git add docs/Sources.md
  git commit -m "docs: Promover $NAME para lista de ativos"
  BRANCH_DL=$(git rev-parse --abbrev-ref HEAD)
  git push origin "$BRANCH_DL"
  echo "🚀 Push concluído no kotatsu-dl!"
  
  cd ../kotatsu-parsers
  echo "🎉 Promoção concluída com sucesso em ambos os repositórios!"
else
  echo "Ok, alterações salvas apenas nos arquivos locais."
fi
