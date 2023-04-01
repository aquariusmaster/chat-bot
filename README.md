# Telegram ChatGPT bot on AWS Lambda

![Chat bot - AWS](https://user-images.githubusercontent.com/6018601/228943317-8a1ff9b4-b0d5-49e3-9d88-e0ceef84c52b.png)

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

Create AWS resources by CloudFormation template: [cloudformation_template.yaml](aws/cloudformation_template.yaml) or [manually](aws/AWS-SETUP.md)

Register bot:
```bash
curl -X POST "https://api.telegram.org/bot{BOT_TOKEN}/setWebhook?url={BOT_URL}"
```
