package org.chessGDK.pieces;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int StartX, int StartY, int EndX, int EndY, Piece[][] board) {
        return false;
    }

}
