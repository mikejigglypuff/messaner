package messaner.service;

import lombok.extern.slf4j.Slf4j;
import messaner.DTO.ChatDTO;
import messaner.DTO.RoomDTO;
import messaner.DTO.UserDTO;
import messaner.model.Chat;
import messaner.model.Room;
import messaner.model.User;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@SpringBootTest
public class RepositoryServiceTest {
    private final RepositoryService repositoryService;
    private final Chat[] compChats;
    private final UserDTO createdRoom;
    private final TransactionTemplate transactionTemplate;
    private final List<UserDTO> subscriptionUsers;
    private final List<UserDTO> notSubUsers;
    private final UserDTO subUnsubUser;
    private final UserDTO subWrongRoomUser;

    @Autowired
    RepositoryServiceTest(RepositoryService repositoryService, TransactionTemplate transactionTemplate) {
        this.repositoryService = repositoryService;
        this.compChats = new Chat[]{
                new Chat(new ChatDTO(
                        "와구와구프린세스", "나는배가고프다"), "erpin", "2024-05-21T23:17:43.927Z"
                        ),
                new Chat(new ChatDTO(
                        "와구와구프린세스", "인민들에게빵을착취하는사악한요정여왕몰아내자"), "komi", "2024-05-22T00:11:50.101Z"),
        };
        this.createdRoom = new UserDTO("개노잼정령산", "naia");
        this.transactionTemplate = transactionTemplate;
        this.subscriptionUsers = Arrays.asList(
                new UserDTO("와구와구프린세스", "erpin"),
                new UserDTO("버터왕국", "user_126a1cd5-c91e-4553-8694-baa38ce4a70d"),
                new UserDTO("사료스탕스", "tig")
        );
        this.notSubUsers = Arrays.asList(
                new UserDTO("와구와구프린세스", "elena"),
                new UserDTO("사료스탕스", "user_126a1cd5-c91e-4553-8694-badi30aoe1kc")//,
                //new UserDTO("와구와구프린세스", "komi")
        );
        this.subUnsubUser = new UserDTO("롤더체스", "goblinMiko");
        this.subWrongRoomUser = new UserDTO("롤xh체스", "goblinMiko");
    }

    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void addChat(String user) {
        ChatDTO chatDTO = new ChatDTO("버터왕국", "버터는놀려야제맛");
        Instant dateTime = Instant.now();

        transactionTemplate.execute(status -> {
            assertTrue(repositoryService.addChat(chatDTO, user, dateTime));

            status.setRollbackOnly();
            return null;
        });

    }

    /*
    @Test
    public void addSubscription() {
        transactionTemplate.execute(status -> {
            assertTrue(repositoryService.addSubscription(subUnsubUser));

            status.setRollbackOnly();
            return null;
        });
    }

     */

    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void chatWrongRoom(String user) {
        ChatDTO chatDTO = new ChatDTO("요정왕국", "버터는놀려야제맛");

        transactionTemplate.execute(status -> {
            assertFalse(repositoryService.addChat(chatDTO, user, null));

            status.setRollbackOnly();
            return null;
        });
    }

    @ParameterizedTest
    @MethodSource("originRooms")
    public void createChannel(List<Room> originRooms) {
        transactionTemplate.execute(status -> {
            assertTrue(repositoryService.createChannel(createdRoom));

            status.setRollbackOnly();
            return null;
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_aa8c4b16-8320-4cd1-897d-ceea077d6b46"})
    public void createUser(String comp) {
        String randID = repositoryService.createUser();
        assertThat(randID.charAt(4)).isEqualTo('_');
        assertThat(randID.length()).isEqualTo(41);

        for(int i = 0; i < 1000; i++) {
            assertThat(randID).isNotEqualTo(comp);
            randID = repositoryService.createUser();
        }

    }

    @Test
    public void getAllRooms() {
        assertThatCode(() -> {
            repositoryService.getRooms(new RoomDTO(""));
        }).doesNotThrowAnyException();
    }

    @Test
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
    public void getNoChats() {
        RoomDTO roomDTO = new RoomDTO("사료스탕스");
        List<Chat> chats = repositoryService.getChats(roomDTO);

        assertThat(chats.size()).isEqualTo(0);
    }

    /*
    @Test
    public void removeSubError() {
        transactionTemplate.execute(status -> {
            assertFalse(repositoryService.removeSubscription(subUnsubUser));

            status.setRollbackOnly();
            return null;
        });
    }

     */

    @Test
    public void removeSubscription() {
        UserDTO userDTO = new UserDTO("와구와구프린세스", "erpin");

        transactionTemplate.execute(status -> {
            assertTrue(repositoryService.removeSubscription(userDTO));

            status.setRollbackOnly();
            return null;
        });
    }

    @ParameterizedTest()
    @MethodSource("originRooms")
    public void removeChannel(List<Room> originRooms) {
        Room deleteRoom = originRooms.get(0);

        transactionTemplate.execute(status -> {
            assertThat(repositoryService.removeChannel(new RoomDTO(deleteRoom.getName()))).isTrue();

            status.setRollbackOnly();
            return null;
        });
    }

    @Test
    public void subWrongRoom() {
        transactionTemplate.execute(status -> {
            assertFalse(repositoryService.addSubscription(subWrongRoomUser));

            status.setRollbackOnly();
            return null;
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"erpin"})
    public void userAlreadyExists(String user) {
        assertTrue(repositoryService.userAlreadyExists(user));
    }

    @ParameterizedTest
    @ValueSource(strings = {"butter"})
    public void userNotExists(String user) {
        assertFalse(repositoryService.userAlreadyExists(user));
    }

    @Test
    public void userNotSubscribed() {
        boolean checkNotSub = true;
        for(UserDTO userDTO : notSubUsers) {
            checkNotSub = !repositoryService.userSubscribed(userDTO);
        }
        assertTrue(checkNotSub);
    }

    @Test
    public void userSubscribed() {
        boolean correctSubscription = true;
        for(UserDTO userDTO : subscriptionUsers) {
            correctSubscription = repositoryService.userSubscribed(userDTO);
        }
        assertTrue(correctSubscription);
    }

    static Stream<Arguments> originRooms() {
        String[] dates = new String[]{"2024-05-21T23:17:43.927Z", "2024-05-22T00:11:50.101Z"};

        RoomDTO roomDTO1 = new RoomDTO("버터왕국");
        RoomDTO roomDTO2 = new RoomDTO("사료스탕스");
        RoomDTO roomDTO3 = new RoomDTO("와구와구프린세스");
        RoomDTO roomDTO4 = new RoomDTO("롤더체스");

        UserDTO userDTO1 = new UserDTO("버터왕국", "user_126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO2 = new UserDTO("사료스탕스", "user_126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO3 = new UserDTO("와구와구프린세스", "user_126a1cd5-c91e-4553-8694-baa38ce4a70d");
        UserDTO userDTO4 = new UserDTO("롤더체스", "user_126a1cd5-c91e-4553-8694-baa38ce4a70d");
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
