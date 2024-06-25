import React, { useState, useEffect, useRef, useCallback } from "react";
import SockJS from "sockjs-client";
import axios from "axios";
import { Client } from "@stomp/stompjs";

const defaultURL = process.env.REACT_APP_DEFAULT_URL;
const stompURL = process.env.REACT_APP_STOMP_URL;

axios.interceptors.request.use(config => {
    const token = localStorage.getItem("token");
    if(token) {
        config.headers.Authorization = token;
    }

    return config;
}, err => {
    console.error(err);
    return Promise.reject(err);
});

axios.interceptors.response.use(res => {
    const token = res.headers.getAuthorization();
    if(token) {
        localStorage.setItem("token", token);
    }

    return res;
}, err => {
    console.error(err);
    return Promise.reject(err);
});

function App() {
    const [roomList, setRoomList] = useState([]);
    const [roomName, setRoomName] = useState("");
    const [message, setMessage] = useState("");
    const [chatting, setChatting] = useState([]);
    const client = useRef(null);

    useEffect(() => {
        if(!client.current) {
            getSession();
        } else {
            getChat();
        }
    }, []);

    const connect = useCallback(() => {
        const url = "와구와구프린세스";
        setRoomName(url);
        const token = localStorage.getItem("token");
        if(token) {
            const stompClient = new Client({
                webSocketFactory: wsFactory(),
                connectHeaders: {
                    "Authorization" : token
                },
                reconnectDelay: 5000,
                heartbeat: {
                    incoming: 4000,
                    outgoing: 4000
                },
                onConnect: frame => {
                    console.log(frame);

                    const originalSubscribe = stompClient.subscribe;
                    const originalPublish = stompClient.publish;
                    
                    stompClient.subscribe(`/topic/${url}`, (destination, callback, headers = {}) => {
                        const customHeaders = {
                            "Authorization": token,
                            ...headers,
                        };

                        return originalSubscribe.call(stompClient, destination, callback, customHeaders);

                    }); //a에 구독

                    console.log("접속 완료");
                    getChat(url);
                },
                onStompError: frame => {
                    console.log(`reported error: ${frame.headers["message"]}`);
                    console.log(`error detail: ${frame.body}`);
                }
            });

            client.current = stompClient;
            client.current.activate();
            console.log(client.current);
        }
    }, []);

    const sendChat = async () => {
        if(client.current && message) {
            client.current.publish({
                destination: "/pub/chat",
                headers: {
                    "Authorization": localStorage.getItem("token")
                },
                body: JSON.stringify({
                    room: roomName,
                    chat: message
                })
            });
        }
    }

    const disconnect = () => {
        if(client.current) {
            client.current.deactivate();
            setChatting([]);
            setMessage("");
            setRoomName("/");
            client.current = null;
        }
    }

    const getSession = async () => {
        await axios({
            method: "get",
            url: defaultURL,
            withCredentials: true,
        });

        getRooms();
    }

    const getRooms = async () => {
        const url = (roomName) ? `?name=${roomName}` : "";
        const rooms = await axios({
            method: "get",
            url: `${defaultURL}/rooms${url}`,
            withCredentials: true
        });

        console.log(rooms);
        setRoomList(rooms.data);
    }

    const createChannel = async () => {
        if(roomName) {
            const callbackUrl = await axios({
                method: "post",
                url: `${defaultURL}/room/create`,
                withCredentials: true,
                body: {
                    room: roomName
                }
            });

            if(callbackUrl === "/") {
                console.error("채팅방 생성 중 문제가 발생했습니다");
            } else {
                connect(callbackUrl);
            }
        }
    }

    const getChat = async (url) => {
        const chat = await axios({
            method: "get",
            url: `${defaultURL}/room/chatting${url}`,
        });
        setChatting(chat.data);
        console.log(chat);
        
    }

    const typeMessage = e => {
        setMessage(e.target.value);
    }

    const typeRoomName = e => {
        setRoomName(e.target.value);
    }

    const wsFactory = () => {
        if("WebSocket" in window) {
            const ws = new WebSocket(`${stompURL}/ws`);
            ws.binaryType = "arraybuffer";
            return ws;
        } else {
            return new SockJS(`${defaultURL}/sockjs`)
        }
    }

    return (client.current) ? <div className="chatRoom">
            <div className="chatRoomHeader">
                <button variant="outline">
                    <ArrowLeftIcon onClick={disconnect} />
                </button>
                <div className="chatRoomName">{roomName}</div>
            </div>
            { chatting.map(chat => (
                <div className="chatRoomBody">
                    <div className="chatInfo">
                        <div>JW</div>
                        <div className="chatWriter">{chat.writer}</div>
                    </div>
                    <div className="chatBubble">
                        <div className="chatMessage">{chat.message}</div>
                        <div className="chatCreatedAt">{chat.createdAt}</div>
                    </div>
                    </div>
            )) }
            <div className="chatRoomFooter">
                <input className="msagInput" placeholder="Type a message" onChange={typeMessage} />
                <button size="icon" onClick={sendChat}>
                    <SendIcon />
                    <span className="msgSendButton">Send</span>
                </button>
            </div>
        </div> : <div className="mainPage">
                    <div className="mainHeader">
                        <div className="mainTitle">Chat Rooms</div>
                        <div className="searchRoom">
                            <input placeholder="채널명을 입력하세요" onChange={typeRoomName} />
                            <button id="searchRoomBtn" onClick={getRooms}>Search</button>
                            <button id="createRoomBtn" onClick={createChannel}>채널 생성</button>
                        </div>
                    </div>
                    <div className="roomList">
                        { Object.values(roomList).map(val => (
                            <label onClick={connect}>
                                <div className="roomInfo" key={val.name}>
                                    <div className="roomName">{val.name}</div>
                                    <div className="roomMember">
                                        <UserIcon />
                                        <span className="roomMemberNum">{val.subscribers.length}</span>
                                    </div>
                                </div>
                            </label>
                        )) } 
                    </div>
                </div>;
}

function UserIcon(props) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

function SendIcon(props) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m22 2-7 20-4-9-9-4Z" />
      <path d="M22 2 11 13" />
    </svg>
  );
}

function ArrowLeftIcon(props) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="m12 19-7-7 7-7" />
      <path d="M19 12H5" />
    </svg>
  )
}

export default App;