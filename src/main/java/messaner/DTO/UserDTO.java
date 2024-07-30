package messaner.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDTO extends RoomDTO {
    protected String user;

    public UserDTO(String room, String user) {
        super(room);
        this.user = user;
    }
}
