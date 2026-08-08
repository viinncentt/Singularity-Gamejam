package at.vl.collisionsystem;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.IgnoreGravity;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class GravitySystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<IgnoreGravity> ignoreGravityMapper;

    private final float gravityStrength;
    private final float maxVelocity;

    public GravitySystem() {
        super(Aspect.all(RigidBody.class, Collider.class));

        gravityStrength = JsonHelper.getConfigValue().getFloat("GravityStrength");
        maxVelocity = gravityStrength;
    }

    @Override
    protected void process(int entityId) {
        RigidBody rb = rigidBodyMapper.get(entityId);
        Collider collider = colliderMapper.get(entityId);
        IgnoreGravity ignoreGravity = ignoreGravityMapper.get(entityId);

        if (rb.grounded) {
            return;
        }

        if (rb.velocity.y <= - maxVelocity) {
            return;
        }

        if (rb.fasterGravity) {
            rb.velocity.y -= (gravityStrength * 1.5f) * world.getDelta();
            return;
        }

        if (rb.slowerGravity) {
            rb.velocity.y -= (gravityStrength * 0.25f) * world.getDelta();
            return;
        }

        rb.velocity.y -= gravityStrength * world.getDelta();
    }
}
