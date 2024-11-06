package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;

public class Knight extends Piece{

    public Knight(boolean isWhite) {
        super(isWhite);
    }

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
}
