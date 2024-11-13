package org.chessGDK.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {
    private ScreenManager screenManager;
    private Stage stage;
    private Skin skin;
    private SelectBox<String> selectBox;

    public MenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        Gdx.input.setInputProcessor(stage);

        createMenuButtons();
    }

    private void createMenuButtons() {
        // Use a table layout for alignment and styling
        Table table = new Table();
        table.setFillParent(true);
        table.center(); // Center the table on the screen
        stage.addActor(table);

        // Add padding and spacing for the table and elements
        table.padTop(50); // Space from the top of the screen
        table.defaults().pad(10).width(250).height(60); // Default padding and button size

        // Singleplayer Button
        TextButton singleplayerButton = new TextButton("Singleplayer", skin);
        singleplayerButton.getLabel().setFontScale(1.2f); // Increase font size
        singleplayerButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playChess();
            }
        });
        table.add(singleplayerButton).fillX().padBottom(15);
        table.row();

        // Difficulty Level SelectBox
        selectBox = new SelectBox<>(skin);
        selectBox.setItems("Level 1", "Level 2", "Level 3");
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Handle level selection if needed
            }
        });
        table.add(selectBox).padBottom(15);
        table.row();

        // Multiplayer Button
        TextButton multiplayerButton = new TextButton("Multiplayer", skin);
        multiplayerButton.getLabel().setFontScale(1.2f);
        multiplayerButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playChess();
            }
        });
        table.add(multiplayerButton).fillX().padBottom(15);
        table.row();

        // Puzzle Button
        TextButton puzzleButton = new TextButton("Puzzle", skin);
        puzzleButton.getLabel().setFontScale(1.2f);
        puzzleButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playChess();
            }
        });
        table.add(puzzleButton).fillX().padBottom(15);
        table.row();

        // Exit Button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.getLabel().setFontScale(1.2f);
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).fillX().padBottom(15);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear the screen with a background color
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1); // Dark gray background

        // Draw the stage elements
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
