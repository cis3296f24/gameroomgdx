package org.chessGDK.pieces;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        // Check if the piece is moving diagonally
        if (Math.abs(startCol - endCol) != Math.abs(startRow - endRow)) {
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
}
