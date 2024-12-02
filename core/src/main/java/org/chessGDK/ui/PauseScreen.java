package org.chessGDK.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class PauseScreen implements Screen{
    private static final int BUTTON_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 50;
    private static final float FONT_SCALE = 1.2f;
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 1f, 1); // Light blue
    private static final Color HOVER_COLOR = new Color(0.3f, 0.7f, 1f, 1);  // Slightly lighter blue on hover
    private Label tooltipLabel;

    private Stage stage;
    private Skin skin;
    private ScreenManager sm;

    public PauseScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // Load the skin
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        tooltipLabel = new Label("", skin);
        sm = ScreenManager.getInstance();
        addListeners();
        // Add the input listeners and buttons
    }

    private void addListeners() {
        // Add the play button
        TextButton playButton = createMenuButton("Resume", "Resumes the game",
                                            "Switching to game screen", sm::resumeGame);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - playButton.getWidth() / 2f,
                               Gdx.graphics.getHeight() / 2f - playButton.getHeight() / 2f + 60);
        stage.addActor(playButton);

        // Add the exit to menu button
        TextButton exitToMenu = createMenuButton("Return to Menu", "Exits to main menu",
                                                "Exiting to menu", sm::exitGame);
        exitToMenu.setPosition(Gdx.graphics.getWidth() / 2f - exitToMenu.getWidth() / 2f,
                               Gdx.graphics.getHeight() / 2f - exitToMenu.getHeight() / 2f);
        stage.addActor(exitToMenu);

        // Add the exit application button
        TextButton exitApp = createMenuButton("Exit App", "Fully closes the app",
                                            "Exiting App", Gdx.app::exit);
        exitApp.setPosition(Gdx.graphics.getWidth() / 2f - exitApp.getWidth() / 2f,
                            Gdx.graphics.getHeight() / 2f - exitApp.getHeight() / 2f - 60);
        stage.addActor(exitApp);

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // Handle the keydown event here
                if (keycode == Input.Keys.ESCAPE) {
                    System.out.println("Switching to the chess screen");
                    sm.resumeGame(); // Switch to the chess screen
                    return true; // Return true if the event was handled
                }
                return false; // Return false if the event was not handled
            }
        });
        stage.addActor(tooltipLabel);
    }

    private TextButton createMenuButton(String text, String tooltipText,
                                        String message, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.getLabel().setFontScale(FONT_SCALE);
        button.setColor(BUTTON_COLOR);

        // Show tooltip on hover and position it intelligently
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                tooltipLabel.setText(tooltipText);
                tooltipLabel.setVisible(true);
                positionTooltip(button);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                tooltipLabel.setVisible(false);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println(message);
                action.run();
                tooltipLabel.setVisible(false);
            }
        });

        return button;
    }

    private void positionTooltip(Actor actor) {
        float tooltipX = actor.getX() + actor.getWidth() + 10; // Position to the right of the actor
        float tooltipY = actor.getY() + actor.getHeight() / 2; // Center vertically with the actor

        // Check if tooltip goes out of screen bounds, adjust if necessary
        if (tooltipX + tooltipLabel.getWidth() > Gdx.graphics.getWidth()) {
            // Place tooltip above the actor if it goes off the right edge
            tooltipX = actor.getX();
            tooltipY = actor.getY() + actor.getHeight() + 10;
        }

        // Update the tooltip label position
        tooltipLabel.setPosition(tooltipX, tooltipY);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
    public void pause() {}

    @Override
    public void resume() {

    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        System.out.println("PauseScreen disposed");
    }
}
