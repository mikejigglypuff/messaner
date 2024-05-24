package messaner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import messaner.DTO.ChatDTO;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;


@Getter
public class Chat {
    private String room;
    private String writer;
    private String message;
    private LocalDateTime createdAt;

    public Chat(ChatDTO chatDTO, String user, LocalDateTime date) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = date;
    }

    public Chat(ChatDTO chatDTO, String user, String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = LocalDateTime.parse(date, dateTimeFormatter);
    }

    public Chat(ChatDTO chatDTO, String user) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = LocalDateTime.now();
    }

    public Chat(){}

    @Override
    public boolean equals(Object o) {
        if(o instanceof Chat chat) {
            return this.room.equals(chat.getRoom()) && this.writer.equals(chat.getWriter())
                && this.message.equals(chat.getMessage()) && this.createdAt.equals(chat.getCreatedAt());
        }
        return false;
    }

}
