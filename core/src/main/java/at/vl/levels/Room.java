package at.vl.levels;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;

import at.vl.Main;
import at.vl.collisionsystem.CollisionSystem;
import at.vl.ecs.debugging.ColliderRenderer;
import at.vl.ecs.debugging.DebugOverlay;
import at.vl.player.PlayerAnimationSystem;
import at.vl.player.PlayerHudSystem;
import at.vl.player.PlayerInputSystem;
import at.vl.player.PlayerRenderSystem;
import at.vl.systems.EnemyAISystem;
import at.vl.systems.EnemyAnimationSystem;
import at.vl.systems.EnemyRenderSystem;
import at.vl.systems.GravitySystem;
import at.vl.systems.MovementSystem;
import at.vl.systems.SpawningSystem;
import at.vl.util.JsonHelper;

public class Room extends GameScreen implements Screen  {
    // ECS
    private World world;
    private WorldConfiguration config;
    private SpawningSystem spawner;

    // Debugging
    private ShapeRenderer shapeRenderer;
    private ColliderRenderer colliderRenderer;

    // Tiled Map
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private float tileSize;

    public boolean nextRoom = false;

    public Room(Main main, int roomNumber) {
        super(main);

        config = new WorldConfigurationBuilder().with(
            new PlayerInputSystem(), new PlayerAnimationSystem(), new PlayerRenderSystem(batch),
            new EnemyAISystem(), new EnemyAnimationSystem(), new EnemyRenderSystem(batch),
            new MovementSystem(), new SpawningSystem(),
            new CollisionSystem(), new GravitySystem(),  new DebugOverlay(), new PlayerHudSystem()
        ).build();

        world = new World(config);

        spawner = world.getSystem(SpawningSystem.class);

        // Tiled Map
        JsonValue room = JsonHelper.getRoomValue().get("Room" + roomNumber);
        tileSize = main.getTileSize();
        map = new TmxMapLoader().load("rooms/" + room.getString("TiledMap"));
        renderer = new OrthogonalTiledMapRenderer(map, 1f / tileSize);

        // Hitboxes
        MapLayer hitboxLayer = map.getLayers().get("hitbox");

        for (MapObject object : hitboxLayer.getObjects()) {
            RectangleMapObject rmp = (RectangleMapObject) object;
            Rectangle rectangle = rmp.getRectangle();
            spawner.spawnGround(rectangle.x / tileSize, rectangle.y / tileSize,
                rectangle.width / tileSize, rectangle.height / tileSize);
        }

        // Spawn Player
        spawner.spawnPlayer(room.getFloat("PlayerSpawnX"), room.getFloat("PlayerSpawnY"));

        // Spawn Enemies
        JsonValue enemies = room.get("Enemies");
        if (enemies != null) {
            for (JsonValue enemy = enemies.child; enemy != null; enemy = enemy.next) {
                if (enemy.name.equals("UndefinedMass")) {
                    spawner.spawnUndefinedMass(enemy.getFloat("X"), enemy.getFloat("Y"));
                }
            }
        }
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

        // Render tiled map
        renderer.setView(camera);
        renderer.render();

        // Draw
        batch.begin();

        world.process();
        batch.end();

        // If player activates next room condition
        nextRoom = true;

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
        shapeRenderer.dispose();
        world.dispose();
    }
}
