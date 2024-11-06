package org.chessGDK.pieces;

public class Knight extends Piece{

    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int StartX, int StartY, int EndX, int EndY, Piece[][] board) {
        return false;
    }
}
