package ro.mlpn.test.kgreeter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import ro.mlpn.test.kgreeter.model.GreetingEvent;
import ro.mlpn.test.kgreeter.model.UserDetails;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Greeter implements RequestHandler<SNSEvent, Object> {

    private ObjectMapper objectMapper;

    private UserRepository userRepository;

    private GreetingMessageBuilder greetingMessageBuilder;

    private GreetingEventsClient notificationClient;

    public Greeter() {
        this(ObjectMapperProvider.get(), new UserRepository(AmazonDynamoDBClientBuilder.defaultClient(), Clock.systemUTC()), new GreetingMessageBuilder(), new GreetingEventsClient(ObjectMapperProvider.get(), buildHttpClient()));
    }

    @Override
    public Object handleRequest(SNSEvent snsEvent, Context context) {
        String snsMessage = snsEvent.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log("Handling " + snsMessage);

        UserDetails newUser = convertToUserDetails(snsMessage);
        List<UserDetails> latestUsers = userRepository.getLatestUsers(3);

        notificationClient.sendGreetingEvent(buildGreetingEvent(newUser, latestUsers));
        userRepository.storeNewUser(newUser);

        return null;
    }

    private GreetingEvent buildGreetingEvent(UserDetails newUser, List<UserDetails> latestUsers) {
        String greetingMessage = greetingMessageBuilder.buildGreetingMessage(newUser, latestUsers);
        List<Long> recentUserIds = latestUsers.stream().map(UserDetails::getId).collect(Collectors.toList());

        return GreetingEvent.builder()
                .sender("mihai@lupan.me")
                .receiver(newUser.getId())
                .message(greetingMessage)
                .recentUserIds(recentUserIds)
                .build();
    }

    private UserDetails convertToUserDetails(String snsMessage) {
        try {
            return objectMapper.readValue(snsMessage, UserDetails.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to convert " + snsMessage + " to " + UserDetails.class.getSimpleName(), e);
        }
    }

    private static HttpClient buildHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }
}
