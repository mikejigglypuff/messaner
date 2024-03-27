import React, { Component, useState, useEffect } from "react";
import roomPage from "/roomPage";
import mainPage from "/mainPage";

class Page extends React.Component {
    const [subscribed, setSubscribed] = useState(false);
    const [roomList, setRoomList] = useState(null);
    const [roomName, setRoomName] = useState("");
    const [message, setMessage] = useState("");
    const [chatting, setChatting] = useState(null);
    const [client, setClient]

    useEffect(() => {
        
    }, []);

    useEffect(() => {
        
    });

    const connect = async (url) => {
        const socket = new SockJS(url);
        setClient(Stomp.Client({
            onConnect: () => {
                stompClient.subscribe("/topic/a", res => {
                    setSubscribed(true);
                }); //a에 구독
            },
        }));
    }
    
    const sendChat = async () => {
        client.
    }
    
    const disconnect = async (roomName) => {
    
    }
    
    const getCookie = () => {
        const roomUrl = await axios({
            method: "get",
            url: "localhost:8080/",
            withCredentials: true,
        });
        
        this.getRooms(roomUrl);
    }
    
    const getRooms = async (e) => {
        const rooms = await axios.get(`localhost:8080/rooms/${e.target.value}`);
        setRoomList(JSON.parse(rooms.data));
    }
    
    const createChannel = async () => {
        const callbackUrl = await axios({
            method: "post",
            url: "localhost:8080/createChannel",
            withCredentials: true,
            body: {
                
            }
        });
    
        if(callbackUrl === "/") {
    
        } else {
    
        }
    }
    
    const getChat = async (roomName) => {
        const chat = await axios({
            url: `localhost:8080/room/chatting/${roomName}`,
            method: "get",
        });
        return JSON.parse(chat.data);
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
                            <div className="flex-1"> //useState를 사용해야 할 부분
                                <Input className="rounded-full border-0" placeholder="Type a message" onChange={this.typeMessage} />
                            </div>
                            <Button size="icon" onClick={sendMessage()}>
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
                    <div className="w-[300px] flex items-center"> //useState를 사용해야 할 부분
                        <Input placeholder="Search rooms" onChange={typeRoomName} />
                            <Button onClick={getRooms()}>Search</Button>
                        </div>
                    </div>
                    <div className="flex-1 min-h-0 mt-4">
                        <Card>
                            <CardContent className="p-0">
                                <div className="overflow-auto h-96">
                                    <div className="grid gap-px">
                                        roomList.map(room => {
                                            <Card className="rounded-0">
                                                <label onClick={connect(room.name)}>
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

default Page;