package ro.mlpn.test.kgreeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ro.mlpn.test.kgreeter.model.GreetingEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@AllArgsConstructor
class GreetingEventsClient {

    private ObjectMapper objectMapper;

    private HttpClient httpClient;

    @SneakyThrows
    public void sendGreetingEvent(GreetingEvent buildGreetingEvent) {
        httpClient.send(new RequestBuilder(objectMapper).buildRequest(buildGreetingEvent), HttpResponse.BodyHandlers.discarding());
    }

    @AllArgsConstructor
    static class RequestBuilder {

        private ObjectMapper objectMapper;

        @SneakyThrows
        public HttpRequest buildRequest(GreetingEvent buildGreetingEvent) {
            return HttpRequest.newBuilder()
                    .uri(new URI("https://notification-backend-challenge.main.komoot.net/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(buildGreetingEvent)))
                    .build();
        }
    }
}
