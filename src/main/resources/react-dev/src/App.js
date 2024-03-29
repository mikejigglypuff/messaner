import logo from './logo.svg';

import React, { useState, useEffect } from "react";
import "./index.css";
import SockJS from "sockjs-client";
import axios from "axios";
import { Stomp } from "@stomp/stompjs";

function App() {
    const [roomList, setRoomList] = useState([]);
    const [roomName, setRoomName] = useState("");
    const [message, setMessage] = useState("");
    const [chatting, setChatting] = useState([]);
    const [client, setClient] = useState(null);

    useEffect(() => {
        if(!client) {
            getCookie();
        } else {
            getChat();
        }
    }, [], [client]);

    const connect = (url) => {
        const socket = new SockJS(url);
        setClient(Stomp.Client({
            brokerUrl: `${process.env.DEFAULT_URL}/main/room/${url}`,
            onConnect: () => {
                client.subscribe(`/topic/${url}`, res => {
                    console.log("접속 완료");

                }); //a에 구독
            },
            reconnectDelay: 5000,
            heartbeat: {
                incoming: 10000,
                outgoing: 10000
            },

        }));

        client.activate();
    }

    const sendChat = async () => {
        if(client && message) {
            client.publish({
                destination: `${process.env.DEFAULT_URL}/app/chat`,
                body: JSON.stringify({
                    room: roomName,
                    chat: message
                })
            });

            setMessage("");
        }
    }

    const disconnect = () => {
        if(client && client.connected()) {
            client.deactivate();
            setChatting([]);
            setMessage("");
            setRoomName("");
            setClient(null);
        }
    }

    const getCookie = async () => {
        await axios({
            method: "get",
            url: `${process.env.DEFAULT_URL}/`,
            withCredentials: true,
        });

        getRooms();
    }

    const getRooms = async () => {
        const url = (roomName) ? roomName : "";
        const rooms = await axios.get(`${process.env.DEFAULT_URL}/rooms/${url}`);
        setRoomList(JSON.parse(rooms.data));
    }

    const createChannel = async () => {
        if(roomName) {
            const callbackUrl = await axios({
                method: "post",
                url: `${process.env.DEFAULT_URL}/createChannel`,
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

    const getChat = async () => {
        if(roomName && client) {
            const chat = await axios({
                method: "get",
                url: `${process.env.DEFAULT_URL}/room/chatting/${roomName}`,
            });
            setChatting(JSON.parse(chat.data));
        }
    }

    const typeMessage = e => {
        setMessage(e.target.value);
        console.log(e.target.value);
    }

    const typeRoomName = e => {
        setRoomName(e.target.value);
        console.log(e.target.value);
    }

    return (client) ? chatting.map(chat => {
            <div className="flex flex-col h-[480px] border rounded-lg">
                <div className="flex items-center justify-between p-4 border-b dark:border-gray-800">
                    <button variant="outline">
                        <ArrowLeftIcon className="h-6 w-6" onClick={disconnect()} />
                    </button>
                    <div className="flex items-center space-x-4">
                        <div className="font-bold">chat.room</div>
                    </div>
                </div>
                <div className="flex-1 p-4 grid gap-4">
                    <div className="flex items-start space-x-2">
                        <div className="w-8 h-8">
                            <div alt="Jenny Wilson" src="/placeholder-user.jpg" />
                            <div>JW</div>
                            <div className="text-xs text-gray-500 dark:text-gray-400">chat.writer</div>
                        </div>
                        <div className="bg-gray-100 dark:bg-gray-800 rounded-xl p-4">
                            Hi! How can I help you today?
                            <div className="text-xs text-gray-500 dark:text-gray-400">chat.createdAt</div>
                        </div>
                    </div>
                </div>
                <div className="border-t flex p-2 gap-2">
                    <div className="flex-1">
                        <input className="rounded-full border-0" placeholder="Type a message" onChange={typeMessage} />
                    </div>
                    <button size="icon" onClick={sendChat()}>
                        <SendIcon className="h-4 w-4" />
                         <span className="sr-only">Send</span>
                    </button>
                </div>
            </div>
        }) :
        <div className="flex flex-col w-full min-h-0">
            <div className="flex items-center gap-5 min-w-0 w-full">
                <div className="flex-1 min-w-0">
                    <div>Chat Rooms</div>
                </div>
                <div className="w-[300px] flex items-center">
                    <input placeholder="Search rooms" onChange={typeRoomName} />
                    <button onClick={getRooms()}>Search</button>
                    <button onClick={createChannel}>채널</button>
                </div>
            </div>
            <div className="flex-1 min-h-0 mt-4">
                <div className="rounded-0">
                    { roomList && roomList.map(r => {
                        <label onClick={connect(r.name)}>
                            <div className="flex items-center gap-4 py-3">
                                <div className="flex-1 min-w-0">
                                    <div className="text-base font-medium leading-6">r.name</div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <UserIcon className="w-4 h-4" />
                                    <span className="text-sm">r.subscribers.length</span>
                                </div>
                            </div>
                        </label>
                    }) } 
                </div>
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