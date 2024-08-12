package messaner.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.factory.GsonFactory;
import messaner.model.AggNameResult;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import messaner.repository.ChatRepository;
import messaner.repository.RoomRepository;
import messaner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@RequiredArgsConstructor
@Slf4j
@Service
public class RepositoryServiceImpl implements RepositoryService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GsonFactory gsonFactory;

    @Override
    public Chat addChat(ChatDTO chatDTO, String user, Instant dateTime) {
        return (chatRepository.insertChat(chatDTO, user, dateTime))
                ? new Chat(chatDTO, user)
                : new Chat();
    }

    @Override
    public boolean addSubscription(UserDTO userDTO) {
        return userRepository.insertSub(userDTO);
    }

    //
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
        return roomRepository.deleteRoom(roomDTO);
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
