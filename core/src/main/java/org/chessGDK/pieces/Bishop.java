package org.chessGDK.pieces;

/**
 * Represents a Bishop chess piece in the game.
 * Extends Piece class.
 */
public class Bishop extends Piece {

    /**
     * Constructor for the Bishop class.
     * Initializes a Bishop piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public Bishop(boolean isWhite) {
        super(isWhite);
    }
    /**
     * Validates whether the Bishop's move is legal according to chess rules.
     * The Bishop can move diagonally in as many sqaure it wants as long as there are no pieces in the way,
     * and it can capture an opponent's piece at the destination.
     *
     * @param startCol the starting column of the Bishop.
     * @param startRow the starting row of the Bishop.
     * @param endCol   the destination column of the Bishop.
     * @param endRow   the destination row of the Bishop.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */

    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        // Check if the piece is moving diagonally
        if (Math.abs(startCol - endCol) != Math.abs(startRow - endRow)) {
            return false;
        }
        else if (startCol == endCol && startRow == endRow) {
            return false;
        }

        // Check if there are any pieces between the start and end
        int xDir = startCol < endCol ? 1 : -1;
        int yDir = startRow < endRow ? 1 : -1;

        int i = startCol + xDir;
        int j = startRow + yDir;

        while (i != endCol) {
            if (board[j][i] != null) {
                return false;
            }
            i += xDir;
            j += yDir;
        }

        // Check if the end position is empty or has an opponent's piece
        return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
    }

    @Override
    public String toString() {
        return isWhite() ? "B" : "b";
    }

    public Piece copy() {
        return new Bishop(this.isWhite);
    }
}
