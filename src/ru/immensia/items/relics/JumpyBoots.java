package ru.immensia.items.relics;

import org.bukkit.Color;
import org.bukkit.Input;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.EntityUtil;

public class JumpyBoots extends SpecialItem {

    private static final int CD = 12;
    private static final double MUL_DIR = 0.2d;
    private static final double ADD_DY = 0.4d;

    public JumpyBoots(final ItemStack it) {super(it);}

    public void onJump(final PlayerInputEvent e) {
        final Player p = e.getPlayer();
        if (p.isInsideVehicle()) return;
        final Input in = e.getInput();
        if (!in.isJump()) {
            if (p.getVelocity().getY() < 0) return;
            p.setFreezeTicks(8);
            return;
        }
        if (p.getFreezeTicks() == 0) return;
        p.setFreezeTicks(0);
        if (p.getVelocity().getY() < 0) return;
        if (p.hasCooldown(item())) return;
        p.setCooldown(item(), CD);
        p.setVelocity(p.getVelocity().add(p.getEyeLocation().getDirection()
            .setY(0d).multiply(MUL_DIR).setY(ADD_DY)));
        EntityUtil.moveffect(p, Sound.ENTITY_ENDER_DRAGON_FLAP,
            1.4f, Color.fromRGB(160, 250, 255));
    }

    protected void onAttack(final EquipmentSlot eq, final EntityDamageByEntityEvent e) {}
    protected void onDefense(final EquipmentSlot eq, final EntityDamageEvent e) {}
    protected void onShoot(final EquipmentSlot eq, final ProjectileLaunchEvent e) {}
    protected void onInteract(final EquipmentSlot eq, final PlayerInteractEvent e) {}
}
