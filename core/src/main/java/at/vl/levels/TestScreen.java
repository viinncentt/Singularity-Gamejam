package at.vl.levels;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import at.vl.Main;
import at.vl.collisionsystem.CollisionSystem;
import at.vl.systems.GravitySystem;
import at.vl.player.PlayerHudSystem;
import at.vl.player.PlayerRenderSystem;
import at.vl.systems.EnemyAISystem;
import at.vl.systems.EnemyAnimationSystem;
import at.vl.systems.EnemyRenderSystem;
import at.vl.systems.SpawningSystem;
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

    private ColliderRenderer colliderRenderer;

    public TestScreen(Main main) {
        super(main);

        config = new WorldConfigurationBuilder().with(
            new PlayerInputSystem(), new PlayerAnimationSystem(), new PlayerRenderSystem(batch),
            new EnemyAISystem(), new EnemyAnimationSystem(), new EnemyRenderSystem(batch),
            new MovementSystem(), new SpawningSystem(),
            new CollisionSystem(), new GravitySystem(),  new DebugOverlay(), new PlayerHudSystem()
        ).build();

        world = new World(config);

        spawner = world.getSystem(SpawningSystem.class);
        spawner.spawnGround(0f, 0f, 15f, 1f);
        spawner.spawnPlayer(1f, 10f);
        spawner.spawnUndefinedMass(9f, 10f);
        //spawner.spawnUndefinedMass(8f, 10f);

        shapeRenderer = new ShapeRenderer();
        colliderRenderer = new ColliderRenderer(world, camera, shapeRenderer);
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

        // Draw
        batch.begin();

        world.process();
        batch.end();

        colliderRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height, true);

        world.getSystem(PlayerHudSystem.class).resize(width, height);
    }
    @Override
    public void dispose() {
        super.dispose();
        tex.dispose();
        shapeRenderer.dispose();
        world.dispose();
    }
}
