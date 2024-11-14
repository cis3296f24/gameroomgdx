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

public class PauseScreen implements Screen{
    private Stage stage;
    private Skin skin;
    private ScreenManager game; // Reference to the main game class

    public PauseScreen(ScreenManager game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Add the input listeners and buttons
        addListeners();
    } 

    private void addListeners() {
        TextButton playButton = new TextButton("Resume Chess", skin);
        playButton.setSize(200, 50);
        playButton.setPosition(Gdx.graphics.getWidth() / 2f - playButton.getWidth() / 2f,
                               Gdx.graphics.getHeight() / 2f - playButton.getHeight() / 2f);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to the chess screen");
                game.togglePause(); // Switch to the chess screen                
            }
        });

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // Handle the keydown event here
                if (keycode == Input.Keys.ESCAPE) {
                    System.out.println("Switching to the chess screen");
                    game.togglePause(); // Switch to the chess screen
                    return true; // Return true if the event was handled
                }
                return false; // Return false if the event was not handled
            }
        });

        stage.addActor(playButton);

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
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
