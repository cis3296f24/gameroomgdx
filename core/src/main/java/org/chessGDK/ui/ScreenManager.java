package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import org.chessGDK.logic.GameManager;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    private GameManager gm;

    // Create references to different screens
    private ChessBoardScreen chessBoardScreen;
    private MenuScreen menuScreen;
    private PuzzleScreen puzzleScreen;

    // Variable for AI difficulty level
    private int difficulty = 0;
    private boolean condition = true;

    @Override
    public void create() {
        // display the menu at the start
        displayMenu();

    }

    public void displayMenu(){
        menuScreen = new MenuScreen(this);
        this.setScreen(menuScreen);
    }

    public void playPuzzle(){
        try {
            gm = new GameManager(difficulty, false);
            puzzleScreen = new PuzzleScreen(this);
            puzzleScreen.loadTextures(gm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set the screen to Chess
        this.setScreen(puzzleScreen);
    }

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    // Add other methods to manage game state, screens, etc.    
    public void playChess() {
        try {
            gm = new GameManager(difficulty, true);
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

    }

    // Add other methods to manage game state, screens, etc.
}
