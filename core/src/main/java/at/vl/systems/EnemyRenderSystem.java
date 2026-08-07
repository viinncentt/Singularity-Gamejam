package at.vl.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Enemy;

public class EnemyRenderSystem extends IteratingSystem {

    private SpriteBatch batch;

    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Collider> colliderMapper;

    public EnemyRenderSystem(SpriteBatch batch) {
        super(Aspect.all(Enemy.class, Animator.class, Collider.class));
        this.batch = batch;
    }

    @Override
    protected void process(int entityId) {
        Animator animator = animatorMapper.get(entityId);
        //if(animator.currentFrame == null) return;
        Collider collider = colliderMapper.get(entityId);

        batch.draw(animator.currentFrame, collider.rect.x, collider.rect.y, 1f, 1f);
        batch.flush();

    }
}
