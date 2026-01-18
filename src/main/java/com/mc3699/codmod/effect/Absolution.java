package com.mc3699.codmod.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mc3699.codmod.Codmod;
import com.mc3699.codmod.network.CodVariables;
import com.mc3699.codmod.registry.CodMobEffects;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class Absolution extends MobEffect {

    // public static final ResourceLocation HealthBenefits = ResourceLocation.fromNamespaceAndPath(Codmod.MOD_ID, "absolutiontier1");
    // public static final AttributeModifier HealthAddition = new AttributeModifier(HealthBenefits , .6, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    public static final List<net.minecraft.core.Holder<MobEffect>> bypass = List.of(
            CodMobEffects.ABSOLUTION,
            CodMobEffects.INSANITY,
            CodMobEffects.RESOLUTION
        );

    public static final Supplier<AttachmentType<Integer>> EFFECT_DATA = CodVariables.ATTACHMENT_TYPES.register("absolution_data", () ->
        AttachmentType.builder(() -> 0) // dont worry about initialization
        .serialize(Codec.INT)
        .copyOnDeath()
        .build()
    );

    public Absolution() {
        super(MobEffectCategory.NEUTRAL, 1);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) { //the client doesnt need to track / use it
            return true;
        }

        if (!(entity instanceof Player)) { // just run it only on players
            return true;
        }

        boolean ResolutionActive = entity.hasEffect(bypass.get(2));
        boolean InsanityActive = entity.hasEffect(bypass.get(1));

        if (entity.getActiveEffects().size() > 1) {
            List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects()); // make a copy of the current effects so that way you dont manipulate the list while going through it.
        
            int multiplier = effects.size() - 1;
            int addedtotal = 0;

            for (MobEffectInstance effectInstance : effects) {
                int effectamplifier = effectInstance.getAmplifier(); // scale of the effect
                int effectduration = effectInstance.getDuration(); // how long it lasts in ticks

                net.minecraft.core.Holder<MobEffect> effect = effectInstance.getEffect();

                if (bypass.contains(effect)) { // is it in the whitelist?
                    continue;
                } else {
                    addedtotal += effectamplifier*(effectduration/20); // add to the total the multiple of the amplifier and the seconds it lasts.
                    entity.removeEffect(effectInstance.getEffect()); // remove the effect off the entity
                }
            }

            int previous = entity.getData(Absolution.EFFECT_DATA.get());
            
            addedtotal = Mth.clamp(previous+(addedtotal*multiplier),0,1000000);

            entity.setData(Absolution.EFFECT_DATA.get(), addedtotal); // sets the total to the entity with the effect
        } 

        if (!ResolutionActive && InsanityActive) {
            entity.addEffect(new MobEffectInstance(bypass.get(2),14*20,0));
        }
        if (InsanityActive) {
            entity.removeEffect(bypass.get(1));
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 2 == 0; // runs every 5 ticks
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
    }

    @SubscribeEvent
    public static void applyClampedResistance(LivingDamageEvent.Pre event) {
        int total = event.getEntity().getData(Absolution.EFFECT_DATA.get()); // get the potion total

        if (!event.getEntity().hasEffect(CodMobEffects.ABSOLUTION.getDelegate())) { // check if entity has the effect 
            return;
        }

        float reduction = Mth.clamp(total/120000.0f, 0.0f, .4f); // produce the reduction float. every enchanted golden apple is 1500.0f, it caps out at 48000. 

        float currentDamage = event.getNewDamage();
        float finalDamage = currentDamage * (1.0f - reduction);

        event.setNewDamage(Math.max(0, finalDamage)); // the only way to make it work on all types of damage, from true damage (warden) to fall damage.

        Codmod.LOGGER.info("Reduced damage: {} -> {}", 
        currentDamage, event.getContainer().getNewDamage());
    }

    @Override
    public void onEffectStarted(LivingEntity entiy, int amplifier) {

    }

}
