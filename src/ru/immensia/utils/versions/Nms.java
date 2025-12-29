package ru.immensia.utils.versions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.persistence.PaperPersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.key.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.entity.CraftEntityTypes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.immensia.items.ItemBuilder;
import ru.immensia.items.PDC;
import ru.immensia.objects.Duo;
import ru.immensia.utils.locs.BVec;


public class Nms {

    private static final Key chatKey;
    private static final String signId;

    static {
        chatKey = Key.key("ostrov_chat", "listener");
        signId = BlockEntityType.getKey(BlockEntityType.SIGN).toString();
    }

    public static void fakeItem(final Player p, final ItemStack it, final int slot) {
        sendPacket(p, new ClientboundSetPlayerInventoryPacket(slot, net.minecraft.world.item.ItemStack.fromBukkitCopy(it)));
    }

    private static final int OFH_SLOT = 40;
    public static void totemPop(final Player p, final ItemStack totem) {
        fakeItem(p, new ItemBuilder(totem).set(DataComponentTypes.DEATH_PROTECTION,
            DeathProtection.deathProtection().build()).build(), OFH_SLOT);
        sendPacket(p, new ClientboundEntityEventPacket(Craft.toNMS(p), EntityEffect.PROTECTED_FROM_DEATH.getData()));
        fakeItem(p, p.getInventory().getItemInOffHand(), OFH_SLOT);
    }

    public static void totemWorldPop(final Player p, final ItemStack totem) {
        final PlayerInventory inv = p.getInventory();
        final ItemStack ofh = inv.getItemInOffHand();
        inv.setItemInOffHand(new ItemBuilder(totem).set(DataComponentTypes.DEATH_PROTECTION,
            DeathProtection.deathProtection().build()).build());
        p.playEffect(EntityEffect.PROTECTED_FROM_DEATH);
        inv.setItemInOffHand(ofh);
    }




    public static final CraftPersistentDataTypeRegistry PDT_REG = new CraftPersistentDataTypeRegistry();
    public static PersistentDataContainer newPDC() {return new CraftPersistentDataContainer(PDT_REG);}
    public static PersistentDataContainer newPDC(final PersistentDataContainerView data) {
        if (!(data instanceof final PaperPersistentDataContainerView pd))
            return new CraftPersistentDataContainer(PDT_REG);
        final Map<String, Tag> tags = new HashMap<>();
        for (final NamespacedKey nk : pd.getKeys()) {
            final String k = nk.asString(); tags.put(k, pd.getTag(k));
        }
        return new CraftPersistentDataContainer(tags, PDT_REG);
    }
    public static final String P_B_V = "PublicBukkitValues";
    public static void setCustomData(final ItemStack it, final PDC.Data data) {
        if (it == null || data.isEmpty()) return;
        final DataComponentPatch.Builder builder = DataComponentPatch.builder();
        final CraftPersistentDataContainer pdc = new CraftPersistentDataContainer(PDT_REG);
        for (final Duo<NamespacedKey, Serializable> en : data) {
            switch (en.val()) {
                case final Byte d -> pdc.set(en.key(), PersistentDataType.BYTE, d);
                case final Long d -> pdc.set(en.key(), PersistentDataType.LONG, d);
                case final Integer d -> pdc.set(en.key(), PersistentDataType.INTEGER, d);
                case final Float d -> pdc.set(en.key(), PersistentDataType.FLOAT, d);
                case final Double d -> pdc.set(en.key(), PersistentDataType.DOUBLE, d);
                case final byte[] d -> pdc.set(en.key(), PersistentDataType.BYTE_ARRAY, d);
                case final int[] d -> pdc.set(en.key(), PersistentDataType.INTEGER_ARRAY, d);
                case final String d -> pdc.set(en.key(), PersistentDataType.STRING, d);
                default -> pdc.set(en.key(), PersistentDataType.STRING, en.val().toString());
            }
        }
        final CompoundTag pdcTag = new CompoundTag();
        for (final Map.Entry<String, Tag> en : pdc.getRaw().entrySet()) {
            pdcTag.put(en.getKey(), en.getValue());
        }
        final CompoundTag ct = new CompoundTag();
        ct.put(P_B_V, pdcTag);
        builder.set(DataComponents.CUSTOM_DATA, CustomData.of(ct));
        if (it instanceof final CraftItemStack cit) {
            cit.handle.applyComponents(builder.build());
        }
    }

    @Deprecated
    public static void setCustomData(final ItemStack it, final PersistentDataContainerView data) {
        it.editPersistentDataContainer(pdc -> data.copyTo(pdc, true));
    /*if (it == null || data.isEmpty() || !(data instanceof final PaperPersistentDataContainerView pd)) return;
    final DataComponentPatch.Builder builder = DataComponentPatch.builder();
    final Map<String, Tag> tags = new HashMap<>();
    for (final NamespacedKey nk : pd.getKeys()) {
      final String k = nk.asString(); tags.put(k, pd.getTag(k));
    }
    final CraftPersistentDataContainer pdc = new CraftPersistentDataContainer(tags, PDT_REG);
    final CompoundTag pdcTag = new CompoundTag();
    for (final Map.Entry<String, Tag> en : pdc.getRaw().entrySet()) {
      pdcTag.put(en.getKey(), en.getValue());
    }
    final CompoundTag ct = new CompoundTag();
    ct.put(P_B_V, pdcTag);
    builder.set(DataComponents.CUSTOM_DATA, CustomData.of(ct));
    if (it instanceof final CraftItemStack cit) {
      cit.handle.applyComponents(builder.build());
    }*/
    }

    public static BlockType fastType(final World w, int x, int y, int z) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState iBlockData = sl.getBlockState(new BlockPos(x, y, z));
        return Craft.fromNMS(iBlockData.getBlock());
    }

    public static BlockType fastType(final World w, final BVec bv) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState iBlockData = sl.getBlockState(new BlockPos(bv.x, bv.y, bv.z));
        return Craft.fromNMS(iBlockData.getBlock());
    }

    @Deprecated
    public static Material getFastMat(final Location loc) {
        final ServerLevel sl = Craft.toNMS(loc.getWorld());
        final BlockState iBlockData = sl.getBlockState(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return iBlockData.getBukkitMaterial();
    }

    public static BlockType fastType(final Location loc) {
        final ServerLevel sl = Craft.toNMS(loc.getWorld());
        final BlockState iBlockData = sl.getBlockState(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return Craft.fromNMS(iBlockData.getBlock());
    }

    public static BlockData fastData(final World w, int x, int y, int z) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState iBlockData = sl.getBlockState(new BlockPos(x, y, z));
        return iBlockData.createCraftBlockData();
    }

    public static BlockData fastData(final World w, final BVec bv) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState iBlockData = sl.getBlockState(new BlockPos(bv.x, bv.y, bv.z));
        return iBlockData.createCraftBlockData();
    }

    public static BlockData fastData(final Location loc) {
        final ServerLevel sl = Craft.toNMS(loc.getWorld());
        final BlockState iBlockData = sl.getBlockState(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return iBlockData.createCraftBlockData();
    }

    public static void fastType(final World w, final List<BlockPosition> poss, final BlockType bt) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState bs = Craft.toNMS(bt).defaultBlockState();
        for (final BlockPosition bp : poss) {
            setNmsData(sl, new BlockPos(bp.blockX(), bp.blockY(), bp.blockZ()), bs);
        }
    }

    public static void fastData(final World w, final BVec bv, final BVec dims, final BlockData bd) {
        final ServerLevel sl = Craft.toNMS(w);
        final BlockState bs = Craft.toNMS(bd);
        for (int x_ = 0; x_ != dims.x; x_++) {
            for (int z_ = 0; z_ != dims.y; z_++) {
                for (int y_ = 0; y_ != dims.z; y_++) {
                    setNmsData(sl, new BlockPos(bv.x + x_, bv.y + y_, bv.z + z_), bs);
                }
            }
        }
    }

    //упрощенный вид
    private static final int FST_FLAGS = 2 | 16 | 1024;
    protected static void setNmsData(final ServerLevel sl, final BlockPos pos, final BlockState curr) {
        final BlockState old = sl.getBlockState(pos);
        if (old.hasBlockEntity() && curr.getBlock() != old.getBlock()) {
            sl.removeBlockEntity(pos);
        }
        final boolean success = sl.setBlock(pos, curr, FST_FLAGS); // NOTIFY | NO_OBSERVER | NO_PLACE (custom)
        if (success) sl.sendBlockUpdated(pos, old, curr, 3);
    }

    public static int getTps() {
        return MinecraftServer.TPS;
    }

    public static int getitemDespawnRate(final World w) { //skyworld
        return Craft.toNMS(w).spigotConfig.itemDespawnRate;
    }

    public static EntityType typeByClass(final Class<? extends LivingEntity> cls) {
        return CraftEntityTypes.getEntityTypeData(cls).entityType();
    }

    //bukkit : 0-8 hotbar 9-35 inventory, 39 helmet, 38 chestplate, 37 leggings, 36 boots, 40 offhand
    //nms ARMOR_SLOT_START 5-8, INV_SLOT_START 9-35,  hotbar 36-44, SHIELD_SLOT 45
    public static void sendFakeEquip(final Player p, final int nmsInventorySlot, final ItemStack item) {
//Ostrov.log_warn("sendFakeEquip " + playerInventorySlot + " " + item.getType());
        final ServerPlayer sp = Craft.toNMS(p); //5-шлем
        sp.connection.send(new ClientboundContainerSetSlotPacket(sp.inventoryMenu.containerId,
            sp.inventoryMenu.getStateId(), nmsInventorySlot, net.minecraft.world.item.ItemStack.fromBukkitCopy(item)));
    }

    public static void sendChunkChange(final Player p, final Chunk chunk) {
        chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        final ServerLevel ws = Craft.toNMS(p.getWorld());
        final LevelChunk nmsChunk = ws.getChunkIfLoaded(chunk.getX(), chunk.getZ());
        if (nmsChunk == null) return;
        final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
            nmsChunk, ws.getLightEngine(), null, null, true);
        sendPacket(p, packet);//toNMS(p).c.a(packet);//sendPacket(p, packet);
    }

    public static void swing(final LivingEntity le, final EquipmentSlot hand) {
        Craft.toNMS(le).swinging = false;
        le.swingHand(hand);
    }

    public static void noFallDmg(final Player pl) {
        Craft.toNMS(pl).setIgnoreFallDamageFromCurrentImpulse(true);
    }

    public static boolean hasFallDmg(final Player pl) {
        return !Craft.toNMS(pl).isIgnoringFallDamageFromCurrentImpulse();
    }

    public static void zoom(final Player pl, final float zoom) {
        final Abilities ab = new Abilities();
        ab.invulnerable = ab.flying = ab.mayfly = ab.instabuild
            = switch (pl.getGameMode())
        {case CREATIVE, SPECTATOR -> true; default -> false;};
        if (zoom > 10f) {
            final float rev = Math.max(21f-zoom, 1f);
            ab.setWalkingSpeed(rev * rev * -0.1f);
        } else ab.setWalkingSpeed(zoom * zoom / 10f);
        Nms.sendPacket(pl, new ClientboundPlayerAbilitiesPacket(ab));
    }

    public static void sendPacket(final Player p, final Packet<?> packet) {
//Ostrov.log_warn("sendPacket "+packet.getClass().getName());
        Craft.toNMS(p).connection.send(packet);
    }

    public static void sendWorldPacket(final World w, final Packet<?> packet) {
        for (final Player p : w.getPlayers()) Craft.toNMS(p).connection.send(packet);
    }

    public static void sendWorldPacket(final World w, final Predicate<Player> send, final Packet<?> packet) {
        for (final Player p : w.getPlayers()) if (send.test(p)) Craft.toNMS(p).connection.send(packet);
    }

    @SafeVarargs
    public static void sendPackets(final Player p, Packet<ClientGamePacketListener>... packets) {
        Craft.toNMS(p).connection.send(new ClientboundBundlePacket(Arrays.asList(packets)));
    }

    @SafeVarargs
    public static void sendWorldPackets(final World w, Packet<ClientGamePacketListener>... packets) {
        final ClientboundBundlePacket cbp = new ClientboundBundlePacket(Arrays.asList(packets));
        for (Player p : w.getPlayers()) Craft.toNMS(p).connection.send(cbp);
    }

    @SafeVarargs
    public static void sendWorldPackets(final World w, final Predicate<Player> send, Packet<ClientGamePacketListener>... packets) {
        final ClientboundBundlePacket cbp = new ClientboundBundlePacket(Arrays.asList(packets));
        for (Player p : w.getPlayers()) if (send.test(p)) Craft.toNMS(p).connection.send(cbp);
    }

}