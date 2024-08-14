package messaner.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ChatDTO extends RoomDTO{
    protected String chat;

    @JsonCreator
    public ChatDTO(@JsonProperty("chat") String chat, @JsonProperty("room") String room) {
        super(room);
        this.chat = chat;
    }
}
