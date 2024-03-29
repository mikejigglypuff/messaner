package messaner.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import messaner.DTO.ChatDTO;

import java.util.Date;


@RequiredArgsConstructor
@Getter
public class Chat {
    private String room;
    private String writer;
    private String message;
    private Date createdAt;

    public Chat(ChatDTO chatDTO) {
        room = chatDTO.getRoom();
        writer = chatDTO.getUser();
        message = chatDTO.getChat();
        createdAt = new Date();
    }
}
