package com.keinloree.client;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.CargoShip;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Nave de carga de Kein Loree: casco en cuña con torre de mando, cabina, dos góndolas, contenedor bajo el vientre,
 * rampa trasera y tren de aterrizaje plegable. El vuelo (cabeceo, alabeo, tren, rampa, empuje) se anima a partir
 * del estado sincronizado de la entidad.
 */
public class CargoShipModel<T extends CargoShip> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "cargo_ship"), "main");

    private static final float DEG = (float) (Math.PI / 180.0D);
    /** Angulo de la rampa abierta: apoya su borde en el suelo. */
    private static final float RAMP_OPEN = 2.618F;

    private final ModelPart root;
    private final ModelPart ship;
    private final ModelPart pod;
    private final ModelPart ramp;
    private final ModelPart gearF;
    private final ModelPart gearL;
    private final ModelPart gearR;
    private final ModelPart flameL;
    private final ModelPart flameR;
    private final ModelPart liftL;
    private final ModelPart liftR;
    private final ModelPart liftF;

    public CargoShipModel(ModelPart root) {
        this.root = root;
        this.ship = root.getChild("ship");
        this.pod = this.ship.getChild("pod");
        this.ramp = this.ship.getChild("ramp");
        this.gearF = this.ship.getChild("gear_f");
        this.gearL = this.ship.getChild("gear_l");
        this.gearR = this.ship.getChild("gear_r");
        this.flameL = this.ship.getChild("flame_l");
        this.flameR = this.ship.getChild("flame_r");
        this.liftL = this.ship.getChild("lift_l");
        this.liftR = this.ship.getChild("lift_r");
        this.liftF = this.ship.getChild("lift_f");
    }

    /** Geometria generada por el script de modelos: no editar a mano. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition v_ship = root.addOrReplaceChild("ship", CubeListBuilder.create()
                , PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition v_hull = v_ship.addOrReplaceChild("hull", CubeListBuilder.create()
                .texOffs(288, 101).addBox(-15.0F, -6.0F, 36.0F, 30.0F, 9.0F, 4.0F)
                .texOffs(380, 134).addBox(-16.0F, 3.0F, 36.0F, 32.0F, 3.0F, 4.0F)
                .texOffs(356, 101).addBox(-15.0F, -6.0F, 32.0F, 30.0F, 9.0F, 4.0F)
                .texOffs(0, 141).addBox(-16.0F, 3.0F, 32.0F, 32.0F, 3.0F, 4.0F)
                .texOffs(258, 79).addBox(-16.0F, -6.0F, 28.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(264, 125).addBox(-17.0F, 3.0F, 28.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(330, 79).addBox(-16.0F, -6.0F, 24.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(340, 125).addBox(-17.0F, 3.0F, 24.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(402, 79).addBox(-16.0F, -6.0F, 20.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(416, 125).addBox(-17.0F, 3.0F, 20.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(0, 101).addBox(-16.0F, -6.0F, 16.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(0, 134).addBox(-17.0F, 3.0F, 16.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(72, 101).addBox(-16.0F, -6.0F, 12.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(76, 134).addBox(-17.0F, 3.0F, 12.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(144, 101).addBox(-16.0F, -6.0F, 8.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(152, 134).addBox(-17.0F, 3.0F, 8.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(216, 101).addBox(-16.0F, -6.0F, 4.0F, 32.0F, 9.0F, 4.0F)
                .texOffs(228, 134).addBox(-17.0F, 3.0F, 4.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(424, 101).addBox(-16.0F, -5.0F, 0.0F, 32.0F, 8.0F, 4.0F)
                .texOffs(304, 134).addBox(-17.0F, 3.0F, 0.0F, 34.0F, 3.0F, 4.0F)
                .texOffs(0, 114).addBox(-15.0F, -4.0F, -4.0F, 30.0F, 7.0F, 4.0F)
                .texOffs(72, 141).addBox(-16.0F, 3.0F, -4.0F, 32.0F, 3.0F, 4.0F)
                .texOffs(136, 114).addBox(-14.0F, -3.0F, -8.0F, 28.0F, 6.0F, 4.0F)
                .texOffs(144, 141).addBox(-15.0F, 3.0F, -8.0F, 30.0F, 3.0F, 4.0F)
                .texOffs(200, 114).addBox(-12.0F, -3.0F, -12.0F, 24.0F, 6.0F, 4.0F)
                .texOffs(212, 141).addBox(-13.0F, 3.0F, -12.0F, 26.0F, 3.0F, 4.0F)
                .texOffs(456, 114).addBox(-11.0F, -2.0F, -16.0F, 22.0F, 5.0F, 4.0F)
                .texOffs(272, 141).addBox(-12.0F, 3.0F, -16.0F, 24.0F, 3.0F, 4.0F)
                .texOffs(96, 125).addBox(-10.0F, -1.0F, -20.0F, 20.0F, 4.0F, 4.0F)
                .texOffs(328, 141).addBox(-11.0F, 3.0F, -20.0F, 22.0F, 3.0F, 4.0F)
                .texOffs(144, 125).addBox(-9.0F, -1.0F, -24.0F, 18.0F, 4.0F, 4.0F)
                .texOffs(380, 141).addBox(-10.0F, 3.0F, -24.0F, 20.0F, 3.0F, 4.0F)
                .texOffs(468, 141).addBox(-7.0F, 0.0F, -28.0F, 14.0F, 3.0F, 4.0F)
                .texOffs(428, 141).addBox(-8.0F, 3.0F, -28.0F, 16.0F, 3.0F, 4.0F)
                .texOffs(280, 148).addBox(-6.0F, 1.0F, -32.0F, 12.0F, 2.0F, 4.0F)
                .texOffs(0, 148).addBox(-7.0F, 3.0F, -32.0F, 14.0F, 3.0F, 4.0F)
                .texOffs(344, 148).addBox(-5.0F, 1.0F, -36.0F, 10.0F, 2.0F, 4.0F)
                .texOffs(36, 148).addBox(-6.0F, 3.0F, -36.0F, 12.0F, 3.0F, 4.0F)
                .texOffs(456, 148).addBox(-4.0F, 2.0F, -40.0F, 8.0F, 1.0F, 4.0F)
                .texOffs(68, 148).addBox(-5.0F, 3.0F, -40.0F, 10.0F, 3.0F, 4.0F)
                .texOffs(10, 155).addBox(-3.0F, 3.0F, -44.0F, 6.0F, 0.0F, 4.0F)
                .texOffs(96, 148).addBox(-4.0F, 3.0F, -44.0F, 8.0F, 3.0F, 4.0F)
                .texOffs(380, 114).addBox(-1.0F, 3.0F, -52.0F, 2.0F, 2.0F, 8.0F)
                .texOffs(30, 155).addBox(-1.0F, 3.0F, -54.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(178, 48).addBox(-11.0F, -9.0F, 10.0F, 22.0F, 3.0F, 24.0F)
                .texOffs(120, 148).addBox(-9.0F, -8.0F, 6.0F, 18.0F, 2.0F, 4.0F)
                .texOffs(406, 48).addBox(-9.0F, -13.0F, 14.0F, 18.0F, 4.0F, 18.0F)
                .texOffs(90, 79).addBox(-7.0F, -16.0F, 18.0F, 14.0F, 3.0F, 12.0F)
                .texOffs(496, 101).addBox(-1.0F, -26.0F, 24.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(54, 155).addBox(-6.0F, -23.0F, 24.0F, 12.0F, 1.0F, 2.0F)
                .texOffs(38, 155).addBox(-1.0F, -28.0F, 24.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(256, 114).addBox(-4.0F, -7.0F, -2.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(216, 125).addBox(-3.0F, -9.0F, -1.0F, 6.0F, 2.0F, 6.0F)
                .texOffs(60, 125).addBox(-3.0F, -8.0F, -9.0F, 1.0F, 1.0F, 8.0F)
                .texOffs(60, 125).mirror().addBox(2.0F, -8.0F, -9.0F, 1.0F, 1.0F, 8.0F).mirror(false)
                .texOffs(0, 125).addBox(-7.0F, -9.0F, -10.0F, 14.0F, 5.0F, 4.0F)
                .texOffs(164, 148).addBox(-8.0F, -4.0F, -10.0F, 16.0F, 2.0F, 4.0F)
                .texOffs(68, 114).addBox(-7.0F, -10.0F, -14.0F, 14.0F, 7.0F, 4.0F)
                .texOffs(204, 148).addBox(-8.0F, -3.0F, -14.0F, 16.0F, 2.0F, 4.0F)
                .texOffs(104, 114).addBox(-6.0F, -10.0F, -18.0F, 12.0F, 7.0F, 4.0F)
                .texOffs(244, 148).addBox(-7.0F, -3.0F, -18.0F, 14.0F, 2.0F, 4.0F)
                .texOffs(352, 114).addBox(-5.0F, -8.0F, -22.0F, 10.0F, 6.0F, 4.0F)
                .texOffs(312, 148).addBox(-6.0F, -2.0F, -22.0F, 12.0F, 2.0F, 4.0F)
                .texOffs(36, 125).addBox(-4.0F, -6.0F, -26.0F, 8.0F, 5.0F, 4.0F)
                .texOffs(372, 148).addBox(-5.0F, -1.0F, -26.0F, 10.0F, 2.0F, 4.0F)
                .texOffs(424, 148).addBox(-3.0F, -3.0F, -30.0F, 6.0F, 2.0F, 4.0F)
                .texOffs(400, 148).addBox(-4.0F, -1.0F, -30.0F, 8.0F, 2.0F, 4.0F)
                .texOffs(0, 48).addBox(16.0F, -2.0F, 6.0F, 2.0F, 3.0F, 28.0F)
                .texOffs(0, 48).mirror().addBox(-18.0F, -2.0F, 6.0F, 2.0F, 3.0F, 28.0F).mirror(false)
                .texOffs(124, 48).addBox(18.0F, -2.0F, 9.0F, 2.0F, 3.0F, 25.0F)
                .texOffs(124, 48).mirror().addBox(-20.0F, -2.0F, 9.0F, 2.0F, 3.0F, 25.0F).mirror(false)
                .texOffs(358, 48).addBox(20.0F, -2.0F, 12.0F, 2.0F, 3.0F, 22.0F)
                .texOffs(358, 48).mirror().addBox(-22.0F, -2.0F, 12.0F, 2.0F, 3.0F, 22.0F).mirror(false)
                .texOffs(0, 79).addBox(22.0F, -2.0F, 15.0F, 2.0F, 3.0F, 19.0F)
                .texOffs(0, 79).mirror().addBox(-24.0F, -2.0F, 15.0F, 2.0F, 3.0F, 19.0F).mirror(false)
                .texOffs(76, 0).addBox(24.0F, -8.0F, 12.0F, 12.0F, 12.0F, 32.0F)
                .texOffs(272, 0).addBox(22.0F, -5.0F, 12.0F, 16.0F, 6.0F, 32.0F)
                .texOffs(0, 0).addBox(27.0F, -10.0F, 12.0F, 6.0F, 16.0F, 32.0F)
                .texOffs(60, 48).addBox(28.0F, -9.0F, 14.0F, 4.0F, 1.0F, 28.0F)
                .texOffs(142, 79).addBox(25.0F, -7.0F, 8.0F, 10.0F, 10.0F, 4.0F)
                .texOffs(78, 125).addBox(27.0F, -5.0F, 5.0F, 6.0F, 6.0F, 3.0F)
                .texOffs(46, 155).addBox(29.0F, -3.0F, 3.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(170, 79).addBox(25.0F, -7.0F, 44.0F, 10.0F, 10.0F, 4.0F)
                .texOffs(198, 79).addBox(24.0F, -8.0F, 44.0F, 12.0F, 12.0F, 2.0F)
                .texOffs(368, 0).addBox(38.0F, -14.0F, 28.0F, 2.0F, 20.0F, 16.0F)
                .texOffs(42, 79).addBox(38.0F, -9.0F, 22.0F, 2.0F, 15.0F, 6.0F)
                .texOffs(444, 148).addBox(38.0F, -16.0F, 40.0F, 2.0F, 2.0F, 4.0F)
                .texOffs(288, 114).addBox(26.0F, 6.0F, 18.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(76, 0).mirror().addBox(-36.0F, -8.0F, 12.0F, 12.0F, 12.0F, 32.0F).mirror(false)
                .texOffs(272, 0).mirror().addBox(-38.0F, -5.0F, 12.0F, 16.0F, 6.0F, 32.0F).mirror(false)
                .texOffs(0, 0).mirror().addBox(-33.0F, -10.0F, 12.0F, 6.0F, 16.0F, 32.0F).mirror(false)
                .texOffs(60, 48).mirror().addBox(-32.0F, -9.0F, 14.0F, 4.0F, 1.0F, 28.0F).mirror(false)
                .texOffs(142, 79).mirror().addBox(-35.0F, -7.0F, 8.0F, 10.0F, 10.0F, 4.0F).mirror(false)
                .texOffs(78, 125).mirror().addBox(-33.0F, -5.0F, 5.0F, 6.0F, 6.0F, 3.0F).mirror(false)
                .texOffs(46, 155).mirror().addBox(-31.0F, -3.0F, 3.0F, 2.0F, 2.0F, 2.0F).mirror(false)
                .texOffs(170, 79).mirror().addBox(-35.0F, -7.0F, 44.0F, 10.0F, 10.0F, 4.0F).mirror(false)
                .texOffs(198, 79).mirror().addBox(-36.0F, -8.0F, 44.0F, 12.0F, 12.0F, 2.0F).mirror(false)
                .texOffs(368, 0).mirror().addBox(-40.0F, -14.0F, 28.0F, 2.0F, 20.0F, 16.0F).mirror(false)
                .texOffs(42, 79).mirror().addBox(-40.0F, -9.0F, 22.0F, 2.0F, 15.0F, 6.0F).mirror(false)
                .texOffs(444, 148).mirror().addBox(-40.0F, -16.0F, 40.0F, 2.0F, 2.0F, 4.0F).mirror(false)
                .texOffs(288, 114).mirror().addBox(-34.0F, 6.0F, 18.0F, 8.0F, 2.0F, 8.0F).mirror(false)
                .texOffs(320, 114).addBox(-4.0F, 6.0F, -38.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(400, 114).addBox(-15.0F, 3.0F, 10.0F, 3.0F, 6.0F, 4.0F)
                .texOffs(414, 114).addBox(12.0F, 3.0F, 10.0F, 3.0F, 6.0F, 4.0F)
                .texOffs(428, 114).addBox(-15.0F, 3.0F, -14.0F, 3.0F, 6.0F, 4.0F)
                .texOffs(442, 114).addBox(12.0F, 3.0F, -14.0F, 3.0F, 6.0F, 4.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_pod = v_ship.addOrReplaceChild("pod", CubeListBuilder.create()
                .texOffs(164, 0).addBox(-13.0F, 6.0F, -16.0F, 26.0F, 10.0F, 28.0F)
                .texOffs(480, 148).addBox(-14.0F, 15.0F, 10.0F, 2.0F, 2.0F, 3.0F)
                .texOffs(490, 148).addBox(12.0F, 15.0F, 10.0F, 2.0F, 2.0F, 3.0F)
                .texOffs(500, 148).addBox(-14.0F, 15.0F, -17.0F, 2.0F, 2.0F, 3.0F)
                .texOffs(0, 155).addBox(12.0F, 15.0F, -17.0F, 2.0F, 2.0F, 3.0F)
                , PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition v_ramp = v_ship.addOrReplaceChild("ramp", CubeListBuilder.create()
                .texOffs(270, 48).addBox(-10.0F, 0.0F, -24.0F, 20.0F, 2.0F, 24.0F)
                , PartPose.offset(0.0F, 6.0F, 40.0F));
        PartDefinition v_gear_f = v_ship.addOrReplaceChild("gear_f", CubeListBuilder.create()
                .texOffs(74, 79).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(240, 125).addBox(-3.0F, 13.0F, -3.0F, 6.0F, 2.0F, 6.0F)
                , PartPose.offset(0.0F, 3.0F, -20.0F));
        PartDefinition v_gear_l = v_ship.addOrReplaceChild("gear_l", CubeListBuilder.create()
                .texOffs(58, 79).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                .texOffs(188, 125).addBox(-4.0F, 16.0F, -3.0F, 8.0F, 2.0F, 6.0F)
                , PartPose.offset(30.0F, 0.0F, 14.0F));
        PartDefinition v_gear_r = v_ship.addOrReplaceChild("gear_r", CubeListBuilder.create()
                .texOffs(58, 79).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F).mirror(false)
                .texOffs(188, 125).mirror().addBox(-4.0F, 16.0F, -3.0F, 8.0F, 2.0F, 6.0F).mirror(false)
                , PartPose.offset(-30.0F, 0.0F, 14.0F));
        PartDefinition v_flame_l = v_ship.addOrReplaceChild("flame_l", CubeListBuilder.create()
                .texOffs(404, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 28.0F)
                , PartPose.offset(30.0F, -2.0F, 48.0F));
        PartDefinition v_flame_r = v_ship.addOrReplaceChild("flame_r", CubeListBuilder.create()
                .texOffs(404, 0).mirror().addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 28.0F).mirror(false)
                , PartPose.offset(-30.0F, -2.0F, 48.0F));
        PartDefinition v_lift_l = v_ship.addOrReplaceChild("lift_l", CubeListBuilder.create()
                .texOffs(226, 79).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
                , PartPose.offset(30.0F, 8.0F, 22.0F));
        PartDefinition v_lift_r = v_ship.addOrReplaceChild("lift_r", CubeListBuilder.create()
                .texOffs(226, 79).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F).mirror(false)
                , PartPose.offset(-30.0F, 8.0F, 22.0F));
        PartDefinition v_lift_f = v_ship.addOrReplaceChild("lift_f", CubeListBuilder.create()
                .texOffs(242, 79).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
                , PartPose.offset(0.0F, 8.0F, -34.0F));
        return LayerDefinition.create(mesh, 512, 256);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pt = ageInTicks - entity.tickCount;
        float gear = Mth.clamp(entity.getGear(pt), 0.0F, 1.0F);
        float rampOpen = Mth.clamp(entity.getRamp(pt), 0.0F, 1.0F);
        float thrust = Mth.clamp(entity.getThrust(pt), 0.0F, 1.0F);
        float lift = Mth.clamp(entity.getLift(pt), 0.0F, 1.0F);

        // Cabeceo (positivo = morro abajo) y alabeo (positivo = baja el lado izquierdo)
        this.ship.xRot = headPitch * DEG;
        this.ship.zRot = entity.getRoll(pt) * DEG;

        // Tren de aterrizaje: extendido apunta abajo; plegado se gira hacia atras dentro del casco
        float fold = (1.0F - gear) * (Mth.PI / 2.0F);
        this.gearF.xRot = fold;
        this.gearL.xRot = fold;
        this.gearR.xRot = fold;

        this.ramp.xRot = rampOpen * RAMP_OPEN;
        this.pod.visible = entity.hasCargo();

        // Chorros de los motores principales: se alargan con el empuje y parpadean
        float flicker = 1.0F + Mth.sin(ageInTicks * 2.3F) * 0.07F + Mth.sin(ageInTicks * 5.1F) * 0.04F;
        boolean burning = thrust > 0.1F;
        this.flameL.visible = burning;
        this.flameR.visible = burning;
        float len = (0.25F + 0.95F * thrust) * flicker;
        float thick = 0.8F + 0.2F * thrust;
        this.flameL.zScale = len;
        this.flameR.zScale = len;
        this.flameL.xScale = thick;
        this.flameR.xScale = thick;
        this.flameL.yScale = thick;
        this.flameR.yScale = thick;

        // Toberas de sustentacion
        boolean lifting = lift > 0.1F;
        this.liftL.visible = lifting;
        this.liftR.visible = lifting;
        this.liftF.visible = lifting;
        float ll = (0.3F + 0.9F * lift) * (1.0F + Mth.sin(ageInTicks * 2.9F + 1.0F) * 0.08F);
        this.liftL.yScale = ll;
        this.liftR.yScale = ll;
        this.liftF.yScale = ll;
    }
}
