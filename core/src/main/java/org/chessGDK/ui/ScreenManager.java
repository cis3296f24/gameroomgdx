package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import org.chessGDK.logic.GameManager;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    private GameManager gm;

    // Create references to different screens
    private static ScreenManager instance;
    private ChessBoardScreen chessBoardScreen;
    private MenuScreen menuScreen;
    private PauseScreen pauseScreen;

    private boolean paused = false;
    private PuzzleScreen puzzleScreen;

    private puzzleFENs puzzle;

    // Variable for AI difficulty level
    private int difficulty = 0;
    private String FEN;

    // Private constructor to prevent external instantiation
    private ScreenManager() {}

    // Singleton instance
    public static ScreenManager getInstance() {
        if (instance == null) {
            synchronized (ScreenManager.class) {
                if (instance == null) {
                    instance = new ScreenManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void create() {
        // display the menu at the start
        displayMenu();

    }

    public void displayMenu(){
        this.setScreen(getMenuScreen());
    }

    public void playPuzzle(){
        try {
            puzzle = new puzzleFENs();
            FEN = puzzle.getRandomPuzzle();
            gm = new GameManager(difficulty, FEN);
            puzzleScreen = getPuzzleScreen();
            puzzleScreen.loadTextures(gm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set the screen to Chess
        this.setScreen(puzzleScreen);
        menuScreen.dispose();
        menuScreen = null;
    }

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    // Add other methods to manage game state, screens, etc.
    public void playChess() {
        try {
            gm = new GameManager(difficulty);
            chessBoardScreen = getChessBoardScreen();
            pauseScreen = getPauseScreen();
            chessBoardScreen.loadTextures(gm);
            gm.startGameLoopThread();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set the screen to Chess
        this.setScreen(chessBoardScreen);
        menuScreen.dispose();
        menuScreen = null;
    }

    public void playFreeMode() {
        try {
            gm = new GameManager(-1);
            chessBoardScreen = getChessBoardScreen();
            pauseScreen = getPauseScreen();
            chessBoardScreen.loadTextures(gm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set the screen to Chess
        this.setScreen(chessBoardScreen);
        menuScreen.dispose();
        menuScreen = null;
    }

    // Add other methods to manage game state, screens, etc.
    public void loadSaveState() {
        try {
            String fen = "rnbqkb1r/p1pp1ppp/1p2pn2/8/2PP4/4B2N/PP2PPPP/RN1QKB1R";
            gm = new GameManager(difficulty, fen);
            chessBoardScreen = new ChessBoardScreen();
            chessBoardScreen.loadTextures(gm);
            //chessBoardScreen.addButtons(gm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set the screen to Chess
        this.setScreen(chessBoardScreen);
        menuScreen.dispose();
        menuScreen = null;
    }

    public void exitChess() {
        // Set the screen to Menu
        gm.exitGame();
        gm = null;
        chessBoardScreen.dispose();
        chessBoardScreen = null;
        this.setScreen(getMenuScreen());
        pauseScreen.dispose();
        pauseScreen = null;
        if(paused){
            paused = false;
        }
    }

    public MenuScreen getMenuScreen() {
        if (menuScreen == null) {
            menuScreen = new MenuScreen();
        }
        return menuScreen;
    }

    public ChessBoardScreen getChessBoardScreen() {
        if (chessBoardScreen == null) {
            chessBoardScreen = new ChessBoardScreen();
        }
        return chessBoardScreen;
    }

    public PauseScreen getPauseScreen() {
        if (pauseScreen == null) {
            pauseScreen = new PauseScreen();
        }
        return pauseScreen;
    }

    public PuzzleScreen getPuzzleScreen() {
        if (puzzleScreen == null) {
            puzzleScreen = new PuzzleScreen();
        }
        return puzzleScreen;
    }

    public void togglePause() {
        // Set the screen to Menu
        if (!paused) {
            paused = true;
            System.out.println("Game Paused");
            this.setScreen(getPauseScreen());
        }
        else {
            paused = false;
            System.out.println("Game Resumed");
            this.setScreen(getChessBoardScreen());
        }
    }

    @Override
    public void render() {
        // This will call the render method of the current screen
        super.render();
    }

    @Override
    public void dispose() {
        if(gm != null){
            gm.exitGame();
            gm = null;
        }
        // Dispose of all the screens
        if (chessBoardScreen != null) {
            chessBoardScreen.dispose();
            chessBoardScreen = null;
        }
        if (menuScreen != null) {
            menuScreen.dispose();
            chessBoardScreen = null;
        }
        if (pauseScreen != null) {
            pauseScreen.dispose();
            chessBoardScreen = null;
        }
    }

    // Add other methods to manage game state, screens, etc.
}
