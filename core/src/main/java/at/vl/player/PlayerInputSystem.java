package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class PlayerInputSystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Facing> facingMapper;
    private ComponentMapper<Animator> animatorMapper;

    private Collider collider;
    private RigidBody rb;
    private Facing facing;
    private Animator animator;

    private final float speed;
    private float currentSpeed;
    private final float startSpeed;
    private final float stopSpeed;

    private float moveX;

    // Jumping
    private boolean isJumping;
    private float baseJumpForce;
    private float maxJumpForce;

    private float jumpHoldTime = 0f;
    private final float maxJumpTime;

    // Stop player from holding jump
    private boolean wasJumpPressed = false;

    // Coyote time
    private float coyoteTimer = 0f;
    private final float coyoteTime;

    // Jump buffering
    private float jumpBufferTimer = 0f;
    private final float jumpBufferTime;

    // Landing
    private boolean wasGrounded;
    private float landStateTime = 0f;
    private final float landDuration;
    private final boolean landMovementLock;

    // Abilities
    private boolean filled = false;
    private float suckingTimer;
    private final float suckingTime;

    private float shootingTimer;
    private final float shootingTime;

    public PlayerInputSystem() {
        super(Aspect.all(Player.class, Player.class, RigidBody.class, Collider.class, Animator.class));

        speed = JsonHelper.getConfigValue().getFloat("PlayerSpeed");
        startSpeed = JsonHelper.getConfigValue().getFloat("StartSpeed");
        stopSpeed = JsonHelper.getConfigValue().getFloat("StopSpeed");

        baseJumpForce = JsonHelper.getConfigValue().getFloat("BaseJumpForce");
        maxJumpForce = JsonHelper.getConfigValue().getFloat("MaxJumpForce");
        maxJumpTime = JsonHelper.getConfigValue().getFloat("MaxJumpTime");

        landDuration = JsonHelper.getConfigValue().getFloat("LandDuration") / 100f;
        landMovementLock = JsonHelper.getConfigValue().getBoolean("LandMovementLock");

        coyoteTime = JsonHelper.getConfigValue().getFloat("CoyoteTime");

        jumpBufferTime = JsonHelper.getConfigValue().getFloat("JumpBufferTime");

        // Abilities
        suckingTime = JsonHelper.getConfigValue().getFloat("SuckingTime");
        shootingTime = JsonHelper.getConfigValue().getFloat("ShootingTime");
    }

    @Override
    protected void process(int entityId) {
        collider = colliderMapper.get(entityId);
        rb = rigidBodyMapper.get(entityId);
        facing = facingMapper.get(entityId);
        animator = animatorMapper.get(entityId);

        currentSpeed = speed;

        // For testing purposes
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            collider.rect.x = 1f;
            collider.rect.y = 1f;
        }

        // Walking
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (!(moveX >= speed))  {
                moveX += currentSpeed / startSpeed;
            } else {
                moveX = speed;
            }
            facing.lookingRight = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (!(moveX <= -speed)) {
                moveX -= currentSpeed / startSpeed;
            } else {
                moveX = -speed;
            }
            facing.lookingRight = false;
        }

        // If no movement key pressed reset movement
        if (!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D)) {
            if (moveX > 0) {
                moveX -= currentSpeed / stopSpeed;
                if (moveX < 0) moveX = 0;
            } else if (moveX < 0) {
                moveX += currentSpeed / stopSpeed;
                if (moveX > 0) moveX = 0;
            }
        }


        rb.velocity.x = moveX;
        rb.movedX = moveX != 0f;

        // Jumping
        jumpHandler();


        // Falling
        if (!rb.grounded && rb.velocity.y < 0f) {
            animator.currentState = State.FALLING;
            rb.fasterGravity = true;
        } else {
            rb.fasterGravity = false;
        }

        // Landing
        if (rb.grounded && !wasGrounded) {
            landStateTime = landDuration;
        }

        // Count down landing timer
        if (landStateTime > 0f) {
            landStateTime -= world.getDelta();
        }

        // Save grounded state for next frame
        wasGrounded = rb.grounded;

        // State Handler
        stateHandler();

        // Abilities
        abilityHandler();
    }

    private void stateHandler() {
        if (landStateTime > 0f) {
            animator.currentState = State.LANDING;
        } else if (isJumping || (!rb.grounded && rb.velocity.y > 0f)) {
            animator.currentState = State.JUMPING;
        } else if (!rb.grounded && rb.velocity.y < 0f) {
            animator.currentState = State.FALLING;
        } else if (moveX != 0f) {
            animator.currentState = State.WALKING;
        } else {
            animator.currentState = State.IDLE;
        }
    }

    private void jumpHandler() {
        if (rb.grounded) {
            coyoteTimer = coyoteTime;
        } else {
            coyoteTimer -= world.getDelta();
        }

        boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.W);

        if (jumpPressed && !wasJumpPressed) {
            jumpBufferTimer = jumpBufferTime;
        } else if (jumpBufferTimer > 0f) {
            jumpBufferTimer -= world.getDelta();
        }

        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            rb.velocity.y = baseJumpForce;
            rb.movedY = true;
            isJumping = true;
            jumpHoldTime = 0f;

            jumpBufferTimer = 0f;
            coyoteTimer = 0f;
        }

        wasJumpPressed = jumpPressed;

        if (isJumping) {
            jumpHoldTime += world.getDelta();

            boolean stillHolding = Gdx.input.isKeyPressed(Input.Keys.W);
            boolean withinMaxHold = jumpHoldTime < maxJumpTime;

            if (stillHolding && withinMaxHold && rb.velocity.y > 0f) {
                float t = jumpHoldTime / maxJumpTime;
                rb.velocity.y =
                    baseJumpForce + (maxJumpForce - baseJumpForce) * t;
            } else {
                isJumping = false;

                if (!stillHolding && rb.velocity.y > baseJumpForce) {
                    rb.velocity.y = baseJumpForce;
                }
            }
        }
    }


    private void abilityHandler() {
        if (filled) {
            // Shooting
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                animator.currentState = State.SHOOTING;
                shootingTimer += world.getDelta();

                if (shootingTimer >= shootingTime) {
                    // Shooting completed
                    filled = false;
                    // Reset timer
                    shootingTimer = 0f;
                }
            } else {
                // Reset time if not holding space
                shootingTimer = 0f;
            }
        } else {
            // Sucking
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                animator.currentState = State.SUCKING;
                suckingTimer += world.getDelta();

                if (suckingTimer >= suckingTime) {
                    // Suck completed
                    filled = true;
                    // Reset timer
                    suckingTimer = 0f;
                }
            } else {
                // Reset time if not holding space
                suckingTimer = 0f;
            }
        }
    }
}
