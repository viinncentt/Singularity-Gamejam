package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;

import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.RigidBody;

public class EnemyAISystem extends IteratingSystem {

    private ComponentMapper<Enemy> enemyMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Facing> facingMapper;
    private ComponentMapper<Player> playerMapper;

    private EntitySubscription playerSubscription;

    public EnemyAISystem() {
        super(Aspect.all(Enemy.class, RigidBody.class, Collider.class, Animator.class));
    }

    @Override
    protected void initialize() {
        playerSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {
        Enemy enemy = enemyMapper.get(entityId);
        RigidBody rb = rigidBodyMapper.get(entityId);

        if (rb.isBeingSucked) {
            return; // frozen while being sucked in — PlayerInputSystem owns its velocity right now
        }

        Collider collider = colliderMapper.get(entityId);
        Animator animator = animatorMapper.get(entityId);
        Facing facing = facingMapper.get(entityId);

        IntBag players = playerSubscription.getEntities();
        if (players.isEmpty()) return;

        int playerId = players.get(0);
        Player player = playerMapper.get(playerId);
        Collider playerCollider = colliderMapper.get(playerId);
        RigidBody playerRb = rigidBodyMapper.get(playerId);

        float dx = playerCollider.rect.x - collider.rect.x;
        float dy = playerCollider.rect.y - collider.rect.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= enemy.detectionRadius) {
            float direction = Math.signum(dx);
            if (direction != 0f) {
                enemy.lastDirection = direction;
            } else {
                direction = -enemy.lastDirection;
            }

            if (direction > 0) {
                facing.lookingRight = true;
            } else {
                facing.lookingRight = false;
            }

            if (distance <= enemy.attackRange) {
                animator.currentState = State.ATTACKING;
                if (!enemy.hasDealtDamage) {
                    player.currentHealth -= 1;
                    enemy.hasDealtDamage = true;
                    playerRb.velocity.x = direction * enemy.knockbackStrength;
                    playerRb.knockedBack = true;
                    playerRb.knockbackTimer = playerRb.knockbackDuration;
                }
            } else {
                animator.currentState = State.WALKING;
                rb.velocity.x = direction * enemy.maxSpeed;
                enemy.hasDealtDamage = false;
            }
        } else {
            rb.velocity.x = 0;
            animator.currentState = State.IDLE;
            enemy.hasDealtDamage = false;
        }
    }
}
