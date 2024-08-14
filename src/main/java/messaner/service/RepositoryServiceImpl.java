package messaner.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.GsonFactory;
import messaner.repository.ChatRepository;
import messaner.repository.RoomRepository;
import messaner.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class RepositoryServiceImpl implements RepositoryService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GsonFactory gsonFactory;

    @Override
    public boolean addSubscription(UserDTO userDTO) {
        return userRepository.insertSub(userDTO);
    }

    @Override
    public boolean createChannel(UserDTO userDTO) {
        return roomRepository.insertRoom(userDTO);
    }

    @Override
    public String createUser() {
        String uuid = "user_" + UUID.randomUUID();
        while (userAlreadyExists(uuid)) {
            uuid = "user" + UUID.randomUUID();
        }
        return uuid;
    }

    @Override
    public String getChatsGson(RoomDTO roomDTO) throws NoSuchElementException {
        Gson gson = gsonFactory.instantGson();
        return gson.toJson(chatRepository.getChats(roomDTO));
    }
    

    @Override
    public String getRoomsGson(RoomDTO roomDTO) throws NoSuchElementException {
        Gson gson = gsonFactory.instantGson();
        return gson.toJson(roomRepository.getRooms(roomDTO));
    }

    @Override
    public boolean removeChannel(RoomDTO roomDTO) {
        return roomRepository.removeRoom(roomDTO);
    }

    @Override
    public boolean removeSubscription(UserDTO userDTO) {
        return userRepository.removeSub(userDTO);
    }

    @Override
    public boolean userAlreadyExists(String user) {
        return userRepository.userExists(user);
    }

    @Override
    public boolean userSubscribed(UserDTO userDTO) {
        return userRepository.isSubscribed(userDTO);
    }
}
