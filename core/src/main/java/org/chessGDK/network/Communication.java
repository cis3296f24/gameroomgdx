package org.chessGDK.network;

import com.esotericsoftware.kryonet.*;
import org.chessGDK.logic.GameManager;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// combining server and client to reduce duplicate code
public class Communication {
    private EndPoint endPoint;
    private GameManager gameManager;
    private boolean isServer;
    private boolean startColor;
    public String startPos;

    public Communication(GameManager gm, boolean isServer) throws IOException {
        this.gameManager = gm;
        this.isServer = isServer;
        if(isServer){
            Server server = new Server();
            endPoint = server;
            server.start();
            server.bind(53555, 54777);
        } else {
            Client client = new Client();
            endPoint = client;
            client.start();
            client.connect(5000, "127.0.0.1", 53555, 54777);
        }

        endPoint.getKryo().register(String.class);
        endPoint.addListener(new Listener(){
            @Override
            public void received(Connection connection, Object object){
                if(object instanceof String){
                    String string = (String) object;
                    System.out.println("Message received: " + string);
                    gameManager.queueMove(string);
                }
            }

//            @Override
//            public void connected(Connection connection){
//                if (isServer) {
//                    // Retrieve the current FEN from the GameManager
//                    String currentFEN = gameManager.getFenFromAI(); // Ensure you have this method in GameManager
//                    System.out.println("Client connected. Sending current FEN: " + currentFEN);
//
//                    // Send the FEN to the newly connected client
//                    connection.sendTCP(currentFEN);
//                }
//            }

            @Override
            public void disconnected(Connection connection){
                System.out.println("Disconnected");
                close();
                System.exit(0);
            }
        });
    }

    public void sendMove(String move){
        if(isServer){
            ((Server) endPoint).sendToAllTCP(move);
        } else {
            ((Client) endPoint).sendTCP(move);
        }
    }

    public void sendFen(String fen){

    }

    public void close(){
        endPoint.stop();

        endPoint.close();

    }
}


