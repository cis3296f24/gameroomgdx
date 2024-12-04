package org.chessGDK.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
/**
 * Represents the a game over screen which appears when the checkmate is made or a puzzle is solved. The player can
 * choose to reset the game, see the board or exit to menu
 */
public class GameOverScreen implements Screen {
    /** Stage for holding UI components.*/
    private Stage stage;
    /** The Skin used for styling UI components.*/
    private Skin skin;
    /** Navigates between screens.*/
    private ScreenManager sm;
    /**
     * Constructor for Game Over screen.
     * Initializes the stage, skin, and sets up the input processor for the screen.
     */
    public GameOverScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // Load the skin
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Add the input listeners and buttons
        addListeners();
        addTitle();
    }
    /**
     * adds Game Over title
     */
    private void addTitle() {
         //add Label for the title
        Label titleLabel = new Label("Game Over", skin);
        titleLabel.setFontScale(2); // Scale up the font for emphasis
        titleLabel.setAlignment(Align.center); // Center-align the text

        // Position the label at the top center of the screen
        titleLabel.setPosition(
                Gdx.graphics.getWidth() / 2f - titleLabel.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f + 100
        );

        // Add the label to the stage
        stage.addActor(titleLabel);
    }
    /**
     * adds different buttons for menu.
     */
    private void addListeners() {
        // Add the play button
        TextButton playButton = new TextButton("Play Again?", skin);
        playButton.setSize(200, 50);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - playButton.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f - playButton.getHeight() / 2f);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to the chess screen");
                sm.exitGame();  // Exits the current game
                sm.playChess();  // Starts a new game
            }
        });
        TextButton showBoardButton = new TextButton("Show Board?", skin);
        showBoardButton.setSize(200, 50);
        showBoardButton.setPosition(Gdx.graphics.getWidth() / 2f - showBoardButton.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f - showBoardButton.getHeight() / 2f-100);

        showBoardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to the chess screen");
                sm.showBoard(); // Switch to the chess screen
            }
        });

        // Add the exit button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.setSize(200, 50);
        exitButton.setPosition(Gdx.graphics.getWidth() / 2f - exitButton.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f - exitButton.getHeight() / 2f - 200);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Exiting the game");
                sm.exitGame();
            }
        });

        stage.addActor(playButton);
        stage.addActor(exitButton);
        stage.addActor(showBoardButton);
    }



    @Override
    public void show() {
            Gdx.input.setInputProcessor(stage);
            sm = ScreenManager.getInstance();

        }

    @Override
    public void render(float delta) {
            ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
            stage.act(delta);
            stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        System.out.println("GameOverScreen disposed");
    }
}
