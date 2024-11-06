package org.chessGDK.pieces;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.files.FileHandle;

public abstract class Piece {
    protected boolean isWhite;
    private Texture pieceTexture;
    private boolean animating;
    private float xPos;
    private float yPos;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
        String texturePath = "Chess_" +
            toString().toLowerCase() +
            (isWhite() ? "l" : "d") +
            "t100.png";
        pieceTexture = new Texture(texturePath);
    }

    public boolean isWhite() {
        return isWhite;
    }

    public abstract boolean isValidMove (int startX, int startY, int endX, int endY, Piece[][] board);

    public boolean hasMoved() {
        return false;
    }

    public Texture getTexture() {
        return pieceTexture;
    }

    public boolean setTexture(Texture texture) {
        if (texture == null)
            return false;
        pieceTexture = texture;
        return true;
    }
    public boolean isAnimating() {
        return animating;
    }

    public void toggleAnimating() {
        animating = !animating;
    }

    public float getXPos() {
        return xPos;
    }
    public float getYPos() {
        return yPos;
    }
    public void setPosition(float x, float y) {
        xPos = x;
        yPos = y;
    }

    public boolean enPassant() {
        return false;
    }

}
