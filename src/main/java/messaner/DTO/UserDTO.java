package messaner.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class UserDTO extends RoomDTO {
    protected String user;

    public UserDTO(String room, String user) {
        super(room);
        this.user = user;
    }
}
