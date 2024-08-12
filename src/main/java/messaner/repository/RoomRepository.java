package messaner.repository;

import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Room;

import java.util.List;

public interface RoomRepository {
    public boolean insertRoom(UserDTO userDTO);

    public List<Room> getRooms(RoomDTO roomDTO);

    public boolean deleteRoom(RoomDTO roomDTO);

    public boolean roomExists(String room);
}
