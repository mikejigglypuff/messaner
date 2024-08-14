package messaner.repository;

import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Room;

import java.util.List;

public interface RoomRepository {
    public List<Room> getRooms(RoomDTO roomDTO);
    public boolean insertRoom(UserDTO userDTO);
    public boolean removeRoom(RoomDTO roomDTO);
    public boolean roomExists(String room);
}
