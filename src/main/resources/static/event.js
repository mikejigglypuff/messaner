import SockJS from 'sockjs-client';
import StompJs, {Message as MessageType, Client} from '@stomp/stompjs';

export const connect = async (url) => {
    const socket = new SockJS(url);
    const stompClient = Stomp.Client({
        onConnect: () => {
            stompClient.subscribe("/topic/a", res => {}); //a에 구독
        },
    });
}

export const getRooms = async (e) => {
    const rooms = await axios.get(`loaclhost:8080/rooms/${e.target.value}`);
    return JSON.parse(rooms.data);
}

export const getCookie = () => {
    const roomUrl = await axios({
        method: "get",
        url: "localhost:8080/",
        withCredentials: true,
    });

    return await getRooms(roomUrl);
}

export const createChannel = async () => {
    const callbackUrl = await axios({
        url: "localhost:8080/createChannel",
        method: "post",
        body: {

        }
    });

    if(callbackUrl === "/") {

    } else {

    }
}

export const getChat = async (roomName) => {
    const chat = await axios({
        url: `localhost:8080/room/chatting/${roomName}`,
        method: "get",
    });
    return JSON.parse(chat.data);
}

export const sendChat = async () => {
    stompClient.
}

export const disconnect = async (roomName) => {

}