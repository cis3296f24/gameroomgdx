package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;

public class Pawn extends Piece{

    private boolean enPassant;

    public Pawn(boolean isWhite) {
        super(isWhite);
        enPassant = false;
    }

    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int direction = isWhite() ? 1 : -1;
        if (startCol == endCol && board[endRow][endCol] == null) {
            if (startRow + direction == endRow) {
                enPassant = false;
                return true;
            } else if (startRow + direction * 2 == endRow) {
                boolean valid = startRow == (isWhite() ? 1 : 6);
                enPassant = valid;
                return valid;
            }
        } else if (Math.abs(endCol - startCol) == 1 && startRow + direction == endRow) {
            enPassant = false;
            if (board[endRow][endCol] != null)
                return board[endRow][endCol].isWhite() != isWhite();
            return false;
        }
        return false;
    }

    public boolean enPassant() {
        return enPassant;
    }

    @Override
    public String toString() {
        return isWhite() ? "P" : "p";
    }
}
