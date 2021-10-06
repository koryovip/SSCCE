package jp.co.syslinks.sscce.java.utils.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
@JsonSerialize(using = LocalDateTimeSerializer.class)
@JsonDeserialize(using = LocalDateTimeDeserializer.class)
private LocalDateTime ignoreUntil;
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException {
        return LocalDateTime.parse(arg0.getText(), dtf);
    }
}