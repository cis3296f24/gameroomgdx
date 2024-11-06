package org.chessGDK.pieces;

public class Queen extends Piece{

    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean isValidMove(int StartX, int StartY, int EndX, int EndY, Piece[][] board) {
        return false;
    }
}
