package org.chessGDK.pieces;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Represents a generic piece in a game.
 * This abstract class defines the common fields and behavior shared by all game pieces,
 * such as color, movement validation, texture handling, and rendering.
 * All specific chess pieces must extend this class. Extends actor class.
 */
public abstract class Piece extends Actor {
    /**  boolean indicating whether the piece is white (true) or black (false) */

    protected boolean isWhite;
    /** Stores the texture of the piece for graphical representation.*/
    private Texture pieceTexture;
    private boolean animating;
    /** Indicates whether Piece has moved. */

    private boolean moved;

    /**
     * Constructor for the Piece class.
     * Initializes the piece's color and sets its texture based on the color and type.
     *
     * @param isWhite a boolean indicating whether the piece is white (true) or black (false).
     */
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
    /**
     * Renders the piece on the screen using the specified Batch.
     * If the texture is set, it draws the texture at the piece's position and size.
     *
     * @param batch       the Batch used for rendering.
     * @param parentAlpha the parent alpha, used for blending.
     */
    public void draw(Batch batch, float parentAlpha) {
        if (getTexture() != null) {
            batch.draw(getTexture(), getX(), getY(), getWidth(), getHeight());
        }
    }

    public abstract Piece copy();

}
