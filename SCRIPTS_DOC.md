🇺🇸 English | 🇧🇷 Português

# Ferramentas de Automação do Kotatsu (pt-BR) 🇧🇷

Este diretório contém os scripts oficiais de automação locais para simplificar o desenvolvimento, portabilidade e versionamento de fontes (parsers) para o projeto **Kotatsu**.

---

## 🛠️ Scripts e Ferramentas Locais

Como o projeto do Kotatsu exige que a documentação de fontes (`Sources.md`) seja mantida sincronizada entre os repositórios `kotatsu-parsers` e `kotatsu-dl`, desenvolvemos scripts interativos em Bash para realizar o trabalho braçal de forma automatizada e segura diretamente pelo seu terminal:

### Criar Novo Parser (`./create.sh`)
Um script interativo que gera toda a estrutura inicial para um novo site (fonte) do zero. Ele cria o arquivo Kotlin (`.kt`), preenche as anotações do Kotatsu (ID, Nome e pt-BR) e já insere o novo site automaticamente na lista de **Em Planejamento** (`Sources.md`) nos dois repositórios.

```bash
./create.sh
```

### Promover Parser para Produção (`./promote.sh`)
Quando você terminar de programar a lógica Kotlin do parser e validar o funcionamento, utilize este script para "promover" a extensão. 

Ele remove o site da tabela de Planejamento e o insere automaticamente em ordem alfabética na tabela de **Ativas** (nos dois repositórios). Em seguida, ele abre um diálogo para adicionar as mudanças no Git (`git add`), comitar (`git commit`) e enviar (`git push`) para as suas respectivas branches customizadas.

```bash
./promote.sh "Nome do Site" "NomeDaClasse.kt"
# Exemplo: ./promote.sh "Nexus Toons" "NexusToons.kt"
```

---

## 🏗️ Fluxo de Trabalho (Workflow)
1. Rode `./create.sh` e responda às perguntas no terminal.
2. Abra a pasta `kotatsu-parsers/src/...` e codifique a lógica do site no arquivo recém-criado.
3. Teste localmente executando o `./gradlew shadowJar` no `kotatsu-dl` para validar se o mangá baixa com sucesso.
4. Rode `./promote.sh` informando o nome oficial do site para promover a documentação e realizar o *Push* para a nuvem de uma só vez!
