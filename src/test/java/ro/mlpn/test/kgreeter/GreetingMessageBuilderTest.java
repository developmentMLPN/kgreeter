package ro.mlpn.test.kgreeter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ro.mlpn.test.kgreeter.model.UserDetails;

import java.util.Collections;
import java.util.List;

class GreetingMessageBuilderTest {

    private GreetingMessageBuilder messageBuilder = new GreetingMessageBuilder();


    @Test
    void givenNoRecentUsers_messageCreationShouldWork() {
        String message = messageBuilder.buildGreetingMessage(userWithName("User1"), Collections.emptyList());

        Assertions.assertEquals("Hi User1, welcome to komoot.", message);
    }

    @Test
    void givenOneRecentUsers_messageCreationShouldWork() {
        String message = messageBuilder.buildGreetingMessage(userWithName("User2"), List.of(userWithName("User1")));

        Assertions.assertEquals("Hi User2, welcome to komoot. User1 also joined recently.", message);
    }

    @Test
    void givenTwoRecentUsers_messageCreationShouldWork() {
        String message = messageBuilder.buildGreetingMessage(userWithName("User3"), List.of(userWithName("User1"), userWithName("User2")));

        Assertions.assertEquals("Hi User3, welcome to komoot. User1 and User2 also joined recently.", message);
    }

    @Test
    void givenThreeRecentUsers_messageCreationShouldWork() {
        String message = messageBuilder.buildGreetingMessage(userWithName("User4"), List.of(userWithName("User1"), userWithName("User2"), userWithName("User3")));

        Assertions.assertEquals("Hi User4, welcome to komoot. User1, User2 and User3 also joined recently.", message);
    }

    @Test
    void givenTooManyRecentUsers_exceptionIsThrown() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            messageBuilder.buildGreetingMessage(userWithName("User5"), List.of(userWithName("User1"), userWithName("User2"), userWithName("User3"), userWithName("User4")));
        });
    }

    private UserDetails userWithName(String name) {
        UserDetails userDetails = new UserDetails();
        userDetails.setName(name);
        return userDetails;
    }
}