package org.chessGDK.ai;

import java.io.*;
import java.lang.ProcessBuilder;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class StockfishAI {
    private final Process stockfishProcess;
    private final BufferedReader inputReader;
    private final OutputStream outputStream;
    private final int depth;
    private int difficulty = 0;

    public StockfishAI(int depth, int difficulty) throws IOException {
        String path = System.getProperty("root.path");
        this.depth = depth;
        if (path == null) {
            path = getPathForJar(path);
        }

        System.out.println("Root Path: " + path);
        FileHandle stockfishHandle = Gdx.files.local(path + "stockfish/stockfish-windows-x86-64-avx2.exe");

        ProcessBuilder processBuilder = new ProcessBuilder(stockfishHandle.path());
        this.difficulty = difficulty;
        stockfishProcess = processBuilder.start();
        inputReader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        outputStream = stockfishProcess.getOutputStream();
        sendCommand("uci\n");
        sendDifficulty();
        readInfo();

        System.out.println("Stockfish: Universal Chess Interface - initialized");
        // Close streams and process when done
    }

    private static String getPathForJar(String path) {
        try {
            // Locate and open the manifest file in the JAR
            InputStream manifestStream = StockfishAI.class.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (manifestStream != null) {
                Manifest manifest = new Manifest(manifestStream);
                Attributes attributes = manifest.getMainAttributes();

                // Retrieve the "Asset-Path" property or any other property you defined
                path = attributes.getValue("Root-Path");
                System.out.println("Root Path from Manifest: " + path);
            } else {
                System.err.println("Manifest not found in JAR.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    // Send a command to Stockfish as a newline terminated string
    private void sendCommand(String command) throws IOException {
        outputStream.write((command).getBytes());
        outputStream.flush();
    }

    private void sendDifficulty() throws IOException {
        String command = "setoption name Skill Level value " + difficulty + "\n";
        System.out.println("command sent: " + command);
        sendCommand(command);
    }

    private void readInfo() throws IOException {
        String line;
        while ((line = inputReader.readLine()) != null) {
            System.out.println(line); // Print or process each line as needed
            if (line.equals("uciok") || line.equals("readyok")) {
                System.out.println("Stockfish: " + line);
                break; // Break the loop on specific end markers or responses
            }
        }
    }

    private String[] readMove() throws IOException {
        String[] moves = {"", ""};
        String line;
        while ((line = inputReader.readLine()) != null) {
          //  System.out.println("Stockfish:" + line);
            if (line.startsWith("bestmove")) {
                moves[0] = line.split(" ")[1];  // Extract the move from the response
                if (line.split(" ").length > 2)
                    moves[1] = line.split(" ")[3];
                break;
            }
        }
        return moves;
    }

    public boolean checkmate(String fen) throws IOException {
        if (fen.isEmpty())
            return false;
        String toSend = "position fen " + fen + "\n";
        sendCommand(toSend);
        // Request the best move
        toSend = "go movetime 10\n";
        sendCommand(toSend);
        String[] moves = readMove();
        return moves[0].equalsIgnoreCase("(none)");        
    }

    public String getBestMove(String fen) throws IOException {
        // Send the position in FEN format
        String toSend = "position startpos\n";
        if (!fen.isEmpty())
            toSend = "position fen " + fen + "\n";
        sendCommand(toSend);
        // Request the best move
        toSend = "go movetime 50\n";
        sendCommand(toSend);

        String[] moves = readMove();
        String bestMove = moves[0];
        String ponder = moves[1];

        System.out.println("BestMove: " + bestMove + "\nPonder: " + ponder);

        // Read the response until we find the best move
        return bestMove;  // Return the best move found
    }

    public void close() throws IOException {
        try {
            if (inputReader != null) {
                inputReader.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (stockfishProcess != null) {
                stockfishProcess.destroy();  // Terminate the Stockfish process
            }
            System.out.println("Stockfish closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getLegalMoves(String fen) throws IOException {
        // Send the position in FEN format
        String toSend = "position fen " + fen + "\n";
        sendCommand(toSend);

        // Request the perft command with depth 1
        sendCommand("go perft 1\n");

        String line;
        StringBuilder legalMoves = new StringBuilder();
        // Read Stockfish's response
        while ((line = inputReader.readLine()) != null) {
            //System.out.println("Stockfish: " + line);

            // Look for the "Legal moves:" line
            if (line.endsWith(": 1")) {
                line = line.substring(0,4);
                legalMoves.append(line).append(","); // Extract moves
                //System.out.println(legalMoves);
            }

            // Break on a stopping point to avoid infinite loops
            if (line.startsWith("Nodes searched")) {
                break;

           }


        }

        return legalMoves.toString();
    }
    public boolean checklLegalMoves(String move, String legalMoves){
        String[] MoveArray = legalMoves.split(",");
        return Arrays.asList(MoveArray).contains(move);
    }
}
