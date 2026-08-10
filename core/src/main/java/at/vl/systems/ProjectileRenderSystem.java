package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Projectile;
import at.vl.ecs.components.RigidBody;

public class ProjectileRenderSystem extends IteratingSystem {
    private SpriteBatch batch;

    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Collider> colliderMapper;

    public ProjectileRenderSystem(SpriteBatch batch) {
        super(Aspect.all(Projectile.class, Collider.class, RigidBody.class));
        this.batch = batch;
    }

    @Override
    protected void process(int entityId) {
        Animator animator = animatorMapper.get(entityId);
        if (animator.currentFrame == null) return;

        Collider collider = colliderMapper.get(entityId);


        batch.draw(animator.currentFrame, collider.rect.x - 0.125f, collider.rect.y - 0.125f, 0.5f, 0.5f);

    }
}
