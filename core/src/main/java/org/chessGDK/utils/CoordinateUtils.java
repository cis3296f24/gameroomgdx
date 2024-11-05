package org.chessGDK.utils;

public class CoordinateUtils {

    private final int tileSize;

    public CoordinateUtils(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * Converts world coordinates to board X position.
     * @param worldX The X world coordinate.
     * @return The corresponding board X position.
     */
    public int worldToBoardX(float worldX) {
        return (int) (worldX / tileSize);
    }

    /**
     * Converts world coordinates to board Y position.
     * @param worldY The Y world coordinate.
     * @return The corresponding board Y position.
     */
    public int worldToBoardY(float worldY) {
        return (int) (worldY / tileSize);
    }
}
