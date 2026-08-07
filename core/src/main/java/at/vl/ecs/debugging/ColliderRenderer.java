package at.vl.ecs.debugging;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;

public class ColliderRenderer {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private ComponentMapper<Collider> colliderMapper;
    private EntitySubscription subscription;

    private World world;

    public ColliderRenderer(World world, OrthographicCamera camera, ShapeRenderer shapeRenderer) {
        this.world = world;
        this.camera = camera;
        this.shapeRenderer = shapeRenderer;

        colliderMapper = world.getMapper(Collider.class);
        subscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(Collider.class));
    }

    public void render() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        IntBag entityIds = subscription.getEntities();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (int i = 0; i < entityIds.size(); i++) {
            Collider collider = colliderMapper.get(entityIds.get(i));

            shapeRenderer.rect(
                collider.rect.x,
                collider.rect.y,
                collider.rect.width,
                collider.rect.height
            );
        }

        shapeRenderer.end();
    }
}
