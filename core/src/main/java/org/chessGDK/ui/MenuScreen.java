package org.chessGDK.ui;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
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

import static com.badlogic.gdx.Gdx.input;

public class MenuScreen implements Screen{
    private ScreenManager screenManager;
    private Stage stage;
    private Skin skin;
    private SelectBox<String> selectBox;


    public MenuScreen(ScreenManager screenManager){
        this.screenManager = screenManager;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        input.setInputProcessor(stage);

        createMenuButtons();
    }

    private void createMenuButtons(){
        //using tables for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton Singleplayer = new TextButton("Singleplayer", skin);
        table.add(Singleplayer);
        Singleplayer.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y){
                screenManager.playChess();
            }
        });

        selectBox = new SelectBox<>(skin);
        table.add(selectBox);
        selectBox.setItems("Level 1", "Level 2", "Level 3");

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        table.row();

        TextButton Multiplayer = new TextButton("Multiplayer", skin);
        table.add(Multiplayer);
        Multiplayer.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y){
                screenManager.playChess();
            }
        });

        table.row();

        TextButton Puzzle = new TextButton("Puzzle", skin);
        table.add(Puzzle);
        Puzzle.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y){
                screenManager.playChess();
            }
        });

        table.row();

        TextButton Exit = new TextButton("Exit", skin);
        table.add(Exit);
        Exit.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y){
                Gdx.app.exit();
            }
        });
    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1,1,1,1,true);

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
    }
}
