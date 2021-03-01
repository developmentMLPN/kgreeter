package ro.mlpn.test.kgreeter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import lombok.AllArgsConstructor;
import ro.mlpn.test.kgreeter.model.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amazonaws.services.dynamodbv2.model.ComparisonOperator.EQ;

@AllArgsConstructor
class UserRepository {

    static final String TABLE_NAME = "UserGreetingEvents";

    static final String EVENT_CREATED_USER = "UserCreate";

    static final String COLUMN_EVENT_KEY = "EventKey";
    static final String COLUMN_EVENT_TIMESTAMP = "EventTimestamp";
    static final String COLUMN_USER_ID = "UserId";
    static final String COLUMN_USER_NAME = "UserName";
    static final String COLUMN_TTL = "TTL";

    private AmazonDynamoDB dynamoDB;

    private Clock clock;

    public void storeNewUser(UserDetails userDetails) {
        Map<String, AttributeValue> userCreatedEventRowProperties = new HashMap<>();
        userCreatedEventRowProperties.put(COLUMN_EVENT_KEY, new AttributeValue().withS(EVENT_CREATED_USER));
        userCreatedEventRowProperties.put(COLUMN_EVENT_TIMESTAMP, new AttributeValue().withN(String.valueOf(Instant.now(clock).toEpochMilli())));
        userCreatedEventRowProperties.put(COLUMN_USER_ID, new AttributeValue().withN(userDetails.getId().toString()));
        userCreatedEventRowProperties.put(COLUMN_USER_NAME, new AttributeValue().withS(userDetails.getName()));
        userCreatedEventRowProperties.put(COLUMN_TTL, new AttributeValue().withN(String.valueOf(calculateTtl().getEpochSecond())));

        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(userCreatedEventRowProperties);
        dynamoDB.putItem(putItemRequest);
    }

    public List<UserDetails> getLatestUsers(int maxRetrievedEntries) {
        QueryRequest queryRequest = buildLatestUserQueryRequest(maxRetrievedEntries);

        QueryResult result = dynamoDB.query(queryRequest);

        return result.getItems().stream()
                .map(this::convertToUserDetails)
                .collect(Collectors.toList());
    }

    private QueryRequest buildLatestUserQueryRequest(int maxRetrievedEntries) {
        HashMap<String, Condition> keyConditions = new HashMap<>();

        keyConditions.put(COLUMN_EVENT_KEY, new Condition().withComparisonOperator(EQ)
                .withAttributeValueList(new AttributeValue().withS(EVENT_CREATED_USER)));

        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withScanIndexForward(false)
                .withLimit(maxRetrievedEntries)
                .withKeyConditions(keyConditions);
    }

    private Instant calculateTtl() {
        return Instant.now(clock).plus(Duration.ofDays(10));
    }

    private UserDetails convertToUserDetails(Map<String, AttributeValue> stringAttributeValueMap) {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(Long.valueOf(stringAttributeValueMap.get(COLUMN_USER_ID).getN()));
        userDetails.setName(stringAttributeValueMap.get(COLUMN_USER_NAME).getS());

        return userDetails;
    }
}
