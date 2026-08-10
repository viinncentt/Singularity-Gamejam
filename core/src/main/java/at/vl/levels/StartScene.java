package at.vl.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import at.vl.Main;
import at.vl.levels.Room;

public class StartScene implements Screen {

    private final Main main;
    private Music music;
    private Stage stage;
    private BitmapFont font;

    public StartScene(Main main) {
        this.main = main;
    }

    @Override
    public void show() {
        music = Gdx.audio.newMusic(Gdx.files.internal("music/intro.mp3"));
        music.setLooping(false);
        music.play();

        music.setOnCompletionListener(m -> {
            main.setScreen(new Room(main, 1));
            dispose();
        });


        stage = new Stage(new ScreenViewport());
        font = new BitmapFont();
        font.getData().setScale(3f);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label label = new Label("Words", style);
        label.setPosition(
            (stage.getViewport().getWorldWidth() - label.getWidth()) / 2f,
            (stage.getViewport().getWorldHeight() - label.getHeight()) / 2f
        );
        stage.addActor(label);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        if (music != null) music.dispose();
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
    }
}
