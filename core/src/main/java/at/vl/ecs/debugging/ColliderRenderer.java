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

public class ColliderRenderer extends BaseSystem {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private ComponentMapper<Collider> colliderMapper;
    private EntitySubscription subscription;

    public ColliderRenderer(OrthographicCamera camera, ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
    }

    @Override
    protected void initialize() {
        subscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(Collider.class));
    }

    @Override
    protected void processSystem() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        IntBag entityIds = subscription.getEntities();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (int i = 0; i < entityIds.size(); i++) {
            Collider collider = colliderMapper.get(entityIds.get(i));
            shapeRenderer.rect(
                collider.rect.x, collider.rect.y,
                collider.rect.width, collider.rect.height
            );
        }

        shapeRenderer.end();
    }
}
