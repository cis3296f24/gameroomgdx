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

public class ChessBoardScreen implements Screen {
    private final SpriteBatch batch;
    private Texture boardTexture;
    private static final int TILE_SIZE = Gdx.graphics.getWidth() / 8;
    private Piece[][] board;
    private Blank[][] possibilities;
    private GameManager gm;
    private ScreenManager sm;
    private Stage stage;
    private Skin skin;

    private Vector2 startPosition, targetPosition, currentPosition;
    private Piece animatedPiece;
    private float elapsedTime = 0f;
    private float totalTime = .2f;
    private final Array<PieceAnimation> activeAnimations = new Array<>();
    private OrthographicCamera camera;

    public ChessBoardScreen(ScreenManager sm) {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800);
        this.sm = sm;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        addControlButtons(); // Add control buttons here
    }

    public void addControlButtons() {
        // Resize Button
        TextButton resizeButton = new TextButton("Resize", skin);
        resizeButton.setPosition(10, 70);
        resizeButton.setSize(200, 50);
        resizeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Resize Button Clicked");
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });
        stage.addActor(resizeButton);

        // Pause Button
        TextButton pauseButton = new TextButton("Pause", skin);
        pauseButton.setPosition(10, 130);
        pauseButton.setSize(200, 50);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Pause Button Clicked");
                pause();
            }
        });
        stage.addActor(pauseButton);

        // Resume Button
        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.setPosition(10, 190);
        resumeButton.setSize(200, 50);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Resume Button Clicked");
                resume();
            }
        });
        stage.addActor(resumeButton);

        // Hide Button
        TextButton hideButton = new TextButton("Hide", skin);
        hideButton.setPosition(10, 250);
        hideButton.setSize(200, 50);
        hideButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Hide Button Clicked");
                hide();
            }
        });
        stage.addActor(hideButton);
    }

    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("blue3.jpg");
        this.gm = gm;
        board = gm.getBoard();
        possibilities = gm.getPossibilities();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Piece piece = board[i][j];
                Blank b = possibilities[i][j];
                b.setPosition(j * TILE_SIZE, i * TILE_SIZE);
                if (piece != null) {
                    piece.setPosition(j * TILE_SIZE, i * TILE_SIZE);
                }
            }
        }
    }

    private boolean applyTexture(Piece piece) {
        if (piece == null)
            return false;
        String texturePath = "Chess_" +
                piece.toString() +
                (piece.isWhite() ? "l" : "d") +
                "t100.png";
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        piece.setTexture(texture);
        return true;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(boardTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        updateAnimations(delta);
        drawPieces();

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void startPieceAnimation(Piece piece, int startX, int startY, int endX, int endY) {
        animatedPiece = piece;
        startPosition = new Vector2(startX * TILE_SIZE, startY * TILE_SIZE);
        targetPosition = new Vector2(endX * TILE_SIZE, endY * TILE_SIZE);
        activeAnimations.add(new PieceAnimation(piece, startPosition, targetPosition));
    }

    public void updateAnimations(float delta) {
        for (PieceAnimation animation : activeAnimations) {
            animation.update(delta);
            Texture pieceTexture = animation.piece.getTexture();
            batch.draw(pieceTexture, animation.startPosition.x, animation.startPosition.y, TILE_SIZE, TILE_SIZE - 5);

            if (animation.isDone()) {
                animation.piece.toggleAnimating();
                activeAnimations.removeValue(animation, true);
            }
        }
    }

    private void drawPieces() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Piece piece = board[i][j];
                Blank b = possibilities[i][j];
                batch.draw(b.getTexture(), b.getXPos(), b.getYPos(), TILE_SIZE, TILE_SIZE);
                if (piece == null || piece.isAnimating()) continue;
                batch.draw(piece.getTexture(), piece.getXPos(), piece.getYPos(), TILE_SIZE, TILE_SIZE - 5);
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
        System.out.println("Screen resized to: " + width + "x" + height);
    }

    @Override
    public void pause() {
        System.out.println("Game paused");
    }

    @Override
    public void resume() {
        System.out.println("Game resumed");
    }

    @Override
    public void hide() {
        System.out.println("Screen hidden");
    }
}
