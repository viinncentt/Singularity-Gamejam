package at.vl.collisionsystem;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.RigidBody;

public class CollisionSystem extends IteratingSystem {

    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;

    private EntitySubscription collidableSubscription;

    private final Rectangle intersection = new Rectangle();
    private final float velocityCap = -50f;

    public CollisionSystem() {
        super(Aspect.all(Collider.class, RigidBody.class));
    }

    @Override
    protected void initialize() {
        collidableSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(Collider.class));
    }

    @Override
    protected void process(int entityId) {
        RigidBody rb = rigidBodyMapper.get(entityId);

        // Velocity capping
        if (rb.velocity.y <= velocityCap) {
            rb.velocity.y = velocityCap;
        }

        moveX(entityId);
        resolveX(entityId);

        moveY(entityId);
        resolveY(entityId);
    }

    private void moveX(int entityId) {
        Collider collider = colliderMapper.get(entityId);
        RigidBody rb = rigidBodyMapper.get(entityId);

        if (rb.velocity.x != 0f) {
            collider.rect.x += rb.velocity.x * world.getDelta();
            rb.movedX = true;
        } else {
            rb.movedX = false;
        }
    }

    private void resolveX(int entityId) {
        RigidBody rb = rigidBodyMapper.get(entityId);
        if (!rb.movedX) return;

        Collider bodyCollider = colliderMapper.get(entityId);

        IntBag collidableIds = collidableSubscription.getEntities();
        for (int i = 0; i < collidableIds.size(); i++) {
            int otherId = collidableIds.get(i);
            if (otherId == entityId) continue;

            Collider otherCollider = colliderMapper.get(otherId);

            if (Intersector.intersectRectangles(bodyCollider.rect, otherCollider.rect, intersection)) {
                if (intersection.height < intersection.width) continue;

                boolean right = bodyCollider.rect.x + bodyCollider.rect.width / 2f
                    < otherCollider.rect.x + otherCollider.rect.width / 2f;

                if (right) {
                    bodyCollider.rect.x -= intersection.width;
                } else {
                    bodyCollider.rect.x += intersection.width;
                }
                rb.velocity.x = 0;
            }
        }
    }

    private void moveY(int entityId) {
        Collider collider = colliderMapper.get(entityId);
        RigidBody rb = rigidBodyMapper.get(entityId);

        if (rb.velocity.y != 0f) {
            collider.rect.y += rb.velocity.y * world.getDelta();
            rb.movedY = true;
        } else {
            rb.movedY = false;
        }
    }

    private void resolveY(int entityId) {
        RigidBody rb = rigidBodyMapper.get(entityId);
        Collider bodyCollider = colliderMapper.get(entityId);

        IntBag collidableIds = collidableSubscription.getEntities();

        // Ground check using a 1-pixel feet sensor
        Rectangle feet = new Rectangle(
            bodyCollider.rect.x + 1,
            bodyCollider.rect.y - 0.001f,
            bodyCollider.rect.width - 2,
            0.001f
        );

        // Assume player isn't grounded
        rb.grounded = false;
        for (int i = 0; i < collidableIds.size(); i++) {
            int otherId = collidableIds.get(i);
            if (otherId == entityId) continue;

            Collider otherCollider = colliderMapper.get(otherId);

            if (feet.overlaps(otherCollider.rect)) {
                rb.grounded = true;
                break;
            }
        }


        if (!rb.movedY) return;

        for (int i = 0; i < collidableIds.size(); i++) {
            int otherId = collidableIds.get(i);
            if (otherId == entityId) continue;

            Collider otherCollider = colliderMapper.get(otherId);

            if (Intersector.intersectRectangles(bodyCollider.rect, otherCollider.rect, intersection)) {
                boolean up = bodyCollider.rect.y + bodyCollider.rect.height / 2f
                    < otherCollider.rect.y + otherCollider.rect.height / 2f;

                if (up) {
                    bodyCollider.rect.y -= intersection.height;
                } else {
                    bodyCollider.rect.y += intersection.height;
                    rb.grounded = true;
                }
                rb.velocity.y = 0;
            }
        }
    }
}
