package org.chessGDK.pieces;

public class King extends Piece{

    public King(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int StartX, int StartY, int EndX, int EndY, Piece[][] board) {
        return false;
    }
}
