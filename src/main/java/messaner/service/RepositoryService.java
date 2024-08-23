package messaner.service;

import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;

public interface RepositoryService {

  public boolean addSubscription(UserDTO userDTO);

  public boolean createChannel(UserDTO userDTO);

  public String createUser();

  public String getChatsGson(RoomDTO roomDTO);

  public String getRoomsGson(RoomDTO roomDTO);

  public boolean removeChannel(RoomDTO roomDTO);

  public boolean removeSubscription(UserDTO userDTO);

  public boolean userAlreadyExists(String user);

  public boolean userSubscribed(UserDTO userDTO);
}
