package ru.immensia.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;


public class BlockUtil {

    public static final BlockData air = BlockType.AIR.createBlockData();

    public static boolean is(final Block b, final BlockType bt) {
        return b.getType().asBlockType().equals(bt);
    }

    public static void set(final Block b, final BlockType bt, final boolean upd) {
        if (BlockType.AIR.equals(bt)) {
            b.setBlockData(air, upd);
            return;
        }
        b.setBlockData(bt.createBlockData(), upd);
    }

    public static Block getSignAttachedBlock(final Block b) {
        if (b.getState() instanceof final Sign sign
                && sign.getBlockData() instanceof final WallSign signData) {
            return b.getRelative(signData.getFacing().getOppositeFace());

        }
        return b.getRelative(BlockFace.DOWN);
    }

}
