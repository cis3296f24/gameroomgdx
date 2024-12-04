package org.chessGDK.ai;

import java.io.*;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private String FEN;
    private List<String> returnList;


    public StockfishAI(int depth, int difficulty, String fen) throws IOException {
        String path = System.getProperty("assets.path");
        this.depth = depth;
        returnList = new ArrayList<String>();
        if (path == null) {
            path = getPathForJar(path);
        }
        System.out.println("Root Path: " + path);
        FileHandle stockfishHandle = Gdx.files.local(path + "stockfish/stockfish-windows-x86-64-avx2.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(stockfishHandle.path());
        stockfishProcess = processBuilder.start();
        // Sets input and output
        input = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

        sendCommand("uci");
        this.difficulty = difficulty;
        if (difficulty >= 0)
            setDifficulty();
        waitForResponse();
        System.out.println("Stockfish: Universal Chess Interface - initialized");
        setPosition(fen);
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

    private boolean sendCommand(String command) {
        returnList.clear();
        //System.out.println("Command sent: " + command);
        if (!stockfishProcess.isAlive()) {
            System.out.println("Stockfish process is dead");
            return false;
        }
        try {
            output.write(command + "\n");
            output.flush();
        } catch (IOException e) {
            System.out.println("Error writing to Stockfish: " + e.getMessage());
        }
        return true;
        //System.out.println("command sent :" + command);
    }

    /**
     * The method reads a line of response from Stockfish.
     * @return
     */
    private String getLine() {
        String line = null;
        try {
            line = input.readLine();
            if (line == null)
                return null;
            returnList.add(line);
        } catch (IOException e) {
            System.out.println("Error reading from Stockfish: " + e.getMessage());
        }
        return line;
    }

    private void setDifficulty() {
        String command = "setoption name Skill Level value " + difficulty;
        sendCommand(command);
    }

    private void setPosition(String fen) {
        if (fen.contains("startpos"))
            sendCommand(fen);
        else
            sendCommand("position fen " + fen);
        updateFEN();
    }

    public void sendPosition(String fen) {
        setPosition(fen);
    }

    // Wait for Stockfish's response
    private void waitForResponse(){
        String line;
        while ((line = getLine()) != null) {
            System.out.println(line);
            if (line.contains("uciok") || line.contains("readyok")) {
                break;
            }
        }
    }

    private String[] readMove(){
        String[] moves = {"", ""};
        String line;
        while ((line = getLine()) != null) {
            //System.out.println("Stockfish: " + line);
            if (line.startsWith("bestmove")) {
                moves[0] = line.split(" ")[1];  // Extract the move from the response
                if (line.split(" ").length > 2)
                    moves[1] = line.split(" ")[3];
                break;
            }
        }
        return moves;
    }

    public boolean checkmate() throws IOException {
        //setPosition(fen);
        // Request the best move
        String toSend = "go movetime 10";
        sendCommand(toSend);
        String[] moves = readMove();
        return moves[0].equalsIgnoreCase("(none)");
    }

    public String getFEN() {
        return FEN;
    }

    public void updateFEN() {
        sendCommand("d");
        String line;
        while ((line = getLine()) != null) {
            if(line.startsWith("Fen: ")) {
                FEN = line.substring(5);
                System.out.println(FEN);
                break;
            }
        }
    }

    public String[] getBestMove(){
        String toSend = "go movetime 400";
        sendCommand(toSend);
        return readMove();
    }

    public String[] getBestMove(String fen){
        // Send the position in FEN format
        String toSend = fen;
        setPosition(toSend);
        // Request the best move
        toSend = "go movetime 400";
        sendCommand(toSend);

        // Read the response until we find the best move
        return readMove();  // Return the best move found
    }

    public String getLegalMoves(){
        // Send the position in FEN format
        // Request the perft command with depth 1
        sendCommand("go perft 1");

        String line;
        StringBuilder legalMoves = new StringBuilder();
        // Read Stockfish's response
        while ((line = getLine()) != null) {
            //System.out.println("Stockfish: " + line);
            // Break on a stopping point to avoid infinite loops
            if (line.startsWith("Nodes searched")) {
                break;
            }
            // Look for the "Legal moves:" line
            if (line.endsWith(": 1")) {
                line = line.substring(0,4);
                legalMoves.append(line).append(","); // Extract moves
                //System.out.println(legalMoves);
            }


        }
        sendCommand("stop");
        return legalMoves.toString();
    }
    public boolean parseLegalMoves(String move, String legalMoves){
        String[] MoveArray = legalMoves.split(",");
        return Arrays.asList(MoveArray).contains(move);
    }

    public void printBoard() {
        sendCommand("d");
        String line;
        while ((line = getLine()) != null) {
            System.out.println(line);
        }
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
}
