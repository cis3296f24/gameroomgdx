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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
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
    private String difficultyText;

    public MenuScreen() {
        this.screenManager = ScreenManager.getInstance();
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
        TextButton singleplayerButton = createMenuButton("Singleplayer", "Start a game against AI", screenManager::playChess);
        table.add(singleplayerButton).fillX().padBottom(15);
        table.row();

        // Difficulty Level SelectBox
        selectBox = new SelectBox<>(skin);
        selectBox.setItems("Novice", "Intermediate", "Expert", "Master");
        difficultyText = "Difficulty Novice set - ELO: 800";
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLevel = selectBox.getSelected();
                int difficulty;
                tooltipLabel.setVisible(false);

                switch (selectedLevel) {
                    case "Novice":
                        difficulty = 0;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Novice set - ELO: 800");
                        difficultyText = "Difficulty Novice set - ELO: 800";
                        break;
                    case "Intermediate":
                        difficulty = 2;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Intermediate set - ELO: 1200");
                        difficultyText = "Difficulty Intermediate set - ELO: 1200";
                        break;
                    case "Expert":
                        difficulty = 5;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Expert set - ELO: 1600");
                        difficultyText = "Difficulty Expert set - ELO: 1600";
                        break;
                    case "Master":
                        difficulty = 9;
                        screenManager.setDifficulty(difficulty);
                        System.out.println("Difficulty Master set - ELO: 2000");
                        difficultyText = "Difficulty Master set - ELO: 2000";
                        break;
                }
            }
        });
        selectBox.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                tooltipLabel.setText(difficultyText);
                tooltipLabel.setVisible(true);
                positionTooltip(selectBox);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                tooltipLabel.setVisible(false);
            }
        });
        selectBox.getList().addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                int index = selectBox.getList().getSelectedIndex();
                if (index != -1) {
                    // Get the stage coordinates of the list
                    Vector2 listPosition = selectBox.getList().localToStageCoordinates(new Vector2(0, 0));

                    // Calculate the Y-coordinate of the selected item
                    float itemHeight = selectBox.getList().getItemHeight();
                    float selectedItemY = listPosition.y + selectBox.getList().getHeight() - (index + 1) * itemHeight;

                    // Set the tooltip position to the right of the selected item
                    tooltipLabel.setPosition(listPosition.x + selectBox.getList().getWidth() + 10, selectedItemY + itemHeight / 2);
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
        table.add(selectBox).padBottom(15);
        table.row();

        // Multiplayer Button
        TextButton multiplayerButton = createMenuButton("Multiplayer", "Play against another player", screenManager::playChess);
        table.add(multiplayerButton).fillX().padBottom(15);
        table.row();

        // Puzzle Button
        TextButton puzzleButton = createMenuButton("Puzzle", "Try solving chess puzzles", screenManager::playChess);
        table.add(puzzleButton).fillX().padBottom(15);
        table.row();

        //Free Mode Button
        TextButton freeModeButton = createMenuButton("Free Mode", "Play chess without any restrictions", screenManager::playFreeMode);
        table.add(freeModeButton).fillX().padBottom(15);
        table.row();

        // Exit Button
        TextButton exitButton = createMenuButton("Exit", "Close the application", Gdx.app::exit);
        table.add(exitButton).fillX().padBottom(15);
    }

    private TextButton createMenuButton(String text, String tooltipText, Runnable action) {
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
                action.run();
                tooltipLabel.setVisible(false);
            }

        });

        return button;
    }
    
    private void positionTooltip(Actor button) {
        float tooltipX = button.getX() + button.getWidth() + 10; // Position to the right of the button
        float tooltipY = button.getY() + button.getHeight() / 2; // Center vertically with the button

        // Check if tooltip goes out of screen bounds, adjust if necessary
        if (tooltipX + tooltipLabel.getWidth() > Gdx.graphics.getWidth()) {
            // Place tooltip above the button if it goes off the right edge
            tooltipX = button.getX();
            tooltipY = button.getY() + button.getHeight() + 10;
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
        System.out.println("MenuScreen disposed");
    }
}
