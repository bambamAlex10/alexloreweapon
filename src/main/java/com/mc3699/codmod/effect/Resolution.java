package com.mc3699.codmod.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.text.html.parser.Entity;

import com.mc3699.codmod.Codmod;
import com.mc3699.codmod.network.CodVariables;
import com.mc3699.codmod.registry.CodMobEffects;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.api.distmarker.Dist;

public class Resolution extends MobEffect {

    public static final ResourceLocation IntegritiesGift = ResourceLocation.fromNamespaceAndPath(Codmod.MOD_ID,
            "resolutiontier1");

    private static void playSound(LevelAccessor world, double x, double y, double z, String soundId, SoundSource source,
            float volume, float pitch) {
        if (world instanceof Level level) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!level.isClientSide()) {
                level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), source,
                        volume, pitch);
            } else {
                level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)),
                        source, volume, pitch, false);
            }
        }
    }

    public Resolution() {
        super(MobEffectCategory.NEUTRAL, 1);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                IntegritiesGift,
                .05f,
                AttributeModifier.Operation.ADD_VALUE);

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                IntegritiesGift,
                .5f,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @EventBusSubscriber(modid = Codmod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public class ClientHandling {
        @SubscribeEvent
        public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
            net.minecraft.world.entity.Entity self = event.getCamera().getEntity();

            if (self instanceof Player player && player.hasEffect(CodMobEffects.RESOLUTION.getDelegate())) {

                event.setRed(0f);
                event.setGreen(0f);
                event.setBlue(0f);
            }
        }

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {

            // if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) {
            //     return;
            // }

            net.minecraft.world.entity.Entity self = event.getCamera().getEntity();

            if (self instanceof Player player && player.hasEffect(CodMobEffects.RESOLUTION.getDelegate())) {

                event.setNearPlaneDistance(0.05f);
                event.setFarPlaneDistance(.1f);
                event.setFogShape(FogShape.CYLINDER);

                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
                return;

            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || !player.hasEffect(CodMobEffects.RESOLUTION.getDelegate()))
                return;

            Minecraft mc = Minecraft.getInstance();
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            
            OutlineBufferSource outlineBuffers = mc.renderBuffers().outlineBufferSource();

            outlineBuffers.setColor(255, 0, 0, 1);

            for (net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof LivingEntity target && entity != player) {
                    double rangeSq = 30.0 * 30.0;
                    if (player.distanceToSqr(entity) < rangeSq) {
                        PoseStack poseStack = event.getPoseStack();
                        
                        double x = Mth.lerp(event.getPartialTick().getGameTimeDeltaTicks(), entity.xOld, entity.getX())
                                - event.getCamera().getPosition().x;
                        double y = Mth.lerp(event.getPartialTick().getGameTimeDeltaTicks(), entity.yOld, entity.getY())
                                - event.getCamera().getPosition().y;
                        double z = Mth.lerp(event.getPartialTick().getGameTimeDeltaTicks(), entity.zOld, entity.getZ())
                                - event.getCamera().getPosition().z;

                        poseStack.pushPose();
                        poseStack.translate(x, y, z);

                        dispatcher.render(entity, 0, 0, 0, 0,
                                event.getPartialTick().getGameTimeDeltaTicks(),
                                poseStack, outlineBuffers,
                                dispatcher.getPackedLightCoords(entity,
                                        event.getPartialTick().getGameTimeDeltaTicks()));

                        poseStack.popPose();
                    }
                }
            }
            
            outlineBuffers.endOutlineBatch();
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 2 == 0; // runs every 5 ticks
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);

        
        if (entity instanceof Player) {
            if (entity.getName().getString().equals("AliveAlex1027")) {
            playSound(entity.level(), entity.getX(), entity.getY(), entity.getZ(), "codmod:alexult", SoundSource.PLAYERS, .4f, 1);
            } else if (entity.getName().getString().equals("BigManRake")) {
            playSound(entity.level(), entity.getX(), entity.getY(), entity.getZ(), "codmod:rakeult", SoundSource.PLAYERS, .4f, 1);
            } else {
                playSound(entity.level(), entity.getX(), entity.getY(), entity.getZ(), "codmod:standardult", SoundSource.PLAYERS, .4f, 1);
            }
        } else {
            playSound(entity.level(), entity.getX(), entity.getY(), entity.getZ(), "codmod:standardult", SoundSource.PLAYERS, .4f, 1);
        }
    }

}
