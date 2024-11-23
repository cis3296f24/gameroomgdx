package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Piece extends Actor {
    protected boolean isWhite;
    private Texture pieceTexture;
    private boolean animating;
    private boolean moved;
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

    public boolean getMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
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
    public void draw(Batch batch, float parentAlpha) {
        if (getTexture() != null) {
            batch.draw(getTexture(), getX(), getY(), getWidth(), getHeight());
        }
    }

    public abstract Piece copy();
    public boolean enPassant(String move) {
        return false;
    }

}
