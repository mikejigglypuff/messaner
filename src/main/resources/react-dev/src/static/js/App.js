import React, { useState, useEffect, useRef, useCallback, memo } from "react";
import SockJS from "sockjs-client";
import axios from "axios";
import { Client } from "@stomp/stompjs";

const defaultURL = process.env.REACT_APP_DEFAULT_URL;
const stompURL = process.env.REACT_APP_STOMP_URL;

axios.interceptors.request.use(req => {
    const token = localStorage.getItem("token");
    if(token) {
        req.headers.Authorization = token;
    }

    return req;
}, err => {
    console.error(err);
    return Promise.reject(err);
});

axios.interceptors.response.use(res => {
    const token = res.headers.getAuthorization();
    if(token) {
        localStorage.removeItem("token");
        localStorage.setItem("token", token);
        console.log(token);
    }

    return res;
}, err => {
    console.error(err);
    return Promise.reject(err);
});

function App() {
    const [chatting, setChatting] = useState([]);
    const [connected, setConnected] = useState(false);
    const [message, setMessage] = useState("");
    const [roomList, setRoomList] = useState([]);
    const [roomName, setRoomName] = useState("");
    const client = useRef(null);

    const connect = url => {
        setRoomName(url);
        const token = localStorage.getItem("token");
        if(token) {
            if(!client.current) { client.current = stompFactory(token, url); }
            client.current.activate();
            console.log(client.current);
        }
    };

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

    const disconnect = url => {
        if(client.current) {
            if(client.current.connected) {
                unsubscribeRoom(url);
            }
            client.current.deactivate().then(() => {
                console.log('Deactivated');
            }).catch((error) => {
                console.error('Error during deactivation:', error);
                if (error.message === 'Session closed.') {
                    console.log('Session is already closed.');
                }
            });
        }
    }

    const getChat = async (url) => {
        const chat = await axios({
            method: "get",
            url: `${defaultURL}/chats?room=${url}`,
        });

        if(chat.data === `/topic/chatting/${url}`) {
            subscribeRoom(client.current, url);
            getChat(url);
        } else {
            setChatting(chat.data);
            console.log(chat);
        }
    }

    const getRooms = async () => {
        const url = (roomName) ? `?name=${roomName}` : "";
        const rooms = await axios({
            method: "get",
            url: `${defaultURL}/rooms${url}`,
            withCredentials: true
        });

        console.log(rooms);
        console.log(Object.values(rooms.data));
        setRoomList((rooms.data === "no room matches") ? [] : rooms.data);
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
        if(client.current && message && roomName && client.current.connected) {
            console.log("start publishing");
            let token = localStorage.getItem("token");
            client.current.publish({
                destination: `/pub/${roomName}`,
                headers: {
                    "Authorization" : token
                },
                body: JSON.stringify({
                    room: roomName,
                    chat: message
                }),
            });
            setMessage("");
        }
    }

    const stompFactory = (token, url) => {
        const stompClient = new Client({
            brokerURL: `${stompURL}/ws`,
            connectHeaders: {
                "Authorization" : token
            },
            heartbeat: {
                incoming: 15000,
                outgoing: 15000
            },
            onDisconnect: () => {
                setChatting([]);
                setConnected(false);
                setMessage("");
                setRoomName("");
            },
            onStompError: frame => {
                console.log(`reported error: ${frame.headers["message"]}`);
                console.log(`error detail: ${frame}`);
            }
        });

        stompClient.onConnect = () => {
            setConnected(true);
        };

        window.addEventListener("beforeunload", e => {
            disconnect(url);
        });

        if(!("WebSocket" in window)) {
           stompClient.webSocketFactory = () => { new SockJS(`${defaultURL}/sockjs`) }
           stompClient.brokerURL = `${defaultURL}/sockjs`;
        }

        return stompClient;
    }

    const subscribeRoom = (stompClient, url) => {
        const token = localStorage.getItem("token");
        console.log(`/topic/${url}`);
        stompClient.subscribe(`/topic/${url}`, message => {
            console.log(message);

            setChatting(prevChatting => [...prevChatting, JSON.parse(message.body)]);
        }, {
            "Authorization": token
        }); //a에 구독
    }

    const typeMessage = e => {
        setMessage(e.target.value);
    }

    const typeRoomName = e => {
        setRoomName(e.target.value);
    }

    const unsubscribeRoom = url => {
        if(client.current && client.current.connected) {
            const token = localStorage.getItem("token");
            client.current.unsubscribe(`/topic/${url}`, {
                "Authorization": token
            });
        }
    }

    useEffect(() => {
        if(!(client.current && client.current.connected)) {
            getSession();
        } else {
            getChat(roomName);
        }
    }, []);

    useEffect(() => {
        getRooms();
    }, [roomName]);

    useEffect(() => {

    }, [chatting, message]);

    useEffect(() => {
        if(connected) {
            subscribeRoom(client.current, roomName);
            getChat(roomName);
        }
    }, [connected]);

    return (client.current && client.current.connected) ?
        <div class="bg-gray-100 p-4">
        {/* 채팅방 UI */}
            <div class="max-w-2xl mx-auto bg-white shadow-lg rounded-lg">
                <div class="flex items-center p-4 border-b">
                    <button class="p-2 rounded-full hover:bg-gray-200" onClick={() => disconnect(roomName)}>
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <h1 class="flex-grow text-center text-xl font-bold">{roomName}</h1>
                </div>
                
                {   
                    Object.values(chatting).map(chat => (
                        <div class="p-4">
                            <div class="text-center text-gray-500 mb-4">YYYY-MM-dd</div>
                            <div class="space-y-4">
                                <div class="flex items-center">
                                    <div class="font-bold">{chat.writer}</div>
                                    <div class="flex-grow mx-2 p-2 bg-gray-200 rounded">{chat.message}</div>
                                    <div class="text-gray-500">{chat.createdAt}</div>
                                </div>
                            </div>
                        </div>
                )) }
                </div>
        </div> :
        <div class="bg-gray-100 flex items-center justify-center min-h-screen">
            {/* 메인 페이지 UI */}
            <div class="bg-white p-8 rounded-lg shadow-lg w-full max-w-4xl">
                <h1 class="text-3xl font-bold text-center mb-6">Title</h1>
                <div class="flex items-center justify-center mb-6">
                    <input type="text" placeholder="채널명을 입력하세요" class="border border-gray-300 rounded-lg p-2 w-full max-w-md" onChange={typeRoomName} />
                </div>
                <div class="flex items-center justify-center space-x-4 mb-6">
                    <button class="bg-green-500 text-white px-4 py-2 rounded-lg" onClick={getRooms}>Search</button>
                    <button class="bg-green-500 text-white px-4 py-2 rounded-lg" onClick={createChannel}>채팅방 생성</button>
                </div>
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {
                        Object.values(roomList).map(val => (
                        <div class="bg-green-100 border border-green-500 rounded-lg p-4 flex justify-between items-center" onClick={() => connect(val.name)}>
                            <span>{val.name}</span>
                            <div class="flex items-center space-x-1">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fill-rule="evenodd" d="M10 2a6 6 0 00-6 6v4a6 6 0 0012 0V8a6 6 0 00-6-6zM8 8a2 2 0 114 0 2 2 0 01-4 0zm-2 6a4 4 0 018 0H6z" clip-rule="evenodd" />
                                </svg>
                                <span>{val.subscribers.length}</span>
                            </div>
                        </div>
                    )) }
                </div>
             </div>
        </div>
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