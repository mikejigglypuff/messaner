package messaner.repository;

import messaner.DTO.UserDTO;
import messaner.model.User;

import java.util.List;

public interface UserRepository {
    public List<User> getSubscribers(String room);
    public boolean insertSub(UserDTO userDTO);
    public boolean isSubscribed(UserDTO userDTO);
    public boolean removeSub(UserDTO userDTO);
    public boolean userExists(String user);
}
