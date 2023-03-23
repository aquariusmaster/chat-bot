# chat-bot

Build:

```bash
./gradlew clean build
```

Deploy to Aws Lambda:
```bash
./gradlew clean deploy
```
Download binaries in zip archive: `build/distributions/chatgpt-bot.zip`

Environment variables:
```
AI_MODEL=gpt-3.5-turbo
OPENAI_API_KEY=sk-xxx
OPENAI_API_URL=https://api.openai.com/v1/chat/completions
ALLOWED_USERS=123456789
BOT_TOKEN=1234567890:xxx
BOT_URL=https://{api-id}.execute-api.{region}.amazonaws.com/{api-path}
BOT_USERNAME=@BotName
JAVA_TOOL_OPTIONS=-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
DYNAMO_TABLE_NAME=chat-history
HISTORY_LENGTH=8
```
