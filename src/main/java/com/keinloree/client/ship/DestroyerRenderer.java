package com.keinloree.client.ship;

import com.keinloree.client.vfx.ModRenderTypes;
import com.keinloree.client.vfx.VfxEffect;
import com.keinloree.registry.ModSounds;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.ShipTransform;
import com.keinloree.world.design.ShipDesign;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Renderer del destructor. La entidad no dibuja nada por si misma: la malla (ShipMesh) se dibuja en la etapa
 * AFTER_ENTITIES con la rotacion de ShipTransform (Axis.YP.rotationDegrees(180 - yaw)) y los brillos de las toberas,
 * la estela y el salto de hiperespacio en AFTER_PARTICLES. Tambien lleva el zumbido en bucle y la pose sentada del piloto.
 */
@Mod.EventBusSubscriber(modid = "keinloree", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DestroyerRenderer extends EntityRenderer<DestroyerEntity> {
    private static final Map<DestroyerEntity, HumSound> HUMS = new HashMap<>();
    private static final int HYPER_TICKS = 20;

    public DestroyerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DestroyerEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        // la nave se dibuja en onRenderLevel (necesita las matrices del nivel)
    }

    @Override
    public ResourceLocation getTextureLocation(DestroyerEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(DestroyerEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return false;
    }

    /** Para el zumbido y olvida las naves (salir del mundo). */
    static void clear() {
        for (HumSound h : HUMS.values()) {
            h.end();
        }
        HUMS.clear();
    }

    // ------------------------------------------------------------------ transformacion

    /** Transformacion de la nave: translacion relativa a la camara, giro de rumbo y (si cae) cabeceo y alabeo. */
    private static Matrix4f shipMatrix(DestroyerEntity ship, Matrix4f base, Vec3 cam, float pt) {
        Vec3 p = ship.getPosition(pt);
        float yaw = ship.getViewYRot(pt);
        Matrix4f m = new Matrix4f(base).translate((float) (p.x - cam.x), (float) (p.y - cam.y), (float) (p.z - cam.z))
                .rotateY((float) Math.toRadians(180.0F - yaw));
        if (ship.getState() == DestroyerEntity.DESTROYED) {
            int[] b = ShipTransform.solidBounds();
            Vec3 c = ShipTransform.local((b[0] + b[3]) / 2, (b[1] + b[4]) / 2, (b[2] + b[5]) / 2, 0.5D, 0.5D, 0.5D);
            m.translate((float) c.x, (float) c.y, (float) c.z)
                    .rotateX((float) Math.toRadians(-ship.getViewXRot(pt)))
                    .rotateZ((float) Math.toRadians(ship.getRoll()))
                    .translate((float) -c.x, (float) -c.y, (float) -c.z);
        }
        return m;
    }

    // ------------------------------------------------------------------ dibujo

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (ShipClient.ships().isEmpty()) {
            return;
        }
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            DestroyerEntity.renderPilotSitting = false;
            drawMeshes(event);
        } else if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            drawEffects(event);
        }
    }

    private static void drawMeshes(RenderLevelStageEvent event) {
        float pt = event.getPartialTick();
        Vec3 cam = event.getCamera().getPosition();
        // se construye en cuanto hay una nave (aunque este anclada) para que el despegue la encuentre lista
        boolean ready = ShipMesh.ready();
        for (DestroyerEntity ship : ShipClient.ships()) {
            int state = ship.getState();
            if (!DestroyerEntity.meshVisible(state) || ship.isRemoved()) {
                continue;
            }
            if (!ready) {
                return;
            }
            Vec3 p = ship.getPosition(pt);
            AABB box = ShipTransform.worldAABB(p, ship.getViewYRot(pt)).inflate(state == DestroyerEntity.DESTROYED ? 60.0D : 4.0D);
            if (!event.getFrustum().isVisible(box)) {
                continue;
            }
            Matrix4f mv = shipMatrix(ship, event.getPoseStack(), cam, pt);
            boolean offset = DestroyerEntity.blocksPresent(state) || state == DestroyerEntity.DESTROYED;
            ShipMesh.draw(mv, event.getProjectionMatrix(), offset);
        }
    }

    private static void drawEffects(RenderLevelStageEvent event) {
        float pt = event.getPartialTick();
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        float time = (mc.level != null ? mc.level.getGameTime() : 0) + pt;
        boolean any = false;
        for (DestroyerEntity ship : ShipClient.ships()) {
            if (ship.isRemoved()) {
                continue;
            }
            int state = ship.getState();
            Vec3 p = ship.getPosition(pt);
            AABB box = ShipTransform.worldAABB(p, ship.getViewYRot(pt)).inflate(80.0D);
            if (!event.getFrustum().isVisible(box)) {
                continue;
            }
            Matrix4f m = shipMatrix(ship, new Matrix4f(), cam, pt);
            double dx = ship.getX() - ship.xo;
            double dz = ship.getZ() - ship.zo;
            float speed = (float) Math.sqrt(dx * dx + dz * dz); // bloques por tick
            boolean flying = state == DestroyerEntity.FLYING || state == DestroyerEntity.LIFTOFF
                    || state == DestroyerEntity.ALIGN || state == DestroyerEntity.HYPERSPACE_IN
                    || state == DestroyerEntity.DESTROYED;
            float power = flying ? Mth.clamp(0.55F + speed * 1.2F, 0.55F, 1.6F) : 0.4F;
            // toberas: brillo emisivo siempre
            // OJO: en 1.21 pedir otro RenderType al BufferSource cierra el anterior; se pide justo antes de usarlo
            Vector3f t = new Vector3f();
            for (int[] e : ShipDesign.ENGINES) {
                Vec3 l = ShipTransform.local(e[0], e[1], ShipDesign.L, 0.5D, 0.5D, 1.0D);
                m.transformPosition((float) l.x, (float) l.y, (float) l.z, t);
                float pulse = 0.85F + 0.15F * Mth.sin(time * 0.7F + e[0]);
                float r = e[2] * (0.9F + 0.5F * power) * pulse;
                VertexConsumer glow = buffers.getBuffer(ModRenderTypes.glowAdditive(VfxEffect.GLOW));
                quad(glow, t.x, t.y, t.z, r, left, up, 0.25F, 1.0F, 0.55F, 1.1F * power);
                quad(glow, t.x, t.y, t.z, r * 0.45F, left, up, 0.8F, 1.0F, 0.9F, 1.4F * power);
                if (flying && state != DestroyerEntity.DESTROYED) {
                    float len = 10.0F + speed * 90.0F + e[2] * 3.0F;
                    Vector3f back = new Vector3f();
                    m.transformDirection(0.0F, 0.0F, 1.0F, back);
                    VertexConsumer beam = buffers.getBuffer(ModRenderTypes.beamAdditive());
                    streak(beam, t, back, len, r * 0.8F, 0.2F, 1.0F, 0.5F, 0.9F * power);
                    streak(beam, t, back, len * 0.6F, r * 0.35F, 0.8F, 1.0F, 0.9F, 1.2F * power);
                }
            }
            any = true;
            // salto de hiperespacio: destello y estelas en los primeros ticks
            if (state == DestroyerEntity.HYPERSPACE_IN && ship.tickCount < HYPER_TICKS + 20) {
                float f = 1.0F - (ship.tickCount + pt) / (HYPER_TICKS + 20.0F);
                if (f > 0.0F) {
                    hyperspace(buffers, m, left, up, f, ship.tickCount + pt);
                }
            }
        }
        if (any) {
            buffers.endBatch();
        }
    }

    /** Cuadrado de brillo mirando a la camara (posiciones relativas a la camara). */
    private static void quad(VertexConsumer vc, float x, float y, float z, float half, Vector3f left, Vector3f up,
                             float r, float g, float b, float k) {
        float cr = Mth.clamp(r * k, 0.0F, 1.0F);
        float cg = Mth.clamp(g * k, 0.0F, 1.0F);
        float cb = Mth.clamp(b * k, 0.0F, 1.0F);
        float ax = left.x * half;
        float ay = left.y * half;
        float az = left.z * half;
        float bx = up.x * half;
        float by = up.y * half;
        float bz = up.z * half;
        vc.addVertex(x + ax + bx, y + ay + by, z + az + bz).setUv(0.0F, 0.0F).setColor(cr, cg, cb, 1.0F);
        vc.addVertex(x - ax + bx, y - ay + by, z - az + bz).setUv(1.0F, 0.0F).setColor(cr, cg, cb, 1.0F);
        vc.addVertex(x - ax - bx, y - ay - by, z - az - bz).setUv(1.0F, 1.0F).setColor(cr, cg, cb, 1.0F);
        vc.addVertex(x + ax - bx, y + ay - by, z + az - bz).setUv(0.0F, 1.0F).setColor(cr, cg, cb, 1.0F);
    }

    /** Estela: banda que se apaga a lo largo de {@code dir} (unitaria), siempre de cara a la camara. */
    private static void streak(VertexConsumer vc, Vector3f from, Vector3f dir, float len, float width,
                               float r, float g, float b, float k) {
        // lado = dir x vista (la camara esta en el origen)
        Vector3f side = new Vector3f(dir).cross(from.x, from.y, from.z);
        if (side.lengthSquared() < 1.0E-6F) {
            return;
        }
        side.normalize(width);
        float ex = from.x + dir.x * len;
        float ey = from.y + dir.y * len;
        float ez = from.z + dir.z * len;
        float cr = Mth.clamp(r * k, 0.0F, 1.0F);
        float cg = Mth.clamp(g * k, 0.0F, 1.0F);
        float cb = Mth.clamp(b * k, 0.0F, 1.0F);
        vc.addVertex(from.x + side.x, from.y + side.y, from.z + side.z).setColor(cr, cg, cb, 1.0F);
        vc.addVertex(from.x - side.x, from.y - side.y, from.z - side.z).setColor(cr, cg, cb, 1.0F);
        vc.addVertex(ex, ey, ez).setColor(0.0F, 0.0F, 0.0F, 1.0F);
        vc.addVertex(ex, ey, ez).setColor(0.0F, 0.0F, 0.0F, 1.0F);
    }

    /** Destello grande y haces de luz hacia atras (la nave sale del salto). */
    private static void hyperspace(MultiBufferSource.BufferSource buffers, Matrix4f m, Vector3f left, Vector3f up,
                                   float f, float age) {
        VertexConsumer glow = buffers.getBuffer(ModRenderTypes.glowAdditive(VfxEffect.GLOW));
        Vector3f c = new Vector3f();
        Vec3 mid = ShipTransform.local(0, 20, 90, 0.5D, 0.5D, 0.5D);
        m.transformPosition((float) mid.x, (float) mid.y, (float) mid.z, c);
        float k = f * f;
        quad(glow, c.x, c.y, c.z, 70.0F * (1.2F - f * 0.4F), left, up, 0.5F, 0.9F, 1.0F, 1.4F * k);
        quad(glow, c.x, c.y, c.z, 28.0F, left, up, 1.0F, 1.0F, 1.0F, 1.8F * k);
        Vector3f back = new Vector3f();
        m.transformDirection(0.0F, 0.0F, 1.0F, back);
        VertexConsumer beam = buffers.getBuffer(ModRenderTypes.beamAdditive());
        RandomSource rnd = RandomSource.create(1234L);
        Vector3f origin = new Vector3f();
        for (int i = 0; i < 48; i++) {
            Vec3 l = ShipTransform.local(rnd.nextInt(60) - 30, rnd.nextInt(40), rnd.nextInt(180), 0.5D, 0.5D, 0.5D);
            m.transformPosition((float) l.x, (float) l.y, (float) l.z, origin);
            float len = 60.0F + rnd.nextFloat() * 140.0F * (0.4F + (1.0F - f));
            streak(beam, origin, back, len, 0.4F + rnd.nextFloat() * 0.8F, 0.6F, 0.9F, 1.0F, 1.6F * k);
        }
    }

    // ------------------------------------------------------------------ pose sentada del piloto

    @SubscribeEvent
    public static void onLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity().getVehicle() instanceof DestroyerEntity ship && ship.getPilotId() == event.getEntity().getId()) {
            DestroyerEntity.renderPilotSitting = true;
        }
    }

    @SubscribeEvent
    public static void onLivingPost(RenderLivingEvent.Post<?, ?> event) {
        DestroyerEntity.renderPilotSitting = false;
    }

    // ------------------------------------------------------------------ zumbido en bucle

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        Iterator<Map.Entry<DestroyerEntity, HumSound>> it = HUMS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DestroyerEntity, HumSound> e = it.next();
            if (e.getKey().isRemoved() || e.getValue().isStopped()) {
                e.getValue().end();
                it.remove();
            }
        }
        for (DestroyerEntity ship : ShipClient.ships()) {
            if (!ship.isRemoved() && !HUMS.containsKey(ship)) {
                HumSound h = new HumSound(ship);
                HUMS.put(ship, h);
                mc.getSoundManager().play(h);
            }
        }
    }

    /** Zumbido de la nave: bucle sin posicion cuyo volumen baja con la distancia a la caja de la nave. */
    private static final class HumSound extends AbstractTickableSoundInstance {
        private static final double RANGE = 190.0D;
        private final DestroyerEntity ship;

        HumSound(DestroyerEntity ship) {
            super(ModSounds.DESTROYER_HUM.get(), SoundSource.AMBIENT, RandomSource.create());
            this.ship = ship;
            this.looping = true;
            this.delay = 0;
            this.attenuation = Attenuation.NONE;
            this.volume = 0.001F;
            this.x = ship.getX();
            this.y = ship.getY();
            this.z = ship.getZ();
        }

        void end() {
            stop();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            Minecraft mc = Minecraft.getInstance();
            if (ship.isRemoved() || mc.player == null) {
                stop();
                return;
            }
            this.x = ship.getX();
            this.y = ship.getY();
            this.z = ship.getZ();
            AABB box = ShipTransform.worldAABB(ship.position(), ship.getYRot());
            Vec3 e = mc.player.position();
            double dx = Math.max(Math.max(box.minX - e.x, 0.0D), e.x - box.maxX);
            double dz = Math.max(Math.max(box.minZ - e.z, 0.0D), e.z - box.maxZ);
            double dy = Math.max(Math.max(box.minY - e.y, 0.0D), e.y - box.maxY);
            double d = Math.sqrt(dx * dx + dy * dy * 0.5D + dz * dz);
            float fall = (float) Mth.clamp(1.0D - d / RANGE, 0.0D, 1.0D);
            boolean moving = ship.getState() == DestroyerEntity.FLYING || ship.getState() == DestroyerEntity.ALIGN
                    || ship.getState() == DestroyerEntity.HYPERSPACE_IN || ship.getState() == DestroyerEntity.LIFTOFF;
            float base = moving ? 1.0F : 0.5F;
            this.volume = Math.max(0.001F, base * fall * fall);
            this.pitch = moving ? 1.0F : 0.85F;
        }
    }
}
