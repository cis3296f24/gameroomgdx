package org.chessGDK.pieces;

public class Rook extends Piece {

    public Rook(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int StartX, int StartY, int EndX, int EndY, Piece[][] board) {
        return false;
    }

}
