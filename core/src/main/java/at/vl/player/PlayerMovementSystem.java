package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;

public class PlayerMovementSystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;

    public PlayerMovementSystem() {
        super(Aspect.all(Player.class, RigidBody.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {

        // Get rigid body and collider
        RigidBody rb = rigidBodyMapper.get(entityId);
        Collider collider = colliderMapper.get(entityId);

        // Translate velocity into position
        collider.rect.x += rb.velocity.x * world.getDelta();
        collider.rect.y += rb.velocity.y * world.getDelta();
    }
}
