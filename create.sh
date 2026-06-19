#!/bin/bash

# Script interativo para criar a estrutura base de um novo parser no Kotatsu

echo "✨ Criador Automático de Parsers Kotatsu ✨"
echo "-----------------------------------------------"

read -p "📝 Nome do Site (ex: Manga Livre): " NAME
if [ -z "$NAME" ]; then echo "❌ Nome inválido"; exit 1; fi

SLUG=$(echo "$NAME" | tr '[:upper:]' '[:lower:]' | tr -d ' ' | tr -d '-')
CLASS_NAME=$(echo "$NAME" | awk '{for(i=1;i<=NF;i++)sub(/./,toupper(substr($i,1,1)),$i)}1' | tr -d ' ')

read -p "📂 ID Enum (Aperte Enter para '${SLUG^^}'): " CUSTOM_ID
ID=${CUSTOM_ID:-${SLUG^^}}

read -p "🌐 Domínio sem https (ex: mangalivre.net): " DOMAIN
if [ -z "$DOMAIN" ]; then echo "❌ Domínio inválido"; exit 1; fi

read -p "🔞 Conteúdo explícito/NSFW? (y/n): " -n 1 -r NSFW_REPLY
echo
if [[ $NSFW_REPLY =~ ^[Yy]$ ]]; then
  NSFW="true"
else
  NSFW="false"
fi

echo "-----------------------------------------------"
echo "🛠️ Criando arquivos no repositório..."

PARSER_DIR="src/main/kotlin/org/koitharu/kotatsu/parsers/site/pt"
PARSER_PATH="$PARSER_DIR/${CLASS_NAME}.kt"

if [ -f "$PARSER_PATH" ]; then
  echo "❌ Erro: O arquivo $PARSER_PATH já existe."
  exit 1
fi

cat > "$PARSER_PATH" <<EOF
package org.koitharu.kotatsu.parsers.site.pt

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaParserSource
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.PagedMangaParser
import org.koitharu.kotatsu.parsers.config.ConfigKey

@MangaSourceParser("$ID", "$NAME", "pt")
internal class ${CLASS_NAME}(context: MangaLoaderContext) :
    PagedMangaParser(context, MangaParserSource.$ID, 30) {

    override val configKeyDomain = ConfigKey.Domain("$DOMAIN")
    override val isNsfw = $NSFW
    
    // TODO: Implementar webClient, parse de lista e capítulos
}
EOF

# Atualiza a aba de Planejamento no Sources.md dos dois repositórios
for REPO in . ../kotatsu-dl; do
  DOC_PATH="$REPO/docs/Sources.md"
  if [ -f "$DOC_PATH" ]; then
    python3 -c "
import sys
name = sys.argv[1]
domain = sys.argv[2]
path = sys.argv[3]
with open(path, 'r') as f:
    lines = f.readlines()
out_lines = []
for i, line in enumerate(lines):
    out_lines.append(line)
    if '## 🟡 Em Planejamento' in line:
        out_lines.append('\n')
        out_lines.append('| ' + name + ' | [' + domain + '](https://' + domain + ') | \`-\` |\n')
with open(path, 'w') as f:
    f.writelines(out_lines)
" "$NAME" "$DOMAIN" "$DOC_PATH"
    echo "✅ $NAME adicionado ao $DOC_PATH (Em Planejamento)"
  fi
done

echo "✅ Arquivo Kotlin gerado: $PARSER_PATH"
echo "🎉 Tudo pronto! Agora é só programar a lógica de extração no Kotlin."
