package org.chessGDK.ui;

import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.Blank;
import org.chessGDK.pieces.Pawn;
import org.chessGDK.pieces.Piece;
import org.chessGDK.utils.CoordinateUtils;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;

public class PieceInputHandler extends InputAdapter {
    private Piece selectedPiece = null;
    private Piece hiddenPiece = null;
    private Vector2 liftChars = new Vector2();
    private final Vector3 liftPositon = new Vector3();
    private final Vector3 dropPosition = new Vector3();
    private boolean firstClick = true; // To track if it's the first click
    private boolean isDragging = false;
    private String move;

    private final ChessBoardScreen screen;
    private final GameManager gm;
    private final Camera camera;
    private final Piece[][] board;
    private final Blank[][] possibilities;
    private int TILE_SIZE;

    private CoordinateUtils coords;

    public PieceInputHandler(ChessBoardScreen screen, GameManager gm, Camera camera, Piece[][] board, Blank[][] p, int tileSize) {
        move = "";
        this.screen = screen;
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
            ScreenManager.getInstance().pauseGame();
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
        // First click: Select a piece if there's one at this position
        if (board[liftY][liftX] == null) {
            System.out.println("No piece at: " + (char) (liftX + 'a') + ", " + (char) (liftY + '1'));
            return;
        }
        else if (gm.multiplayerMode && (board[liftY][liftX].isWhite() != gm.getPlayerColor())) {
            System.out.println("Not your turn");
            return;
        }
        else if (board[liftY][liftX].isWhite() != gm.isWhiteTurn()) {
            System.out.println("Not your turn");
            return;
        }
        move = "";
        hiddenPiece = board[liftY][liftX];

        selectedPiece = board[liftY][liftX].copy();
        selectedPiece.setPosition(worldCoordinates.x - 50, worldCoordinates.y - 50);
        selectedPiece.setWidth(hiddenPiece.getWidth());
        selectedPiece.setHeight(hiddenPiece.getHeight());
        selectedPiece.setVisible(true);

        hiddenPiece.setVisible(false);
        hiddenPiece.getParent().addActor(selectedPiece);
        isDragging = true;
        firstClick = false; // Switch to second click
        liftX += 'a';
        liftY += '1';
        liftChars = new Vector2(liftX, liftY);
        System.out.println("Selected piece at: " + (char) liftX + ", " + (char) liftY);
        showPossible();

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

        move = String.valueOf((char) liftChars.x) +
            (char) liftChars.y +
            (char) (placeX + 'a') +
            (char) (placeY + '1');
        if (gm.isLegalMove(move)) {
            System.out.println("Placed piece at: " + (char) (placeX + 'a') + ", " + (char) (placeY + '1'));
            isDragging = false;
            clearPossible();

            // Check for pawn promotion
            if (selectedPiece instanceof Pawn) {
                boolean isWhite = hiddenPiece.isWhite();
                boolean needsPromotion = (isWhite && placeY == 7) || (!isWhite && placeY == 0);
                if (needsPromotion) {
                    // Show promotion dialog and defer move queuing
                    showPromotionOptions();
                    return; // Exit early, move will be queued in the callback
                }
            }

            // If no promotion, queue the move immediately
            gm.queueMove(move);
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

    public void showPromotionOptions() {
        InputProcessor prevInput = Gdx.input.getInputProcessor();
        Dialog promotionDialog = new Dialog("Choose a new Rank", screen.skin) {
            @Override
            protected void result(Object object) {
                String choice = (String) object;
                System.out.println(choice);
                // Append promotion choice to the move
                move += choice;

                // Queue the move after promotion choice is made
                gm.queueMove(move);

                // Reset state after queuing the move
                selectedPiece.remove();
                firstClick = true;
                selectedPiece = null;
                liftChars = null;
            }

            @Override
            public void hide() {
                this.remove();
                Gdx.input.setInputProcessor(prevInput);
            }
        };

        promotionDialog.text("Select a piece:");
        promotionDialog.button("Queen", "q");
        promotionDialog.button("Rook", "r");
        promotionDialog.button("Bishop", "b");
        promotionDialog.button("Knight", "n");
        promotionDialog.show(screen.stage);
        Gdx.input.setInputProcessor(screen.stage);
    }

    private void showPossible() {
        String legalMoves;
        try {
            legalMoves = gm.getLegalMoves();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String string : legalMoves.split(",")) {
            if (string.startsWith("" + (char) liftChars.x + (char) liftChars.y)) {
                int col = string.charAt(2) - 'a';
                int row = string.charAt(3) - '1';
                Blank temp = possibilities[row][col];
                if (board[row][col] != null)
                    temp.setTexture(new Texture("red_25_alpha.png"));
                else
                    temp.setTexture(new Texture("green_25_alpha.png"));
            }
        }
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
