package ru.immensia.items.relics;

import java.util.HashSet;
import java.util.Set;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.boot.IStrap;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.locs.BVec;
import ru.immensia.utils.versions.Nms;

public class TreeAxe extends SpecialItem {

    final int MAX_CNT = 40;

    public TreeAxe(final ItemStack it) {super(it);}

    public void onBreak(final BlockBreakEvent e) {
        final Player pl = e.getPlayer();
        final ItemStack axe = pl.getInventory().getItemInMainHand();
        final BVec lc = BVec.of(e.getBlock());
        final World w = e.getBlock().getWorld();
        if (!LOGS.contains(Nms.fastType(w, lc))) return;
        for (final BVec chop : checkTree(lc, w)) {
            w.getBlockAt(chop.x, chop.y, chop.z).breakNaturally(axe, true, true);
        }
    }

    private static final Set<BlockType> LOGS = IStrap.getAll(BlockTypeTagKeys.LOGS);
    private static final BlockFace[] topped = {BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
        BlockFace.SELF, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    private static final BlockFace[] sided = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    private Set<BVec> checkTree(final BVec start, final World w) {
        final Set<BVec> toChop = new HashSet<>();
        final Set<BVec> current = new HashSet<>();
        final Set<BVec> next = new HashSet<>();
        toChop.add(start); next.add(start);
        do {
            current.clear();
            current.addAll(next);
            next.clear();
            for (final BVec prnt : current) {
                for (final BlockFace bf : sided) {
                    final BVec nps = prnt.add(bf.getModX(),0, bf.getModZ());
                    if (!LOGS.contains(Nms.fastType(w, nps))) continue;
                    if (toChop.add(nps)) next.add(nps);
                }
                for (final BlockFace bf : topped) {
                    final BVec nps = prnt.add(bf.getModX(), 1, bf.getModZ());
                    if (!LOGS.contains(Nms.fastType(w, nps))) continue;
                    if (toChop.add(nps)) next.add(nps);
                }
            }
        } while (!next.isEmpty() && toChop.size() < MAX_CNT);
        return toChop;
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
