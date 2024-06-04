package messaner.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import messaner.DTO.ChatDTO;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;


@Getter
public class Chat {
    private String room;
    private String writer;
    private String message;
    private Instant createdAt;

    public Chat(ChatDTO chatDTO, String user, Instant date) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = date;
    }

    public Chat(ChatDTO chatDTO, String user, String date) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = Instant.parse(date);
    }

    public Chat(ChatDTO chatDTO, String user) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = Instant.now();
    }

    public Chat(){}

    @Override
    public boolean equals(Object o) {
        if(o instanceof Chat chat) {
            return this.toString().equals(chat.toString());
        }
        return false;
    }

    @Override
    public String toString() {
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.KOREA)
                        .withZone(ZoneId.systemDefault());
        return "room: " + room + " writer: " + writer + " msg: " + message +
                " createdAt: " + dateTimeFormatter.format(createdAt);
    }
}
