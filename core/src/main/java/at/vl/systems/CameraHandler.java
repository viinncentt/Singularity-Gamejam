package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;

public class CameraHandler extends IteratingSystem {
    private ComponentMapper<Player> playerMapper;
    private ComponentMapper<Collider> colliderMapper;

    private Collider collider;
    private Player player;

    private OrthographicCamera camera;

    public CameraHandler(OrthographicCamera camera) {
        super(Aspect.all(Player.class, Collider.class));

        this.camera = camera;
    }
    @Override
    protected void process(int entityId) {
        Player player = playerMapper.get(entityId);
        Collider collider = colliderMapper.get(entityId);
        float centerX = collider.rect.x + collider.rect.width / 2f;
        float centerY = collider.rect.y + collider.rect.height / 2f;

        float lerpFactor = MathUtils.clamp(5f * world.getDelta(), 0f, 1f);
        camera.position.x = MathUtils.lerp(camera.position.x, centerX, lerpFactor);
        camera.position.y = MathUtils.lerp(camera.position.y, centerY, lerpFactor);

    }

    private float x;
    private float y;

    private boolean isInitialized;

    public void lock() {
        if (!isInitialized) {
            x = camera.position.x;
            y = camera.position.y;

            isInitialized = true;
        }

        // Set position
        camera.position.x = x;
        camera.position.y = y;
    }
}
