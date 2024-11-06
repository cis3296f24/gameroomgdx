package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;

public class Queen extends Piece{

    public Queen(boolean isWhite) {
        super(isWhite);
    }

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
}
