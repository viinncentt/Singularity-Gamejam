package at.vl.ecs.debugging;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;


public class DebugOverlay extends IteratingSystem {
    public Stage stage;
    private ScreenViewport screenViewport;

    private Table table;

    // Artemis ODB
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Animator> animatorMapper;

    private RigidBody rb;
    private Collider collider;
    private Animator animator;

    // Things to show
    private Label fps;
    private Label playerX;
    private Label playerY;
    private Label playerVelocityX;
    private Label playerVelocityY;
    private Label playerIsGrounded;
    private Label playerState;

    public DebugOverlay() {
        super(Aspect.all(Player.class, RigidBody.class, Collider.class, Animator.class));

        screenViewport = new ScreenViewport();
        screenViewport.setUnitsPerPixel(1f);
        stage = new Stage(screenViewport);

        table = new Table();
        table.top();
        table.pad(5f);
        table.left();
        table.setFillParent(true);

        Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        // Labels
        fps = new Label("", style);
        playerX = new Label("", style);
        playerY = new Label("", style);
        playerVelocityX = new Label("", style);
        playerVelocityY = new Label("", style);
        playerIsGrounded = new Label("", style);
        playerState = new Label("", style);

        table.row().width(150f);
        table.add(fps);
        table.row().width(150f);
        table.add(playerX);
        table.row().width(150f);
        table.add(playerY);
        table.row().width(150f);
        table.add(playerVelocityX);
        table.row().width(150f);
        table.add(playerVelocityY);
        table.row().width(150f);
        table.add(playerIsGrounded);
        table.row().width(150f);
        table.add(playerState);
        table.row().width(150f);

        stage.addActor(table);
    }


    @Override
    protected void process(int entityId) {
        collider = colliderMapper.get(entityId);
        rb = rigidBodyMapper.get(entityId);
        animator = animatorMapper.get(entityId);

        stage.act(world.getDelta());

        fps.setText("fps: " + Gdx.graphics.getFramesPerSecond());
        playerX.setText("x: " + String.format("%.2f", collider.rect.x));
        playerY.setText("y: " + String.format("%.2f", collider.rect.y));
        playerVelocityX.setText("velocity x: " + String.format("%.2f", rb.velocity.x));
        playerVelocityY.setText("velocity y: " + String.format("%.2f",  rb.velocity.y));
        playerIsGrounded.setText("isGrounded: " + rb.grounded);
        playerState.setText("state:" + animator.currentState);
        stage.draw();
    }
}
