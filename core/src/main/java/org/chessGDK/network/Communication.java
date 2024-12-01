package org.chessGDK.network;

import com.esotericsoftware.kryonet.*;
import org.chessGDK.logic.GameManager;

import javax.print.attribute.standard.Severity;
import java.io.IOException;

// combining server and client to reduce duplicate code
public class Communication {
    private EndPoint endPoint;
    private GameManager gameManager;
    private boolean isServer;

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
                    String receivedFEN = (String) object;
                    System.out.println("FEN received: " + receivedFEN);
                    //gameManager.parseFen(receivedFEN);
                    gameManager.notifyMoveMade();
                }

            }

            @Override
            public void disconnected(Connection connection){
                System.out.println("Disconnected");
                close();
                System.exit(0);
            }
        });
    }

    public void sendFEN(String fen){
        if(isServer){
            ((Server) endPoint).sendToAllTCP(fen);
        } else {
            ((Client) endPoint).sendTCP(fen);
        }
    }

    public void close(){
        endPoint.stop();

        if(endPoint instanceof Client){
            endPoint.close();
        } else {
            endPoint.close();
        }

    }
}


