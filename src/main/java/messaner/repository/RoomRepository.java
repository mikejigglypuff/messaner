package messaner.repository;

import java.util.List;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Room;

public interface RoomRepository {

  public List<Room> getRooms(RoomDTO roomDTO);

  public boolean insertRoom(UserDTO userDTO);

  public boolean removeRoom(RoomDTO roomDTO);

  public boolean roomExists(String room);
}
