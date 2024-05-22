package messaner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import messaner.DTO.ChatDTO;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@RequiredArgsConstructor
@Getter
public class Chat {
    private String room;
    private String writer;
    private String message;
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date createdAt;

    public Chat(ChatDTO chatDTO, String user, Date date) {
        room = chatDTO.getRoom();
        writer = user;
        message = chatDTO.getChat();
        createdAt = date;
    }
}
