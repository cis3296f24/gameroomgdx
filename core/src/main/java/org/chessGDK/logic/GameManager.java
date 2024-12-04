
package org.chessGDK.logic;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;

import org.chessGDK.network.Communication;
import org.chessGDK.pieces.*;
import org.chessGDK.ai.StockfishAI;

import org.chessGDK.ui.GameOverScreen;
import org.chessGDK.ui.ScreenManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * The GameManager class handles the logic of the chess game which applies to the player vs AI mode, puzzle mode, player
 * vs player mode, free mode. It also handles save states. It implements the game loop, piece moves, turn order, player
 * input and board state,
 */
public class GameManager extends ScreenAdapter {
    /** Queue for waiting for moves in game loop. */
    private final BlockingQueue<String> moveQueue = new LinkedBlockingQueue<>();
    /** Thread that manages the game loop. */
    private Thread gameLoopThread;
    /** Indicates whether it's white's turn. */
    private boolean whiteTurn;
    /** Indicates the player's color (true for white, false for black). */
    private boolean playerColor;
    /** 2D Array corresponding to a chessboard with pieces. */
    private final Piece[][] board;
    /** 2D array representing possible moves for a piece. */
    private final Blank[][] possibilities;
    /** AI engine for generating moves and validating game state(See stockfish AI class). */
    private final StockfishAI stockfishAI;
    /** Indicates if playing free mode. */
    private boolean freeMode = false;
    /** Indicates if playing puzzle mode. */
    private boolean puzzleMode = false;
    /** Indicates if game is over. */
    public volatile boolean gameOver = false;
    /** Indicates if playing multiplayer mode. */
    public boolean multiplayerMode = false;
    /** Stack of moves played for move history */
    private final Stack<String> moveList;
    /** Queue of move sequences for puzzle mode */
    private Queue<String> solutionList;
    /** Board reprsentation in FEN format which can be used with Stockfisj */
    private String FEN;
    /** Duration of animations for moving pieces. */
    private final float duration = .15f;
    /** Screen displayed when game is over */
    private final GameOverScreen gameOverScreen;
    /** Map of moves which reprsenting castling the King and Rook */
    private final HashMap<String, String> castleMoves;
    /** String of all legal moves in the current position. */
    private String legalMoves;
    /** Handles network communication for multiplayer games. (See communication class) */
    private Communication communication;
    /** Indicates if player is the host in multiplayer mode*/
    private boolean isHost;
    /** Best move suggested from Stockfish. */
    private String[] bestMove;
    /** Sound when a piece successfully moves. */
    private final Sound moveSound;
    /** Sound when a piece gets captured. */
    private final Sound killSound;
    /**
     * Constructor for the GameManager with specified difficulty, board state, and multiplayer role.
     *
     * @param difficulty  the difficulty level (-1 for free mode, -2 for puzzle mode,
     *                    -3 for multiplayer, otherwise AI difficulty level)
     * @param fen         the initial board state in FEN format
     * @param HostOrClient specifies "Host" or "Client" role for multiplayer
     */
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
            try {
                communication = new Communication(this, isHost);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        int DEPTH = 12;
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
        System.out.println("Start Color: " + (playerColor ? "White" : "Black"));

        gameOverScreen = new GameOverScreen();

        moveSound = Gdx.audio.newSound(Gdx.files.internal("move.mp3"));
        killSound = Gdx.audio.newSound(Gdx.files.internal("kill.mp3"));

    }
    /**
     * Initializes the puzzle solution list.
     *
     * @param solutions the solution sequence as a string
     */
    private void setupSolutions(String solutions) {
        solutionList = new ArrayDeque<>();
        solutions = solutions.replace("\n", "");
        solutionList.addAll(Arrays.asList(solutions.split(",")));
    }
    /**
     * Starts gameloop function in a new thread.
     *
     */
    public void startGameLoopThread() {
        gameLoopThread = new Thread(this::gameLoop) {{
            setDaemon(true);
        }};
        gameLoopThread.start();
    }

    /**
     *  Loop that handles turn order, updates the game state, starts puzzles and loading from savestates.
     */
    private void gameLoop () {
        // Handles starting puzzles and loading from save states
        if(whiteTurn != playerColor)
            aiTurn();
        while (!gameOver) {
            try {
                // If it's the player's turn, wait for a move from the queue
                checkforcheckmate();
                System.out.println("Waiting for move...");
                String move = moveQueue.take(); // Blocks until a move is added
                if(multiplayerMode && (whiteTurn == playerColor)){
                    communication.sendMove(move);
                }
                movePiece(move);
                updateBoardState();
                toggleTurn();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Game loop interrupted!");
                break;
            }
        }
        System.out.printf("Game over: %s - %s\n",gameOver, Thread.currentThread().getName());
    }

    /**
     * Updates the board by getting new legal moves and it also checks puzzle completion in puzzle mode
     */
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

    /**
     * Toggles the turn between players based on the mode.
     */
    public void toggleTurn() {
        whiteTurn = !whiteTurn;
        if (freeMode)
            return;
        if (puzzleMode && solutionList.isEmpty()) {
            gameOver = true;
        }
        else if (multiplayerMode)
            multiplayerTurns();
        else
            normalTurns();
    }

    /**
     * Handles turns for Player vs AI mode
     */
    private void normalTurns() {
        if (whiteTurn == playerColor)
            playerTurn();
        else
            aiTurn();
    }
    /**
     * Handles turns for Player vs Player mode
     */
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
    /**
     * Executes the AI's turn in game and handles AI move in puzzles.
     */
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

    /**
     * see method in StockFishAI class
     */
    public String[] getBestMove() throws IOException{
        return stockfishAI.getBestMove();
    }
    /**
     * see method in StockFishAI class
     */
    public String getLegalMoves() throws IOException {
        return stockfishAI.getLegalMoves();
    }
    /**
     * see method in StockFishAI class
     */
    public String getFenFromAI() {
        return stockfishAI.getFEN();
    }
    /**
     * Checks whether the current game state is a checkmate.
     */
    private void checkforcheckmate() {
        try {
            //System.out.println("FEN after move: " + fen + "\nStockfish's Best Move: " + bestMove);
            if (stockfishAI.checkmate()) {
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
    /**
     * Checks whether move is legal with Stockfish or if the move is incorrect in puzzle mode.
     *  @param move move to be checked if legal
     */
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
    /**
     * Adds a move to the move queue for processing.
     *
     * @param move the move string to queue
     */
    public void queueMove(String move) {
        moveQueue.add(move);
    }
    /**
     * Executes a move on the board; it identifies where the piece that wants to be moved is and its destination square
     * and ensures it can be played. It also handles castling, manages puzzle conditons, move animations and sound
     * @param move the move to be played
     */
    public void movePiece(String move) {
        if (move.isEmpty())
            return;
        if (puzzleMode && !solutionList.isEmpty())
            if(!solutionList.peek().equals(move))
                return;
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
            if (piece instanceof Pawn) {
                if (FEN.contains(move.substring(2))) {
                    int offset = piece.isWhite() ? -1 : +1;
                    contested = board[endRow + offset][endCol];
                    board[endRow + offset][endCol] = null;
                }
            }
            if (contested != null) {
                contested.remove();
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
            if (piece instanceof King) {
                piece.setMoved(true);
                handleCastling(move);
            }

            printBoard();
            System.out.println("Moved: " + move);
            moveList.push(move);

            if (moveSound != null) {
                moveSound.play(); // Play move sound
            }

        }
    }

    //King : "e1c1 e1g1 e8c8 e8g8", Rook: "a1d1 h1f1 a8d8 h8f8"
    /**
     * Allows castling to be played by moving the rook to its castling square
     * @param move the move to be played
     */
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
    /**
     * Translates the string move into zero based indices which are used with the internal board
     * @param move the move to be played
     * @return A parsed char array where parsed[0] and parsed[1] are the starting column and row and
     * parsed[2] and parsed[3] are the ending column and row.
     */
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
    /**
     * Promotes a pawn to a specified piece.
     *
     * @param rank  the rank to promote to ('q', 'r', 'b', 'n')
     * @param endRow the row of the promotion square
     * @param endCol the column of the promotion square
     */
    private void promote(char rank, int endRow, int endCol) {
        int tileSize;
        int targetX;
        int targetY;
        Piece piece;
        switch (rank) {
            case 'q':
                piece = board[endRow][endCol];
                tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
                targetX = endCol * tileSize;
                targetY = endRow * tileSize;
                board[endRow][endCol] = new Queen(piece.isWhite());
                board[endRow][endCol].setPosition(targetX,targetY);
                board[endRow][endCol].setWidth(piece.getWidth());
                board[endRow][endCol].setHeight(piece.getHeight());
                board[endRow][endCol].setVisible(true);
                piece.getStage().addActor(board[endRow][endCol]);
                piece.remove();
                return;
            case 'r':
                piece = board[endRow][endCol];
                tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
                targetX = endCol * tileSize;
                targetY = endRow * tileSize;
                board[endRow][endCol] = new Rook(piece.isWhite());
                board[endRow][endCol].setPosition(targetX,targetY);
                board[endRow][endCol].setWidth(piece.getWidth());
                board[endRow][endCol].setHeight(piece.getHeight());
                board[endRow][endCol].setVisible(true);
                piece.getStage().addActor(board[endRow][endCol]);
                piece.remove();
                return;
            case 'b':
                piece = board[endRow][endCol];
                tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
                targetX = endCol * tileSize;
                targetY = endRow * tileSize;
                board[endRow][endCol] = new Bishop(piece.isWhite());
                board[endRow][endCol].setPosition(targetX,targetY);
                board[endRow][endCol].setWidth(piece.getWidth());
                board[endRow][endCol].setHeight(piece.getHeight());
                board[endRow][endCol].setVisible(true);
                piece.getStage().addActor(board[endRow][endCol]);
                piece.remove();
                return;
            case 'n':
                piece = board[endRow][endCol];
                tileSize = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 8;
                targetX = endCol * tileSize;
                targetY = endRow * tileSize;
                board[endRow][endCol] = new Knight(piece.isWhite());
                board[endRow][endCol].setPosition(targetX,targetY);
                board[endRow][endCol].setWidth(piece.getWidth());
                board[endRow][endCol].setHeight(piece.getHeight());
                board[endRow][endCol].setVisible(true);
                piece.getStage().addActor(board[endRow][endCol]);
                piece.remove();
                return;
            default:
        }
    }

    public void sendPosToStockfish(String fen) {
        System.out.println(fen);
        stockfishAI.sendPosition(fen);
    }
    /**
     * Takes last move and adds it to the end of the fen
     *
     * @param fen String representation of board state
     * @return a string with the fen of the current board and the last move
     */
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
    /**
     * convert a string representation of a piece into an actual Piece object to help turns fen String into an actual board
     *
     * @param p String to reprsent the piece
     * @return temp Piece object for game
     */
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
    /**
     * Goes through the fen string and creates a board based on the fen
     *
     * @param fen String representation of board statee
     */
    public void parseFen(String fen){
        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
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
            playerColor = !whiteTurn;
        else
            playerColor = whiteTurn;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean getPlayerColor() {
        return playerColor;
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

    /**
     * Prints internal reprsentation of the board which is a two dimensional array
     *
     */
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


    /**
     *  Saves the game state by writing the FEN to a file in the games CWD.
     */
    // Saves the game state by writing the FEN to a file in the games CWD
    public void saveGame() {
        String gameFen = getFenFromAI();
                //create saves folder if it does not exist
        File folder = new File("saves");
        if(!folder.exists()){
            folder.mkdirs();
        }
        File[] list = folder.listFiles();
        String path = "saves/"+list.length;
        File f = new File(path);
        if(f.exists()){
            if(!f.delete()){
                System.out.println("Save Failed");
            }
        }
        else{
            System.out.println(path);
            try {
                if(f.createNewFile()){
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                        writer.write(gameFen);
                    } catch (IOException e) {
                        System.err.println("Error writing to file: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Cleans up resources such as stockfish and gameloop and exits the game.
     */
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
