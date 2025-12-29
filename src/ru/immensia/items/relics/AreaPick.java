package ru.immensia.items.relics;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.PlayerInventory;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.BlockUtil;
import ru.immensia.utils.locs.LocUtil;

public class AreaPick extends SpecialItem {

    public static final int PITCH_HRZ = 46;

    public AreaPick(final ItemStack it) {super(it);}

    public void onBreak(final BlockBreakEvent e) {
        final Player pl = e.getPlayer();
        final PlayerInventory inv = pl.getInventory();
        final Location elc = pl.getEyeLocation();
        final BlockFace bf = switch ((int) elc.getPitch() / PITCH_HRZ) {
            case 1 -> BlockFace.DOWN;
            case 0 -> LocUtil.yawToFace(elc.getYaw(), false);
            case -1 -> BlockFace.UP;
            default -> BlockFace.SELF;
        };
        final BlockType bt = e.getBlock().getType().asBlockType();
        for (final Block b : around(e.getBlock(), bf)) {
            if (!BlockUtil.is(b, bt)) continue;
            b.breakNaturally(inv.getItemInMainHand(), true, true);
            pl.damageItemStack(EquipmentSlot.HAND, 1);
        }
    }

    private List<Block> around(final Block bl, final BlockFace bf) {
        final List<Block> bls = new ArrayList<>(9);
        if (bf.getModY() != 0) {
            for (final BlockFace b : BlockFace.values()) {
                switch (b) {
                    case NORTH, SOUTH, WEST, EAST, NORTH_EAST,
                         SOUTH_EAST, NORTH_WEST, SOUTH_WEST:
                        bls.add(bl.getRelative(b));
                    default: break;
                }
            }
            return bls;
        }
        bls.add(bl.getRelative(BlockFace.DOWN));
        bls.add(bl.getRelative(BlockFace.UP));
        for (final BlockFace b : BlockFace.values()) {
            switch (b) {
                case NORTH, SOUTH, WEST, EAST:
                    if ((bf.getModX() & 1) == (b.getModX() & 1)
                        && (bf.getModZ() & 1) == (b.getModZ() & 1)) continue;
                    final Block rb = bl.getRelative(b);
                    bls.add(rb.getRelative(BlockFace.DOWN));
                    bls.add(rb.getRelative(BlockFace.UP));
                    bls.add(rb);
                default: break;
            }
        }
        return bls;
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
