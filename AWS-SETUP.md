
### Create a role
Create a trust policy for the Lambda service in a file named lambda-trust-policy.json:
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
Create the IAM role using the AWS CLI and the trust policy:
```bash
aws iam create-role --role-name lambda-cloudwatch-dynamodb-role --assume-role-policy-document file://lambda-trust-policy.json
```
Note the Arn of the newly created role in the output. You'll need it later when creating the Lambda function.

Create a custom inline policy with the specified CloudWatch and DynamoDB permissions. Save the policy in a file named custom-policy.json:
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
        "dynamodb:CreateTable",
        "dynamodb:PutItem",
        "dynamodb:DescribeTable",
        "dynamodb:DeleteItem",
        "dynamodb:GetItem",
        "dynamodb:ListTables"
      ],
      "Resource": "arn:aws:dynamodb:*:*:table/*"
    }
  ]
}
```
Add the custom inline policy to the IAM role using the AWS CLI:
```bash
aws iam put-role-policy --role-name lambda-cloudwatch-dynamodb-role --policy-name CustomCloudWatchDynamoDBPolicy --policy-document file://custom-policy.json
```
### Create the Lambda function using the AWS CLI:
```bash
aws lambda create-function --function-name chatgpt-bot \
  --runtime java11 \
  --handler com.anderb.chatbot.BotApplication::handleRequest \
  --zip-file fileb://build/distribution/chatgpt-bot.zip \
  --role ARN_OF_THE_ROLE \
  --environment 'Variables={AI_MODEL=gpt-3.5-turbo, OPENAI_API_KEY=sk-xxx, ...}'
```
Replace ARN_OF_THE_ROLE with the Arn of the role you created.

### Create an API Gateway:
```bash
aws apigateway create-rest-api --name chatgpt-bot-api
```
Note the id of the newly created API in the output. You'll need it later when creating the resource and method.

Create a resource in the API Gateway:
```bash
aws apigateway create-resource --rest-api-id YOUR_API_ID --parent-id YOUR_API_ROOT_RESOURCE_ID --path-part your-path
```
Replace YOUR_API_ID with the API id you obtained, and YOUR_API_ROOT_RESOURCE_ID with the root resource ID (/). You can find the root resource ID by running:
```bash
aws apigateway get-resources --rest-api-id YOUR_API_ID
```
Note the id of the newly created resource in the output. You'll need it later when creating the method.

Create a method for the resource:
```bash
aws apigateway put-method --rest-api-id YOUR_API_ID --resource-id YOUR_RESOURCE_ID --http-method POST --authorization-type NONE
```
Replace YOUR_API_ID with the API id and YOUR_RESOURCE_ID with the resource id you obtained previously.

Add the request parameter to the method request:
```bash
aws apigateway update-method --rest-api-id YOUR_API_ID --resource-id YOUR_RESOURCE_ID --http-method POST --patch-operations op='add',path='/requestParameters/integration.request.header.X-Amz-Invocation-Type',value="'Event'"
```

### Integrate the Lambda function with the API Gateway method
Create a Lambda function integration for the method using the AWS CLI:
```bash
aws apigateway put-integration --rest-api-id YOUR_API_ID --resource-id YOUR_RESOURCE_ID --http-method POST --type AWS_PROXY --integration-http-method POST --uri arn:aws:apigateway:YOUR_REGION:lambda:path/2015-03-31/functions/arn:aws:lambda:YOUR_REGION:YOUR_ACCOUNT_ID:function:my-function/invocations --request-parameters integration.request.header.X-Amz-Invocation-Type=method.request.header.X-Amz-Invocation-Type
```
