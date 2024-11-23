package org.chessGDK.ai;

import java.io.*;
import java.lang.ProcessBuilder;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class StockfishAI {
    private final Process stockfishProcess;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final int depth;
    private int difficulty = 0;
    private String FEN = "startpos";

    public StockfishAI(int depth, int difficulty, String fen) throws IOException {
        String path = System.getProperty("assets.path");
        this.depth = depth;
        if (path == null) {
            path = getPathForJar(path);
        }
        System.out.println("Root Path: " + path);
        FileHandle stockfishHandle = Gdx.files.local(path + "stockfish/stockfish-windows-x86-64-avx2.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(stockfishHandle.path());
        this.difficulty = difficulty;
        stockfishProcess = processBuilder.start();
        // Sets input and
        input = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));
        sendCommand("uci");
        if (difficulty >= 0)
            setDifficulty();
        waitForResponse();

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

    // Send a command to Stockfish will add newline before sending
    private void sendCommand(String command) throws IOException {
        output.write(command + "\n");
        output.flush();
        //System.out.println("command sent :" + command);
    }

    private void setDifficulty() throws IOException {
        String command = "setoption name Skill Level value " + difficulty;
        sendCommand(command);
    }

    private void setPosition(String fen) throws IOException {
        if (fen.contains("0 1"))
            sendCommand("position startpos");
        else
            sendCommand("position fen " + fen);
    }

    // Wait for Stockfish's response
    private void waitForResponse() throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
            if (line.contains("uciok") || line.contains("readyok")) {
                break;
            }
        }
    }

    private String[] readMove() throws IOException {
        String[] moves = {"", ""};
        String line;
        while ((line = input.readLine()) != null) {
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
        setPosition(fen);
        // Request the best move
        String toSend = "go movetime 10";
        sendCommand(toSend);
        String[] moves = readMove();
        return moves[0].equalsIgnoreCase("(none)");
    }

    public String getBestMove(String fen) throws IOException {
        // Send the position in FEN format
        String toSend = fen;
        setPosition(toSend);
        // Request the best move
        toSend = "go movetime 100";
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
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
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
        setPosition(fen);
        // Request the perft command with depth 1
        sendCommand("go perft 1");

        String line;
        StringBuilder legalMoves = new StringBuilder();
        // Read Stockfish's response
        while ((line = input.readLine()) != null) {
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
    public boolean parseLegalMoves(String move, String legalMoves){
        String[] MoveArray = legalMoves.split(",");
        return Arrays.asList(MoveArray).contains(move);
    }
}
