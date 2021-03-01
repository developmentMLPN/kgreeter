###Considerations

- functionality is very limited in scope which makes it ideal candidate for a serverless function
- as the function needs some state (latest users) we will store the newly created user details in dynamodb (temp using TTL)

###Instalation

In ideal circumstances (with enough time at hand) I would have provided a `terraform` configuration file to provision the env.

Hopefully a step by step guide would be enough for now :)

####Dynamo db
Create a new dynamodb table named  `UserGreetingEvents` that has the following properties: 

- Partition Key -> name : `EventKey`, type : `String`
- Sort Key -> name : `EventTimestamp`, type : `Number`

TTL can be activated on the attribute `TTL` to make sure the entries ar deleted after 10 days. Considering the scope of this test this part is Optional but the function will make sure this attribute is populated correctly.

####Lambda
Create new function by giving it any name you like and pick `Java 11 Corretto` as runtime.

Make sure the function will create a new execution role (this is the default - nothing to do) as we will modify it to allow Lambda to work with DynamoDb

On the function dashboard click the `Add Trigger` button. Pick `SNS` as type from the dropdown and enter the arn of the `SNS` topic we need to subscribe to (`arn:aws:sns:eu-west-1:963797398573:challenge-backend-signups`). Make sure `Enable trigger` is checked (it is by default).

On the `Function Code` panel click on the `Actions` dropdown and pick `Upload a zip or jar file`. You can either build the maven project to generate the jar yourself or use the already build one available at https://github.com/developmentMLPN/kgreeter/releases/tag/1 (`komoot-lambda-greeter-1.0-SNAPSHOT.jar`)

On the `Runtime settings` panel click `Edit` and make sure the `Handler` property is set to `ro.mlpn.test.kgreeter.Greeter::handleRequest`

On the trigger page switch to the `Permission` tab and click on the execution role name in order to navigate to the `IAM` console to edit it.


####Execution role permission update
On the role dashboard page click `Add inline policy` to add a new policy that will allow our lambda function to interact with dynamodb.

On the `Create policy` page pick the JSON tab and paste the following JSON:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "dynamodb:PutItem",
                "dynamodb:Query"
            ],
            "Resource": "arn:aws:dynamodb:*:963797398573:table/UserGreetingEvents"
        }
    ]
}
```
Please make sure the `Resource` arn matches the one corresponding to the newly created dynamodb table. (I used the same account as the one found on the sns so I hope the JSON can be used as is without modification )

###Execution

At this point the env should be ready to handle the `SNS` events.
For each event it receives the following should happen:

- read `JSON` from event to determine name and id.
- read the most recently created user details (up to 3 users details will be retrived from dynamodb)
- compute the greeting message based on new user details and number (and details) of recently created users. (message is different based on number of recent users)
- make `POST` request to `https://notification-backend-challenge.main.komoot.net/` with the required information in `JSON` format.
- store received users details in dynamodb.

###TODOs

- By far the biggest todo would be to automate env. creation using terraform. Sorry for not spending time on this but this would require quite some effort (and time) from my part as at this point I would not know how to do everything by hearth and I would need to research.
- allow for function configuration. Currently quite a few settings are hard coded (developer email, TTL, POST endpoint, Table name). Configuration should be easy to support though lambada env. variables that are loadded at runtime.
- I would also store the notification as an event in the `UserGreetingEvents` table to keep track of send notifications.
