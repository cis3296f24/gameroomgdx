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

/**
 * This class handles the connection between the server and client for multiplayer mode.
 * It uses Java Websockets.
 * Sends the move message over TCP connection.
 */
public class Communication {
    /**
     * The gameManager handles most of the game logic.
     */
    private GameManager gameManager;
    /**
     * The isServer checks for if the current player will be hosting the server.
     */
    private boolean isServer;
    /**
     * The server of the multiplayer mode.
     */
    private WebSocketServer server;
    /**
     * The client that will connect to the server.
     */
    private WebSocketClient client;

    /**
     * The constructor initializes the game manager and the boolean isServer
     * If condition checks on if the player is hosting the server or is a client connecting to a server.
     * The Server will open and bind to a port.
     * The Client will initiate a connection to the same port and IP as the server.
     * @param gm                            The game logic.
     * @param isServer                      Checking if the player is hosting.
     * @throws IOException
     * @throws URISyntaxException
     */
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

    /**
     * The chess move will be sent as a string across the TCp connection.
     * @param move          String that defines the move.
     */
    public void sendMove(String move) {
        if (isServer) {
            // Send move to all clients connected to the server
            server.broadcast(move);
        } else {
            // Send move to server
            client.send(move);
        }
    }

    /**
     * The close method is used to close the server and client.
     */
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
