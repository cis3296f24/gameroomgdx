package org.chessGDK.network;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.chessGDK.logic.GameManager;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public class Communication {
    private GameManager gameManager;
    private boolean isServer;
    private WebSocketServer server;
    private WebSocketClient client;

    public Communication(GameManager gm, boolean isServer) throws IOException, URISyntaxException {
        this.gameManager = gm;
        this.isServer = isServer;

        if (isServer) {
            server = new WebSocketServer(new InetSocketAddress("127.0.0.1", 53555)) {
                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    System.out.println("New client connected: " + conn.getRemoteSocketAddress());
                    //conn.send(gm.getFenFromAI());
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    System.out.println("Message received: " + message);
                    gameManager.queueMove(message);
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    ex.printStackTrace();
                }

                @Override
                public void onStart() {
                    System.out.println("Server started successfully");
                }
            };
            server.start();
        } else {
            client = new WebSocketClient(new URI("ws://127.0.0.1:53555")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to server");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("Message received from server: " + message);
                    if (message.contains("/"))
                        gm.sendPosToStockfish(message);
                    else
                        gameManager.queueMove(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Connection closed");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        }
    }

    public void sendMove(String move) {
        if (isServer) {
            // Send move to all clients connected to the server
            server.broadcast(move);
        } else {
            // Send move to server
            client.send(move);
        }
    }

    public void close() {
        if (isServer) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            client.close();
        }
    }
}
