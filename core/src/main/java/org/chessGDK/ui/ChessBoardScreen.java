package org.chessGDK.ui;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.*;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


/** First screen of the application. Displayed after the application is created. */
public class ChessBoardScreen implements Screen {
    private final SpriteBatch batch;
    /*
    private Texture whitePawnTexture, blackPawnTexture, whiteRookTexture, blackRookTexture,
                    whiteKnightTexture, blackKnightTexture, whiteBishopTexture, blackBishopTexture,
                    whiteQueenTexture, blackQueenTexture, whiteKingTexture, blackKingTexture;

     */
    private Texture boardTexture;
    private static int TILE_SIZE = Gdx.graphics.getWidth()/8;
    private Piece[][] board;
    private Blank[][] possibilities;
    private GameManager gm;
    public Stage stage;
    private Skin skin;
    private PieceInputHandler inputHandler;

    // Variables for piece movement animation
    private Vector2 startPosition, targetPosition;
    private final Array<PieceAnimation> activeAnimations = new Array<>();
    private OrthographicCamera camera;
    private ScreenManager sm;

    public ChessBoardScreen() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(); // Initialize the camera
        camera.setToOrtho(false, 800, 800); // Set the viewport size
        this.sm = ScreenManager.getInstance();

        // Initialize the Stage and Skin for Buttons (not in use)
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load your skin file

    }

    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("brown.png");
        this.gm = gm;
        board = gm.getBoard();
        possibilities = gm.getPossibilities();
        inputHandler = new PieceInputHandler(gm, camera, board, possibilities, TILE_SIZE);
        Gdx.input.setInputProcessor(inputHandler);
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
        Gdx.gl.glClearColor(1, 1, 1, 1); // Clear to white
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begin drawing
        batch.begin();
        // Draw the chessboard (you can later add pieces and other elements)
        batch.draw(boardTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        /*
        if (animatedPiece != null && animatedPiece.isAnimating())
            animatePiece(delta);

         */
        //updateAnimations(delta);
        drawPieces();


        batch.end();

        // Update and draw the stage (which contains the button)
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }


    private void drawPieces() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Piece piece = board[i][j];
                Blank b = possibilities[i][j];
                batch.draw(b.getTexture(), b.getXPos(), b.getYPos(), TILE_SIZE, TILE_SIZE);
                if (piece == null || piece.isAnimating()) continue;
                //batch.draw(piece.getTexture(), piece.getX(), piece.getY(), TILE_SIZE, TILE_SIZE - 5);
            }
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
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
        ScreenManager.getInstance().togglePause();
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
