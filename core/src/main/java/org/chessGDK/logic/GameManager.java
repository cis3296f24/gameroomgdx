
package org.chessGDK.logic;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;

import org.chessGDK.network.Communication;
import org.chessGDK.pieces.*;
import org.chessGDK.ai.StockfishAI;

import org.chessGDK.ui.ChessBoardScreen;
import org.chessGDK.ui.GameOverScreen;
import org.chessGDK.ui.ScreenManager;

import java.io.IOException;
import java.util.Queue;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashMap;
import java.util.Stack;
import java.util.TimerTask;



public class GameManager extends ScreenAdapter {
    private final BlockingQueue<String> moveQueue = new LinkedBlockingQueue<>();
    private Thread gameLoopThread;

    private boolean whiteTurn;
    private boolean startColor;
    private final Piece[][] board;
    private final Blank[][] possibilities;
    private final StockfishAI stockfishAI;
    private final int DEPTH = 12;
    private boolean freeMode = false;
    private boolean puzzleMode = false;
    public volatile boolean gameOver = false;
    private boolean multiplayerMode = false;
    private final Stack<String> moveList;
    private Queue<String> solutionList;
    private String FEN;
    private final float duration = .15f;
    private GameOverScreen gameOverScreen;
    private final HashMap<String, String> castleMoves;
    private String legalMoves;
    private Communication communication;
    private boolean isHost;
    private String[] bestMove;
    private Sound moveSound;
    private Sound killSound;

    public GameManager(int difficulty, String fen, String HostOrClient) throws IOException {

        board = new Piece[8][8];
        possibilities = new Blank[8][8];
        legalMoves = "";
        FEN = fen.split("\t")[0];
        if (difficulty == -1) {
            freeMode = true;
        }
        else if (difficulty == -2) {
            puzzleMode = true;
            difficulty = 20;
            setupSolutions(fen.split("\t")[1].replace(" ", ","));
        }
        else if (difficulty == -3) {
            multiplayerMode = true;
            isHost = HostOrClient.equals("Host");
            communication = new Communication(this, isHost);
        }
        stockfishAI = new StockfishAI(DEPTH, difficulty, FEN);
        FEN = getFenFromAI();
        castleMoves = new HashMap<>();
        castleMoves.put("e1c1", "a1d1");
        castleMoves.put("e1g1", "h1f1");
        castleMoves.put("e8c8", "a8d8");
        castleMoves.put("e8g8", "h8f8");
        moveList = new Stack<>();
        parseFen(FEN);
        printBoard();
        updateBoardState();
        System.out.println("Start Color: " + (startColor ? "White" : "Black"));

        gameOverScreen = new GameOverScreen();

        moveSound = Gdx.audio.newSound(Gdx.files.internal("move.mp3"));
        killSound = Gdx.audio.newSound(Gdx.files.internal("kill.mp3"));

    }

    private void setupSolutions(String solutions) {
        solutionList = new ArrayDeque<>();
        solutions = solutions.replace("\n", "");
        solutionList.addAll(Arrays.asList(solutions.split(",")));
    }

    public void startGameLoopThread() {
        gameLoopThread = new Thread(this::gameLoop) {{
            setDaemon(true);
        }};
        gameLoopThread.start();
    }

    private void gameLoop () {
        if(puzzleMode)
            aiTurn();
        while (!gameOver) {
            try {
                // If it's the player's turn, wait for a move from the queue
                System.out.println("Waiting for move...");
                String move = moveQueue.take(); // Blocks until a move is added
                if(multiplayerMode && (whiteTurn == startColor)){
                    communication.sendMove(move);
                }
                movePiece(move);
                updateBoardState();
                toggleTurn();
                checkforcheckmate(move);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Game loop interrupted!");
                break;
            }
        }
        System.out.printf("Game over: %s - %s\n",gameOver, Thread.currentThread().getName());
    }

    private boolean isAITurn() {
        if (puzzleMode) {
            return whiteTurn == startColor;
        }
        return !whiteTurn; // Assuming AI is always black and whiteTurn tracks player turns
    }

    private void updateBoardState() {
        String toStock = appendLastMove(FEN);
        sendPosToStockfish(toStock);
        FEN = getFenFromAI();
        try {
            bestMove = getBestMove();
            legalMoves = getLegalMoves();        // Get all legal moves for after last move
            System.out.println("Legal Moves: " + legalMoves);
        } catch (IOException e) {
            System.out.println("update board state failed");
            gameOver = true;
        }
        if (puzzleMode) {
            System.out.println("Puzzle Move: " + solutionList.peek());
            if (solutionList.peek() == null) {
                System.out.println("Puzzle Completed!");
                gameOver = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        ScreenManager.getInstance().setScreen(gameOverScreen);
                    }
                }, 2f); // 2 seconds delay
            }
        }
        else {
            System.out.println("Best Move: " + bestMove[0]);
        }
    }

    public void toggleTurn() {
        whiteTurn = !whiteTurn;
        if (puzzleMode)
            puzzleTurns();
        else if (freeMode)
            freeTurns();
        else if (multiplayerMode)
            multiplayerTurns();
        else
            normalTurns();
    }

    private void normalTurns() {
        if (whiteTurn == startColor)
            playerTurn();
        else
            aiTurn();
    }

    private void freeTurns() {
        playerTurn();
    }

    private void puzzleTurns() {
        if (solutionList.isEmpty()) {
            gameOver = true;
            return;
        }
        if (whiteTurn != startColor)
            aiTurn();
        else
            playerTurn();
    }

    private void multiplayerTurns() {
        if((isHost && whiteTurn) || (!isHost && !whiteTurn)){
            playerTurn();
        }
        else
            System.out.println("Opponent's turn");
    }

    private void playerTurn() {
        System.out.println("Player's turn");
    }

    public void aiTurn() {
        System.out.println("AI's turn");
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (puzzleMode && !solutionList.isEmpty())
                    queueMove(solutionList.peek());
                else if (!puzzleMode)
                    queueMove(bestMove[0]);
            }
        }, .1f); // Delay by .5 second
    }

    public String[] getBestMove() throws IOException{
        return stockfishAI.getBestMove();
    }

    public String[] getBestMove(String fen) throws IOException {
        return stockfishAI.getBestMove(fen);
    }

    public String getLegalMoves() throws IOException {
        return stockfishAI.getLegalMoves();
    }

    public String getFenFromAI() {
        return stockfishAI.getFEN();
    }

    private void checkforcheckmate(String fen) {
        try {
            //System.out.println("FEN after move: " + fen + "\nStockfish's Best Move: " + bestMove);
            if (stockfishAI.checkmate(fen)) {
                System.out.println("Checkmate!");
                gameOver = true;

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        ScreenManager.getInstance().setScreen(gameOverScreen);
                    }
                }, 2f); // 2 seconds delay
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLegalMove(String move) {
        if(!puzzleMode && !stockfishAI.parseLegalMoves(move, legalMoves)){
            System.out.println("Illegal move");
            return false;
        }
        else if (puzzleMode && !solutionList.isEmpty()) {
            if (!solutionList.peek().equals(move)) {
                System.out.println("Incorrect puzzle move");
                return false;
            }
        }
        return true;
    }

    public boolean queueMove(String move) {
        return moveQueue.add(move);
    }

    public boolean movePiece(String move) {
        if (move.isEmpty())
            return false;
        if (puzzleMode && !solutionList.isEmpty())
            if(!solutionList.peek().equals(move))
                return false;
            else
                solutionList.remove();
        char[] parsedMove = parseMove(move);
        int startCol = parsedMove[0];
        int startRow = parsedMove[1];
        int endCol = parsedMove[2];
        int endRow = parsedMove[3];
        char newRank;
        Piece piece = board[startRow][startCol];

        Piece contested = board[endRow][endCol];
        // Ensure the right piece color is moving according to the turn
        if (piece != null) {
            if (contested != null) {
                contested.remove();
            }

            if (killSound != null) {
                killSound.play();
            }

            int tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
            float targetX = endCol * tileSize;
            float targetY = endRow * tileSize;
            piece.setVisible(true);
            piece.addAction(Actions.moveTo(targetX, targetY, duration, Interpolation.linear));
            board[endRow][endCol] = piece;
            board[startRow][startCol] = null;
            if (parsedMove.length == 5) {
                newRank = parsedMove[4];
                // Changes piece on next render call
                Gdx.app.postRunnable(() -> {
                    promote(newRank, endRow, endCol);
                });
            }
            if (piece instanceof Rook) {
                piece.setMoved(true);
            }
            if (piece instanceof King){
                piece.setMoved(true);
                handleCastling(move);
            }

            printBoard();
            System.out.println("Moved: " + move);
            moveList.push(move);

            if (moveSound != null) {
                moveSound.play(); // Play move sound
            }

            return true;
        }
        return false;
    }

    //King : "e1c1 e1g1 e8c8 e8g8", Rook: "a1d1 h1f1 a8d8 h8f8"
    private void handleCastling(String move) {
        if (!castleMoves.containsKey(move))
            return;
        String rookMove = castleMoves.get(move);
        char[] parsedMove = parseMove(rookMove);
        int startCol = parsedMove[0];
        int startRow = parsedMove[1];
        int endCol = parsedMove[2];
        int endRow = parsedMove[3];
        int tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
        float targetX = endCol * tileSize;
        float targetY = endRow * tileSize;
        board[endRow][endCol] = board[startRow][startCol];
        board[startRow][startCol] = null;
        board[endRow][endCol].addAction(Actions.moveTo(targetX, targetY, duration, Interpolation.linear));

    }

    public void undo() {
        String move = moveList.pop();
        char[] temp = parseMove(move);
        board[temp[2]][temp[3]].setPosition(temp[0], temp[1]);
    }

    private static char[] parseMove(String move) {
        if (move == null) {
            return null;
        }
        char[] parsed;
        //parsed = move.getBytes(StandardCharsets.US_ASCII);
        parsed = move.toCharArray();

        // Changes rank/file ASCII representation into array indexes
        parsed[0] -= 'a';
        parsed[1] -= '1';
        parsed[2] -= 'a';
        parsed[3] -= '1';
        return parsed;
    }


    private boolean checkLegalMoves(String move, String fen) {
        if(!stockfishAI.parseLegalMoves(move, legalMoves)){
            System.out.println("Illegal move");
            return false;
        }
        return true;
    }

    private boolean promote(char rank, int endRow, int endCol) {
        switch (rank) {
            case 'q':
                board[endRow][endCol] = new Queen(whiteTurn);
                return true;
            case 'r':
                board[endRow][endCol] = new Rook(whiteTurn);
                return true;
            case 'b':
                board[endRow][endCol] = new Bishop(whiteTurn);
                return true;
            case 'n':
                board[endRow][endCol] = new Knight(whiteTurn);
                return true;
            default:
                return false;
        }
    }

    public void sendPosToStockfish(String fen) {
        System.out.println(fen);
        stockfishAI.sendPosition(fen);
    }

    private String appendLastMove(String fen) {
        StringBuilder fenWithMove = new StringBuilder();
        fenWithMove.append(fen);
        if (!moveList.isEmpty()) {
            fenWithMove.append(" moves ");
            String move = moveList.peek();
            fenWithMove.append(move);
        }
        return fenWithMove.toString();
    }

    public Piece getPieceFromString(String p){
        Piece temp = null;
        if(p.equalsIgnoreCase("P")){
            temp = new Pawn(p.equals("P"));
        }
        else if (p.equalsIgnoreCase("R")) {
            temp = new Rook(p.equals("R"));
        }
        else if (p.equalsIgnoreCase("B")) {
            temp = new Bishop(p.equals("B"));

        }else if (p.equalsIgnoreCase("Q")) {
            temp = new Queen(p.equals("Q"));

        }else if (p.equalsIgnoreCase("K")) {
            temp = new King(p.equals("K"));

        }else if (p.equalsIgnoreCase("N")) {
            temp = new Knight(p.equals("N"));

        }
        return temp;
    }

    public void parseFen(String fen){
        for(int i = 0; i < board.length; i++) {
            Arrays.fill(board[i], null);
        }
        for (int i = 0; i < possibilities.length; i++) {
            for (int j = 0; j < possibilities[i].length; j++) {
                possibilities[i][j] = new Blank();
            }
        }
        int row = 7;
        int col = 0;
        for(int i = 0; i < fen.length(); i++){
            if(fen.charAt(i) == '/'){
                row--;
                col = 0;
                continue;
            } else if (Character.isDigit(fen.charAt(i))) {
                col = col + Character.getNumericValue(fen.charAt(i)) - 1;
            } else if (fen.charAt(i) == ' ') {
                break;
            }
            else{
                board[row][col] = getPieceFromString(Character.toString(fen.charAt(i)));
            }
            col++;
        }
        String[] parts = fen.split(" ");
        whiteTurn = parts[1].equals("w");
        if ((multiplayerMode && !isHost) || puzzleMode)
            startColor = !whiteTurn;
        else
            startColor = whiteTurn;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean isStartColor() {
        return startColor;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Blank[][] getPossibilities(){
        return possibilities;
    }

    public void printBoard() {
        for (int row = 7; row >= 0; row--) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    System.out.print(piece + " ");
                } else {
                    System.out.print("- ");
                }
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }

    @Override
    public void render(float delta) {
        super.render(delta);

    }

    public void exitGame() {
        if (moveSound != null) {
            moveSound.dispose(); // Added here
        }
        if(communication != null){
            communication.close();
        }
        if (killSound != null) {
            killSound.dispose();
        }
        if (stockfishAI != null) {
            try {
                stockfishAI.close();  // Close the Stockfish AI
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gameLoopThread.interrupt();
        Gdx.input.setInputProcessor(null);
        // Perform any other cleanup needed for the game
        System.out.println("GameManager closed.");
    }
}
