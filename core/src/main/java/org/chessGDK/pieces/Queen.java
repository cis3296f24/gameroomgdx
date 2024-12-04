package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
/**
 * Represents a Queen chess piece in the game.
 * Extends Piece class.
 */
public class Queen extends Piece{
    /**
     * Constructor for the Queen class.
     * Initializes a Queen piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public Queen(boolean isWhite) {
        super(isWhite);
    }
    /**
     * Validates whether the Queen's move is legal according to chess rules.
     * The Queen can move in any direction, in any amount of squares as long as there are not pieces in the way
     * It can capture an opponent's piece at the destination
     *
     * @param startCol the starting column of the Queen.
     * @param startRow the starting row of the Queen.
     * @param endCol   the destination column of the Queen.
     * @param endRow   the destination row of the Queen.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */
    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        if (startCol == endCol && startRow == endRow)
            return false;
        int diffX = Math.abs(endCol - startCol);
        int diffY = Math.abs(endRow - startRow);
        int xDir;
        int yDir;
        // Check if there are any pieces between the start and end
        if (diffX == 0 || diffY == 0 || diffX == diffY) {
            xDir = diffX == 0 ? 0 : (startCol < endCol ? 1 : -1);
            yDir = diffY == 0 ? 0 : (startRow < endRow ? 1 : -1);
            int i = startCol + xDir;
            int j = startRow + yDir;
            while (i != endCol || j != endRow) {
                if (board[j][i] != null) {
                    return false;
                }
                i += xDir;
                j += yDir;
            }
            return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
        }
        return false;
    }

    @Override
    public String toString() {
        return isWhite() ? "Q" : "q";
    }

    public Piece copy() {
        return new Queen(this.isWhite);
    }
}
