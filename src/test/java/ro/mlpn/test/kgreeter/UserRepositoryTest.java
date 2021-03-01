package ro.mlpn.test.kgreeter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.mlpn.test.kgreeter.model.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.mlpn.test.kgreeter.UserRepository.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private AmazonDynamoDB dynamoDB;

    private Clock clock = Clock.fixed(Instant.parse("2021-03-01T13:20:30Z"), ZoneId.of("UTC"));

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(dynamoDB, clock);
    }

    @Test
    void givenNewUser_storagePropertiesAreCorrectlySet() {
        UserDetails userDetails = newUserDetails("TestName", 2L);

        userRepository.storeNewUser(userDetails);

        ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDB).putItem(putItemRequestArgumentCaptor.capture());
        PutItemRequest putItemRequest = putItemRequestArgumentCaptor.getValue();

        assertEquals(TABLE_NAME, putItemRequest.getTableName());

        Map<String, AttributeValue> item = putItemRequest.getItem();
        assertEquals(EVENT_CREATED_USER, item.get(COLUMN_EVENT_KEY).getS());
        assertEquals(String.valueOf(Instant.now(clock).toEpochMilli()), item.get(COLUMN_EVENT_TIMESTAMP).getN());
        assertEquals(userDetails.getName(), item.get(COLUMN_USER_NAME).getS());
        assertEquals(String.valueOf(userDetails.getId()), item.get(COLUMN_USER_ID).getN());
        assertEquals(String.valueOf(Instant.now(clock).plus(Duration.ofDays(10)).getEpochSecond()), item.get(COLUMN_TTL).getN());
    }

    @Test
    void givenLatestUserQuery_requestIsCorrectlyCreated() {
        QueryResult result = mock(QueryResult.class);
        when(result.getItems()).thenReturn(Collections.emptyList());
        when(dynamoDB.query(any())).thenReturn(result);

        userRepository.getLatestUsers(5);

        ArgumentCaptor<QueryRequest> queryRequestArgumentCaptor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(dynamoDB).query(queryRequestArgumentCaptor.capture());

        QueryRequest queryRequest = queryRequestArgumentCaptor.getValue();
        assertEquals(TABLE_NAME, queryRequest.getTableName());
        assertEquals(5, queryRequest.getLimit());
        assertFalse(queryRequest.getScanIndexForward());
    }

    @Test
    void givenTwoRecentUsers_theyAreMappedToUserDetails() {
        UserDetails user1 = newUserDetails("User2", 5L);
        UserDetails user2 = newUserDetails("User4", 6L);

        QueryResult result = mock(QueryResult.class);
        when(result.getItems()).thenReturn(List.of(resultForUser(user1), resultForUser(user2)));
        when(dynamoDB.query(any())).thenReturn(result);

        List<UserDetails> latestUsers = userRepository.getLatestUsers(2);

        assertEquals(2, latestUsers.size());
        assertTrue(latestUsers.contains(user1));
        assertTrue(latestUsers.contains(user2));
    }

    private UserDetails newUserDetails(String name, Long id) {
        UserDetails userDetails = new UserDetails();
        userDetails.setName(name);
        userDetails.setId(id);

        return userDetails;
    }

    private Map<String, AttributeValue> resultForUser(UserDetails userDetails) {
        Map<String, AttributeValue> userValuesByColumnName = new HashMap<>();
        userValuesByColumnName.put(COLUMN_USER_ID, new AttributeValue().withN(String.valueOf(userDetails.getId())));
        userValuesByColumnName.put(COLUMN_USER_NAME, new AttributeValue().withS(userDetails.getName()));

        return userValuesByColumnName;
    }
}