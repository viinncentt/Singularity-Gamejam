package at.vl.collisionsystem;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.IgnoreGravity;
import at.vl.ecs.components.RigidBody;

public class GravitySystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<IgnoreGravity> ignoreGravityMapper;

    private final float gravityStrength = 20f;
    private final float maxVelocity = 20f;

    public GravitySystem() {
        super(Aspect.all(RigidBody.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {
        // Apply gravity velocity to all entities with a Rigid body that don't have the ignore gravity component
        RigidBody rb = rigidBodyMapper.get(entityId);
        Collider collider = colliderMapper.get(entityId);
        IgnoreGravity ignoreGravity = ignoreGravityMapper.get(entityId);

        if (rb.grounded) {
           return;
        }

        if (rb.velocity.y <= - maxVelocity) {
            return;
        }

        rb.velocity.y -= gravityStrength * world.getDelta();
    }
}
