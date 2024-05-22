package messaner.service;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryServiceTest {
    private final RepositoryService repositoryService;

    @Autowired
    RepositoryServiceTest(RepositoryService repositoryService){
        this.repositoryService = repositoryService;
    }

    //완
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
            soft.assertThat(rooms.get(i)).usingRecursiveAssertion().isEqualTo(originRooms.get(i));
        }
    }

    @Test
    @Order(1)
    public void getChats() {
        RoomDTO roomDTO = new RoomDTO("와구와구프린세스");
        List<Chat> chats = repositoryService.getChats(roomDTO);

        List<Chat> comp = new ArrayList<>();
        comp.add(new Chat(new ChatDTO("와구와구프린세스", "나는배가고프다"), "erpin", new Date()));
        comp.add(new Chat(new ChatDTO("와구와구프린세스", "인민들에게빵을착취하는사악한요정여왕몰아내자"), "komi", new Date()));

        SoftAssertions soft = new SoftAssertions();

        for(int i = 0; i < chats.size(); i++) {
            soft.assertThat(chats.get(i)).usingRecursiveAssertion().isEqualTo(comp.get(i));
        }
    }

    //완
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

    //완
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

        repositoryService.addChat(chatDTO, user);

        List<Room> room = repositoryService.getRooms(new RoomDTO("버터왕국"));

        assertThat(room.get(0).getChats().contains(new Chat(chatDTO, user, new Date()))).isTrue();
        assertThat(room.get(0).getSubscribers().get(0).getChats().contains(new Chat(chatDTO, user, new Date()))).isTrue();
    }

    @Test
    @Order(2)
    public void addSubscription() {
        RoomDTO roomDTO = new RoomDTO("롤더체스");
        UserDTO userDTO = new UserDTO("롤더체스", "goblinMiko");

        repositoryService.addSubscription(userDTO);

        List<Room> room = repositoryService.getRooms(roomDTO);

        assertThat(room.get(0).getSubscribers().contains(new User(userDTO, new ArrayList<>()))).isTrue();
    }

    //완
    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void chatWrongRoom(String user) {
        ChatDTO chatDTO = new ChatDTO("요정왕국", "버터는놀려야제맛");

        assertThat(repositoryService.addChat(chatDTO, user)).isFalse();
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("originRooms")
    public void createChannel(List<Room> originRooms) {
        UserDTO userDTO = new UserDTO("개노잼정령산", "naia");
        repositoryService.createChannel(userDTO);

        int expected = originRooms.size();
        List<Room> rooms = repositoryService.getRooms(new RoomDTO(""));

        assertThat(rooms.size()).isEqualTo(expected + 1);

        Room newRoom = rooms.get(expected);
        assertThat(newRoom).extracting("name").isEqualTo(userDTO.getRoom());
        assertThat(newRoom.getSubscribers().get(0)).extracting("name").isEqualTo(userDTO.getUser());
    }

    //완
    @Test
    @Order(2)
    public void subWrongRoom() {
        UserDTO userDTO = new UserDTO("롤토체스", "goblinMiko");

        assertThat(repositoryService.addSubscription(userDTO)).isFalse();
    }

    //완
    @Test
    @Order(3)
    public void removeSubError() {
        UserDTO userDTO = new UserDTO("롤토체스", "goblinMiko");
        boolean remove = repositoryService.RemoveSubscription(userDTO);

        assertThat(remove).isFalse();
    }

    @Test
    @Order(3)
    public void removeSubscription() {
        UserDTO userDTO = new UserDTO("롤더체스", "goblinMiko");
        repositoryService.RemoveSubscription(userDTO);

        List<User> userList = repositoryService.getRooms(new RoomDTO("롤더체스")).get(0).getSubscribers();

        assertThat(userList.contains(new User(userDTO, new ArrayList<>()))).isTrue();
    }

    static Stream<Arguments> originRooms() {
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

        chatList2.add(new Chat(chatDTO1, "와구와구프린세스", new Date()));
        chatList3.add(new Chat(chatDTO2, "와구와구프린세스", new Date()));
        chatList4.add(new Chat(chatDTO1, "와구와구프린세스", new Date()));
        chatList4.add(new Chat(chatDTO2, "와구와구프린세스", new Date()));

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
