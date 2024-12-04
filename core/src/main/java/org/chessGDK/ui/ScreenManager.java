package org.chessGDK.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.chessGDK.logic.GameManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
/**
 * This class handles the scene selection and menus in the application and the transitions between the different screens
 * for each mode.
 */
/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class ScreenManager extends Game {
    /** The game manager handling the state and logic of the chess game. */
    private GameManager gm;
    /** Integer that represents free mode */
    private static final int FREE_MODE = -1;
    /** Integer that represents puzzle mode */
    private static final int PUZZLE_MODE = -2;
    /** Integer that represents multiplayer mode */
    private static final int MULTIPLAYER_MODE = -3;
    /** Game mode reprsented an integer */
    public int MODE;
    /**String represention of initial chess board setup*/
    private static final String NEW_GAME = "position startpos";
    /**String represention in FEN notation of initial chess board setup*/
    private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    /**Singleton instance of Screen Manager*/
    // Create references to different screens
    private static ScreenManager instance;
    /**Screen for the chess board*/
    private ChessBoardScreen chessBoardScreen;
    /**Screen for Menu*/
    private MenuScreen menuScreen;
    /**Screen for Pause menu*/
    private PauseScreen pauseScreen;
    /**Indicates whether screen is paused*/
    private boolean paused = false;
    /** FENs for board states used on puzzle mode */
    private puzzleFENs puzzle = new puzzleFENs();
    /**Saved fen used in save states*/
    private String savedFEN = START_FEN;



    // Variable for AI difficulty level
    /**AI difficulty level*/
    private int difficulty = 0;

    // For Network Setup
    /**Used for network setup*/
    private String HostOrClient = "";

    // Currently only supports localHost
    private String serverIP = "127.0.0.1";

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
    /**Sets screen to Menu*/
    public void displayMenu(){
        this.setScreen(getMenuScreen());
    }

    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }

    public void setHostOrClient(String HostOrClient){
        this.HostOrClient = HostOrClient;
    }

    // Add other methods to manage game state, screens, etc.
    /**Initializes board screen, pause screen, game loop and game manager. Sets screen to chess board*/
    public void playChess() {
        try {
            MODE = 0;
            gm = new GameManager(difficulty, NEW_GAME, HostOrClient);
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
    /**Initializes board screen, pause screen, game loop and game manager. Sets screen to a random fen used in puzzle
     * mode*/
    public void playPuzzle(){
        try {
            MODE = PUZZLE_MODE;
            String fen = puzzle.getRandomPuzzle();
            System.out.println(fen);
            gm = new GameManager(PUZZLE_MODE, fen, HostOrClient);
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
    /**Initializes board screen, pause screen, game loop and game manager. Sets screen to a chessboard used for free
     * mode where you can player against yourself*/
    public void playFreeMode() {
        try {
            MODE = 0;
            gm = new GameManager(FREE_MODE, NEW_GAME, HostOrClient);
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
    /**Initializes board screen, pause screen, game loop and game manager. Sets screen to a chessboard used for multiplayer
     * mode */
    public void playMultiplayer(){
        try {
            gm = new GameManager(MULTIPLAYER_MODE, NEW_GAME, HostOrClient);
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
    /**Initializes board screen, pause screen, game loop and game manager
     *  for save states. Sets screen to a chessboard based on given fen  */
    public void loadSaveState() {
        try {
            gm = new GameManager(difficulty, savedFEN, HostOrClient);
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
    /** sets screen to menu*/
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
    public void showBoard(){
        System.out.println("Showing Board");
        this.setScreen(getChessBoardScreen());
    }

    public void saveGame() {
        gm.saveGame();
    }
    /**sets screen to pause screen */
    public void pauseGame() {
        System.out.println("Game Paused");
        this.setScreen(pauseScreen);
        paused = true;
    }
    /**sets board back to game from pause screen */
    public void resumeGame() {
        System.out.println("Game Resumed");
        this.setScreen(chessBoardScreen);
        paused = false;
    }
    @Override
    public void render() {
        // This will call the render method of the current screen
        super.render();
    }

    @Override
    /** Disposes all screens for cleanup */
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

    public void setSavedFEN(String fen){
        savedFEN = fen;
    }

    // Add other methods to manage game state, screens, etc.
}
