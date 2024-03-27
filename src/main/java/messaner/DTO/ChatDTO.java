package messaner.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ChatDTO extends UserDTO{
    protected String chat;

    public ChatDTO(String room, String user, String chat) {
        super(room, user);
        this.chat = chat;
    }
}
