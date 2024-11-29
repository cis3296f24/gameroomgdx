package org.chessGDK.ui;

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


/** First screen of the application. Displayed after the application is created. */
public class ChessBoardScreen implements Screen {
    private final SpriteBatch batch;

    private Texture boardTexture;
    private static int TILE_SIZE = Gdx.graphics.getWidth()/8;
    private Piece[][] board;
    private Blank[][] possibilities;
    public Stage stage;
    private Skin skin;
    private PieceInputHandler inputHandler;
    private OrthographicCamera camera;
    private puzzleFENs puzzle;

    public ChessBoardScreen() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(); // Initialize the camera
        camera.setToOrtho(false, 800, 800); // Set the viewport size
        // Initialize the Stage and Skin for Buttons (not in use)
        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load your skin file

    }

    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("brown.png");
        board = gm.getBoard();
        possibilities = gm.getPossibilities();
        inputHandler = new PieceInputHandler(gm, camera, board, possibilities, TILE_SIZE);
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

    public Stage getStage() {
        return stage;
    }

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
