package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Player;
import at.vl.ecs.components.Projectile;
import at.vl.ecs.components.RigidBody;

public class ProjectileSystem extends IteratingSystem {
    private ComponentMapper<Projectile> projectileMapper;
    private ComponentMapper<Collider> colliderMapper;
    private ComponentMapper<Player> playerMapper;

    private EntitySubscription playerSubscription;

    public ProjectileSystem() {
        super(Aspect.all(Projectile.class, Collider.class, RigidBody.class));
    }

    @Override
    protected void initialize() {
        playerSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Collider.class));
    }

    @Override
    protected void process(int entityId) {
        Projectile projectile = projectileMapper.get(entityId);
        Collider collider = colliderMapper.get(entityId);

        // Out-of-range check — deletes once traveled distance exceeds maxDistance
        float dx = collider.rect.x - projectile.spawnX;
        float dy = collider.rect.y - projectile.spawnY;
        float traveled = (float) Math.sqrt(dx * dx + dy * dy);
        if (traveled >= projectile.maxDistance) {
            world.deleteEntity(world.getEntity(entityId));
            world.delete(entityId);
            return;
        }

        // Player collision check — deletes on hit
        IntBag players = playerSubscription.getEntities();
        if (players.isEmpty()) return;

        int playerId = players.get(0);
        Collider playerCollider = colliderMapper.get(playerId);

        if (collider.rect.overlaps(playerCollider.rect)) {
            Player player = playerMapper.get(playerId);
            player.currentHealth -= 1;
            world.deleteEntity(world.getEntity(entityId));
        }
    }
}
