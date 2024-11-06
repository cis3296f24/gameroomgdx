package org.chessGDK.pieces;

public class Pawn extends Piece{

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Pawn logic
        int direction = isWhite ? 1 : -1;
        // Standard move (one square forward)
        if (endX == startX + direction && startY == endY && board[endX][endY] == null) {
            return true;
        }

        // First move can be two squares forward
        if ((isWhite() && startX == 1 || !isWhite() && startX == 6)
                && endX == startX + 2 * direction
                && startY == endY
                && board[endX][endY] == null) {
            return true;
        }

        return false;
    }

}
