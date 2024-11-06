package org.chessGDK.pieces;

public abstract class Piece {
    protected boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public abstract boolean isValidMove (int StartX, int StartY, int EndX, int EndY, Piece[][] board);
}
