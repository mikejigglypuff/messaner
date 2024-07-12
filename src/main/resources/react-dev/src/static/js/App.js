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
        console.log(token);

        localStorage.setItem("sessionId", genSessionID());
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

    const connect = useCallback(() => {
        const url = "와구와구프린세스";
        setRoomName(url);
        const token = localStorage.getItem("token");
        if(token) {
            client.current = stompFactory(token, url);
            client.current.activate();
            console.log(client.current);
        }
    }, []);

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

    const disconnect = () => {
        if(client.current) {
            client.current.deactivate();
            setChatting([]);
            setMessage("");
            setRoomName("/");
            client.current = null;
        }
    }

    const getChat = async (url) => {
        const chat = await axios({
            method: "get",
            url: `${defaultURL}/chats?name=${url}&sessionId=${getSessionID()}`,
        });

        if(chat.data === "/topic/chatting/") {

        }
        setChatting(chat.data);
        console.log(chat);
    }

    const getRooms = async () => {
        const url = (roomName) ? `?name=${roomName}` : "";
        const rooms = await axios({
            method: "get",
            url: `${defaultURL}/rooms/${url}`,
            withCredentials: true
        });

        console.log(rooms);
        setRoomList(rooms.data);
    }

    const getSession = async () => {
        await axios({
            method: "get",
            url: defaultURL,
            withCredentials: true,
        });

        getRooms();
    }

    const sendChat = async () => {
        if(client.current && message) {
            client.current.publish({
                destination: `/pub/chat?sessionId=${getSessionID()}`,
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

    const stompFactory = (token, url) => {
        const client = new Client({
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

                client.subscribe(`/topic/${url}`, message => {
                    console.log(message);
                }, {
                    "Authorization": token
                }); //a에 구독

                getChat(url);
            },
            onStompError: frame => {
                console.log(`reported error: ${frame.headers["message"]}`);
                console.log(`error detail: ${frame.body}`);
            }
        });

        if("WebSocket" in window) {
            client.brokerURL = `${stompURL}/ws`;
        } else {
            client.webSocketFactory = () => { new SockJS(`${defaultURL}/sockjs`) }
        }

        return client;
    }

    const typeMessage = e => {
        setMessage(e.target.value);
    }

    const typeRoomName = e => {
        setRoomName(e.target.value);
    }

    useEffect(() => {
        if(!client.current) {
            getSession();
        } else {
            getChat();
        }
    }, []);

    return (client.current) ? <div className="chatRoom">
            <div className="chatRoomHeader">
                <button variant="outline">
                    <ArrowLeftIcon onClick={disconnect} />
                </button>
                <div className="chatRoomName">{roomName}</div>
            </div>
            { Object.values(chatting).map(chat => (
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

const genRandString = (length) => {
    return Math.random().toString(length);
}

const genSessionID = () => {
    return genRandString(8) + "-" + genRandString(4) + "-" + genRandString(4) + 
        + "-" + genRandString(4) + "-" + genRandString(12);
}

const getSessionID = () => {
    if(!localStorage.getItem("sessionId")) {
        localStorage.setItem("sessionId", genSessionID());
    }
    return localStorage.getItem("sessionId");
}

export default App;