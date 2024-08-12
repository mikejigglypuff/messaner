package messaner.repository;

import messaner.DTO.UserDTO;
import messaner.model.User;

import java.util.List;

public interface UserRepository {
    public boolean insertSub(UserDTO userDTO);

    public List<User> getSubscribers(String room);

    public boolean removeSub(UserDTO userDTO);

    public boolean userExists(String user);

    public boolean isSubscribed(UserDTO userDTO);
}
