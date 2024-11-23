package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;

public class King extends Piece{

    private boolean moved;

    public King(boolean isWhite) {
        super(isWhite);
        moved = false;
    }

    @Override
    public boolean isValidMove(int startCol, int startRow, int endCol, int endRow, Piece[][] board) {
        int diffX = Math.abs(endCol - startCol);
        int diffY = Math.abs(endRow - startRow);
        int xDir = startCol < endCol ? 1 : -1;
        if (diffX == 2 && diffY == 0 && !moved) {
            // King side castle
            if (endCol == 6 && startRow == endRow && board[startRow][7] != null && !((Rook) board[startRow][7]).getMoved()) {
                for (int i = startCol + 1; i < endCol; i++) {
                    if (board[startRow][i] != null) {
                        return false;
                    }
                }
                return true;
            // Queen side castle
            } else if (endCol == 2 && startRow == endRow && board[startRow][0] != null && !((Rook) board[startRow][0]).getMoved()) {
                for (int i = startCol - 1; i > endCol; i--) {
                    if (board[startRow][i] != null) {
                        return false;
                    }
                }
                return true;
            }
        }
        // King can move one square in any direction
        if (diffX <= 1 && diffY <= 1) {
            return board[endRow][endCol] == null || board[endRow][endCol].isWhite() != isWhite();
        }
        return false;
    }

    public boolean getMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return isWhite() ? "K" : "k";
    }

    public Piece copy() {
        return new King(this.isWhite);
    }
}
