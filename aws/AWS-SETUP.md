!!! THIS IS AN EXPERIMENTAL CONFIG
### Create DynamoDB table
```bash
aws dynamodb create-table \
    --table-name chat-history \
    --attribute-definitions AttributeName=chat_id,AttributeType=N \
    --key-schema AttributeName=chat_id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
```
### Create a role
1. Create a trust policy for the Lambda service in a file named lambda-trust-policy.json:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```
2. Create the IAM role using the AWS CLI and the trust policy:
```bash
aws iam create-role --role-name lambda-cloudwatch-dynamodb-role --assume-role-policy-document file://aws/lambda-trust-policy.json
```
Note the Arn of the newly created role in the output. You'll need it later when creating the Lambda function.

3. Create a custom inline policy with the specified CloudWatch and DynamoDB permissions. Save the policy in a file named custom-policy.json:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem"
      ],
      "Resource": "arn:aws:dynamodb:*:*:table/chat-history"
    }
  ]
}
```
4. dd the custom inline policy to the IAM role using the AWS CLI:
```bash
aws iam put-role-policy --role-name lambda-cloudwatch-dynamodb-role --policy-name CustomCloudWatchDynamoDBPolicy --policy-document file://aws/custom-policy.json
```
### Create an API Gateway:
1. Create a REST API:
```bash
aws apigateway create-rest-api --name chatgpt-bot-api
```
Note the id of the newly created API in the output. You'll need it later when creating the resource and method.
2. Get the root resource ID:
```bash
aws apigateway get-resources --rest-api-id YOUR_API_ID
```
Replace YOUR_API_ID with the API ID you obtained in the previous step. In the response, look for the id value of the root resource ("/"). This is your root resource ID (YOUR_ROOT_RESOURCE_ID).

3. Create a resource in the API Gateway:
```bash
aws apigateway create-resource --rest-api-id YOUR_API_ID --parent-id YOUR_ROOT_RESOURCE_ID --path-part my-function
```
Replace YOUR_API_ID and YOUR_ROOT_RESOURCE_ID with the values you obtained earlier. Take note of the id value in the response. This is your new resource ID (YOUR_RESOURCE_ID).

4. Create a method (e.g., POST) for the new resource::
```bash
aws apigateway put-method --rest-api-id YOUR_API_ID --resource-id YOUR_RESOURCE_ID --http-method POST --authorization-type NONE
```
Replace YOUR_API_ID with the API id and YOUR_RESOURCE_ID with the resource id you obtained previously.

### Create the Lambda function using the AWS CLI:
1. Create a JSON file named env-variables.json with the following content:
```json
{
  "Variables": {
    "AI_MODEL": "gpt-3.5-turbo",
    "OPENAI_API_KEY": "sk-xxx",
    "OPENAI_API_URL": "https://api.openai.com/v1/chat/completions",
    "ALLOWED_USERS": "123456789",
    "BOT_TOKEN": "1234567890:xxx",
    "BOT_URL": "https://{api-id}.execute-api.{region}.amazonaws.com/{api-path}",
    "BOT_USERNAME": "@BotName",
    "JAVA_TOOL_OPTIONS": "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug",
    "DYNAMO_TABLE_NAME": "chat-history",
    "HISTORY_LENGTH": "8"
  }
}
```
Replace placeholders with the appropriate values for your configuration. We will get BOT_URL later for now you can leave it empty("").

2. Create lambda function:
```bash
aws lambda create-function --function-name chatgpt-bot \
  --runtime java11 \
  --handler com.anderb.chatbot.BotApplication::handleRequest \
  --zip-file fileb://build/distributions/chatgpt-bot.zip \
  --role ARN_OF_THE_ROLE \
  --timeout 300 \
  --architectures arm64 \
  --memory-size 512 \
  --environment file://aws/env-variables.json
```
Replace ARN_OF_THE_ROLE with the Arn of the role you created. Take note of the FunctionArn value in the response.

### Integrate the Lambda function with the API Gateway method
1. Define the integration configuration JSON for your method. For example, to integrate your API Gateway with a Lambda function, the JSON configuration would look like this:
```json
{
  "type": "AWS",
  "httpMethod": "POST",
  "uri": "arn:aws:apigateway:YOUR_REGION:lambda:path/2015-03-31/functions/arn:aws:lambda:YOUR_REGION:YOUR_ACCOUNT_ID:function:{my-function}/invocations",
  "passthroughBehavior": "when_no_match",
  "contentHandling": "CONVERT_TO_TEXT",
  "timeoutInMillis": 29000,
  "requestParameters" : {
    "integration.request.header.X-Amz-Invocation-Type" : "'Event'"
  }
}
```
2. Create a Lambda function integration for the method using the AWS CLI:
```bash
aws apigateway put-integration \
  --rest-api-id YOUR_API_ID \
  --resource-id YOUR_RESOURCE_ID \
  --integration-http-method POST \
  --cli-input-json file://aws/integration.json.json
```
3. Run the following AWS CLI command to add an integration response with status code 200:
```bash
aws apigateway put-integration-response \
  --rest-api-id YOUR_API_ID \
  --resource-id YOUR_RESOURCE_ID \
  --http-method POST \
  --status-code 200
```
4. After adding the integration response, you'll need to redeploy the API for the changes to take effect:
```bash
aws apigateway create-deployment --rest-api-id YOUR_API_ID --stage-name YOUR_STAGE_NAME
```
5. Add the permission using the AWS CLI:
```bash
aws lambda add-permission \
  --function-name YOUR_FUNCTION_NAME \
  --statement-id apigateway-invoke \
  --action lambda:InvokeFunction \
  --principal apigateway.amazonaws.com \
  --source-arn "arn:aws:execute-api:YOUR_REGION:YOUR_ACCOUNT_ID:YOUR_API_ID/*/YOUR_HTTP_METHOD/YOUR_RESOURCE_PATH"
```
### Update `BOT_URL` env
1. Get API Gateway endpoint URL:
```bash
aws apigateway get-stages --rest-api-id YOUR_API_ID
```
2. Now you can construct the API Gateway endpoint URL using the API ID, region, and stage name. The endpoint URL format is:
```bash
https://{api-id}.execute-api.{region}.amazonaws.com/{stage}/{my-function}
```
3. Put it in env-variables.json BOT_URL and update the Lambda function configuration with the new environment variables:
```bash
aws lambda update-function-configuration \
--function-name YOUR_FUNCTION_NAME \
--environment file://path/to/updated-env.json
```
