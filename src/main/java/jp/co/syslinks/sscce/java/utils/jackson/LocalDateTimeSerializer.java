package jp.co.syslinks.sscce.java.utils.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
@JsonSerialize(using = LocalDateTimeSerializer.class)
@JsonDeserialize(using = LocalDateTimeDeserializer.class)
private LocalDateTime ignoreUntil;
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(LocalDateTime value, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
        arg1.writeString(dtf.format(value));
    }
}
