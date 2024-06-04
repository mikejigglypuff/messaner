package messaner;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@ComponentScan(basePackages = {"messaner"})
@PropertySource("classpath:application-prod.properties")
@Slf4j
public class MessanerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessanerApplication.class, args);
	}

    public static class LocalDateTimeAdapter implements
            JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }

        @Override
        public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(formatter));
        }
    }
}