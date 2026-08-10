package at.vl.player;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import at.vl.ecs.components.Animator;
import at.vl.ecs.components.Collider;
import at.vl.ecs.components.Player;
import at.vl.util.JsonHelper;

public class PlayerRenderSystem extends IteratingSystem {

    private SpriteBatch batch;

    private ComponentMapper<Animator> animatorMapper;
    private ComponentMapper<Collider> colliderMapper;

    public PlayerRenderSystem(SpriteBatch batch) {
        super(Aspect.all(Player.class, Animator.class, Collider.class));
        this.batch = batch;
    }

    @Override
    protected void process(int entityId) {
         Animator animator = animatorMapper.get(entityId);
        if (animator.currentFrame == null) return;
        Collider collider = colliderMapper.get(entityId);

        batch.draw(animator.currentFrame, collider.rect.x - 0.15f, collider.rect.y, 1f, 1f);

        if (animator.effectsFrame != null) {
            float width = JsonHelper.getConfigValue().getFloat("PlayerSuckingParticlesWidth");
            float height = JsonHelper.getConfigValue().getFloat("PlayerSuckingParticlesHeight");
            float centerX = collider.rect.x + collider.rect.width / 2f;
            float centerY = collider.rect.y + collider.rect.height / 2f;

            batch.draw(animator.effectsFrame, centerX - width / 2f, centerY - height / 2f, width, height);
        }
        batch.flush();
    }

}
