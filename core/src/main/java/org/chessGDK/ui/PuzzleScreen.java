package org.chessGDK.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.Blank;
import org.chessGDK.pieces.Piece;

public class PuzzleScreen implements Screen {
    private final SpriteBatch batch;
    private Texture boardTexture;
    private static final int TILE_SIZE = Gdx.graphics.getWidth()/8;
    private Piece[][] board;
    private Blank[][] possibilities;
    private GameManager gm;
    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;

    public PuzzleScreen() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(); // Initialize the camera
        camera.setToOrtho(false, 800, 800); // Set the viewport size

        // Initialize the Stage and Skin for Buttons (not in use)
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Set the stage to handle input
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load your skin file

    }

    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("blue3.jpg");
        this.gm = gm;

        board = gm.getBoard();
        possibilities = gm.getPossibilities();
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

    @Override
    public void render(float delta) {
        // Clear the screen with a solid color (black, in this case)
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
    }

    @Override
    public void show() {
        PieceInputHandler inputHandler = new PieceInputHandler(gm, camera, board, possibilities, TILE_SIZE);
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
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