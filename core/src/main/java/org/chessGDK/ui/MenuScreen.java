package org.chessGDK.ui;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

        TextButton Play = new TextButton("Play", skin);
        table.add(Play);
        Play.addListener(new ClickListener(){
            public void clicked (InputEvent event, float x, float y){
                screenManager.playChess();
            }
        });

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
