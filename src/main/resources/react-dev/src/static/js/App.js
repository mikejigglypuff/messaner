import React, { useState, useEffect, useRef } from "react";
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
        <div className="bg-gray-100 p-4">
        {/* 채팅방 UI */}
            <div className="max-w-2xl mx-auto bg-white shadow-lg rounded-lg">
                <div className="flex items-center p-4 border-b">
                    <button className="p-2 rounded-full hover:bg-gray-200" onClick={() => disconnect(roomName)}>
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 19l-7-7 7-7" />
                        </svg>
                    </button>
                    <h1 className="flex-grow text-center text-xl font-bold">{roomName}</h1>
                </div>
                {   
                    Object.values(chatting).map(chat => (
                        <div className="p-4" key={`chat_{$chat.writer}_${chat.createdAt}`}>
                            <div className="text-center text-gray-500 mb-4">YYYY-MM-dd</div>
                            <div className="space-y-4">
                                <div className="flex items-center">
                                    <div className="font-bold">{chat.writer}</div>
                                    <div className="flex-grow mx-2 p-2 bg-gray-200 rounded">{chat.message}</div>
                                    <div className="text-gray-500">{chat.createdAt}</div>
                                </div>
                            </div>
                        </div>
                )) }
                <div className="mt-4 flex items-center">
                    <input
                        type="text"
                        placeholder="메시지를 입력하세요..."
                        className="flex-grow p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        onChange={typeMessage}
                    />
                    <button className="ml-2 p-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600" onClick={sendChat}>전송</button>
                </div>
            </div>
        </div> :
        <div className="bg-gray-100 flex items-center justify-center min-h-screen">
            {/* 메인 페이지 UI */}
            <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-4xl">
                <h1 className="text-3xl font-bold text-center mb-6">Title</h1>
                <div className="flex items-center justify-center mb-6">
                    <input type="text" placeholder="채널명을 입력하세요" className="border border-gray-300 rounded-lg p-2 w-full max-w-md" onChange={typeRoomName} />
                </div>
                <div className="flex items-center justify-center space-x-4 mb-6">
                    <button className="bg-green-500 text-white px-4 py-2 rounded-lg" onClick={getRooms}>Search</button>
                    <button className="bg-green-500 text-white px-4 py-2 rounded-lg" onClick={createChannel}>채팅방 생성</button>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {
                        Object.values(roomList).map(val => (
                        <div
                          className="bg-green-100 border border-green-500 rounded-lg p-4 flex justify-between items-center"
                          onClick={() => connect(val.name)}
                          key={`room_${val.name}`}
                        >
                            <span>{val.name}</span>
                            <div className="flex items-center space-x-1">
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M10 2a6 6 0 00-6 6v4a6 6 0 0012 0V8a6 6 0 00-6-6zM8 8a2 2 0 114 0 2 2 0 01-4 0zm-2 6a4 4 0 018 0H6z" clipRule="evenodd" />
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