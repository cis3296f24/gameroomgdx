
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;



public class GameManager extends ScreenAdapter {
    private final Object turnLock = new Object();
    private boolean whiteTurn;
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
    private boolean multiplayerMode = false;
    private boolean gameOver = false;
    private Stack<String> moveList;
    private String FEN;
    private float duration = .1f;
    private final HashMap<String, String> castleMoves;
    private String legalMoves;
    private Communication communication;
    private boolean isHost;
    private Sound moveSound;

    public GameManager(int difficulty, String fen, String HostOrClient) throws IOException {
        board = new Piece[8][8];
        possibilities = new Blank[8][8];
        whiteTurn = true;
        FEN = fen;
        castleMoves = new HashMap<>();
        castleMoves.put("e1c1", "a1d1");
        castleMoves.put("e1g1", "h1f1");
        castleMoves.put("e8c8", "a8d8");
        castleMoves.put("e8g8", "h8f8");
        castlingPieces = new Piece[6];
        castlingRights = "KQkq";
        moveList = new Stack<>();
        parseFen(FEN);
        if (difficulty == -1) {
            freeMode = true;
        }
        else if (difficulty == -2) {
            puzzleMode = true;
            difficulty = 20;
        }
        else if (difficulty == -3) {
            multiplayerMode = true;
        }

        if(multiplayerMode){
            isHost = HostOrClient.equals("Host");
            communication = new Communication(this, isHost);
        }

        stockfishAI = new StockfishAI(DEPTH, difficulty, FEN);
        legalMoves = getLegalMoves();
        printBoard();

        moveSound = Gdx.audio.newSound(Gdx.files.internal("move.mp3"));
    }


    public void startGameLoopThread() {
        new Thread(this::gameLoop){{setDaemon(true);}}.start();
    }

    public void notifyMoveMade() {
        synchronized (turnLock) {
            turnLock.notifyAll(); // Notify the game loop that a move has been made
        }
    }

    private void gameLoop() {
        // Start the game loop
        while (!gameOver) {

            try {
                FEN = generateFen();
                legalMoves = getLegalMoves();        // Get all legal moves for after last move

                System.out.println("Current FEN:" + FEN);
                System.out.println("Legal Moves: " + legalMoves);

                makeNextMove();

                synchronized (turnLock) {
                    // Make the next move
                    try {
                        turnLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        gameOver = true;
                    }
                }

                FEN = generateFen();

                if(!freeMode)
                    checkforcheckmate(FEN);

            } catch (IOException e) {
                e.printStackTrace();
                gameOver = true;
            }

        }
        exitGame();
    }

    private void waitForOpponentMove(){
        synchronized (turnLock){
            try{
                turnLock.wait();;
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                gameOver = true;

            }
        }
    }

    public void makeNextMove() {
        if(multiplayerMode){
            if((isHost && whiteTurn) || (!isHost && !whiteTurn)){
                playerTurn();
            } else {
                waitForOpponentMove();
            }
        } else {
            if (whiteTurn)
                playerTurn(); // White player move logic
            else {
                if(freeMode || multiplayerMode)
                    playerTurn();
                else
                    aiTurn(); // Black (AI) move logic
            }
        }

    }

    private boolean playerTurn() {
        try {
            String bestMove = getBestMove(FEN);
            System.out.println("Best Move: " + bestMove);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean aiTurn() {
        String fen;
        fen = FEN;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    // Retrieve the best move from Stockfish after the delay
                    String bestMove = getBestMove(fen);
                    System.out.println("Best Move: " + bestMove);
                    boolean moved = movePiece(bestMove);
                    System.out.println("Move " + bestMove + ": " + moved);
                    //printBoard();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, .1f); // Delay by .5 second

        return true;
    }

    public String getBestMove(String fen) throws IOException {
        return stockfishAI.getBestMove(fen);
    }

    public String getLegalMoves() throws IOException {
        return stockfishAI.getLegalMoves(FEN);
    }

    public boolean movePiece(String move) {
        String fen;
        fen = FEN;
        if (move.isEmpty()) {
            return false;
        }

        char[] parsedMove = parseMove(move);
        int startCol = parsedMove[0];
        int startRow = parsedMove[1];
        int endCol = parsedMove[2];
        int endRow = parsedMove[3];
        char newRank;
        Piece piece = board[startRow][startCol];
        if (!freeMode) {
            if (!checkLegalMoves(move, fen)) {
                return false;
            }
        }
        Piece contested = board[endRow][endCol];
        System.out.println(startCol);
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
            if(!whiteTurn)
                fullMoves++;
            whiteTurn = !whiteTurn;
            halfMoves++;
            moveList.push(move);

            if(multiplayerMode){
                String updatedFen = generateFen();
                communication.sendFEN(updatedFen);
            }
            if (moveSound != null) {
                moveSound.play(); // Play move sound
            }

            notifyMoveMade();
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

    private static char[] parseMove(String bestMove) {
        if (bestMove == null) {
            return null;
        }
        char[] parsed;
        //parsed = bestMove.getBytes(StandardCharsets.US_ASCII);
        parsed = bestMove.toCharArray();

        // Changes rank/file ASCII representation into array indexes
        parsed[0] -= 'a';
        parsed[1] -= '1';
        parsed[2] -= 'a';
        parsed[3] -= '1';
        return parsed;
    }
    private void checkforcheckmate(String fen) {
        try {
            //System.out.println("FEN after move: " + fen + "\nStockfish's Best Move: " + bestMove);
            if(stockfishAI.checkmate(fen)){
                System.out.println("checkmate");
                gameOver = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        // 7. Add movelist if there is one
        if (!moveList.isEmpty()) {
            String moves = moveList.toString();
            moves = moves.replace('[', ' ').replace(']', ' ');
            moves = moves.trim();
            fen.append(" moves ");
            for (String move : moves.split(","))
                fen.append(move);
        }
        return fen.toString();
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
        if (!parts[2].equals("-"))
            castlingRights = castlingPiecesFromString(parts[2]);
        if (!parts[3].equals("-")) {
            enPassantSquare = parts[3];
        }
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
        if (moveSound != null) {
            moveSound.dispose(); // Added here
        }
        if(communication != null){
            communication.close();
        }


        if (stockfishAI != null) {
            try {
                stockfishAI.close();  // Close the Stockfish AI
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Perform any other cleanup needed for the game
        System.out.println("GameManager closed.");
    }
}
