package ru.immensia.items.relics;

import java.util.HashMap;
import java.util.Map;
import io.papermc.paper.math.Position;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.locs.BVec;
import ru.immensia.utils.versions.Nms;

public class CrossWeb extends SpecialItem {

    private static final BlockFace[] FACES = {BlockFace.SELF, BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final BlockData WEB = BlockType.COBWEB.createBlockData();

    public CrossWeb(final ItemStack it) {
        super(it);
    }

    public void onHit(final ProjectileHitEvent e) {
        final World w = e.getEntity().getWorld();
        final BVec loc = BVec.of(EntityUtil.center(e.getEntity()));
        final Map<Position, BlockData> bmp = new HashMap<>(FACES.length);
        for (final BlockFace bf : FACES) {
            final BVec bv = loc.add(bf.getModX(), bf.getModY(), bf.getModZ());
            if (!Nms.fastType(w, bv).isAir()) continue;
            bmp.put(bv.center(w), WEB);
        }

        for (final Player pl : w.getPlayers()) {
            pl.sendMultiBlockChange(bmp);
        }
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
