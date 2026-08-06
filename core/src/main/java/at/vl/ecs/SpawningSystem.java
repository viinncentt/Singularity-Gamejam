package at.vl.ecs;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.PlayerInput;
import at.vl.ecs.components.RigidBody;

public class SpawningSystem extends BaseSystem {
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<RigidBody> rigidbodyMapper;

    public Entity spawnPlayer(float x, float y) {
        Entity entity = world.createEntity();
        EntityEdit edit = entity.edit();

        Collider collider = colliderMapper.create(entity);
        collider.rect.set(x, y, 1f, 1f);

        edit.create(RigidBody.class);

        // Input
        edit.create(PlayerInput.class);

        // Animator
        Animator animator = edit.create(Animator.class);
        animator.currentState = State.IDLE;

        // Facing
        Facing facing = edit.create(Facing.class);
        facing.lookingRight = true;

        return entity;
    }

    public Entity spawnEnemy(float x, float y, float width, float height) {
        Entity entity = world.createEntity();

        Collider collider = colliderMapper.create(entity);
        collider.rect.set(x, y, width, height);

        RigidBody rigidbody = rigidbodyMapper.create(entity);
        rigidbody.velocity.set(0f, 0f);

        return entity;
    }

    public Entity spawnGround(float x, float y, float width, float height) {
        Entity entity = world.createEntity();

        Collider collider = colliderMapper.create(entity);
        collider.rect.set(x, y, width, height);

        return entity;
    }

    @Override
    protected void processSystem() {

    }
}
