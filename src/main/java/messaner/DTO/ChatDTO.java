package messaner.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ChatDTO extends RoomDTO{
    protected String chat;

    public ChatDTO(String room, String chat) {
        super(room);
        this.chat = chat;
    }
}
