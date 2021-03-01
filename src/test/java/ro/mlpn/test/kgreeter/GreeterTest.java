package ro.mlpn.test.kgreeter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.mlpn.test.kgreeter.model.GreetingEvent;
import ro.mlpn.test.kgreeter.model.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GreeterTest {

    private static final String TEST_GREETING_MESSAGE = "hello";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GreetingMessageBuilder greetingMessageBuilder;

    @Mock
    private GreetingEventsClient notificationClient;

    @Mock
    private Context context;

    private Greeter greeter;

    @BeforeEach
    void setUp() {
        when(greetingMessageBuilder.buildGreetingMessage(any(), any())).thenReturn(TEST_GREETING_MESSAGE);
        when(context.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));
        greeter = new Greeter(objectMapper, userRepository, greetingMessageBuilder, notificationClient);
    }

    @Test
    void givenNewSnsMessage_itIsHandledAsExpected() throws Exception {
        UserDetails newUser = mock(UserDetails.class);
        List<UserDetails> recentUsers = Collections.emptyList();

        when(objectMapper.readValue(any(String.class), eq(UserDetails.class))).thenReturn(newUser);
        when(userRepository.getLatestUsers(anyInt())).thenReturn(recentUsers);

        greeter.handleRequest(snsEventWithEmptyJson(), context);

        ArgumentCaptor<GreetingEvent> greetingEventArgumentCaptor = ArgumentCaptor.forClass(GreetingEvent.class);
        verify(notificationClient).sendGreetingEvent(greetingEventArgumentCaptor.capture());
        verify(userRepository).storeNewUser(newUser);

        GreetingEvent greetingEvent = greetingEventArgumentCaptor.getValue();
        Assertions.assertEquals(TEST_GREETING_MESSAGE, greetingEvent.getMessage());
        Assertions.assertEquals("mihai@lupan.me", greetingEvent.getSender());
        Assertions.assertEquals(Collections.emptyList(), greetingEvent.getRecentUserIds());
    }

    private SNSEvent snsEventWithEmptyJson() {
        SNSEvent.SNSRecord snsRecord = new SNSEvent.SNSRecord();
        snsRecord.setSns(new SNSEvent.SNS().withMessage("{}"));
        return new SNSEvent().withRecords(List.of(snsRecord));
    }
}