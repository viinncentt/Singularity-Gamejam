package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.PlayerInput;
import at.vl.ecs.components.RigidBody;

public class PlayerInputSystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Facing> facingMapper;

    private final float maxVelocity = 10f;
    private float moveX;

    public PlayerInputSystem() {
        super(Aspect.all(PlayerInput.class, RigidBody.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {
        RigidBody rb = rigidBodyMapper.get(entityId);
        Facing facing = facingMapper.get(entityId);

        moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX = 5f;
            facing.lookingRight = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX = -5f;
            facing.lookingRight = false;
        }

        rb.velocity.x = moveX;
        rb.movedX = moveX != 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W) && rb.grounded) {
            rb.velocity.y = 8f;
            rb.movedY = true;
        }
    }
}
