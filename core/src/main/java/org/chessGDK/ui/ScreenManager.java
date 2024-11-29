package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import org.chessGDK.logic.GameManager;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    private GameManager gm;
    private static final int FREE_MODE = -1;
    private static final int PUZZLE_MODE = -2;
    public int MODE;
    private static final String NEW_GAME = "position startpos";
    private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Create references to different screens
    private static ScreenManager instance;
    private ChessBoardScreen chessBoardScreen;
    private MenuScreen menuScreen;
    private PauseScreen pauseScreen;
    private GameOverScreen gameOverScreen;

    private boolean paused = false;
    private puzzleFENs puzzle = new puzzleFENs();



    // Variable for AI difficulty level
    private int difficulty = 0;

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

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    // Add other methods to manage game state, screens, etc.
    public void playChess() {
        try {
            MODE = 0;
            gm = new GameManager(difficulty, NEW_GAME);
            chessBoardScreen = getChessBoardScreen();
            gameOverScreen = getGameOverScreen();
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

    public void playPuzzle(){
        try {
            MODE = PUZZLE_MODE;
            String fen = puzzle.getRandomPuzzle();
            System.out.println(fen);
            gm = new GameManager(PUZZLE_MODE, fen);
            chessBoardScreen = getChessBoardScreen();
            gameOverScreen = getGameOverScreen();
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
            MODE = 0;
            gm = new GameManager(FREE_MODE, NEW_GAME);
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

    // Add other methods to manage game state, screens, etc.
    public void loadSaveState() {
        try {
            String fen = "r1bqkbnr/ppp2ppp/2n5/3pp3/6P1/3P1P2/PPP1P2P/RNBQKBNR w KQkq - 1 4";
            gm = new GameManager(difficulty, fen);
            chessBoardScreen = getChessBoardScreen();
            gameOverScreen = getGameOverScreen();
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

    public void exitGame() {
        // Set the screen to Menu
        displayMenu();
        if (chessBoardScreen != null) {
            chessBoardScreen.dispose();
            chessBoardScreen = null;
        }
        if (pauseScreen != null) {
            pauseScreen.dispose();
            pauseScreen = null;
        }
        if (gameOverScreen != null) {
            gameOverScreen.dispose();
            gameOverScreen = null;
        }
        if(paused){
            paused = false;
        }
        gm = null;
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

    public GameOverScreen getGameOverScreen() {
        if (gameOverScreen == null) {
            gameOverScreen = new GameOverScreen();
        }
        return gameOverScreen;
    }

    public void togglePause() {
        // Set the screen to Menu
        if (!paused) {
            paused = true;
            System.out.println("Game Paused");
            this.setScreen(pauseScreen);
        }
        else {
            paused = false;
            System.out.println("Game Resumed");
            this.setScreen(chessBoardScreen);
        }
    }

    public void pauseGame() {
        System.out.println("Game Paused");
        this.setScreen(pauseScreen);
        paused = true;
    }
    public void resumeGame() {
        System.out.println("Game Resumed");
        this.setScreen(chessBoardScreen);
        paused = false;
    }
    @Override
    public void render() {
        // This will call the render method of the current screen
        if (gm != null && gm.isGameOver() && !paused)
            this.setScreen(getGameOverScreen());
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
