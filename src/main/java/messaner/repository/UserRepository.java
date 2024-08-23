package messaner.repository;

import java.util.List;
import messaner.DTO.UserDTO;
import messaner.model.User;

public interface UserRepository {

  public List<User> getSubscribers(String room);

  public boolean insertSub(UserDTO userDTO);

  public boolean isSubscribed(UserDTO userDTO);

  public boolean removeSub(UserDTO userDTO);

  public boolean userExists(String user);
}
