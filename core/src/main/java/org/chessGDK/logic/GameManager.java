
package org.chessGDK.logic;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.Gdx;

import org.chessGDK.pieces.*;
import org.chessGDK.ai.StockfishAI;
import org.chessGDK.ui.ChessBoardScreen;
import org.chessGDK.ui.GameOverScreen;
import org.chessGDK.ui.ScreenManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import java.util.TimerTask;


public class GameManager extends ScreenAdapter {
    private final Object turnLock = new Object();
    private boolean whiteTurn;
    private final Piece[][] board;
    private final Blank[][] possibilities;
    private final Piece[] castlingPieces;
    private final StockfishAI stockfishAI;
    private final int DEPTH = 12;
    private int halfMoves;
    private String castlingRights;
    private String enPassantSquare;
    private boolean freeMode = false;
    private boolean gameOver = false;
    private Stack<String> moveList;
    private ChessBoardScreen screen;
    private String FEN;
    private GameOverScreen gameOverScreen;

    public GameManager(int difficulty, String fen) throws IOException {
        board = new Piece[8][8];
        possibilities = new Blank[8][8];
        whiteTurn = true;
        castlingPieces = new Piece[6];
        moveList = new Stack<>();
        setupPieces();
        parseFen(fen);
        if (difficulty == -1) {
            freeMode = true;
        }
        if (freeMode)
            stockfishAI = null;
        else
            stockfishAI = new StockfishAI(DEPTH, difficulty);
        printBoard();
        halfMoves = 0;
        castlingRights = "KQkq";
        enPassantSquare = null;
        screen = ScreenManager.getInstance().getChessBoardScreen();
        gameOverScreen = new GameOverScreen();
    }


    public void startGameLoopThread() {
        new Thread(this::startGameLoop){{setDaemon(true);}}.start();
    }

    public void notifyMoveMade() {
        synchronized (turnLock) {
            turnLock.notifyAll(); // Notify the game loop that a move has been made
        }
    }

    private void startGameLoop() {
        // Start the game loop
        while (!gameOver) {
            synchronized (turnLock) {
                // Make the next move
                try {
                    turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    gameOver = true;
                }
            }
            makeNextMove();
        }
        exitGame();
    }

    public void makeNextMove() {
        if (whiteTurn)
            playerTurn(); // White player move logic
        else
            aiTurn(); // Black (AI) move logic
    }

    private boolean playerTurn() {

        return true;
    }

    public boolean aiTurn() {
        String fen;
        fen = generateFen();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    // Retrieve the best move from Stockfish after the delay
                    String bestMove = getBestMove(fen);
                    
                    System.out.println("FEN: " + fen + "\nBest Move: " + bestMove);
                    boolean moved = movePiece(bestMove);
                    System.out.println("Move " + bestMove + ": " + moved);
                    //printBoard();
                    checkforcheckmate(generateFen());
                    notifyMoveMade();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, .5f); // Delay by .5 second

        return true;
    }

    public String getBestMove(String fen) throws IOException {
        return stockfishAI.getBestMove(fen);
    }

    public String getLegalMoves(String fen) throws IOException {
        return stockfishAI.getLegalMoves(fen);
    }

    private void setupPieces() {
        // Place white pawns on the second row (index 1)
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(true); // White pawns
        }
        // Place white major pieces on the first row (index 0)
        board[0][0] = new Rook(true);    // White rook
        board[0][7] = new Rook(true);    // White rook
        board[0][1] = new Knight(true);  // White knight
        board[0][6] = new Knight(true);  // White knight
        board[0][2] = new Bishop(true);  // White bishop
        board[0][5] = new Bishop(true);  // White bishop
        board[0][3] = new Queen(true);   // White queen
        board[0][4] = new King(true);    // White king

        // Place black pawns on the seventh row (index 6)
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Pawn(false); // Black pawns
        }
        // Place black major pieces on the eighth row (index 7)
        board[7][0] = new Rook(false);   // Black rook
        board[7][7] = new Rook(false);   // Black rook
        board[7][1] = new Knight(false); // Black knight
        board[7][6] = new Knight(false); // Black knight
        board[7][2] = new Bishop(false); // Black bishop
        board[7][5] = new Bishop(false); // Black bishop
        board[7][3] = new Queen(false);  // Black queen
        board[7][4] = new King(false);   // Black king

        // white queenside rook
        castlingPieces[0] = board[0][0];
        // white king
        castlingPieces[1] = board[0][4];
        // white kingside rook
        castlingPieces[2] = board[0][7];

        // black queenside rook
        castlingPieces[3] = board[7][0];
        // black king
        castlingPieces[4] = board[7][4];
        // black kingside rook
        castlingPieces[5] = board[7][7];

        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                possibilities[i][j] = new Blank();
            }
        }
    }

    public boolean movePiece(String move) {
        String fen;
        fen = generateFen();
        if (move.isEmpty()) {
            return false;
        }
        try{
            String LegalMoves = getLegalMoves(fen);
            if(!stockfishAI.checklLegalMoves(move, LegalMoves)){
                System.out.println("Illegal move");
                return false;

            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        char[] parsedMove = parseMove(move);
        int startCol = parsedMove[0];
        int startRow = parsedMove[1];
        int endCol = parsedMove[2];
        int endRow = parsedMove[3];
        char newRank;
        Piece piece = board[startRow][startCol];
        Piece contested = board[endRow][endCol];
        System.out.println(startCol);
        enPassantSquare = null;
        // Ensure the right piece color is moving according to the turn
        if (piece != null && piece.isValidMove(startCol, startRow, endCol, endRow, board)) {
            if (contested != null) {
                contested.remove();
            }
            float targetX = endCol * Gdx.graphics.getHeight()/8f;
            float targetY = endRow * Gdx.graphics.getHeight()/8f;
            float duration = 1.0f;
            piece.setVisible(true);
            piece.addAction(Actions.moveTo(targetX, targetY, duration));
            board[endRow][endCol] = piece;
            board[startRow][startCol] = null;
            //screen.startPieceAnimation(piece, startCol, startRow, endCol, endRow);
            if (parsedMove.length == 5) {
                newRank = parsedMove[4];
                promote(newRank, endRow, endCol);
            }
            if (piece instanceof Pawn && piece.enPassant()) {
                int direction = startRow < endRow ? 1 : -1;
                char temp = move.charAt(3);
                temp += (char)direction;
                enPassantSquare = (char)('a' + endCol) + "" + temp ;
            }
            //piece.toggleAnimating();
            printBoard();

            if(!freeMode)
                checkforcheckmate(fen);

            whiteTurn = !whiteTurn;
            halfMoves++;
            piece.setPosition(endCol * Gdx.graphics.getWidth()/8, endRow * Gdx.graphics.getHeight()/8);
            moveList.push(move);
            return true;
        }
        return false;
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
            if (castlingPieces[1].hasMoved()) {
                castlingRights = castlingRights.replace("K", "");
                castlingRights = castlingRights.replace("Q", "");
            }
            if (castlingPieces[4].hasMoved()) {
                castlingRights = castlingRights.replace("k", "");
                castlingRights = castlingRights.replace("q", "");
            }
            if (castlingPieces[0].hasMoved()) {
                castlingRights = castlingRights.replace("Q", "");
            }
            if (castlingPieces[2].hasMoved()) {
                castlingRights = castlingRights.replace("K", "");
            }
            if (castlingPieces[3].hasMoved()) {
                castlingRights = castlingRights.replace("q", "");
            }
            if (castlingPieces[5].hasMoved()) {
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
        fen.append(halfMoves / 2);

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
        // Perform any other cleanup needed for the game
        System.out.println("GameManager closed.");
    }
}
