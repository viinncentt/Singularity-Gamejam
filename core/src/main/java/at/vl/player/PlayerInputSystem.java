package at.vl.player;

import com.artemis.Aspect;
import com.artemis.AspectSubscriptionManager;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;
import at.vl.util.JsonHelper;

public class PlayerInputSystem extends IteratingSystem {
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Facing> facingMapper;
    private ComponentMapper<Animator> animatorMapper;

    private AspectSubscriptionManager asm;
    private EntitySubscription enemySubscription;

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
    private final float suckingRadius;
    private int suckingTargetId = -1; // -1 means no current target

    private float shootingTimer;
    private final float shootingTime;
    private final float shootingSlowdown;
    private ShootingState currentShootingState = ShootingState.NONE;

    // Shooting abilities
    private final float dashStrength;

    public PlayerInputSystem() {
        super(Aspect.all(Player.class, RigidBody.class, Collider.class, Animator.class));

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
        suckingRadius = JsonHelper.getConfigValue().getFloat("SuckingRadius");

        shootingTime = JsonHelper.getConfigValue().getFloat("ShootingTime");
        shootingSlowdown = JsonHelper.getConfigValue().getFloat("ShootingSlowdown");

        dashStrength = JsonHelper.getConfigValue().getFloat("DashStrength");
    }

    @Override
    protected void initialize() {
        enemySubscription = asm.get(Aspect.all(Enemy.class, Collider.class));
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
            if (!(moveX >= speed)) {
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


        // Knockback overrides normal movement input while active
        if (rb.knockedBack) {
            rb.knockbackTimer -= world.getDelta();
            rb.velocity.x *= 0.9f;
            if (rb.knockbackTimer <= 0f) {
                rb.knockedBack = false;
            }
        } else {
            rb.velocity.x = moveX;
            rb.movedX = moveX != 0f;
        }

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

    private boolean requireSpaceRelease = false;

    private void abilityHandler() {
        boolean spaceHeld = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (requireSpaceRelease) {
            if (!spaceHeld) {
                requireSpaceRelease = false;
            } else {
                spaceHeld = false;
            }
        }

        if (filled) {
            // Shooting
            if (spaceHeld) {
                animator.currentState = State.SHOOTING;
                shootingTimer += world.getDelta();

                shooting();
                // To stop player from looking like flying up
                if (rb.velocity.y > 0f) {
                    rb.velocity.y = 0f;
                }

                if (shootingTimer >= shootingTime) {
                    // Shooting completed
                    filled = false;
                    // Reset timer
                    shootingTimer = 0f;
                    requireSpaceRelease = true;
                }
            } else {
                // Reset time if not holding space
                shootingTimer = 0f;
                rb.slowerGravity = false;
            }
        } else {
            rb.slowerGravity = false;
            // Sucking
            if (spaceHeld) {
                animator.currentState = State.SUCKING;
                if (isEnemyInSuckingRadius()) {
                    // Sucking enemy in
                    suckingTimer += world.getDelta();

                    if (suckingTimer >= suckingTime) {
                        world.delete(suckingTargetId);
                        // Reset target
                        suckingTargetId = -1;
                        // Suck completed
                        filled = true;
                        // Reset timer
                        suckingTimer = 0f;
                        requireSpaceRelease = true;
                    }
                } else {
                    // Reset time if enemy isn't in radius
                    suckingTimer = 0f;
                }
            } else {
                suckingTimer = 0f;
                if (suckingTargetId != -1) {
                    rigidBodyMapper.get(suckingTargetId).isBeingSucked = false;
                    suckingTargetId = -1;
                }
            }
        }

    }

    private boolean isEnemyInSuckingRadius() {
        float px = collider.rect.x;
        float py = collider.rect.y;
        float radiusSq = suckingRadius * suckingRadius;

        if (suckingTargetId != -1) {
            Collider targetCollider = colliderMapper.get(suckingTargetId);
            float dx = targetCollider.rect.x - px;
            float dy = targetCollider.rect.y - py;
            if (dx * dx + dy * dy <= radiusSq) {
                RigidBody targetRb = rigidBodyMapper.get(suckingTargetId);
                targetRb.velocity.x = 0f;
                targetRb.velocity.y = 0f;
                return true;
            }
            rigidBodyMapper.get(suckingTargetId).isBeingSucked = false;
            suckingTargetId = -1;
        }

        IntBag entities = enemySubscription.getEntities();
        int[] ids = entities.getData();
        for (int i = 0, s = entities.size(); i < s; i++) {
            int enemyId = ids[i];
            Collider enemyCollider = colliderMapper.get(enemyId);
            float dx = enemyCollider.rect.x - px;
            float dy = enemyCollider.rect.y - py;
            if (dx * dx + dy * dy <= radiusSq) {
                suckingTargetId = enemyId; // was missing
                RigidBody targetRb = rigidBodyMapper.get(enemyId);
                targetRb.velocity.x = 0f;
                targetRb.velocity.y = 0f;
                targetRb.isBeingSucked = true;
                return true;
            }
        }
        return false;
    }


    private void shooting() {
        world.setDelta(Gdx.graphics.getDeltaTime() * shootingSlowdown);
        rb.slowerGravity = true;
        rb.fasterGravity = false;

        rb.velocity.x = 0f;

        // None by default
        currentShootingState = ShootingState.NONE;

        // Up
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            currentShootingState = ShootingState.UP;
        }

        // Down
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            currentShootingState = ShootingState.DOWN;
        }

        // Right
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentShootingState = ShootingState.RIGHT;
        }

        // Left
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            currentShootingState = ShootingState.LEFT;
        }

        switch (currentShootingState) {
            case UP:
                rb.velocity.y = -dashStrength;
                filled = false;
                wasJumpPressed = true;
                shootingTimer = 0f;
                requireSpaceRelease = true;
                break;
            case DOWN:
                break;
            case RIGHT:
                break;
            case LEFT:
                break;
            default:
                break;
        }
    }
}
