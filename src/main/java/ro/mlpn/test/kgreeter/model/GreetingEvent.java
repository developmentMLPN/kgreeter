package ro.mlpn.test.kgreeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GreetingEvent {

    private String sender;

    private Long receiver;

    private String message;

    @JsonProperty("recent_user_ids")
    private List<Long> recentUserIds = Collections.emptyList();
}
