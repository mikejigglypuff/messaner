import React, { Component, useState, useEffect } from "react";
import roomPage from "/roomPage";
import mainPage from "/mainPage";
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { CardTitle, CardDescription, CardContent, Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { AvatarImage, AvatarFallback, Avatar } from "@/components/ui/avatar"

class Page extends React.Component {
    const [roomList, setRoomList] = useState(null);
    const [roomName, setRoomName] = useState("");
    const [message, setMessage] = useState("");
    const [chatting, setChatting] = useState(null);
    const [client, setClient] = useState(null);

    useEffect(() => {
        if(!client) {
            this.getCookie();
        } else {
            this.getChat();
        }
    }, [], [client]);

    useEffect(() => {

    }, [window.]);

    const connect = (url) => {
        const socket = new SockJS(url);
        setClient(Stomp.Client({
            brokerUrl: `ws://localhost:8080/room/${url}`,
            onConnect: () => {
                stompClient.subscribe(`/topic/${url}`, res => {
                    console.log("접속 완료");

                }); //a에 구독
            },
            reconnectDelay: 5000,

        }));

        client.activate();
    }
    
    const sendChat = async () => {
        if(client && message) {
            client.publish(
                destination: "ws://localhost:8080/chat",
                body: JSON.stringify({
                    room: roomName,
                    chat: message
                });
            );

            setMessage("");
        }
    }
    
    const disconnect = () => {
        if(client && client.connected()) {
            client.deactivate();
            setChatting(null);
            setMessage("");
            setRoomName("");
            setClient(null);
        }
    }
    
    const getCookie = async () => {
        await axios({
            method: "get",
            url: "localhost:8080/",
            withCredentials: true,
        });
        
        this.getRooms();
    }
    
    const getRooms = async () => {
        const rooms = await axios.get(`ws://localhost:8080${url}/${roomName}`);
        setRoomList(JSON.parse(rooms.data));
    }
    
    const createChannel = async () => {
        const callbackUrl = await axios({
            method: "post",
            url: "localhost:8080/createChannel",
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
    
    const getChat = async () => {
        const chat = await axios({
            method: "get",
            url: `localhost:8080/room/chatting/${roomName}`,
        });
        setChatting(JSON.parse(chat.data));
    }
    
    const typeMessage = e => {
        setMessage(e.target.value);
    }

    const typeRoomName = e => {
        setRoomName(e.target.value);
    }

    render() {
        return
            (subscribed) ? chatting.map(chat => {
                <div className="flex flex-col h-[480px] border rounded-lg">
                    <div className="flex items-center justify-between p-4 border-b dark:border-gray-800">
                        <Button variant="outline">
                            <ArrowLeftIcon className="h-6 w-6" onClick={this.disconnect()} />
                        </Button>
                        <div className="flex items-center space-x-4">
                            div className="font-bold">chat.room</div>
                        </div>
                    </div>
                    <div className="flex-1 p-4 grid gap-4">
                        <div className="flex items-start space-x-2">
                            <Avatar className="w-8 h-8">
                                <AvatarImage alt="Jenny Wilson" src="/placeholder-user.jpg" />
                                <AvatarFallback>JW</AvatarFallback>
                                <div className="text-xs text-gray-500 dark:text-gray-400">chat.writer</div>
                            </Avatar>
                            <div className="bg-gray-100 dark:bg-gray-800 rounded-xl p-4">
                                Hi! How can I help you today?
                                <div className="text-xs text-gray-500 dark:text-gray-400">chat.createdAt</div>
                            </div>
                        </div>
                    </div>
                        <div className="border-t flex p-2 gap-2">
                            <div className="flex-1">
                                <Input className="rounded-full border-0" placeholder="Type a message" onChange={this.typeMessage} />
                            </div>
                            <Button size="icon" onClick={this.sendChat()}>
                                <SendIcon className="h-4 w-4" />
                                <span className="sr-only">Send</span>
                            </Button>
                        </div>
                    </div>
                </div>
            }) :
            <div className="flex flex-col w-full min-h-0">
                <div className="flex items-center gap-5 min-w-0 w-full">
                    <div className="flex-1 min-w-0">
                        <div>Chat Rooms</div>
                    </div>
                    <div className="w-[300px] flex items-center">
                        <Input placeholder="Search rooms" onChange={this.typeRoomName} />
                            <Button onClick={this.getRooms()}>Search</Button>
                        </div>
                    </div>
                    <div className="flex-1 min-h-0 mt-4">
                        <Card>
                            <CardContent className="p-0">
                                <div className="overflow-auto h-96">
                                    <div className="grid gap-px">
                                        roomList.map(room => {
                                            <Card className="rounded-0">
                                                <label onClick={this.connect(room.name)}>
                                                    <CardContent className="flex items-center gap-4 py-3">
                                                        <div className="flex-1 min-w-0">
                                                            <CardTitle className="text-base font-medium leading-6">room.name</CardTitle>
                                                        </div>
                                                        <div className="flex items-center gap-2">
                                                            <UserIcon className="w-4 h-4" />
                                                            <span className="text-sm">room.subscribers.length</span>
                                                        </div>
                                                     </CardContent>
                                                </label>
                                            </Card>
                                        })
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </div>
            </div>;
    }
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

export default Page;