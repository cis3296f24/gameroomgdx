package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import org.chessGDK.logic.GameManager;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    private GameManager gm;

    // Create references to different screens
    private ChessBoardScreen chessBoardScreen;


    @Override
    public void create() {
        // Start the game by playing chess
        playChess();
    }

    // Add other methods to manage game state, screens, etc.
    public void playChess() {
        try {
            gm = new GameManager();
            chessBoardScreen = new ChessBoardScreen(this);
            chessBoardScreen.loadTextures(gm);
            //chessBoardScreen.addButtons(gm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the screen to Chess
        this.setScreen(chessBoardScreen);
    }


    @Override
    public void render() {
        // This will call the render method of the current screen
        super.render();
    }

    @Override
    public void dispose() {
        // Dispose of resources when the game ends
        chessBoardScreen.dispose();
    }

    // Add other methods to manage game state, screens, etc.
}
