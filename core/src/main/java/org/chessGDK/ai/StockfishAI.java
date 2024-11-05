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

    public StockfishAI(int depth) throws IOException {
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
        stockfishProcess = processBuilder.start();
        inputReader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        outputStream = stockfishProcess.getOutputStream();
        outputStream.write("uci\n".getBytes());
        outputStream.flush();
        // Read and print Stockfish's response
        String line;
        while ((line = inputReader.readLine()) != null) {
            System.out.println("Stockfish: " + line);
            if (line.equals("uciok")) {
                System.out.println("Stockfish: " + line);
                break;  // Stop reading when Stockfish signals that it is ready
            }
        }

        System.out.println("Stockfish: Universal Chess Interface - initialized");

        // Close streams and process when done
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



    public String getBestMove(String fen) throws IOException {
        // Send the position in FEN format
        String toSend = "position startpos\n";
        if (!fen.isEmpty())
            toSend = "position fen " + fen + "\n";

        outputStream.write(toSend.getBytes());
        outputStream.flush();

        // Request the best move
        toSend = "go depth " + depth + "\n";
        outputStream.write(toSend.getBytes());
        outputStream.flush();

        // Read the response until we find the best move
        String bestMove = null;
        String line;
        while ((line = inputReader.readLine()) != null) {
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1];  // Extract the move from the response
                break;
            }
        }

        return bestMove;  // Return the best move found
    }
}
