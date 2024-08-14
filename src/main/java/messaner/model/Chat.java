package messaner.model;

import lombok.Getter;
import messaner.DTO.ChatDTO;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;


@Getter
public class Chat {
    private String room;
    private String writer;
    private String message;
    private Instant createdAt;

    public Chat(){}

    public Chat(ChatDTO chatDTO, String user) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = Instant.now();
    }

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
