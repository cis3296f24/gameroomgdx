package org.chessGDK.ui;

import com.badlogic.gdx.math.Vector2;
import org.chessGDK.pieces.Piece;

class PieceAnimation {
    private static final float ANIMATION_DURATION = .2f;
    public Piece piece;
    public Vector2 startPosition;
    public Vector2 targetPosition;
    public float elapsedTime;


    public PieceAnimation(Piece piece, Vector2 startPosition, Vector2 targetPosition) {
        this.piece = piece;
        this.startPosition = startPosition.cpy();
        this.targetPosition = targetPosition.cpy();
        this.elapsedTime = 0f;
    }

    // Check if animation is done
    public boolean isDone() {
        return elapsedTime >= ANIMATION_DURATION;
    }

    // Update the animation state
    public void update(float delta) {
        elapsedTime += delta;  // Accumulate the time
        float progress = Math.min(elapsedTime / ANIMATION_DURATION, 1f);  // Normalize to [0, 1]
        startPosition.set(startPosition).lerp(targetPosition, progress);
    }
}
