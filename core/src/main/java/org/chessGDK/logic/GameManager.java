
package org.chessGDK.logic;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;

import org.chessGDK.pieces.*;
import org.chessGDK.ai.StockfishAI;
import org.chessGDK.ui.ScreenManager;

import java.io.IOException;
import java.util.*;


public class GameManager extends ScreenAdapter {
    private final Object turnLock = new Object();
    private Thread gameLoopThread;

    private boolean whiteTurn;
    private boolean startColor;
    private final Piece[][] board;
    private final Blank[][] possibilities;
    private final Piece[] castlingPieces;
    private final StockfishAI stockfishAI;
    private final int DEPTH = 12;
    private int halfMoves;
    private int fullMoves;
    private String castlingRights;
    private String enPassantSquare;
    private boolean freeMode = false;
    private boolean puzzleMode = false;
    private volatile boolean gameOver = false;
    private Stack<String> moveList;
    private Queue<String> solutionList;
    private String FEN;
    private float duration = .15f;
    private final HashMap<String, String> castleMoves;
    private String legalMoves;
    private String[] bestMove;
    private final static ScreenManager sm = ScreenManager.getInstance();

    public GameManager(int difficulty, String fen) throws IOException {
        board = new Piece[8][8];
        possibilities = new Blank[8][8];
        castlingPieces = new Piece[6];
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
        stockfishAI = new StockfishAI(DEPTH, difficulty, FEN);
        FEN = getFenFromAI();
        castleMoves = new HashMap<>();
        castleMoves.put("e1c1", "a1d1");
        castleMoves.put("e1g1", "h1f1");
        castleMoves.put("e8c8", "a8d8");
        castleMoves.put("e8g8", "h8f8");
        moveList = new Stack<>();
        castlingRights = "KQkq";
        parseFen(FEN);
        printBoard();
        updateBoardState();
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

    public void notifyMoveMade() {
        synchronized (turnLock) {
            turnLock.notifyAll(); // Notify the game loop that a move has been made
        }
    }

    public void endGame() {
        synchronized (turnLock) {
            gameOver = true;
            turnLock.notifyAll(); // Wake up any waiting threads
        }
    }

    private void gameLoop() {

        while (!gameOver) {
            System.out.println(FEN);

            synchronized (turnLock) {
                // Check if the game is over before making the next move
                if (gameOver) break;

                makeNextMove(); // Make the current player's move

                // Notify the other thread to proceed
                whiteTurn = !whiteTurn; // Toggle turn

                turnLock.notifyAll();
                // Wait for the other player's move or end of the game
                try {
                    while (!gameOver && whiteTurn) {
                        turnLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    gameOver = true; // Handle interruption by stopping the game
                }
            }
            updateBoardState();
        }
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
            if (solutionList.peek() == null)
                gameOver = true;
        }
        else {
            System.out.println("Best Move: " + bestMove[0]);
            checkforcheckmate(FEN);
        }
    }

    public void makeNextMove() {
        if (puzzleMode)
            puzzleTurns();
        else if (freeMode)
            freeTurns();
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
        if (whiteTurn == startColor)
            aiTurn();
        else
            playerTurn();
    }

    private void playerTurn() {
        synchronized (turnLock) {
            if (gameOver) return;

            System.out.println("Player makes a move.");
            whiteTurn = false; // Player completes their turn
            turnLock.notifyAll(); // Notify the game loop
        }
    }

    public void aiTurn() {
        System.out.println("ai scheduled turn");
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                System.out.println("ai turn ran");
                if (puzzleMode && !solutionList.isEmpty())
                    movePiece(solutionList.peek());
                else if (!puzzleMode)
                    movePiece(bestMove[0]);
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
        if (puzzleMode && !solutionList.isEmpty())
            return solutionList.peek();
        return stockfishAI.getLegalMoves();
    }

    public String getFenFromAI() {
        return stockfishAI.getFEN();
    }

    private boolean checkforcheckmate(String fen) {
        try {
            //System.out.println("FEN after move: " + fen + "\nStockfish's Best Move: " + bestMove);
            if(stockfishAI.checkmate(fen)){
                System.out.println("checkmate");
                gameLoopThread.interrupt();
                gameOver = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gameOver;
    }

    private boolean checkLegalMoves(String move) {
        if(!stockfishAI.parseLegalMoves(move, legalMoves)){
            System.out.println("Illegal move");
            return false;
        }
        return true;
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
        if (!freeMode || !puzzleMode) {
            if (!checkLegalMoves(move)) {
                return false;
            }
        }
        Piece contested = board[endRow][endCol];
        enPassantSquare = null;
        // Ensure the right piece color is moving according to the turn
        if (piece != null) {
            if (contested != null) {
                contested.remove();
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
                promote(newRank, endRow, endCol);
            }
            if (piece instanceof Pawn && piece.enPassant(move)) {
                int direction = startRow > endRow ? 1 : -1;
                char temp = move.charAt(3);
                temp += (char)direction;
                enPassantSquare = (char)('a' + endCol) + "" + temp ;
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
            if(!whiteTurn)
                fullMoves++;
            halfMoves++;
            moveList.push(move);
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
        stockfishAI.sendPosition(fen);
    }

    public String generateFen() {
        StringBuilder fen = new StringBuilder();
        // 1. Piece Placement
        for (int row = 7; row >= 0; row--) {
            int emptyCount = 0;

            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece);
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append("/");
            }
        }
        // 2. Active Color (w or b)
        fen.append(whiteTurn ? " w " : " b ");
        // 3. Castling Availability (KQkq or -)
        if (!castlingRights.isEmpty()) {
            if (castlingPieces[0] != null && castlingPieces[0].getMoved()) {
                castlingRights = castlingRights.replace("K", "");
                castlingRights = castlingRights.replace("Q", "");
            }
            if (castlingPieces[3] != null && castlingPieces[3].getMoved()) {
                castlingRights = castlingRights.replace("k", "");
                castlingRights = castlingRights.replace("q", "");
            }
            if (castlingPieces[1] != null && castlingPieces[1].getMoved()) {
                castlingRights = castlingRights.replace("Q", "");
            }
            if (castlingPieces[2] != null && castlingPieces[2].getMoved()) {
                castlingRights = castlingRights.replace("K", "");
            }
            if (castlingPieces[4] != null && castlingPieces[4].getMoved()) {
                castlingRights = castlingRights.replace("q", "");
            }
            if (castlingPieces[5] != null && castlingPieces[5].getMoved()) {
                castlingRights = castlingRights.replace("k", "");
            }
        }

        fen.append(castlingRights.isEmpty() ? "-" : castlingRights);
        fen.append(" ");
        // 4. En Passant Target Square (e.g., e3 or -)
        fen.append(enPassantSquare != null ? enPassantSquare : "-");
        fen.append(" ");
        // 5. Halfmove Clock
        fen.append(halfMoves).append(" ");
        // 6. Fullmove Number
        fen.append(fullMoves);

        return fen.toString();
    }

    private String appendMoveList(String fen) {
        StringBuilder fenWithMoves = new StringBuilder();
        fenWithMoves.append(fen);
        // 7. Add movelist if there is one
        if (!moveList.isEmpty()) {
            String moves = moveList.toString();
            moves = moves.replace('[', ' ').replace(']', ' ');
            moves = moves.trim();
            fenWithMoves.append(" moves ");
            for (String move : moves.split(","))
                fenWithMoves.append(move);
        }
        return fenWithMoves.toString();
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
        startColor = whiteTurn;
        if (!parts[2].equals("-"))
            castlingRights = castlingPiecesFromString(parts[2]);
        enPassantSquare = parts[3];
        halfMoves = Integer.parseInt(parts[4]);
        fullMoves = Integer.parseInt(parts[5]);
    }

    private String castlingPiecesFromString(String rights) {
        // Process FEN castling rights string
        if (rights.contains("Q")) { // White queen-side
            castlingPieces[0] = board[0][4]; // White king
            castlingPieces[0].setMoved(false); // White king
            castlingPieces[1] = board[0][0]; // White queen-side rook
            castlingPieces[1].setMoved(false); // White queen-side rook
        }
        if (rights.contains("K")) { // White king-side
            castlingPieces[0] = board[0][4]; // White king
            castlingPieces[0].setMoved(false); // White king
            castlingPieces[2] = board[0][7]; // White king-side rook
            castlingPieces[2].setMoved(false); // White king-side rook
        }
        if (rights.contains("q")) { // Black queen-side
            castlingPieces[3] = board[7][4]; // Black king
            castlingPieces[3].setMoved(false); // Black king
            castlingPieces[4] = board[7][0]; // Black queen-side rook
            castlingPieces[4].setMoved(false); // Black queen-side rook
        }
        if (rights.contains("k")) { // Black king-side
            castlingPieces[3] = board[7][4]; // Black king
            castlingPieces[3].setMoved(false); // Black king
            castlingPieces[5] = board[7][7]; // Black king-side rook
            castlingPieces[5].setMoved(false); // Black king-side rook
        }
        return rights;
    }

    public StockfishAI getAI() {
        return stockfishAI;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
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
        if (stockfishAI != null) {
            try {
                stockfishAI.close();  // Close the Stockfish AI
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Gdx.input.setInputProcessor(null);
        // Perform any other cleanup needed for the game
        System.out.println("GameManager closed.");
        sm.togglePause();
        sm.displayMenu();
    }
}
