package com.mc3699.codmod.item;

import com.google.common.eventbus.Subscribe;
import com.mc3699.codmod.Codmod;
import com.mc3699.codmod.registry.CodItems;
import com.mc3699.codmod.registry.CodMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Random;

public class PentienceItem extends SwordItem {

    

    private static final Tier TOOL_TIER = new Tier() {
        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 0f;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 24;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of();
        }
    };

    public PentienceItem() {
        super(TOOL_TIER, new Item.Properties()
                .attributes(SwordItem.createAttributes(TOOL_TIER, 3f, -0.5f))
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) { //tag doesnt work in codmod for some reason...
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
        boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
        onLivingEntityHit(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
        return retval;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        // onRightClick(world, entity.getX(), entity.getY(), entity.getZ(), entity, ar.getObject());
        return ar;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.literal("§oFor with his rebirth, came with him the chains of his past.\n").withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.literal("c/ BigManRake,AliveAlex1027").withStyle(ChatFormatting.BLACK));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity, InteractionHand hand) {
        boolean retval = super.onEntitySwing(itemstack, entity, hand);
        onEntitySwing(entity.level(), entity.getX(), entity.getY(), entity.getZ());
        return retval;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
    if (!level.isClientSide() && entity instanceof LivingEntity living && !(living.hasEffect(CodMobEffects.ABSOLUTION.getDelegate()))) {
        boolean isHeld = isSelected || living.getOffhandItem() == stack;

        if (isHeld) {
            living.addEffect(new MobEffectInstance(CodMobEffects.ABSOLUTION.getDelegate(), 10, 0));
        }
    }
}

    // private static void onRightClick(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        // if (entity == null)
        //     return;

        // playSound(world, x, y, z, "codmod:orchwork", SoundSource.PLAYERS, 0.4f, 1f);

        // for (Entity entityIterator : world.getEntities(entity, new AABB(x + 14, y + 14, z + 14, x - 14, y - 14, z - 14))) {
        //     if (entityIterator instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
        //         livingEntity.addEffect(new MobEffectInstance(CodMobEffects.INSANITY, 300, 0));
        //     }
        // }

        // spawnParticles(world, x, y, z, 200, 14);

        // if (entity instanceof Player player) {
        //     player.getCooldowns().addCooldown(itemstack.getItem(), 650);
        // }
    // }

    private static void onLivingEntityHit(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;

        LivingEntity livingEntity = (LivingEntity) entity;
        float health = livingEntity.getHealth();

        Random rand = new Random();

            int pitch = Mth.nextInt(RandomSource.create(), 12, 15); 
            playSound(world, x, y, z, rand.nextBoolean() ? "codmod:pentinencehit1" : "codmod:pentinencehit2", SoundSource.PLAYERS, 0.2f, pitch / 10f);
    }

    private static void onEntitySwing(LevelAccessor world, double x, double y, double z) {
        int pitch = Mth.nextInt(RandomSource.create(), 12, 15);
        Random rand = new Random();
        
        playSound(world, x, y, z, rand.nextBoolean() ? "codmod:pentinenceswing1" : "codmod:pentinenceswing2", SoundSource.PLAYERS, 0.4f, pitch / 10f);
    }

    private static void playSound(LevelAccessor world, double x, double y, double z, String soundId, SoundSource source, float volume, float pitch) {
        if (world instanceof Level level) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!level.isClientSide()) {
                level.playSound(null, pos, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), source, volume, pitch);
            } else {
                level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)), source, volume, pitch, false);
            }
        }
    }

    // private static void spawnParticles(LevelAccessor world, double x, double y, double z, int amount, double radius) {
    //     for (int i = 0; i < amount; i++) {
    //         double offsetX = Mth.nextDouble(RandomSource.create(), -1, 1) * radius;
    //         double offsetY = Mth.nextDouble(RandomSource.create(), -1, 1) * radius;
    //         double offsetZ = Mth.nextDouble(RandomSource.create(), -1, 1) * radius;
    //         double velX = Mth.nextDouble(RandomSource.create(), -0.05, 0.05);
    //         double velY = Mth.nextDouble(RandomSource.create(), -0.05, 0.05);
    //         double velZ = Mth.nextDouble(RandomSource.create(), -0.05, 0.05);

    //         world.addParticle(ParticleTypes.NOTE, x + offsetX, y + offsetY, z + offsetZ, velX, velY, velZ);
    //     }
    // }

}