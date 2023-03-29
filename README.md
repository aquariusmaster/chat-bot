# Telegram ChatGPT bot on AWS Lambda

![Diagram](https://user-images.githubusercontent.com/6018601/228653094-b00e9251-b529-4ad9-82f4-ccd6d1d54e93.png)

Build:

```bash
./gradlew clean build
```

Deploy to AWS Lambda:
```bash
./gradlew deploy
```
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
SESSION_MAX_LIFETIME=60
```
Create a Telegram bot:
https://core.telegram.org/bots/features#botfather

Create AWS resources: [AWS Lambda](aws/AWS-SETUP.md)

Register bot:
```bash
curl -X POST "https://api.telegram.org/bot{BOT_TOKEN}/setWebhook?url={BOT_URL}"
```
