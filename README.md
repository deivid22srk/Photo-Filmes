# Telegram Video Organizer

Um aplicativo Android moderno construído com Kotlin e Material You (Material Design 3) que se conecta ao Telegram para organizar vídeos por séries, temporadas e episódios.

## 🌟 Características

- **Material You Design**: Interface moderna seguindo as diretrizes do Material Design 3
- **Integração com Telegram**: Conecta-se ao Telegram Bot API para buscar vídeos
- **Organização Automática**: Organiza vídeos automaticamente por séries/temporadas/episódios
- **Tema Dinâmico**: Suporte completo para Material You com cores dinâmicas do sistema
- **Modo Escuro**: Suporte nativo para tema claro e escuro

## 📱 Screenshots

O app possui três telas principais:
- **Home**: Lista de séries com seus respectivos episódios
- **Detalhes da Série**: Visualização de temporadas e episódios específicos
- **Configurações**: Configuração do token do bot do Telegram

## 🚀 Como Usar

### Configurando o Bot do Telegram

1. Abra o Telegram e procure por `@BotFather`
2. Envie o comando `/newbot`
3. Siga as instruções para criar seu bot
4. Copie o token fornecido
5. No app, vá em Configurações e cole o token

### Organizando Vídeos

Para que os vídeos sejam organizados automaticamente, envie-os para seu bot com o seguinte formato no nome ou legenda:

```
NomeDaSerie S01E01 - Nome do Episódio
```

Onde:
- `S01` = Temporada 1
- `E01` = Episódio 1

Exemplos:
- `Breaking Bad S01E01 - Pilot`
- `Game of Thrones S05E09 - The Dance of Dragons`
- `The Office S02E01`

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem de programação
- **Jetpack Compose**: Framework de UI declarativa
- **Material Design 3**: Sistema de design
- **Retrofit**: Cliente HTTP para integração com API
- **Coroutines**: Programação assíncrona
- **Flow**: Streams de dados reativos
- **DataStore**: Armazenamento de preferências
- **Navigation Compose**: Navegação entre telas
- **Coil**: Carregamento de imagens

## 📦 Dependências Principais

```kotlin
androidx.compose.material3:material3:1.2.0
com.squareup.retrofit2:retrofit:2.9.0
androidx.navigation:navigation-compose:2.7.6
androidx.datastore:datastore-preferences:1.0.0
io.coil-kt:coil-compose:2.5.0
```

## 🏗️ Estrutura do Projeto

```
app/
├── data/
│   ├── api/           # Integração com Telegram API
│   ├── model/         # Modelos de dados
│   ├── preferences/   # Gerenciamento de preferências
│   └── repository/    # Camada de repositório
├── ui/
│   ├── navigation/    # Navegação do app
│   ├── screens/       # Telas do aplicativo
│   ├── theme/         # Temas e estilos
│   └── viewmodel/     # ViewModels
└── MainActivity.kt
```

## 🔨 Build

O projeto usa Gradle e pode ser compilado com:

```bash
./gradlew assembleDebug
```

### GitHub Actions

O projeto inclui um workflow de CI/CD que:
- Compila automaticamente o APK em cada push/PR
- Faz upload do APK como artefato
- Usa Java 17 e cache do Gradle para builds mais rápidos

## 📄 Licença

Este projeto é de código aberto e está disponível sob a licença MIT.

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.

## 📞 Suporte

Se você encontrar algum problema ou tiver sugestões, por favor abra uma issue no GitHub.

---

Desenvolvido com ❤️ usando Material You
