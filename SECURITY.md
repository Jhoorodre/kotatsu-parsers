# Política de Segurança

## Versões Suportadas

Apenas a branch `master` recebe correções de segurança. Forks downstream que 
republicam uma versão mais antiga devem aplicar a correção por conta própria.

## Reportando uma Vulnerabilidade

Por favor, **não** abra uma issue pública para problemas de segurança.

Use o sistema de relatórios de vulnerabilidades privados do GitHub:

1. Vá para <https://github.com/YakaTeam/kotatsu-parsers/security/advisories/new>
2. Preencha o que você encontrou, forneça um caso mínimo de reprodução e a versão afetada
   (commit SHA ou tag de release do JitPack).
3. Envie.

Apenas os mantenedores do repositório podem ver o relatório.

### O que está no escopo

- Caminhos de execução de código ou fuga de sandbox na própria biblioteca do parser (por
  exemplo: um parser que escreve arquivos arbitrários, aciona reflexão em
  nomes de classes controlados pelo atacante ou avalia scripts não confiáveis no processo).
- Requisições feitas a hosts indesejados devido à manipulação de URLs em utilitários
  compartilhados (`org.koitharu.kotatsu.parsers.util`, `network`, `core`).
- Vazamento de credenciais / tokens por meio de logs, exceções ou `toString()`.
- Dependências de terceiros com vulnerabilidades conhecidas enviadas por esta biblioteca.

### O que está fora do escopo

- O comportamento dos sites de mangá remotos alvo dos parsers. Isso é responsabilidade
  do site, não nossa.
- Falta de TLS ou cifras fracas em um site analisado (parser).
- Solicitações para burlar CAPTCHA / Cloudflare. Os parsers passam por um cliente HTTP normal 
  e respeitam as proteções do site; marcar um parser como `@Broken`
  quando um site é bloqueado é o comportamento esperado.
- Forks instalados pelo usuário, aplicativos de terceiros que incorporam a biblioteca ou
  modificações feitas fora deste repositório.

## Expectativas de Resposta

Esta é uma biblioteca de código aberto mantida por voluntários. Um cronograma realista:

- Confirmação do recebimento do relatório em alguns dias.
- Triagem e uma leitura aproximada da gravidade em duas semanas.
- Correção mesclada na `master` antes que o aviso seja divulgado publicamente, onde
  for viável.

Se um problema relatado for fundamentalmente um problema de site remoto ou de um aplicativo
downstream, informaremos isso e fecharemos o aviso com uma explicação
em vez de descartá-lo silenciosamente.
