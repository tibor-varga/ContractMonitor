# ContractMonitor
AWS Tool to monitor contracts notice period. This small tool demonstrates how to use:
- AWS Lambda with Java
- Cloudformation to generate the infrastructure
 

# Install instructions
Modify cloudformation.template as you wish:
1. Subscriptions
  "Subscription" : [ {
          "Endpoint" : "xxxx@gmail.com",
          "Protocol" : "email"
        } ]
2. Event Schedule expression:
  "ScheduleExpression" : "rate(1 day)"
3.  "S3Bucket" : "bbaws-lambdacodes"
4.  "NOTICE_RANGE_DAYS" : 100


Generate Lambda code and upload it to S3
  mvc clean install
  
Create the infrastructure    
  aws s3 cp target/contractmonitor-1.0.0.jar s3://[S3BUCKET_IN_CLOUDFORMATION_TEMPLATE]/
  
Confirm the subscription sent during the installation

Import data if there is any:
  aws dynamodb batch-write-item --request-items file://Contracts.json 

  
#Useful commands
delete cloudformation stack: 
	aws cloudformation delete-stack --stack-name [STACK-NAME]

create cloudformation stack:
	aws cloudformation create-stack --stack-name xx4  --notification-arns 'arn:aws:sns:eu-central-1:[ACCOUNT_ID]:SystemMessages' --template-body  file://src/main/resources/cloudformation.template 

simple export:
  	aws dynamodb scan --table-name Contracts >export.json

