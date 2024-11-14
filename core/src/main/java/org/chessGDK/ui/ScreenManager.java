package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import org.chessGDK.logic.GameManager;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    private GameManager gm;

    // Create references to different screens
    private ChessBoardScreen chessBoardScreen;
    private PauseScreen pauseScreen;
    private MenuScreen menuScreen;
    private boolean paused = false;

    // Variable for AI difficulty level
    private int difficulty = 0;

    @Override
    public void create() {
        // display the menu at the start
        displayMenu();

    }

    public void displayMenu(){
        menuScreen = new MenuScreen(this);
        this.setScreen(menuScreen);
    }

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    // Add other methods to manage game state, screens, etc.    
    public void playChess() {
        try {
            gm = new GameManager(this, difficulty);
            chessBoardScreen = new ChessBoardScreen(this);
            pauseScreen = new PauseScreen(this);
            chessBoardScreen.loadTextures(gm);
            //chessBoardScreen.addButtons(gm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the screen to Chess
        this.setScreen(chessBoardScreen);
    }

    public void togglePause() {
        // Set the screen to Menu
        if (!paused) {
            paused = true;
            this.setScreen(pauseScreen);
        }
        else {
            paused = false;
            this.setScreen(chessBoardScreen);
        }
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
        pauseScreen.dispose();
    }

    // Add other methods to manage game state, screens, etc.
}
