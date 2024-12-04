package org.chessGDK.pieces;
/**
 * Represents a Rook chess piece in the game.
 * Extends Piece class.
 */
public class Rook extends Piece {
    /** Indicates whether Rook has moved which is used for castling. */

    private boolean moved;
    /**
     * Constructor for the Rook class.
     * Initializes a Rook piece with the specified color.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
    public Rook(boolean isWhite) {
        super(isWhite);
        moved = false;
    }
    /**
     * Validates whether the Rooks's move is legal according to chess rules.
     * The Queen can move vertically and horizontally, in any amount of squares as long as there are not pieces in the way
     * It can capture an opponent's piece at the destination
     *
     * @param startCol the starting column of the Rook.
     * @param startRow the starting row of the Rook.
     * @param endCol   the destination column of the Rook.
     * @param endRow   the destination row of the Rook.
     * @param board    the current state of the chessboard represented as a 2D array of Piece objects.
     * @return true if the move is valid, false otherwise.
     */
    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        if (startCol == endCol || startRow == endRow) {
            int diffX = endCol - startCol;
            int diffY = endRow - startRow;
            int dirX = diffX == 0 ? 0 : diffX / Math.abs(diffX);
            int dirY = diffY == 0 ? 0 : diffY / Math.abs(diffY);
            int x = startCol + dirX;
            int y = startRow + dirY;
            while (x != endCol || y != endRow) {
                if (board[y][x] != null) {
                    return false;
                }
                x += dirX;
                y += dirY;
            }
            moved = true;
            return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
        }
        return false;
    }

    public boolean getMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return isWhite() ? "R" : "r";
    }

    public Piece copy() {
        return new Rook(this.isWhite);
    }
}
