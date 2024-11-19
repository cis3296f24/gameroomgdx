package org.chessGDK.ai;

import java.io.*;
import java.lang.ProcessBuilder;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class StockfishAI {
    private final Process stockfishProcess;
    private final BufferedReader inputReader;
    private final OutputStream outputStream;
    private final int depth;
    private int difficulty = 0;

    public StockfishAI(int depth, int difficulty) throws IOException {
        String rootPath = System.getProperty("root.path");
        System.out.println(rootPath);
        if (rootPath == null) {
            try {
                // Locate and open the manifest file in the JAR
                InputStream manifestStream = StockfishAI.class.getResourceAsStream("/META-INF/MANIFEST.MF");
                if (manifestStream != null) {
                    Manifest manifest = new Manifest(manifestStream);
                    Attributes attributes = manifest.getMainAttributes();

                    // Retrieve the "Asset-Path" property or any other property you defined
                    rootPath = attributes.getValue("Root-Path");
                    System.out.println("Root Path from Manifest: " + rootPath);
                } else {
                    System.err.println("Manifest not found in JAR.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileHandle stockfishHandle = Gdx.files.internal(rootPath + "/assets/stockfish/stockfish-windows-x86-64-avx2.exe");
        ProcessBuilder processBuilder = new ProcessBuilder(stockfishHandle.file().getAbsolutePath());
        this.depth = depth;
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
}
