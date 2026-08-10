package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntityEdit;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import at.vl.ecs.State;
import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;
import at.vl.ecs.components.Facing;
import at.vl.ecs.components.IgnoreGravity;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.Projectile;
import at.vl.ecs.components.RigidBody;

public class EnemyAISystem extends IteratingSystem {
    private ComponentMapper<Enemy> enemyMapper;
    private ComponentMapper<RigidBody> rigidBodyMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Facing> facingMapper;
    private ComponentMapper<Player> playerMapper;

    private EntitySubscription playerSubscription;
    private EntitySubscription projectileSubscription;

    public EnemyAISystem() {
        super(Aspect.all(Enemy.class, RigidBody.class, Collider.class, Animator.class));
    }

    @Override
    protected void initialize() {
        playerSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Collider.class));
        projectileSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Projectile.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {
        Enemy enemy = enemyMapper.get(entityId);
        RigidBody rb = rigidBodyMapper.get(entityId);

        if (rb.isBeingSucked) {
            return;
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

            float directionY = Math.signum(dy);
            if (directionY != 0f) {
                enemy.lastDirectionY = directionY;
            } else {
                directionY = -enemy.lastDirectionY;
            }

            if (distance <= enemy.shootRange) {
                enemy.shootTimer -= world.getDelta();
                if (enemy.shootTimer <= 0f) {
                    if (projectileSubscription.getEntities().size() < enemy.maxProjectiles) {
                        spawnProjectile(collider, dx, dy, enemy);
                        enemy.shootTimer = enemy.shootCooldown;
                    }
                    // if at the cap, timer stays <= 0 and we retry every frame until a slot frees up
                }
            }

            if (distance <= enemy.attackRange) {
                if (player.dying) return;
                animator.currentState = State.ATTACKING;
                rb.velocity.y = 0;
                if (!enemy.hasDealtDamage) {
                    player.currentHealth -= 1;
                    player.hurting = true;
                    enemy.hasDealtDamage = true;
                    playerRb.velocity.x = direction * enemy.knockbackStrength;
                    playerRb.knockedBack = true;
                    playerRb.knockbackTimer = playerRb.knockbackDuration;
                }
            } else {
                animator.currentState = State.WALKING;
                rb.velocity.x = direction * enemy.maxSpeed;
                rb.velocity.y = directionY * enemy.maxSpeed;
                enemy.hasDealtDamage = false;
            }
        } else {
            rb.velocity.x = 0;
            rb.velocity.y = 0;
            animator.currentState = State.IDLE;
            enemy.hasDealtDamage = false;
        }
    }

    private void spawnProjectile(Collider enemyCollider, float dx, float dy, Enemy enemy) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float vx = length != 0f ? (dx / length) * enemy.projectileSpeed : enemy.projectileSpeed;
        float vy = length != 0f ? (dy / length) * enemy.projectileSpeed : 0f;

        float spawnX = enemyCollider.rect.x + enemyCollider.rect.width / 2f;
        float spawnY = enemyCollider.rect.y + enemyCollider.rect.height / 2f;

        int projectileId = world.create();
        EntityEdit edit = world.edit(projectileId);


        edit.create(IgnoreGravity.class);

        Projectile projectile = edit.create(Projectile.class);
        projectile.spawnX = spawnX;
        projectile.spawnY = spawnY;
        projectile.maxDistance = enemy.projectileMaxDistance;
        projectile.damage = 1;

        Animator animator = edit.create(Animator.class);

        RigidBody rb = edit.create(RigidBody.class);
        rb.velocity.x = vx;
        rb.velocity.y = vy;

        Collider collider = edit.create(Collider.class);
        collider.rect.set(spawnX, spawnY, enemy.projectileWidth, enemy.projectileHeight);
    }
}
