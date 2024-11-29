package org.chessGDK.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameOverScreen implements Screen {
    private static final int BUTTON_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 50;
    private static final float FONT_SCALE = 1.2f;
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 1f, 1); // Light blue
    private static final Color HOVER_COLOR = new Color(0.3f, 0.7f, 1f, 1);  // Slightly lighter blue on hover
    private Label messageLabel;

    private Stage stage;
    private Skin skin;
    private ScreenManager sm;

    public GameOverScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        sm = ScreenManager.getInstance();

        // Display "Game Over" message
        String message;
        if (sm.MODE == -2)
            message = "Puzzle Complete!";
        else
            message = "Checkmate!";
        messageLabel = new Label(message, skin);
        messageLabel.setFontScale(1f);
        messageLabel.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        messageLabel.setPosition(Gdx.graphics.getWidth() / 2f - messageLabel.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f + 100);
        messageLabel.setAlignment(Align.center);
        stage.addActor(messageLabel);

        addListeners();
    }

    private void addListeners() {

        // Add Return to Menu button
        TextButton returnToMenuButton = createMenuButton("Return to Menu", "Return to the main menu",
            "Returning to menu", sm::exitGame);
        returnToMenuButton.setPosition(Gdx.graphics.getWidth() / 2f - returnToMenuButton.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - 30);
        stage.addActor(returnToMenuButton);

        // Add Exit App button
        TextButton exitAppButton = createMenuButton("Exit App", "Fully closes the app",
            "Exiting app", Gdx.app::exit);
        exitAppButton.setPosition(Gdx.graphics.getWidth() / 2f - exitAppButton.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - 90);
        stage.addActor(exitAppButton);
    }

    private TextButton createMenuButton(String text, String tooltipText, String message, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.getLabel().setFontScale(FONT_SCALE);
        button.setColor(BUTTON_COLOR);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println(message);
                action.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.setColor(HOVER_COLOR);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setColor(BUTTON_COLOR);
            }
        });

        return button;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        System.out.println("GameOverScreen disposed");
    }
}

