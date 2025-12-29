package ru.immensia.utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import ru.immensia.utils.colors.TCUtil;

import static org.bukkit.attribute.Attribute.*;

public class ItemUtil {

    public static final ItemStack air, book;
    private static final Set<ItemType> POTION;
    private static final Pattern regex;
    private static final Registry<Attribute> ATTR_REG;
    public static final String OLD_PDC = "custom_data";
//    private static final Gson GSON;

    static {
        ATTR_REG = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);
        regex = Pattern.compile("(.{1,24}(?:\\s|$))|(.{0,24})", Pattern.DOTALL);
        air = ItemType.AIR.createItemStack();
        book = ItemType.WRITTEN_BOOK.createItemStack();
        POTION = Set.of(ItemType.TIPPED_ARROW, ItemType.POTION,
            ItemType.LINGERING_POTION, ItemType.SPLASH_POTION);
    }

    public static int findItem(final Player p, final ItemStack item) {
        for (int i = 0; i < p.getInventory().getContents().length; i++) {
            if (compareItem(p.getInventory().getContents()[i], item, true)) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public static int getCusomModelData(final ItemStack is) {
        if (is != null && is.hasItemMeta() && is.getItemMeta().hasCustomModelData()) {
            return is.getItemMeta().getCustomModelData();
        }
        return 0;
    }

    @Deprecated
    public static ItemStack setCusomModelData(final ItemStack is, final int id) {
        if (is == null) return null;
        final ItemMeta im = is.hasItemMeta() ? is.getItemMeta() : Bukkit.getItemFactory().getItemMeta(is.getType());//is.getItemMeta();
        im.setCustomModelData(id);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack setName(final ItemStack is, final String name) {
        final ItemMeta im = is.getItemMeta();
        im.displayName(TCUtil.form(name));
        is.setItemMeta(im);
        return is;
    }


    public enum Texture {
        nextPage("c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516"),
        previosPage("f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc"),
        add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"),
        //https://minecraft-heads.com/custom-heads/alphabet?start=4720
        //черный стиль - https://minecraft-heads.com/custom-heads/alphabet?start=3600
        _0_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "M2YwOTAxOGY0NmYzNDllNTUzNDQ2OTQ2YTM4NjQ5ZmNmY2Y5ZmRmZDYyOTE2YWVjMzNlYmNhOTZiYjIxYjUifX19"),
        _1_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "Y2E1MTZmYmFlMTYwNThmMjUxYWVmOWE2OGQzMDc4NTQ5ZjQ4ZjZkNWI2ODNmMTljZjVhMTc0NTIxN2Q3MmNjIn19fQ=="),
        _2_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NDY5OGFkZDM5Y2Y5ZTRlYTkyZDQyZmFkZWZkZWMzYmU4YTdkYWZhMTFmYjM1OWRlNzUyZTlmNTRhZWNlZGM5YSJ9fX0="),
        _3_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZmQ5ZTRjZDVlMWI5ZjNjOGQ2Y2E1YTFiZjQ1ZDg2ZWRkMWQ1MWU1MzVkYmY4NTVmZTlkMmY1ZDRjZmZjZDIifX19"),
        _4_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZjJhM2Q1Mzg5ODE0MWM1OGQ1YWNiY2ZjODc0NjlhODdkNDhjNWMxZmM4MmZiNGU3MmY3MDE1YTM2NDgwNTgifX19"),
        _5_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDFmZTM2YzQxMDQyNDdjODdlYmZkMzU4YWU2Y2E3ODA5YjYxYWZmZDYyNDVmYTk4NDA2OTI3NWQxY2JhNzYzIn19fQ=="),
        _6_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "M2FiNGRhMjM1OGI3YjBlODk4MGQwM2JkYjY0Mzk5ZWZiNDQxODc2M2FhZjg5YWZiMDQzNDUzNTYzN2YwYTEifX19"),
        _7_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "Mjk3NzEyYmEzMjQ5NmM5ZTgyYjIwY2M3ZDE2ZTE2OGIwMzViNmY4OWYzZGYwMTQzMjRlNGQ3YzM2NWRiM2ZiIn19fQ=="),
        _8_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "YWJjMGZkYTlmYTFkOTg0N2EzYjE0NjQ1NGFkNjczN2FkMWJlNDhiZGFhOTQzMjQ0MjZlY2EwOTE4NTEyZCJ9fX0="),
        _9_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDZhYmM2MWRjYWVmYmQ1MmQ5Njg5YzA2OTdjMjRjN2VjNGJjMWFmYjU2YjhiMzc1NWU2MTU0YjI0YTVkOGJhIn19fQ=="),
        dot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "MzIzZTYxOWRjYjc1MTFjZGMyNTJhNWRjYTg1NjViMTlkOTUyYWM5ZjgyZDQ2N2U2NmM1MjI0MmY5Y2Q4OGZhIn19fQ=="),
        dotdot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "MmZmY2MzMThjMjEyZGM3NDliNTk5NzU1ZTc2OTdkNDkyMzgyOTkzYzA3ZGUzZjhlNTRmZThmYzdkZGQxZSJ9fX0="),
        up("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NGIyMjFjYjk2MDdjOGE5YmYwMmZlZjVkNzYxNGUzZWIxNjljYzIxOWJmNDI1MGZkNTcxNWQ1ZDJkNjA0NWY3In19fQ=="),
        down("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDhhYWI2ZDlhMGJkYjA3YzEzNWM5Nzg2MmU0ZWRmMzYzMTk0Mzg1MWVmYzU0NTQ2M2Q2OGU3OTNhYjQ1YTNkMyJ9fX0="),
        none(""),
        ;

        public final String value;

        Texture(final String texture) {
            this.value = texture;
        }
    }

    /**
     * @param current текущее lore. null - создать новое
     * @param text    текст. (br в > скобках)- перенос строки. <br>пробел или |
     *                -возможный перенос
     * @param color   null или осносной цвет текста
     * @return
     */
    public static List<Component> lore(@Nullable List<Component> current, final String text, @Nullable String color) {
        if (current == null) current = new ArrayList<>();
        if (text == null) return current;
        final Matcher regexMatcher = regex.matcher(text);
        while (regexMatcher.find()) {
            current.add(TCUtil.form(color == null ? regexMatcher.group() : color + regexMatcher.group()));
        }
    /*final String[] блоки = text.replace('&', '§');
    //else блоки = {text};
    for (final String блок : блоки) {
      final List<String> нарезка = split(блок, 25);
      for (String строчка : нарезка) {
        current.add(clr + строчка);
      }
    }*/
//Ostrov.log("genLore current="+current);
        return current;
    }

    public static List<String> genLore(@Nullable List<String> current, final String text, @Nullable final String color) {
        if (current == null) current = new ArrayList<>();
        final String clr = color == null ? "§7" : color;

        final String[] blocks = text.replace('&', '§').split("<br>");
        //else блоки = {text};
        for (final String блок : blocks) {
            final List<String> split = split(блок, 25);
            for (String строчка : split) {
                current.add(clr + строчка);
            }
        }
//Ostrov.log("genLore current="+current);
        return current;
    }

    //не менять! именно List<Component> !
    public static List<Component> genLore(@Nullable List<Component> current, @Nullable final String text) {
        if (current == null) current = new ArrayList<>();
        if (text == null) return current;

        final String[] blocks = text.replace('&', '§').split("<br>");
        for (final String block : blocks) {
            if (block.length() <= 25) {
                current.add(TCUtil.form(block));
            } else {
                final List<String> split = split(block, 25);
                for (String line : split) {
                    current.add(TCUtil.form(line));
                }
            }
        }
        return current;
    }


    @Deprecated
    public static List<String> split(String block, int lineLenght) {
        List<String> split = new ArrayList<>();
        if (block.length() <= lineLenght) {
            split.add(block);
            return split;
        }

        boolean nextLine = false;
        //int index = 0;
        int currentLineLenght = lineLenght;

        StringBuilder sb = new StringBuilder();
        char[] blockArray = block.toCharArray();

        for (int position = 0; position < blockArray.length; position++) {
//System.out.println("111 index="+index+"  position="+position+" char="+блок_array[position] );

            if (blockArray[position] == '§') {
//System.out.println("skip § 111 position="+position );
                sb.append(blockArray[position]);
                //position++;
                currentLineLenght++;
                position++;
                sb.append(blockArray[position]);
                currentLineLenght++;
                //System.out.println("skip § 222 position="+position );
            } else {
//System.out.println("222 index="+index+"  position="+position );
                if (position != 0 && position % currentLineLenght == 0) {
//System.out.println("nextLine 111 position="+position+"  current_line_lenght="+current_line_lenght );
                    nextLine = true;
                }
                if (nextLine && (blockArray[position] == ' ' || blockArray[position] == '|' || blockArray[position] == ',' || blockArray[position] == '.')) {
                    nextLine = false;
                    split.add(sb.toString());
                    //index++;
                    sb = new StringBuilder();
                    currentLineLenght = lineLenght;
//System.out.println("nextLine 222 index="+index+" position="+position+"  current_line_lenght="+current_line_lenght );
                } else {
                    sb.append(blockArray[position]);
                }
            }
        }
        split.add(sb.toString()); //добавляем, что осталось

        return split;
    }

    public static boolean getItems(Player player, int count, Material mat) {
        final Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);

        int found = 0;
        for (ItemStack stack : ammo.values()) {
            found += stack.getAmount();
        }
        if (count > found) {
            return false;
        }

        for (final Entry<Integer, ? extends ItemStack> en : ammo.entrySet()) {
            ItemStack stack = en.getValue();
            int removed = Math.min(count, stack.getAmount());
            count -= removed;
            if (stack.getAmount() == removed) {
                player.getInventory().setItem(en.getKey(), null);
            } else {
                stack.setAmount(stack.getAmount() - removed);
            }
            if (count <= 0) {
                break;
            }
        }

        player.updateInventory();
        return true;
    }

    public static void substractItemInHand(final Player p, final EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND) {
            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                p.getInventory().setItemInMainHand(air);
            } else {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else if (hand == EquipmentSlot.OFF_HAND) {
            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                p.getInventory().setItemInOffHand(air);
            } else {
                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
            }
        }
    }

    public static boolean substractOneItem(final HumanEntity he, final Material mat) {
        //if (!he.getInventory().contains(mat)) {бессмысленно, там тоже делается обход циклом
        //    return false;
        //}
        ItemStack is;
        for (int i = 0; i < he.getInventory().getContents().length; i++) {
            is = he.getInventory().getContents()[i];
            if (is != null && is.getType() == mat) {
                if (is.getAmount() >= 2) {
                    is.setAmount(is.getAmount() - 1);//he.getInventory().getContents()[i].setAmount(he.getInventory().getContents()[i].getAmount() - 1);
                } else {
                    is = air;//he.getInventory().getContents()[i].setAmount(0);
                }
                he.getInventory().setItem(i, is);
                return true;
            }
        }
        return false;
    }

    public static boolean substractAllItems(final HumanEntity he, final Material mat) {
        if (!he.getInventory().contains(mat)) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < he.getInventory().getContents().length; i++) {
            if (he.getInventory().getContents()[i] != null && he.getInventory().getContents()[i].getType() == mat) {
                he.getInventory().getContents()[i].setAmount(0);
                result = true;
            }
        }
        return result;
    }

    public static boolean substractItem(final Player he, final Material mat, int ammount) {
        if (getItemCount(he, mat) < ammount) {
            return false;
        }
        final ItemStack[] cloneInv = new ItemStack[he.getInventory().getContents().length];// = playerInvClone.getContents();
        ItemStack toClone;
        for (int slot = 0; slot < he.getInventory().getContents().length; slot++) {
            toClone = he.getInventory().getContents()[slot];
            cloneInv[slot] = toClone == null ? null : toClone.clone();
        }
        for (int slot = 0; slot < cloneInv.length; slot++) {
            if (cloneInv[slot] != null && mat == cloneInv[slot].getType()) {
                if (cloneInv[slot].getAmount() == ammount) { //найдено и убрано - дальше не ищем
                    cloneInv[slot] = air.clone();
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break;
                }

                if (cloneInv[slot].getAmount() > ammount) { //найдено больше чем надо - дальше не ищем
                    cloneInv[slot].setAmount(cloneInv[slot].getAmount() - ammount);
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break;
                }

                if (cloneInv[slot].getAmount() < ammount) { //найдено меньше чем надо - убавили требуемое и ищем дальше
                    ammount -= cloneInv[slot].getAmount();
                    //itemFindResult.put(mat, ammount);
                    cloneInv[slot] = air.clone();
                }
            }
        }
        if (ammount == 0) {//if (itemFindResult.isEmpty()) {
            he.getInventory().setContents(cloneInv);
            he.updateInventory();
            return true;
        }
        return false;
    }

    public static int getItemCount(final HumanEntity he, final Material mat) {
        int result = 0;
        for (final ItemStack slot : he.getInventory().getContents()) {
            if (slot != null && slot.getType() == mat) {
                result += slot.getAmount();
            }
        }
        return result;
    }

    public static int repairAll(final Player p) {
        int repaired = 0; //Set <String> repaired = new HashSet<String>() {};
        for (final ItemStack item : p.getInventory()) {
            if (isBlank(item, false) || !hasDur(item)) continue;
            setDur(item, maxDur(item));
            repaired++;
        }

        p.updateInventory();
        return repaired;
    }

    public static boolean hasDur(final ItemStack it) {
        final Integer dmg = it.getData(DataComponentTypes.DAMAGE);
        return dmg != null && dmg != 0;
    }

    public static int maxDur(final ItemStack it) {
        final Integer max = it.getData(DataComponentTypes.MAX_DAMAGE);
        return max == null ? it.getType().asItemType().getMaxDurability() : max;
    }

    public static void setDur(final ItemStack it, final int d) {
        final int max = maxDur(it);
        if (d == max) {
            it.unsetData(DataComponentTypes.DAMAGE);
            return;
        }
        it.setData(DataComponentTypes.DAMAGE, Math.max(0, max - d));
    }

    public static boolean is(final ItemStack item, final ItemType type) {
        return item != null && type.equals(item.getType().asItemType());
    }

    public static boolean isBlank(final ItemStack item, final boolean checkData) {
        if (item == null) return true;
        final ItemType tp = item.getType().asItemType();
        if (tp == ItemType.AIR) return true;
        return checkData && item.getDataTypes().size() > tp.getDefaultDataTypes().size();
    }

    public static boolean hasName(final ItemStack is) {
        return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName();
    }

    public static String getName(final ItemStack is) {
        return hasName(is) ? TCUtil.deform(is.getItemMeta().displayName()) : "";
    }

    public static void giveItemsTo(final Player p, final ItemStack... its) {
        boolean left = false;
        for (final ItemStack it : p.getInventory().addItem(its).values()) {
            p.getWorld().dropItem(p.getLocation(), it);
            left = true;
        }
        if (left) {
            ScreenUtil.sendActionBarDirect(p, "§4В твоем инвентаре не было места, предмет выпал рядом!");
        }
    }

    public static boolean compare(@Nullable final ItemStack is1, @Nullable final ItemStack is2, final Stat... depth) {
        if (is1 == null || is2 == null) return is1 == is2;

        for (final Stat s : depth) {
            if (!testStat(is1, is2, s)) return false;
        }
        return true;
    }

    public enum Stat {TYPE, AMOUNT, NAME, LORE, DAMAGE, MODEL, PDC}

    private static boolean testStat(final ItemStack is1, final ItemStack is2, final Stat s) {
        return switch (s) {
            case AMOUNT -> is1.getAmount() == is2.getAmount();
            case TYPE -> is1.getType().asItemType().equals(is2.getType().asItemType());
            case DAMAGE -> Objects.equals(is1.getData(DataComponentTypes.DAMAGE), is2.getData(DataComponentTypes.DAMAGE))
                && Objects.equals(is1.getData(DataComponentTypes.MAX_DAMAGE), is2.getData(DataComponentTypes.MAX_DAMAGE));
            case NAME -> TCUtil.compare(is1.getData(DataComponentTypes.ITEM_NAME), is2.getData(DataComponentTypes.ITEM_NAME))
                && TCUtil.compare(is1.getData(DataComponentTypes.CUSTOM_NAME), is2.getData(DataComponentTypes.CUSTOM_NAME));
            case MODEL -> Objects.equals(is1.getData(DataComponentTypes.ITEM_MODEL), is2.getData(DataComponentTypes.ITEM_MODEL));
            case LORE -> {
                final ItemLore lore1 = is1.getData(DataComponentTypes.LORE);
                final ItemLore lore2 = is2.getData(DataComponentTypes.LORE);
                if (lore1 == null || lore2 == null)
                    if (!Objects.equals(lore1, lore2)) yield false;

                final List<Component> l1 = lore1.lines();
                final List<Component> l2 = lore2.lines();
                final int size = l1.size();
                if (size != l2.size()) yield false;
                for (int i = 0; i != size; i++) {
                    if (!TCUtil.compare(l1.get(i), l2.get(i)))
                        yield false;
                }
                yield true;
            }
            case PDC -> {
                final PersistentDataContainerView pdc1 = is1.getPersistentDataContainer();
                final PersistentDataContainerView pdc2 = is2.getPersistentDataContainer();
                final Set<NamespacedKey> keys = new HashSet<>();
                keys.addAll(pdc1.getKeys()); keys.addAll(pdc1.getKeys());
                for (final NamespacedKey k : keys) {
                    if (!Objects.equals(pdc1.get(k, PersistentDataType.STRING),
                        pdc2.get(k, PersistentDataType.STRING))) yield false;
                }
                yield true;
            }
        };
    }

    @Deprecated
    public static boolean compareItem(@Nullable final ItemStack is1, @Nullable final ItemStack is2, final boolean checkLore) {
        return checkLore ? compare(is1, is2, Stat.TYPE, Stat.NAME, Stat.LORE) : compare(is1, is2, Stat.TYPE, Stat.NAME);
    }

    public static void fillSign(final Sign sign, String suggest) {
        if (suggest == null || suggest.isEmpty()) {
            return;
        }
        final SignSide sd = sign.getSide(Side.FRONT);
        for (int ln = 0; !suggest.isEmpty() && ln < 4; ln++) {
            if (suggest.length() > 15) {
                sd.line(ln, TCUtil.form(suggest.substring(0, 15)));
                suggest = suggest.substring(15);
                continue;
            }

            sd.line(ln, TCUtil.form(suggest));
            break;
        }
        sign.update();
    }

    public static double getTrimMod(final ItemStack ti, final Attribute atr) {
        if (ti == null) return 0d;

        switch (ti.getType()) {
            case IRON_INGOT -> {//more defense, less mobility
                if (atr.equals(ARMOR)) return 0.2d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(MOVEMENT_SPEED)) return -0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.2d;
            }
            case COPPER_INGOT -> {//more kick, less mining
                if (atr.equals(ATTACK_KNOCKBACK)) return 0.2d;
                else if (atr.equals(JUMP_STRENGTH)) return 0.1d;
                else if (atr.equals(BLOCK_BREAK_SPEED)) return -0.1d;
                else if (atr.equals(GRAVITY)) return -0.1d;
            }
            case GOLD_INGOT -> {//more health, less light
                if (atr.equals(MAX_HEALTH)) return 0.1d;
                else if (atr.equals(GRAVITY)) return 0.1d;
                else if (atr.equals(SNEAKING_SPEED)) return -0.1d;
                else if (atr.equals(BLOCK_BREAK_SPEED)) return 0.1d;
            }
            case AMETHYST_SHARD -> {//more attack, less defense
                if (atr.equals(ATTACK_DAMAGE)) return 0.1d;
                else if (atr.equals(ARMOR)) return -0.2d;
                else if (atr.equals(BLOCK_INTERACTION_RANGE)) return 0.1d;
                else if (atr.equals(ENTITY_INTERACTION_RANGE)) return 0.1d;
            }
            case DIAMOND -> {//buffs armor and damage
                if (atr.equals(ARMOR)) return 0.05d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(ATTACK_DAMAGE)) return 0.05d;
                else if (atr.equals(KNOCKBACK_RESISTANCE)) return -0.1d;
            }
            case EMERALD -> {//more mobility, less damage
                if (atr.equals(MOVEMENT_SPEED)) return 0.1d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(ATTACK_DAMAGE)) return -0.1d;
                else if (atr.equals(JUMP_STRENGTH)) return 0.1d;
            }
            case REDSTONE -> {//more bulk, less kick
                if (atr.equals(MAX_HEALTH)) return 0.1d;
                else if (atr.equals(SCALE)) return 0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.1d;
                else if (atr.equals(JUMP_STRENGTH)) return -0.1d;
            }
            case LAPIS_LAZULI -> {//more mobility, less swing
                if (atr.equals(SNEAKING_SPEED)) return 0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return 0.2d;
                else if (atr.equals(ATTACK_SPEED)) return -0.1d;
                else if (atr.equals(GRAVITY)) return -0.1d;
            }
            case NETHERITE_INGOT -> {//more toughness, less hp
                if (atr.equals(ARMOR)) return 0.1d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.4d;
                else if (atr.equals(MAX_HEALTH)) return -0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.1d;
            }
            case QUARTZ -> {//more damage, less haste
                if (atr.equals(ATTACK_DAMAGE)) return 0.2d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return -0.1d;
                else if (atr.equals(ATTACK_SPEED)) return -0.1d;
                else if (atr.equals(SNEAKING_SPEED)) return -0.1d;
            }
        }
        return 0d;
    }

}