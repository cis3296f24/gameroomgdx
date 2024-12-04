package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
/**
 * Represents a Pawn chess piece in the game.
 * Extends Piece class.
 */
public class Pawn extends Piece{

    private boolean enPassant;
    /**
     * Constructor for the Pawn class.
     * Initializes a Pawn piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public Pawn(boolean isWhite) {
        super(isWhite);
        enPassant = false;
    }
    /**
     * Validates whether the Pawn's move is legal according to chess rules.
     * The Pawn can move one square forward as long as there are no pieces in the way but it can move two squares from
     * its starting position. It can  transform into a new piece if it reaches the end of the board.
     * It can capture an opponent's piece one sqaure diagonally forward to its current position
     *
     * @param startCol the starting column of the Pawn.
     * @param startRow the starting row of the Pawn.
     * @param endCol   the destination column of the Pawn.
     * @param endRow   the destination row of the Pawn.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */
    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int direction = isWhite() ? 1 : -1;
        if (startCol == endCol && board[endRow][endCol] == null) {
            if (startRow + direction == endRow) {
                enPassant = false;
                return true;
            } else if (startRow + direction * 2 == endRow) {
                boolean valid = startRow == (isWhite() ? 1 : 6);
                enPassant = valid;
                return valid;
            }
        } else if (Math.abs(endCol - startCol) == 1 && startRow + direction == endRow) {
            enPassant = false;
            if (board[endRow][endCol] != null)
                return board[endRow][endCol].isWhite() != isWhite();
            return false;
        }
        return false;
    }

    public boolean enPassant(String move) {
        int distance = Math.abs(Character.getNumericValue(move.charAt(1))
                                - Character.getNumericValue(move.charAt(3)));
        return distance == 2;
    }

    @Override
    public String toString() {
        return isWhite() ? "P" : "p";
    }

    public Piece copy() {
        return new Pawn(this.isWhite);
    }
}
