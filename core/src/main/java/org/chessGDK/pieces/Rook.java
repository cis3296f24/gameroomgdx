package org.chessGDK.pieces;

public class Rook extends Piece {

    private boolean moved;

    public Rook(boolean isWhite) {
        super(isWhite);
        moved = false;
    }

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

    public boolean hasMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return isWhite() ? "R" : "r";
    }
}
