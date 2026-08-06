package at.vl.levels;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import at.vl.Main;
import at.vl.collisionsystem.CollisionSystem;
import at.vl.collisionsystem.GravitySystem;
import at.vl.ecs.SpawningSystem;
import at.vl.ecs.debugging.ColliderRenderer;
import at.vl.ecs.debugging.DebugOverlay;
import at.vl.systems.MovementSystem;
import at.vl.player.PlayerAnimationSystem;
import at.vl.player.PlayerInputSystem;

public class TestScreen extends GameScreen {
    private Texture tex;

    // ECS
    private World world;
    private WorldConfiguration config;
    private SpawningSystem spawner;

    // Debugging
    private ShapeRenderer shapeRenderer;

    private PlayerAnimationSystem playerAnimationSystem;

    public TestScreen(Main main) {
        super(main);
        shapeRenderer = new ShapeRenderer();

        playerAnimationSystem = new PlayerAnimationSystem();
        config = new WorldConfigurationBuilder().with(new PlayerInputSystem(), new MovementSystem(), new SpawningSystem(),
            new CollisionSystem(), new GravitySystem(), playerAnimationSystem, new DebugOverlay(),
            new ColliderRenderer(camera, shapeRenderer)).build();

        world = new World(config);

        spawner = world.getSystem(SpawningSystem.class);
        spawner.spawnGround(0f, 0f, 15f, 1f);
        spawner.spawnPlayer(1f, 10f);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        fitViewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0, 0.31f, 0.31f, 1);

        // Artemis ODB
        world.setDelta(delta);
        world.process();

        // Draw
        batch.begin();
        playerAnimationSystem.render(batch);
        batch.end();

    }


    @Override
    public void dispose() {
        super.dispose();
        tex.dispose();
        shapeRenderer.dispose();
        world.dispose();
    }
}
