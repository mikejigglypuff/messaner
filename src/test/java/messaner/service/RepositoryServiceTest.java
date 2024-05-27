package messaner.service;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryServiceTest {
    private final RepositoryService repositoryService;
    private final Chat[] compChats;
    private final UserDTO createdRoom;

    @Autowired
    RepositoryServiceTest(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.compChats = new Chat[]{
                new Chat(new ChatDTO(
                        "와구와구프린세스", "나는배가고프다"), "erpin", LocalDateTime.of(
                                2024, 5, 21, 23, 17, 43, 927000000
                )),
                new Chat(new ChatDTO(
                        "와구와구프린세스", "인민들에게빵을착취하는사악한요정여왕몰아내자"), "komi", LocalDateTime.of(
                                2024, 5, 22, 0, 11, 50, 101000000
                )),
        };
        this.createdRoom = new UserDTO("개노잼정령산", "naia");
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"useraa8c4b16-8320-4cd1-897d-ceea077d6b46"})
    public void createUser(String comp) {
        String randID = repositoryService.createUser();
        assertThat(randID.charAt(4)).isEqualTo('_');
        assertThat(randID.length()).isEqualTo(41);

        for(int i = 0; i < 100000; i++) {
            assertThat(randID).isNotEqualTo(comp);
            randID = repositoryService.createUser();
        }

    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("originRooms")
    public void getAllRooms(List<Room> originRooms) {
        int expected = originRooms.size();

        List<Room> rooms = repositoryService.getRooms(new RoomDTO(""));

        assertThat(rooms.size()).isEqualTo(expected);

        SoftAssertions soft = new SoftAssertions();
        for(int i = 0; i < expected; i++) {
            soft.assertThat(rooms.get(i)).usingRecursiveComparison().isEqualTo(originRooms.get(i));
        }
    }

    @Test
    @Order(1)
    public void getChats() {
        RoomDTO roomDTO = new RoomDTO("와구와구프린세스");
        List<Chat> chats = repositoryService.getChats(roomDTO);

        List<Chat> comp = new ArrayList<>(Arrays.asList(compChats));

        SoftAssertions soft = new SoftAssertions();

        for(int i = 0; i < chats.size(); i++) {
            soft.assertThat(chats.get(i)).usingRecursiveComparison().isEqualTo(comp.get(i));
        }
    }

    @Test
    @Order(1)
    public void getNoChats() {
        RoomDTO roomDTO = new RoomDTO("사료스탕스");
        List<Chat> chats = repositoryService.getChats(roomDTO);

        assertThat(chats.size()).isEqualTo(0);
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void userAlreadyExists(String user) {
        assertThat(repositoryService.userAlreadyExists(user)).isTrue();
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"butter"})
    public void userNotExists(String user) {
        assertThat(repositoryService.userAlreadyExists(user)).isFalse();
    }

    @Test
    @Order(1)
    public void userNotSubscribed() {
        UserDTO userDTO = new UserDTO("와구와구프린세스", "elena");
        assertThat(repositoryService.userSubscribed(userDTO)).isFalse();
    }

    @Test
    @Order(1)
    public void userSubscribed() {
        UserDTO userDTO = new UserDTO("와구와구프린세스", "erpin");
        assertThat(repositoryService.userSubscribed(userDTO)).isTrue();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void addChat(String user) {
        ChatDTO chatDTO = new ChatDTO("버터왕국", "버터는놀려야제맛");
        LocalDateTime dateTime = LocalDateTime.now();

        repositoryService.addChat(chatDTO, user, dateTime);

        Chat compChat = new Chat(chatDTO, user, dateTime);
        List<Room> room = repositoryService.getRooms(new RoomDTO(chatDTO.getRoom()));
        List<Chat> chats = room.get(0).getChats();
        assertThat(chats.get(chats.size() - 1).equals(compChat)).isTrue();

        List<User> subscribers = room.get(0).getSubscribers();
        User erpin = subscribers.get(1);
        List<Chat> erpinChats = erpin.getChats();

        assertThat(!erpinChats.isEmpty() && erpinChats.get(erpinChats.size() - 1).equals(compChat)).isTrue();
    }

    @Test
    @Order(2)
    public void addSubscription() {
        RoomDTO roomDTO = new RoomDTO("롤더체스");
        UserDTO userDTO = new UserDTO("롤더체스", "goblinMiko");

        repositoryService.addSubscription(userDTO);

        List<Room> room = repositoryService.getRooms(roomDTO);

        assertThat(room.get(0).getSubscribers().contains(new User(userDTO))).isTrue();
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void chatWrongRoom(String user) {
        ChatDTO chatDTO = new ChatDTO("요정왕국", "버터는놀려야제맛");

        assertThat(repositoryService.addChat(chatDTO, user, null)).isFalse();
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("originRooms")
    public void createChannel(List<Room> originRooms) {
        repositoryService.createChannel(createdRoom);

        int expected = originRooms.size() + 1;
        List<Room> rooms = repositoryService.getRooms(new RoomDTO(""));

        assertThat(rooms.size()).isEqualTo(expected);

        Room newRoom = rooms.get(0);
        assertThat(newRoom).extracting("name").isEqualTo(createdRoom.getRoom());
        assertThat(newRoom.getSubscribers().get(0)).extracting("name").isEqualTo(createdRoom.getUser());
    }

    @Test
    @Order(2)
    public void subWrongRoom() {
        UserDTO userDTO = new UserDTO("롤토체스", "goblinMiko");

        assertThat(repositoryService.addSubscription(userDTO)).isFalse();
    }

    @Test
    @Order(3)
    public void removeSubError() {
        UserDTO userDTO = new UserDTO("롤토체스", "goblinMiko");
        assertThat(repositoryService.removeSubscription(userDTO)).isFalse();
    }

    @Test
    @Order(3)
    public void removeSubscription() {
        UserDTO userDTO = new UserDTO("롤더체스", "goblinMiko");
        repositoryService.removeSubscription(userDTO);

        List<User> userList = repositoryService.getRooms(new RoomDTO("롤더체스")).get(0).getSubscribers();

        assertThat(userList.contains(new User(userDTO))).isTrue();
    }

    @Test
    @Order(4)
    public void removeChannel() {
        assertThat(repositoryService.removeChannel(new RoomDTO(createdRoom.getRoom()))).isTrue();
    }

    static Stream<Arguments> originRooms() {
        LocalDateTime[] dates = new LocalDateTime[]{
                LocalDateTime.of(
                        2024, 5, 21, 23, 17, 43, 927000000
                ),
                LocalDateTime.of(
                        2024, 5, 22, 0, 11, 50, 101000000
                )
        };

        RoomDTO roomDTO1 = new RoomDTO("버터왕국");
        RoomDTO roomDTO2 = new RoomDTO("사료스탕스");
        RoomDTO roomDTO3 = new RoomDTO("와구와구프린세스");
        RoomDTO roomDTO4 = new RoomDTO("롤더체스");

        UserDTO userDTO1 = new UserDTO("버터왕국", "user126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO2 = new UserDTO("사료스탕스", "user126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO3 = new UserDTO("와구와구프린세스", "user126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO4 = new UserDTO("롤더체스", "user126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO erpin = new UserDTO("와구와구프린세스", "erpin");
        UserDTO komi = new UserDTO("와구와구프린세스", "komi");

        ChatDTO chatDTO1 = new ChatDTO("와구와구프린세스", "나는배가고프다");
        ChatDTO chatDTO2 = new ChatDTO("와구와구프린세스", "인민들에게빵을착취하는사악한요정여왕몰아내자");

        List<User> userList1 = new ArrayList<>();
        List<User> userList2 = new ArrayList<>();
        List<User> userList3 = new ArrayList<>();
        List<User> userList4 = new ArrayList<>();

        List<Chat> chatList1 = new ArrayList<>();
        List<Chat> chatList2 = new ArrayList<>();
        List<Chat> chatList3 = new ArrayList<>();
        List<Chat> chatList4 = new ArrayList<>();

        chatList2.add(new Chat(chatDTO1, "와구와구프린세스", dates[0]));
        chatList3.add(new Chat(chatDTO2, "와구와구프린세스", dates[1]));
        chatList4.add(new Chat(chatDTO1, "와구와구프린세스", dates[0]));
        chatList4.add(new Chat(chatDTO2, "와구와구프린세스", dates[1]));

        userList1.add(new User(userDTO1, chatList1));
        userList2.add(new User(userDTO2, chatList1));
        userList3.add(new User(userDTO3, chatList1));
        userList3.add(new User(erpin, chatList2));
        userList3.add(new User(komi, chatList3));
        userList4.add(new User(userDTO4, chatList1));

        List<Room> rooms = new ArrayList<>();

        rooms.add(new Room(roomDTO1, userList1, chatList1));
        rooms.add(new Room(roomDTO2, userList2, chatList1));
        rooms.add(new Room(roomDTO3, userList3, chatList4));
        rooms.add(new Room(roomDTO4, userList4, chatList1));

        return Stream.of(
            arguments(rooms)
        );
    }
}
