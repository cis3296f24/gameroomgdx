package org.chessGDK.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
    private static final int BUTTON_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 60;
    private static final float FONT_SCALE = 1.2f;
    private static final Color BUTTON_COLOR = new Color(0.2f, 0.6f, 1f, 1); // Light blue
    private static final Color HOVER_COLOR = new Color(0.3f, 0.7f, 1f, 1);  // Slightly lighter blue on hover

    private ScreenManager screenManager;
    private Stage stage;
    private Skin skin;
    private SelectBox<String> selectBox;
    private Label tooltipLabel;

    public MenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        Gdx.input.setInputProcessor(stage);

        createMenuButtons();
    }

    private void createMenuButtons() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Tooltip label (hidden by default)
        tooltipLabel = new Label("", skin);
        tooltipLabel.setColor(Color.GRAY);
        tooltipLabel.setVisible(false);
        stage.addActor(tooltipLabel);

        // Style setup for buttons
        TextButton.TextButtonStyle buttonStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = HOVER_COLOR;

        // Singleplayer Button
        TextButton singleplayerButton = createMenuButton("Singleplayer", "Start a game against AI");
        singleplayerButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playChess();
            }
        });
        table.add(singleplayerButton).fillX().padBottom(15);
        table.row();

        // Difficulty Level SelectBox with Tooltip
        selectBox = new SelectBox<>(skin);
        selectBox.setItems("Novice", "Intermediate", "Expert", "Master");
        System.out.println("Default Difficulty Novice set - ELO: 800");

        // Add a tooltip for each difficulty level
        selectBox.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                String selectedLevel = selectBox.getSelected();
                switch (selectedLevel) {
                    case "Novice":
                        tooltipLabel.setText("Ideal for beginners learning the basics of chess. (ELO: 800)");
                        break;
                    case "Intermediate":
                        tooltipLabel.setText("For casual players familiar with fundamental tactics. (ELO: 1200)");
                        break;
                    case "Expert":
                        tooltipLabel.setText("Challenging level for advanced players mastering strategy. (ELO: 1600)");
                        break;
                    case "Master":
                        tooltipLabel.setText("Play against a high-level AI with professional skills. (ELO: 2000)");
                        break;
                }
                tooltipLabel.setVisible(true);
                positionTooltip(selectBox);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                tooltipLabel.setVisible(false);
            }
        });

        // Add a listener for difficulty changes
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLevel = selectBox.getSelected();
                int difficulty;

                switch (selectedLevel) {
                    case "Novice":
                        difficulty = 0;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Novice set - ELO: 800");
                        break;
                    case "Intermediate":
                        difficulty = 2;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Intermediate set - ELO: 1200");
                        break;
                    case "Expert":
                        difficulty = 5;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Expert set - ELO: 1600");
                        break;
                    case "Master":
                        difficulty = 9;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Master set - ELO: 2000");
                        break;
                }
            }
        });
        table.add(selectBox).padBottom(15);
        table.row();

        // Multiplayer Button
        TextButton multiplayerButton = createMenuButton("Multiplayer", "Play against another player");
        multiplayerButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playChess();
            }
        });
        table.add(multiplayerButton).fillX().padBottom(15);
        table.row();

        // Puzzle Button
        TextButton puzzleButton = createMenuButton("Puzzle", "Try solving chess puzzles");
        puzzleButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.playPuzzle();
            }
        });
        table.add(puzzleButton).fillX().padBottom(15);
        table.row();

        // Load Save State Button
        TextButton loadButton = createMenuButton("Load", "Loads From Preexisting Save State");
        loadButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                screenManager.loadSaveState();
            }
        });
        table.add(loadButton).fillX().padBottom(15);
        table.row();

        // Exit Button
        TextButton exitButton = createMenuButton("Exit", "Close the application");
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).fillX().padBottom(15);
    }

    private TextButton createMenuButton(String text, String tooltipText) {
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
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear the screen with a background color
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
