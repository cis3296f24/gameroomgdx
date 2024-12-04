package org.chessGDK.ui;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;

import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.*;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.ScreenViewport;




/**
 * Represents the main screen for displaying and interacting with the chessboard in the game.
 * This class handles the rendering of the chessboard, pieces, and possible move indicators,
 * as well as user input processing and stage management.
 */
public class ChessBoardScreen implements Screen {
    /** The SpriteBatch used for drawing textures and game elements.*/
    private final SpriteBatch batch;
    /** The texture used to render the chessboard background.*/
    private Texture boardTexture;
    /** The size of a single tile on the chessboard*/
    private static int TILE_SIZE = Gdx.graphics.getWidth()/8;
    /**  TThe 2D array representing the current state of the chessboard pieces .*/
    private Piece[][] board;
    /**  The 2D array representing possible moves as graphical indicators on the board */
    private Blank[][] possibilities;
    /** The Stage used for managing and drawing UI elements and actors.*/
    public Stage stage;
    /** The Skin used for styling UI components.*/
    public Skin skin;
    /** The input handler that processes user interactions with the chessboard.*/
    private PieceInputHandler inputHandler;
    /** The orthographic camera for managing the viewport and rendering perspective.*/
    private final OrthographicCamera camera;
    /**
     * Constructor for the `ChessBoardScreen` class.
     * Initializes the rendering components, stage, camera, and skin for the chessboard screen.
     */
    public ChessBoardScreen() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(); // Initialize the camera
        camera.setToOrtho(false, 800, 800); // Set the viewport size
        // Initialize the Stage and Skin for Buttons (not in use)
        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load your skin file

    }

    /**
     * Loads the textures and initializes the chessboard and prepares the board's for rendering.
     *
     * @param gm the GameManager that provides the chessboard state and logic.
     */
    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("brown.png");
        board = gm.getBoard();
        possibilities = gm.getPossibilities();
        inputHandler = new PieceInputHandler(this, gm, camera, board, possibilities, TILE_SIZE);
        // Pieces load textures when created, placing them displays them
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[i].length; j++) {
                Piece piece = board[i][j];
                Blank b = possibilities[i][j];
                b.setPosition(j*TILE_SIZE, i*TILE_SIZE);
                if (piece != null) {
                    piece.setPosition(j*TILE_SIZE, i*TILE_SIZE);
                    piece.setWidth(TILE_SIZE);
                    piece.setHeight(TILE_SIZE - 5);
                    piece.setVisible(true);
                    stage.addActor(piece);
                }
            }
        }
    }
    /**
     * Renders the chessboard and its elements which is called continuously in the game loop.
     * @param delta the time elapsed since the last frame, in seconds.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1); // Clear to white
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begin drawing
        batch.begin();
        // Draw the chessboard (you can later add pieces and other elements)
        batch.draw(boardTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        drawPossibilities();

        batch.end();

        // Update and draw the stage (which contains the button)
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawPossibilities() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Blank b = possibilities[i][j];
                batch.draw(b.getTexture(), b.getXPos(), b.getYPos(), TILE_SIZE, TILE_SIZE);
            }
        }
    }
    /**
     * Disposes resources associated with the screen, such as textures
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        inputHandler = null;
        System.out.println("ChessBoardScreen disposed");
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }
    /**
     * Handles resizing of the screen. Updates the viewport and recalculates tile size.
     *
     * @param width  the new width of the screen.
     * @param height the new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
        TILE_SIZE = width/8;
        inputHandler.resize(TILE_SIZE);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.

    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }
}
