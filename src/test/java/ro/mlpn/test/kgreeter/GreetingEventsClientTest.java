package ro.mlpn.test.kgreeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ro.mlpn.test.kgreeter.model.GreetingEvent;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class GreetingEventsClientTest {

    @Test
    void givenNewEvent_requestShouldBeCreatedCorrectly() throws Exception {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(new byte[0]);

        GreetingEventsClient.RequestBuilder requestBuilder = new GreetingEventsClient.RequestBuilder(objectMapper);

        HttpRequest request = requestBuilder.buildRequest(Mockito.mock(GreetingEvent.class));
        assertEquals("POST", request.method());
        assertEquals(URI.create("https://notification-backend-challenge.main.komoot.net/"), request.uri());
    }
}