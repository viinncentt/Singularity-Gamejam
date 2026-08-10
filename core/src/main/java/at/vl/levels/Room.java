package at.vl.levels;

import com.artemis.Aspect;
import com.artemis.AspectSubscriptionManager;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;

import at.vl.Main;
import at.vl.collisionsystem.CollisionSystem;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Player;
import at.vl.ecs.debugging.ColliderRenderer;
import at.vl.ecs.debugging.DebugOverlay;
import at.vl.player.PlayerAnimationSystem;
import at.vl.player.PlayerHudSystem;
import at.vl.player.PlayerInputSystem;
import at.vl.player.PlayerRenderSystem;
import at.vl.systems.CameraHandler;
import at.vl.systems.EnemyAISystem;
import at.vl.systems.EnemyAnimationSystem;
import at.vl.systems.EnemyRenderSystem;
import at.vl.systems.GravitySystem;
import at.vl.systems.MovementSystem;
import at.vl.systems.ProjectileAnimationSystem;
import at.vl.systems.ProjectileRenderSystem;
import at.vl.systems.ProjectileSystem;
import at.vl.systems.SpawningSystem;
import at.vl.systems.UndefinedMassType;
import at.vl.util.JsonHelper;

public class Room extends GameScreen implements Screen  {
    // ECS
    private World world;
    private WorldConfiguration config;
    private SpawningSystem spawner;
    private EntitySubscription playerSubscription;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Player> playerMapper;

    // Debugging
    private ShapeRenderer shapeRenderer;
    private ColliderRenderer colliderRenderer;

    private JsonValue room;

    // Tiled Map
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private final float tileSize;

    private boolean nextRoom;
    private final int roomNumber;

    // Fade in
    private float fadeTimer;
    private final float fadeDuration;
    private boolean fading = true;

    public Room(Main main, int roomNumber) {
        super(main);

        config = new WorldConfigurationBuilder().with(
            new ProjectileSystem(), new ProjectileAnimationSystem(), new ProjectileRenderSystem(batch),
            new PlayerInputSystem(), new PlayerAnimationSystem(), new PlayerRenderSystem(batch),
            new EnemyAISystem(), new EnemyAnimationSystem(), new EnemyRenderSystem(batch),
            new MovementSystem(), new SpawningSystem(),

            new CollisionSystem(), new GravitySystem(),  new DebugOverlay(),
            new CameraHandler(camera), new PlayerHudSystem()
        ).build();

        world = new World(config);

        spawner = world.getSystem(SpawningSystem.class);

        // Tiled Map
        room = JsonHelper.getRoomValue().get("Room" + roomNumber);
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
                if (enemy.getString("EnemyType").equals("UndefinedMass")) {
                    UndefinedMassType type = UndefinedMassType.valueOf(enemy.getString("Type"));
                    spawner.spawnUndefinedMass(enemy.getFloat("X"), enemy.getFloat("Y"), type);
                }
            }
        }

        playerSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Collider.class));
        colliderMapper = world.getMapper(Collider.class);
        playerMapper = world.getMapper(Player.class);

        // Debugging
        shapeRenderer = new ShapeRenderer();
        colliderRenderer = new ColliderRenderer(world, camera, shapeRenderer);

        this.roomNumber = roomNumber;
        nextRoom = false;
        fadeDuration = JsonHelper.getConfigValue().getFloat("FadeDuration");
        fadeTimer = fadeDuration;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        fitViewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0, 0f, 0f, 1);

        // Artemis ODB
        world.setDelta(delta);

        // Render tiled map
        renderer.setView(camera);
        renderer.render();

        // Draw
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        world.process();
        batch.end();


        // If player activates next room condition
        IntBag players = playerSubscription.getEntities();
        if (!players.isEmpty()) {
            int playerId = players.get(0);
            Collider playerCollider = colliderMapper.get(playerId);

            if (playerCollider.rect.overlaps(
                new Rectangle(room.getFloat("EndPointX"), room.getFloat("EndPointY"), 1f, 10f))) {
                nextRoom = true;
            }
        }

        // Falls off
        if (!players.isEmpty()) {
            int playerId = players.get(0);
            Collider playerCollider = colliderMapper.get(playerId);
            Player player = playerMapper.get(playerId);

            if (playerCollider.rect.overlaps(
                new Rectangle(0, 0, 100f, 1f))) {
                playerCollider.rect.x = room.getFloat("PlayerSpawnX");
                playerCollider.rect.y = room.getFloat("PlayerSpawnY");
                player.currentHealth -= 1;

                // Respawn Enemies
                AspectSubscriptionManager asm = world.getAspectSubscriptionManager();
                EntitySubscription subscription = asm.get(Aspect.all(Enemy.class));
                IntBag entities = subscription.getEntities();

                int[] ids = entities.getData();
                for (int i = 0, s = entities.size(); i < s; i++) {
                    world.delete(ids[i]);
                }

                JsonValue enemies = room.get("Enemies");
                if (enemies != null) {
                    for (JsonValue enemy = enemies.child; enemy != null; enemy = enemy.next) {
                        if (enemy.getString("EnemyType").equals("UndefinedMass")) {
                            UndefinedMassType type = UndefinedMassType.valueOf(enemy.getString("Type"));
                            spawner.spawnUndefinedMass(enemy.getFloat("X"), enemy.getFloat("Y"), type);
                        }
                    }
                }
            }

        }

        // Dies
        if (!players.isEmpty()) {
            int playerId = players.get(0);
            Collider playerCollider = colliderMapper.get(playerId);
            Player player = playerMapper.get(playerId);

            if (player.readyToRespawn) {
                playerCollider.rect.x = room.getFloat("PlayerSpawnX");
                playerCollider.rect.y = room.getFloat("PlayerSpawnY");
                player.currentHealth = player.maxHealth;

                // Respawn Enemies
                AspectSubscriptionManager asm = world.getAspectSubscriptionManager();
                EntitySubscription subscription = asm.get(Aspect.all(Enemy.class));
                IntBag entities = subscription.getEntities();

                int[] ids = entities.getData();
                for (int i = 0, s = entities.size(); i < s; i++) {
                    world.delete(ids[i]);
                }

                JsonValue enemies = room.get("Enemies");
                if (enemies != null) {
                    for (JsonValue enemy = enemies.child; enemy != null; enemy = enemy.next) {
                        if (enemy.getString("EnemyType").equals("UndefinedMass")) {
                            UndefinedMassType type = UndefinedMassType.valueOf(enemy.getString("Type"));
                            spawner.spawnUndefinedMass(enemy.getFloat("X"), enemy.getFloat("Y"), type);
                        }
                    }
                }
            }
        }

        // Camera reaches end
        Rectangle cameraRect = new Rectangle(camera.position.x, camera.position.y, camera.viewportWidth, camera.viewportHeight);
        if (cameraRect.overlaps(new Rectangle(room.getFloat("CameraEnd"), 6, 1, 100))) {
            world.getSystem(CameraHandler.class).lock();
        }

        if (nextRoom) {
            main.setScreen(new Room(main, roomNumber + 1));
        }

        if (fading) {
            fadeTimer -= delta;
            float alpha = Math.max(0f, fadeTimer / fadeDuration);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, alpha);
            // Cover the full camera view — using viewport width/height in world units
            shapeRenderer.rect(
                camera.position.x - camera.viewportWidth / 2f,
                camera.position.y - camera.viewportHeight / 2f,
                camera.viewportWidth,
                camera.viewportHeight
            );
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            if (fadeTimer <= 0f) {
                fading = false;
            }
        }

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
