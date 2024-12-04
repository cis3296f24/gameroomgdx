package org.chessGDK.pieces;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.files.FileHandle;
/**
 * Represents an empty or blank tile in the chessboard.
 * This class is primarily used to manage the graphical representation of a blank tile
 * and its position on the board.
 */
public class Blank {
    /**
     The texture associated with the blank tile which is set to "blank.png". */
    private Texture pieceTexture;
    /** x coordinate of tile. */
    private float xPos;
    /** y coordinate of tile. */
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
