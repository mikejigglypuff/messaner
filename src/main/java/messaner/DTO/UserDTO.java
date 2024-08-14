package messaner.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDTO extends RoomDTO {
    protected String user;

    public UserDTO(String room, String user) {
        super(room);
        this.user = user;
    }
}
