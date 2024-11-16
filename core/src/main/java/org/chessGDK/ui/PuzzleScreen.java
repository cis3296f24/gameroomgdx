package org.chessGDK.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.chessGDK.logic.GameManager;
import org.chessGDK.pieces.Blank;
import org.chessGDK.pieces.Piece;

public class PuzzleScreen implements Screen {
    private final SpriteBatch batch;
    /*
    private Texture whitePawnTexture, blackPawnTexture, whiteRookTexture, blackRookTexture,
                    whiteKnightTexture, blackKnightTexture, whiteBishopTexture, blackBishopTexture,
                    whiteQueenTexture, blackQueenTexture, whiteKingTexture, blackKingTexture;

     */
    private Texture boardTexture;
    private static final int TILE_SIZE = Gdx.graphics.getWidth()/8;
    private Piece[][] board;
    private Blank[][] possibilities;
    private GameManager gm;
    private ScreenManager sm;
    private Stage stage;
    private Skin skin;

    // Variables for piece movement animation
    private Vector2 startPosition, targetPosition, currentPosition;
    private Piece animatedPiece;
    private float elapsedTime = 0f;  // Time passed since animation started
    private float totalTime = .2f;    // Total time to complete the animation (e.g., 2 seconds)
    private final Array<PieceAnimation> activeAnimations = new Array<>();
    private OrthographicCamera camera;
    private puzzleFENs puzzle;

    public PuzzleScreen(ScreenManager sm) {
        puzzle = new puzzleFENs();
        batch = new SpriteBatch();
        camera = new OrthographicCamera(); // Initialize the camera
        camera.setToOrtho(false, 800, 800); // Set the viewport size
        this.sm = sm;

        // Initialize the Stage and Skin for Buttons (not in use)
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Set the stage to handle input
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load your skin file

    }

    public void addButtons(GameManager gm) {
        // Create the button
        TextButton aiTurnButton = new TextButton("Take your Turn", skin);
        aiTurnButton.setPosition(10, 10); // Set position of the button
        aiTurnButton.setSize(200, 50);    // Set size of the button
        // Add a ClickListener to the button
        aiTurnButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                System.out.println("AI Turn Button Clicked");
                // Call AI move logic here
                boolean went = gm.aiTurn();
                if (!went)
                    System.out.println("aiTakeTurn(): " + went);
            }
        });

        // Add the button to the stage
        stage.addActor(aiTurnButton);
    }

    public void loadTextures(GameManager gm) {
        boardTexture = new Texture("blue3.jpg");
        this.gm = gm;
        String FEN = puzzle.getRandomPuzzle();
        gm.parseFen(FEN);
        gm.getPieceFromString(FEN);

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
        // Clear the screen with a solid color (black, in this case)
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
        updateAnimations(delta);
        drawPieces();


        batch.end();

        // Update and draw the stage (which contains the button)
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // Method to start the animation
    public void startPieceAnimation(Piece piece, int startX, int startY, int endX, int endY) {
        animatedPiece = piece;
        startPosition = new Vector2(startX * TILE_SIZE, startY * TILE_SIZE);
        targetPosition = new Vector2(endX * TILE_SIZE, endY * TILE_SIZE);
        activeAnimations.add(new PieceAnimation(piece, startPosition, targetPosition));
    }

    public void updateAnimations(float delta) {
        for (PieceAnimation animation : activeAnimations) {
            animation.update(delta);  // Delta is passed to ensure frame-rate independence

            // Draw the animated piece
            Texture pieceTexture = animation.piece.getTexture();
            batch.draw(pieceTexture, animation.startPosition.x, animation.startPosition.y, TILE_SIZE, TILE_SIZE - 5);

            if (animation.isDone()) {
                animation.piece.toggleAnimating();
                activeAnimations.removeValue(animation, true); // Remove the animation if done
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