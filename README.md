# kotatsu-parsers

Esta biblioteca fornece uma coleção de parsers (analisadores) de mangá para acesso conveniente a mangás disponíveis na web. Ela pode ser usada em
aplicações JVM e Android. É um fork do [kotatsu-parsers](https://github.com/KotatsuApp/kotatsu-parsers) da organização [KotatsuApp](https://github.com/KotatsuApp).

![Sources count](https://img.shields.io/badge/dynamic/yaml?url=https%3A%2F%2Fraw.githubusercontent.com%2FYakaTeam%2Fkotatsu-parsers%2Frefs%2Fheads%2Fmaster%2F.github%2Fsummary.yaml&query=total&label=manga%20sources&color=%23E9321C) [![](https://jitpack.io/v/YakaTeam/kotatsu-parsers.svg)](https://jitpack.io/#YakaTeam/kotatsu-parsers) [![Build](https://github.com/YakaTeam/kotatsu-parsers/actions/workflows/test-branch.yml/badge.svg?branch=master)](https://github.com/YakaTeam/kotatsu-parsers/actions/workflows/test-branch.yml) ![License](https://img.shields.io/github/license/YakaTeam/kotatsu-parsers)

## Como Usar

1. Adicione no final dos repositórios do seu `build.gradle` principal:

	```groovy
	allprojects {
 		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 	```

2. Adicione a dependência

	Para projetos Java/Kotlin:

	```groovy
 	dependencies {
 		implementation("com.github.YakaTeam:kotatsu-parsers:$parsers_version")
 	}
 	```

	Para projetos Android:

	```groovy
 	dependencies {
 		implementation("com.github.YakaTeam:kotatsu-parsers:$parsers_version") {
 			exclude group: 'org.json', module: 'json'
 		}
 	}
 	```

	As versões estão disponíveis no [JitPack](https://jitpack.io/#YakaTeam/kotatsu-parsers)
	
	Quando usado em projetos Android, 
	o [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) com
	a [especificação NIO](https://developer.android.com/studio/write/java11-nio-support-table) deve estar habilitado para suportar os recursos do Java 8+.

4. Uso no código

   ```kotlin
   val parser = mangaLoaderContext.newParserInstance(MangaParserSource.MANGADEX)
   ```

   `mangaLoaderContext` é uma implementação da classe `MangaLoaderContext`.
   Veja exemplos 
   de implementação no [Android](https://github.com/KotatsuApp/Kotatsu/blob/devel/app/src/main/kotlin/org/koitharu/kotatsu/core/parser/MangaLoaderContextImpl.kt)
   e [Não-Android](https://github.com/YakaTeam/kotatsu-dl/blob/master/src/main/kotlin/org/koitharu/kotatsu/dl/parsers/MangaLoaderContextImpl.kt).

## Projetos que utilizam a biblioteca

- [Doki](https://github.com/DokiTeam/Doki) (WIP, Referência)
- [Kotatsu](https://github.com/KotatsuApp/Kotatsu) (Morto)
- [Kototoro](https://github.com/skepsun/Kototoro)
- [kotatsu-dl](https://github.com/YakaTeam/kotatsu-dl) (Fork de [KotatsuApp](https://github.com/KotatsuApp/kotatsu-dl))
- [Shirizu](https://github.com/ztimms73/shirizu) (Morto)
- [OtakuWorld](https://github.com/jakepurple13/OtakuWorld) (Mudou para o uso das [Extensões do Tachiyomi](https://github.com/tachiyomiorg/extensions))
- [Usagi](https://github.com/UsagiApp/Usagi) (Apenas para o core)
- [Yumemi](https://github.com/YumemiProject/Yumemi) (Arquivado, Referência)

## Contribuição

Consulte [CONTRIBUTING.md](./CONTRIBUTING.md) para ver as diretrizes.

### Licença

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

<div align="left">

Você pode copiar, distribuir e modificar o software contanto que acompanhe as alterações/datas nos arquivos fonte. Qualquer modificação 
ou software que inclua (via compilador) código licenciado sob a GPL também deve ser disponibilizado sob a GPL junto com as instruções de build e 
instalação. Veja [LICENSE](./LICENSE) para mais detalhes.

</div>

### Aviso Legal

**`¯\_(ツ)_/¯`**

Este repositório foi construído por contribuidores / usuários, o conteúdo interno foi fornecido pela **[Gemini](https://gemini.google.com/)**, mas onde ele está, ninguém sabe. Ninguém sabe como funciona.
