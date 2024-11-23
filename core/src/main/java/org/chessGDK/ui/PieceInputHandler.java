package org.chessGDK.ui;

import com.badlogic.gdx.graphics.Camera;
import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.Blank;
import org.chessGDK.pieces.Piece;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.chessGDK.utils.CoordinateUtils;

import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;

public class PieceInputHandler extends InputAdapter {
    private Piece selectedPiece = null;
    private Piece hiddenPiece = null;
    private Vector2 liftChars = new Vector2();
    private Vector3 liftPositon = new Vector3();
    private Vector3 dropPosition = new Vector3();
    private boolean firstClick = true; // To track if it's the first click
    private boolean isDragging = false;

    private final GameManager gm;
    private final Camera camera;
    private final Piece[][] board;
    private final Blank[][] possibilities;
    private int TILE_SIZE;

    private CoordinateUtils coords;

    public PieceInputHandler(GameManager gm, Camera camera, Piece[][] board, Blank[][] p, int tileSize) {
        this.gm = gm;
        this.camera = camera;
        this.board = board;
        this.possibilities = p;
        this.TILE_SIZE = tileSize;
        coords = new CoordinateUtils(TILE_SIZE);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convert screen coordinates to world coordinates
        if (button == Input.Buttons.LEFT) {
            Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
            camera.unproject(worldCoordinates); // Convert to world coordinates
            if (firstClick) {
                liftPositon.set(worldCoordinates); // Store the first click position
                handleLift(screenX, screenY);
            } else {
                dropPosition.set(worldCoordinates); // Store the second click position
                handlePlace(screenX, screenY);
                isDragging = false;
            }
            return true;
        }
        if (button == Input.Buttons.RIGHT && isDragging) {
            cancelLift();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            // Switch to the menu screen when ESC is pressed
            if(isDragging || !firstClick)
                cancelLift();
            System.out.println("Switching to the menu screen");
            ScreenManager.getInstance().togglePause();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (isDragging && selectedPiece != null) {
            // Update the piece's position to follow the cursor
            Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
            camera.unproject(worldCoordinates);
            selectedPiece.setPosition(worldCoordinates.x - 50, worldCoordinates.y - 50);
        }
        return true;
    }

    // Method to handle selecting a piece
    private void handleLift(int screenX, int screenY) {
        Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
        camera.unproject(worldCoordinates);

        int liftX = coords.worldToBoardX(worldCoordinates.x);
        int liftY = coords.worldToBoardY(worldCoordinates.y);
        int oldX = coords.worldToBoardX(worldCoordinates.x);
        int oldY = coords.worldToBoardY(worldCoordinates.y);
        // First click: Select a piece if there's one at this position
        if (board[liftY][liftX] == null) {
            System.out.println("No piece at: " + (char) (liftX + 'a') + ", " + (char) (liftY + '1'));
            return;
        }
        else if (board[liftY][liftX].isWhite() != gm.isWhiteTurn()) {
            System.out.println("Not your turn");
            return;
        }
        hiddenPiece = board[liftY][liftX];

        selectedPiece = board[liftY][liftX].copy();

        selectedPiece.setPosition(worldCoordinates.x - 50, worldCoordinates.y - 50);
        selectedPiece.setWidth(hiddenPiece.getWidth());
        selectedPiece.setHeight(hiddenPiece.getHeight());
        selectedPiece.setVisible(true);
        hiddenPiece.setVisible(false);
        hiddenPiece.getParent().addActor(selectedPiece);
        if (selectedPiece.isWhite() != gm.isWhiteTurn()) {
            System.out.println("Not your turn");
            return;
        }
        isDragging = true;
        firstClick = false; // Switch to second click
        liftX += 'a';
        liftY += '1';
        liftChars = new Vector2(liftX, liftY);
        System.out.println("Selected piece at: " + (char) liftX + ", " + (char) liftY);
        showPossible(oldX, oldY);

    }

    private void cancelLift() {
        System.out.println("Move cancelled");
        clearPossible();
        firstClick = true;
        isDragging = false;
        selectedPiece.remove();
        selectedPiece = null;
        hiddenPiece.setVisible(true);
        hiddenPiece = null;
    }

    // Method to handle placing the piece
    private void handlePlace(int screenX, int screenY) {
        Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
        camera.unproject(worldCoordinates);
        int placeX = coords.worldToBoardX(worldCoordinates.x);
        int placeY = coords.worldToBoardY(worldCoordinates.y);

        placeX += 'a';
        placeY += '1';
        String move = String.valueOf((char) liftChars.x) +
                (char) liftChars.y +
                (char) placeX +
                (char) placeY;
        if (gm.movePiece(move)) {
            System.out.println("Placed piece at: " + (char) placeX + ", " + (char) placeY);
            placeX -= 'a';
            placeY -= '1';
            isDragging = false;
            clearPossible();
            selectedPiece.remove();
        } else {
            liftChars.x -= 'a';
            liftChars.y -= '1';
            cancelLift();
        }
        firstClick = true; // Reset for the next turn
        selectedPiece = null;  // Reset selection
        liftChars = null;
    }

    private void showPossible(int oldX, int oldY) {
        String legalMoves = "";
        try {
            legalMoves = gm.getLegalMoves();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String string : legalMoves.split(",")) {
            if (string.startsWith("" + (char) liftChars.x + (char) liftChars.y)) {
                System.out.println("Legal move: " + string);
                int col = string.charAt(2) - 'a';
                int row = string.charAt(3) - '1';
                Blank temp = possibilities[row][col];
                temp.setTexture(new Texture("green.png"));
            }
        }
//        for(int col = 0; col <8; ++col){
//            for(int row = 0; row<8; ++row){
//                if(!(oldX == col && oldY == row) && selectedPiece.isValidMove(oldX, oldY, col, row, board)){
//                    Blank temp = possibilities[row][col];
//                    temp.setTexture(new Texture("green.png"));
//                }
//            }
//        }
    }

    private void clearPossible() {
        for(int col = 0; col <8; ++col){
            for(int row = 0; row<8; ++row){
                Blank temp = possibilities[row][col];
                temp.setTexture(new Texture("blank.png"));
            }
        }
    }

    public void resize(int tileSize) {
        TILE_SIZE = tileSize;
        coords = new CoordinateUtils(tileSize);
    }


}
