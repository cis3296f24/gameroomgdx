package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
/**
 * Represents a King chess piece in the game.
 * Extends Piece class.
 */
public class King extends Piece{
    /** Indicates whether king has moved which is used for castling. */
    private boolean moved;
    /**
     * Constructor for the King class.
     * Initializes a King piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public King(boolean isWhite) {
        super(isWhite);
        moved = false;
    }
    /**
     * Validates whether the King's move is legal according to chess rules.
     * The King can move one square in any direction as long as there are no pieces in the way and it can also move two
     * squares to the left or right switching places with a corresponding rook piece as long as both pieces have not
     * moved and there is no pieces in the way,
     * and it can capture an opponent's piece at the destination as long as it cannot be checked.
     *
     * @param startCol the starting column of the King.
     * @param startRow the starting row of the King.
     * @param endCol   the destination column of the King.
     * @param endRow   the destination row of the King.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */
    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int diffX = Math.abs(endCol - startCol);
        int diffY = Math.abs(endRow - startRow);
        int xDir = startCol < endCol ? 1 : -1;
        if (diffX == 2 && diffY == 0 && !moved) {
            // King side castle
            if (endCol == 6 && startRow == endRow && board[startRow][7] != null && !((Rook) board[startRow][7]).getMoved()) {
                for (int i = startCol + 1; i < endCol; i++) {
                    if (board[startRow][i] != null) {
                        return false;
                    }
                }
                return true;
            // Queen side castle
            } else if (endCol == 2 && startRow == endRow && board[startRow][0] != null && !((Rook) board[startRow][0]).getMoved()) {
                for (int i = startCol - 1; i > endCol; i--) {
                    if (board[startRow][i] != null) {
                        return false;
                    }
                }
                return true;
            }
        }
        // King can move one square in any direction
        if (diffX <= 1 && diffY <= 1) {
            return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
        }
        return false;
    }

    public boolean getMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return isWhite() ? "K" : "k";
    }

    public Piece copy() {
        return new King(this.isWhite);
    }
}
