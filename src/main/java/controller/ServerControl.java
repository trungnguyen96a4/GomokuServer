package controller;

import dao.UserDAO;
import model.Match;
import model.Request;
import model.Response;
import model.User;
import view.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ServerControl {
    private int port;
    private Server server;
    private UserDAO userDAO;

    private Map<String, String> listUserChallenger;

    private Map<String, SocketChannel> listUserOnline;

    private Map<String, Match> listMatch;

    public ServerControl(int port) {
        listUserOnline = new HashMap<>();
        listUserChallenger = new HashMap<>();
        listMatch = new HashMap<>();
        userDAO = new UserDAO();
        this.port = port;
        initServer();
    }

    private void initServer() {
        server = new Server(port);

        Server.Callback callback = new Server.Callback() {
            @Override
            public void accept(ServerSocketChannel serverSocketChannel, Selector selector) {
                System.out.println("ServerControl: accept");
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        System.out.println("ServerControl: client connected");
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void receiveData(SelectionKey key) {
                System.out.println("ServerControl: receiveData");
                try {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.clear();
                    socketChannel.read(buffer);
                    String message = new String(buffer.array());
                    String typeRequest = message.substring(0, 2);
                    switch (typeRequest) {
                        case Request.LOGIN: {
                            login(socketChannel, message);
                            break;
                        }
                        case Request.REGISTER: {
                            register(socketChannel, message);
                            break;
                        }
                        case Request.GET_LIST_USER_ONLINE: {
                            getListUserOnline(socketChannel);
                            break;
                        }
                        case Request.CHALLENGE: {
                            challenger(socketChannel, message);
                            break;
                        }
                        case Request.CHALLENGER_OK: {
                            challengerOk(message);
                            break;
                        }
                        case Request.CHALLENGER_REJECT: {
                            challengerReject(message);
                            break;
                        }
                        case Request.LOGOUT: {
                            socketChannel.close();
                            break;
                        }
                        case Request.MOVE: {
                            move(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        server.setCallback(callback);
        server.start();
    }

    private void login(SocketChannel socketChannel, String message) {
        try {
            String[] list = message.split("/");
            System.out.println(list[1]);
            System.out.println(list[2]);
            User user = userDAO.selectUser(list[1], list[2]);
            ByteBuffer buffer;
            if (user != null) {
                listUserOnline.put(user.getUsername(), socketChannel);
                buffer = ByteBuffer.wrap(Response.LOGIN_SUCCESSFUL.getBytes());
            } else {
                buffer = ByteBuffer.wrap(Response.LOGIN_FAIL.getBytes());
            }
            System.out.println(new String(buffer.array()));
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void register(SocketChannel socketChannel, String message) {
        try {
            String[] list = message.split("/");
            boolean result = userDAO.checkExisted(list[1], list[2]);
            ByteBuffer buffer;
            if (result) {
                buffer = ByteBuffer.wrap(Response.REGISTER_DUPLICATE.getBytes());
            } else {
                result = userDAO.insertUser(list[1], list[2]);
                if (result) {
                    buffer = ByteBuffer.wrap(Response.REGISTER_SUCCESSFUL.getBytes());
                } else {
                    buffer = ByteBuffer.wrap(Response.REGISTER_FAIL.getBytes());
                }

            }
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getListUserOnline(SocketChannel socketChannel) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(Response.LIST_USER_ONLINE).append("/");
            Set keySet = listUserOnline.keySet();
            for (Object item : keySet) {
                builder.append(item).append("/");
            }
            if (builder.charAt(builder.length() - 1) == '/') {
                builder.deleteCharAt(builder.length() - 1);
            }
            ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void challenger(SocketChannel socketChannel, String message) {
        try {
            String secondPlayer = message.split("/")[1];
            String firstPlayer = searchUsername(socketChannel);
            listUserChallenger.put(firstPlayer, secondPlayer);
            StringBuilder builder = new StringBuilder();
            builder.append(Response.CHALLENGER).append("/").append(firstPlayer).append("/");
            ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String searchUsername(SocketChannel socketChannel) {
        try {
            Set keySet = listUserOnline.keySet();
            for (Object key : keySet) {
                if (listUserOnline.get(key).getRemoteAddress().toString().equals(socketChannel.getRemoteAddress().toString())) {
                    return (String) key;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void challengerOk(String message) throws IOException {
        String secondPlayer = message.split("/")[1];
        String firstPlayer = listUserChallenger.get(secondPlayer);
        Match match = new Match(listUserOnline.get(firstPlayer), listUserOnline.get(secondPlayer));
        String matchKey = UUID.randomUUID().toString();
        listMatch.put(matchKey, match);
        listUserOnline.remove(firstPlayer);
        listUserOnline.remove(secondPlayer);
        listUserChallenger.remove(secondPlayer);
        StringBuilder builder = new StringBuilder();
        builder.append(Response.CREATE_MATCH).append("/").append(matchKey);
        ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
        match.getoPlayer().write(buffer);
        match.getxPlayer().write(buffer);
        match.startGame();
    }

    private void challengerReject(String message) {
        try {
            String username = message.split("/")[1];
            SocketChannel socketChannel = listUserOnline.get(username);
            String fromUsername = listUserChallenger.get(username);
            listUserChallenger.remove(username);
            StringBuilder builder = new StringBuilder();
            builder.append(Response.CHALLENGER_REJECT).append("/");
            builder.append(fromUsername);
            ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes());
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void move(String message) {
        try {
            String[] strings = message.split("/");
            String keyMatch = strings[1];
            int row = Integer.parseInt(strings[2]);
            int col = Integer.parseInt(strings[3]);
            int player = Integer.parseInt(strings[4]);
            Match match = listMatch.get(keyMatch);
            match.moveChess(row, col, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
