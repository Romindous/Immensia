package ru.immensia.utils.locs;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.immensia.utils.EntityUtil;
import ru.immensia.utils.NumUtil;
import ru.immensia.objects.Duo;
import ru.immensia.utils.versions.Nms;


//не переименовывать! юзают все плагины!
public class LocUtil {

    private static final BlockFace[] axial = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
    private static final BlockFace[] radial = {BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST};

    public static BlockFace yawToFace(final float yaw, final boolean cardinal) {
        return cardinal ? radial[Math.round(yaw / 45f) & 0x7]
            : axial[Math.round(yaw / 90f) & 0x3];
    }

    public static BlockFace vecToFace(final Vector dir, final boolean cardinal) {
        return cardinal ? radial[Math.round(NumUtil.getYaw(dir) / 45f) & 0x7]
            : axial[Math.round(NumUtil.getYaw(dir) / 90f) & 0x3];
    }

    public static String toString(final Location loc) {
        if (loc == null) {
            return ":::";
        }
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public static String toDirString(final Location loc) {
        if (loc == null) {
            return ":::::";
        }
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + (int) loc.getYaw() + ":" + (int) loc.getPitch();
    }

    public static int getDistance(final Location loc1, final Location loc2) {
        return NumUtil.sqrt(getDistanceSquared(loc1, loc2));
    }

    public static int getDistanceSquared(final Location loc1, final Location loc2) {
        if (loc1 == null || loc2 == null || !loc1.getWorld()
            .getUID().equals(loc2.getWorld().getUID())) return Integer.MAX_VALUE;
        return NumUtil.square(loc1.getBlockX() - loc2.getBlockX()) +
            NumUtil.square(loc1.getBlockY() - loc2.getBlockY()) +
            NumUtil.square(loc1.getBlockZ() - loc2.getBlockZ());
    }

    public static @Nullable Player getNearPl(final Location loc, final double dst, final @Nullable Predicate<Player> which) {
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ();
        double dS = dst * dst;
        Player fin = null;
        for (final Player e : loc.getWorld().getPlayers()) {
            final Location el = EntityUtil.center(e);
            final double d = Math.pow(el.getX() - X, 2d) + Math.pow(el.getY() - Y, 2d) + Math.pow(el.getZ() - Z, 2d);
            if (d > dS || which != null && !which.test(e)) continue;
            dS = d; fin = e;
        }
        return fin;
    }

    public static @Nullable Player getNearPl(final BVec loc, final int dst, final @Nullable Predicate<Player> which) {
        final int X = loc.x, Y = loc.y, Z = loc.z;
        double dS = dst * dst;
        Player fin = null;
        final World w = loc.w();
        if (w == null) return null;
        for (final Player e : w.getPlayers()) {
            final Location el = EntityUtil.center(e);
            final double d = Math.pow(el.getX() - X, 2d) + Math.pow(el.getY() - Y, 2d) + Math.pow(el.getZ() - Z, 2d);
            if (d > dS || which != null && !which.test(e)) continue;
            dS = d; fin = e;
        }
        return fin;
    }

    private static final Set<BlockType> PASS = Set.of(BlockType.BARRIER, BlockType.STRUCTURE_VOID,
        BlockType.CHORUS_PLANT, BlockType.SWEET_BERRY_BUSH, BlockType.BAMBOO, BlockType.VINE,
        BlockType.WEEPING_VINES, BlockType.TWISTING_VINES, BlockType.LADDER, BlockType.LILY_PAD,
        BlockType.CHORUS_FLOWER);
    //годится ли блок для головы?
    public static boolean isPassable(final BlockType tp) {
        if (tp == null) return false;
        if (tp.isAir()) return true;
        if (BlockType.LAVA.equals(tp)) return false;
        else if (PASS.contains(tp)) return true;
        else return !tp.hasCollision();
    }

    public static <G extends Entity> Collection<G> getChEnts(final Location loc, final double dst, final Class<G> ent, final @Nullable Predicate<G> which) {
        final HashMap<Integer, G> hs = new HashMap<>();
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ(), dS = dst * dst;
        final int mnX = (int) (X + dst) >> 4, mnZ = (int) (Z + dst) >> 4;
        final World w = loc.getWorld();
        for (int cx = (int) (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (int) (Z - dst) >> 4; cz <= mnZ; cz++) {
                if (!w.isChunkLoaded(cx, cz)) continue;
                for (final Entity e : w.getChunkAt(cx, cz).getEntities()) {
                    if (!ent.isInstance(e)) continue;
                    final Location el = EntityUtil.center(e);
                    if (Math.pow(el.getX() - X, 2d) + Math.pow(el.getY() - Y, 2d)
                        + Math.pow(el.getZ() - Z, 2d) > dS) continue;
                    final G ge = ent.cast(e);
                    if (which != null && !which.test(ge)) continue;
                    hs.put(e.getEntityId(), ge);
                }
            }
        }
        return hs.values();
    }

    public static <G extends Entity> G getClsChEnt(final Location loc, final double dst, final Class<G> ent, final @Nullable Predicate<G> which) {
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ();
        final int mnX = (int) (X + dst) >> 4, mnZ = (int) (Z + dst) >> 4;
        final World w = loc.getWorld();

        double dS = dst * dst;
        G fin = null;
        for (int cx = (int) (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (int) (Z - dst) >> 4; cz <= mnZ; cz++) {
                if (!w.isChunkLoaded(cx, cz)) continue;
                for (final Entity e : w.getChunkAt(cx, cz).getEntities()) {
                    if (!ent.isInstance(e)) continue;
                    final Location el = EntityUtil.center(e);
                    final double d = Math.pow(el.getX() - X, 2d)
                        + Math.pow(el.getY() - Y, 2d) + Math.pow(el.getZ() - Z, 2d);
                    if (d > dS) continue;
                    final G ge = ent.cast(e);
                    if (which != null && !which.test(ge)) continue;
                    dS = d; fin = ge;
                }
            }
        }
        return fin;
    }

    public static <G extends Entity> Collection<G> getChEnts(final BVec loc, final int dst, final Class<G> ent, final @Nullable Predicate<G> which) {
        final World w = loc.w(); if (w == null) return List.of();
        final HashMap<Integer, G> hs = new HashMap<>();
        final int X = loc.x, Y = loc.y, Z = loc.z, dS = dst * dst;
        final int mnX = (X + dst) >> 4, mnZ = (Z + dst) >> 4;
        for (int cx = (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (Z - dst) >> 4; cz <= mnZ; cz++) {
                if (!w.isChunkLoaded(cx, cz)) continue;
                for (final Entity e : loc.w().getChunkAt(cx, cz).getEntities()) {
                    if (!ent.isInstance(e)) continue;
                    final Location el = EntityUtil.center(e);
                    final int dx = el.getBlockX() - X,
                        dy = el.getBlockY() - Y, dz = el.getBlockZ() - Z;
                    if (dx * dx + dy * dy + dz * dz > dS) continue;
                    final G ge = ent.cast(e);
                    if (which != null && !which.test(ge)) continue;
                    hs.put(e.getEntityId(), ge);
                }
            }
        }
        return hs.values();
    }

    public static <G extends Entity> G getClsChEnt(final BVec loc, final int dst, final Class<G> ent, final @Nullable Predicate<G> which) {
        final World w = loc.w(); if (w == null) return null;
        final int X = loc.x, Y = loc.y, Z = loc.z;
        final int mnX = (X + dst) >> 4, mnZ = (Z + dst) >> 4;
        int dS = dst * dst;
        G fin = null;
        for (int cx = (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (Z - dst) >> 4; cz <= mnZ; cz++) {
                if (!w.isChunkLoaded(cx, cz)) continue;
                for (final Entity e : loc.w().getChunkAt(cx, cz).getEntities()) {
                    if (!ent.isInstance(e)) continue;
                    final Location el = EntityUtil.center(e);
                    final int d = NumUtil.square(el.getBlockX() - X)
                        + NumUtil.square(el.getBlockY() - Y)
                        + NumUtil.square(el.getBlockZ() - Z);
                    if (d > dS) continue;
                    final G ge = ent.cast(e);
                    if (which != null && !which.test(ge)) continue;
                    dS = d; fin = ge;
                }
            }
        }
        return fin;
    }

    @Deprecated
    public static TraceResult trace(final Location org, final Vector dir, final double dst, final OnPosData done) {
        return trace(org, dir.normalize().multiply(dst), done);
    }

    public static TraceResult trace(final Location org, final Vector dir, final OnPosData done) {
        final double dst = dir.length();
        final double dirX = dir.getX() / dst, dirY = dir.getY() / dst, dirZ = dir.getZ() / dst;
        final int finX = (int) (dir.getX() + org.getX()), finY = (int) (dir.getY() + org.getY()), finZ = (int) (dir.getZ() + org.getZ());
        int mapX = (int) Math.floor(org.getX()), mapY = (int) Math.floor(org.getY()), mapZ = (int) Math.floor(org.getZ());
        final double deltaDistX = Math.abs(1.0F / dirX), deltaDistY = Math.abs(1.0F / dirY), deltaDistZ = Math.abs(1.0F / dirZ);

        final int stepX, stepY, stepZ;
        double sideDistX, sideDistY, sideDistZ;
        if (dirX < 0.0F) {
            stepX = -1;
            sideDistX = (org.getX() - mapX) * deltaDistX;
        } else {
            stepX = 1;
            sideDistX = (1.0F - org.getX() + mapX) * deltaDistX;
        }
        if (dirY < 0.0F) {
            stepY = -1;
            sideDistY = (org.getY() - mapY) * deltaDistY;
        } else {
            stepY = 1;
            sideDistY = (1.0F - org.getY() + mapY) * deltaDistY;
        }
        if (dirZ < 0.0F) {
            stepZ = -1;
            sideDistZ = (org.getZ() - mapZ) * deltaDistZ;
        } else {
            stepZ = 1;
            sideDistZ = (1.0F - org.getZ() + mapZ) * deltaDistZ;
        }

        if (Double.isNaN(sideDistX)) sideDistX = Double.POSITIVE_INFINITY;
        if (Double.isNaN(sideDistY)) sideDistY = Double.POSITIVE_INFINITY;
        if (Double.isNaN(sideDistZ)) sideDistZ = Double.POSITIVE_INFINITY;

        final World w = org.getWorld();
        final List<Duo<BlockPosition, BlockData>> info =
            new ArrayList<>(NumUtil.abs(finX - mapX) + NumUtil.abs(finY - mapY) + NumUtil.abs(finZ - mapZ));
        while (true) {
            if (sideDistZ < sideDistX && sideDistZ < sideDistY) {
                sideDistZ += deltaDistZ;
                mapZ += stepZ;
            } else if (sideDistX < sideDistY) {
                sideDistX += deltaDistX;
                mapX += stepX;
            } else {
                sideDistY += deltaDistY;
                mapY += stepY;
            }

//            w.spawnParticle(Particle.FLAME, org.clone().add(sideDistX, sideDistY, sideDistZ), 2, 0d, 0d, 0d, 0d);
            final BlockData bd = Nms.fastData(w, mapX, mapY, mapZ);
            final BlockPosition bp = Position.block(mapX, mapY, mapZ);
            if (done.test(bp, bd)) {
                return new TraceResult(info, BVec.of(w, mapX, mapY, mapZ), false);
            }

            info.add(new Duo<>(bp, bd));
            if ((mapX - finX) * stepX > 0 || (mapY - finY) * stepY > 0 || (mapZ - finZ) * stepZ > 0) {
                final BlockPosition lbp = info.getLast().key();
                return new TraceResult(info, BVec.of(w, lbp.blockX(), lbp.blockY(), lbp.blockZ()), true);
            }
        }
    }

    public interface OnPosData {
        boolean test(final BlockPosition pos, final BlockData data);
    }

    public record TraceResult(List<Duo<BlockPosition, BlockData>> posData, BVec fin, boolean endDst) {
        public List<Block> blocks() {
            final World w = fin.w(); if (w == null) return List.of();
            return posData.stream().map(bs -> w.getBlockAt(bs.key().toLocation(w))).toList();
        }

        public boolean has(final BlockPosition pos) {
            for (final Duo<BlockPosition, BlockData> pd : posData)
                if (pd.key().equals(pos)) return true;
            return false;
        }
    }


    //координата в long взято из BlockPosition
    public static long asLong(final Location loc) {
        return (((long) loc.getBlockX() & (long) 67108863) << 38) | (((long) loc.getBlockY() & (long) 4095)) | (((long) loc.getBlockZ() & (long) 67108863) << 12);
    }

    public static int getX(long packedPos) {
        return (int) (packedPos >> 38); // Paper - simplify/inline
    }

    public static int getY(long packedPos) {
        return (int) ((packedPos << 52) >> 52); // Paper - simplify/inline
    }

    public static int getZ(long packedPos) {
        return (int) ((packedPos << 26) >> 38);  // Paper - simplify/inline
    }


    //координата чанка запакованная в int
    public static int cLoc(final Chunk chunk) {
        return cLoc(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static int cLoc(final Location loc) {
        return cLoc(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static int cLoc(final String worldName, final int cX, final int cZ) {
        //return worldName.length()<<26 | (cX+4096)<<13 | (cZ+4096);
        return (cX + 4096) << 13 | (cZ + 4096);
    }

    public static Chunk getChunk(final String worldName, final int cLoc) {
        return Bukkit.getWorld(worldName).getChunkAt(getChunkX(cLoc), getChunkZ(cLoc));
    }

    public static int getChunkX(int cLoc) { //len<<26 | (x+4096)<<13 | (z+4096);
        return ((cLoc >> 13 & 0x1FFF) - 4096); //8191 = 1FFF = 0b00000000_00000000_00011111_11111111
    }

    public static int getChunkZ(int cLoc) { //len<<26 | (x+4096)<<13 | (z+4096);
        return ((cLoc & 0x1FFF) - 4096); //8191 = 1FFF = 0b00000000_00000000_00011111_11111111
    }

    public static Set<String> worldNames() {
        final Set<String> list = new HashSet<>();
        for (World w : Bukkit.getWorlds()) {
            list.add(w.getName());
        }
        return list;
    }

}
