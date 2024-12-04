package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
/**
 * Represents a Knight chess piece in the game.
 * Extends Piece class.
 */
public class Knight extends Piece{
    /**
     * Constructor for the Knight class.
     * Initializes a Knight piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public Knight(boolean isWhite) {
        super(isWhite);
    }
    /**
     * Validates whether the Knights's move is legal according to chess rules.
     * The Knight can move in an L shape(2x3 squares) and jump over pieces,
     * and it can capture an opponent's piece at the destination.
     *
     * @param startCol the starting column of the Knight.
     * @param startRow the starting row of the Knight.
     * @param endCol   the destination column of the Knight.
     * @param endRow   the destination row of the Knight.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */
    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int diffX = Math.abs(endCol - startCol);
        int diffY = Math.abs(endRow - startRow);

        if ((diffX == 2 && diffY == 1) || (diffX == 1 && diffY == 2)) {
            return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
        }
        return false;
    }

    @Override
    public String toString() {
        return isWhite() ? "N" : "n";
    }

    public Piece copy() {
        return new Knight(this.isWhite);
    }
}
