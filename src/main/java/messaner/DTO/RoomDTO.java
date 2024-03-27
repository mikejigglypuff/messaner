package messaner.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class RoomDTO {
    protected String room;

    public RoomDTO(String room) {
        this.room = room;
    }
}
