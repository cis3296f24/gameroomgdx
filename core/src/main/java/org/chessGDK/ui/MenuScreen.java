package org.chessGDK.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class MenuScreen implements Screen {

    // Constants
    private static final int BUTTON_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 60;
    private static final float FONT_SCALE = 1.5f;
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 1f, 1); // Light blue
    private static final Color HOVER_COLOR = new Color(0.3f, 0.7f, 1f, 1);  // Slightly lighter blue on hover

    // Fields
    private final ScreenManager screenManager;
    private final Stage stage;
    private final Skin skin;
    private SelectBox<String> diffSelectBox;
    private SelectBox<String> HOCSelectBox;
    private SelectBox<String> saveSelectBox;
    private Label tooltipLabel;
    private String difficultyText;

    // Constructor
    public MenuScreen() {
        this.screenManager = ScreenManager.getInstance();
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));
        Gdx.input.setInputProcessor(stage);

    }

    // Create all menu buttons and UI components
    private void createMenuButtons() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Tooltip label (hidden by default)
        tooltipLabel = new Label("", skin);
        tooltipLabel.setColor(Color.LIGHT_GRAY);
        tooltipLabel.setVisible(false);
        stage.addActor(tooltipLabel);

        // Add Title Label
        Label titleLabel = new Label("Chess", skin);
        titleLabel.setFontScale(2.5f);
        titleLabel.setColor(Color.WHITE);
        table.add(titleLabel).padBottom(40).center();
        table.row();

        // Add Singleplayer button
        table.add(createMenuButton("Singleplayer", "Start a game against AI", screenManager::playChess))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
        table.row();

        // Add Difficulty SelectBox
        createDifficultySelectBox(table);

        // Add Multiplayer button
        table.add(createMenuButton("Multiplayer", "Play against another player", screenManager::playMultiplayer))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
        table.row();

        // Add Host or Client SelectBox
        createHOCSelectBox(table);

        // Add Puzzle button
        table.add(createMenuButton("Puzzle", "Try solving chess puzzles", screenManager::playPuzzle))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
        table.row();

        // Add Free Mode button
        table.add(createMenuButton("Free Mode", "Play chess without any restrictions", screenManager::playFreeMode))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
        table.row();

        // Add Load button
        table.add(createMenuButton("Load", "Load from a preexisting save state", screenManager::loadSaveState))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
        table.row();

        //Allow user to select save
        createSavesSelectBox(table);

        // Add Exit button
        table.add(createMenuButton("Exit", "Close the application", Gdx.app::exit))
                .width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(15);
    }
    //Create saved games select box
    private void createSavesSelectBox(Table table){
        File folder = new File("saves");
        if(!folder.exists()){
            folder.mkdirs();
        }
        Array<String> arr = new Array<>();
        String folderPath = "saves"; // Replace with your folder path

        // Using Files.walk() to traverse the folder and its subfolders
        try (Stream<Path> paths = Files.walk(Path.of(folderPath))) {
            paths
                    .filter(Files::isRegularFile) // Filter for only files
                    .forEach(filePath -> {
                        // Read file content
                        arr.add(String.valueOf(filePath.getFileName()));
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saveSelectBox = new SelectBox<>(skin);
        saveSelectBox.setItems(arr);

        saveSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = saveSelectBox.getSelected();
                System.out.println(selected);
                try (BufferedReader br = new BufferedReader(new FileReader("saves/"+selected))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        screenManager.setSavedFEN(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
            }
        });

        table.add(saveSelectBox).padBottom(15);
        table.row();
    }

    // Create Difficulty SelectBox
    private void createDifficultySelectBox(Table table) {
        diffSelectBox = new SelectBox<>(skin);
        diffSelectBox.setItems("Novice", "Intermediate", "Expert", "Master");

        diffSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLevel = diffSelectBox.getSelected();
                int difficulty;

                switch (selectedLevel) {
                    case "Novice":
                        difficulty = 0;
                        difficultyText = "Difficulty Novice set - ELO: 800";
                        break;
                    case "Intermediate":
                        difficulty = 2;
                        difficultyText = "Difficulty Intermediate set - ELO: 1200";
                        break;
                    case "Expert":
                        difficulty = 5;
                        difficultyText = "Difficulty Expert set - ELO: 1600";
                        break;
                    case "Master":
                        difficulty = 9;
                        difficultyText = "Difficulty Master set - ELO: 2000";
                        break;
                    default:
                        difficulty = 0;
                }

                screenManager.setDifficulty(difficulty);
                System.out.println(difficultyText);
            }
        });
        diffSelectBox.getList().addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                int index = diffSelectBox.getList().getSelectedIndex();
                if (index != -1) {
                    // Get the stage coordinates of the list
                    Vector2 listPosition = diffSelectBox.getList().localToStageCoordinates(new Vector2(0, 0));

                    // Calculate the Y-coordinate of the selected item
                    float itemHeight = diffSelectBox.getList().getItemHeight();
                    float selectedItemY = listPosition.y + diffSelectBox.getList().getHeight() - (index + 1) * itemHeight;

                    // Set the tooltip position to the right of the selected item
                    tooltipLabel.setPosition(listPosition.x + diffSelectBox.getList().getWidth() + 10, selectedItemY + itemHeight / 2);
                    switch(index) {
                        case 0:
                            tooltipLabel.setText("Play against an AI with an ELO rating of 800");
                            tooltipLabel.setVisible(true);
                            break;
                        case 1:
                            tooltipLabel.setText("Play against an AI with an ELO rating of 1200");
                            tooltipLabel.setVisible(true);
                            break;
                        case 2:
                            tooltipLabel.setText("Play against an AI with an ELO rating of 1600");
                            tooltipLabel.setVisible(true);
                            break;
                        case 3:
                            tooltipLabel.setText("Play against an AI with an ELO rating of 2000");
                            tooltipLabel.setVisible(true);
                            break;
                    }
                }
                return super.mouseMoved(event, x, y);
            }
        });
        table.add(diffSelectBox).padBottom(15);
        table.row();
    }

    // Create Host or Client SelectBox
    private void createHOCSelectBox(Table table) {
        HOCSelectBox = new SelectBox<>(skin);
        HOCSelectBox.setItems("Host", "Client");

        HOCSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = HOCSelectBox.getSelected();

                if ("Host".equals(selected)) {
                    screenManager.setHostOrClient("Host");
                    System.out.println("Setting up server");
                } else if ("Client".equals(selected)) {
                    screenManager.setHostOrClient("Client");
                    System.out.println("Setting up client");
                }
            }
        });
        HOCSelectBox.getList().addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                int index = HOCSelectBox.getList().getSelectedIndex();
                if (index != -1) {
                    // Get the stage coordinates of the list
                    Vector2 listPosition = HOCSelectBox.getList().localToStageCoordinates(new Vector2(0, 0));

                    // Calculate the Y-coordinate of the selected item
                    float itemHeight = HOCSelectBox.getList().getItemHeight();
                    float selectedItemY = listPosition.y + HOCSelectBox.getList().getHeight() - (index + 1) * itemHeight;

                    // Set the tooltip position to the right of the selected item
                    tooltipLabel.setPosition(listPosition.x + HOCSelectBox.getList().getWidth() + 10, selectedItemY + itemHeight / 2);
                    switch(index) {
                        case 0:
                            tooltipLabel.setText("Host a server for client");
                            tooltipLabel.setVisible(true);
                            break;
                        case 1:
                            tooltipLabel.setText("Connect as a client to server");
                            tooltipLabel.setVisible(true);
                            break;
                    }
                }
                return super.mouseMoved(event, x, y);
            }
        });

        table.add(HOCSelectBox).padBottom(15);
        table.row();
    }

    // Create a menu button
    private TextButton createMenuButton(String text, String tooltipText, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.getLabel().setFontScale(FONT_SCALE);
        button.setColor(BUTTON_COLOR);

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
                action.run();
                tooltipLabel.setVisible(false);
            }
        });

        return button;
    }

    // Position tooltip intelligently
    private void positionTooltip(Actor actor) {
        float tooltipX = actor.getX() + actor.getWidth() + 10;
        float tooltipY = actor.getY() + actor.getHeight() / 2;

        if (tooltipX + tooltipLabel.getWidth() > Gdx.graphics.getWidth()) {
            tooltipX = actor.getX();
            tooltipY = actor.getY() + actor.getHeight() + 10;
        }

        tooltipLabel.setPosition(tooltipX, tooltipY);
    }

    // Screen lifecycle methods
    @Override
    public void show() {
        createMenuButtons();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear the screen with a simple background color
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
        System.out.println("MenuScreen disposed");
    }
}
