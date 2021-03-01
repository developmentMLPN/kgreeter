package ro.mlpn.test.kgreeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
class ObjectMapperProvider {

    private static ObjectMapper INSTANCE;

    public static ObjectMapper get() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapper();
        }
        return INSTANCE;
    }
}
