package org.chessGDK.pieces;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.files.FileHandle;

public class Blank {
    private Texture pieceTexture;
    private float xPos;
    private float yPos;

    public Blank() {
        pieceTexture = new Texture("blank.png");
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
}
