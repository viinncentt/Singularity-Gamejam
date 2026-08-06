package at.vl.collisionsystem;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.RigidBody;

public class CollisionListener extends BaseSystem {
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;

    // Anything with a Collider can be collided against — static geometry
    // (Collider only) and dynamic actors (Collider + Rigidbody) alike.
    private EntitySubscription collidableSubscription;

    // Only entities with BOTH Collider and Rigidbody get moved/resolved.
    private EntitySubscription dynamicBodySubscription;

    private final Rectangle intersection = new Rectangle();
    private final float velocityCap = -50f;

    @Override
    protected void processSystem() {
        IntBag dynamicBodyIds = dynamicBodySubscription.getEntities();

        // Reset moved flags
        for (int i = 0; i < dynamicBodyIds.size(); i++) {
            RigidBody rb = rigidBodyMapper.get(dynamicBodyIds.get(i));
            rb.movedX = false;
            rb.movedY = false;
        }

        // Velocity Capping
        for (int i = 0; i < dynamicBodyIds.size(); i++) {
            RigidBody rb = rigidBodyMapper.get(dynamicBodyIds.get(i));
            if (rb.velocity.y <= velocityCap) {
                rb.velocity.y = velocityCap;
            }
        }
    }

    @Override
    protected void initialize() {
        collidableSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(Collider.class));
        dynamicBodySubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(Collider.class, RigidBody.class));
    }

    public void refresh() {
        // No-op — subscriptions stay in sync automatically.
    }

    public void resolveX(Entity dynamicBody) {
        RigidBody rb = rigidBodyMapper.get(dynamicBody);
        if (!rb.movedX) return;

        Collider bodyCollider = colliderMapper.get(dynamicBody);
        int selfId = dynamicBody.getId();

        IntBag collidableIds = collidableSubscription.getEntities();
        for (int i = 0; i < collidableIds.size(); i++) {
            int otherId = collidableIds.get(i);
            if (otherId == selfId) continue; // don't collide against self

            Collider otherCollider = colliderMapper.get(otherId);

            if (Intersector.intersectRectangles(bodyCollider.rect, otherCollider.rect, intersection)) {
                // Skip: if the vertical overlap is shallower, this is really a Y-axis
                // (landing/ceiling) collision and should be left to resolveY.
                if (intersection.height < intersection.width) continue;

                boolean right = bodyCollider.rect.x + bodyCollider.rect.width / 2f
                    < otherCollider.rect.x + otherCollider.rect.width / 2f;
                resolveCollisionX(bodyCollider, rb, intersection, right);
            }
        }
    }

    public void resolveY(Entity dynamicBody) {
        RigidBody rb = rigidBodyMapper.get(dynamicBody);
        Collider bodyCollider = colliderMapper.get(dynamicBody);
        int selfId = dynamicBody.getId();

        IntBag collidableIds = collidableSubscription.getEntities();

        // Check if body is grounded first — if it overlaps with even one
        // collidable, it's grounded
        for (int i = 0; i < collidableIds.size(); i++) {
            int otherId = collidableIds.get(i);
            if (otherId == selfId) continue;

            Collider otherCollider = colliderMapper.get(otherId);

            if (bodyCollider.rect.overlaps(otherCollider.rect)) {
                boolean up = bodyCollider.rect.y + bodyCollider.rect.height / 2f
                    < otherCollider.rect.y + otherCollider.rect.height / 2f;
                if (!up) {
                    rb.grounded = true;
                }
                break;
            } else {
                rb.grounded = false;
            }
        }

        for (int i = 0; i < collidableIds.size(); i++) {
            // If body hasn't moved on Y axis, return
            if (!rb.movedY) return;

            int otherId = collidableIds.get(i);
            if (otherId == selfId) continue;

            Collider otherCollider = colliderMapper.get(otherId);

            // Resolve collision if there is any
            if (Intersector.intersectRectangles(bodyCollider.rect, otherCollider.rect, intersection)) {
                boolean up = bodyCollider.rect.y + bodyCollider.rect.height / 2f
                    < otherCollider.rect.y + otherCollider.rect.height / 2f;
                resolveCollisionY(bodyCollider, rb, intersection, up);
            }
        }
    }

    private void resolveCollisionX(Collider bodyCollider, RigidBody rb, Rectangle intersection, boolean right) {
        if (right) {
            bodyCollider.rect.x -= intersection.width;
        } else {
            bodyCollider.rect.x += intersection.width;
        }
        rb.velocity.x = 0;
    }

    private void resolveCollisionY(Collider bodyCollider, RigidBody rb, Rectangle intersection, boolean up) {
        if (up) {
            bodyCollider.rect.y -= intersection.height;
        } else {
            bodyCollider.rect.y += intersection.height;
            rb.grounded = true;
        }
        rb.velocity.y = 0;
    }
}
