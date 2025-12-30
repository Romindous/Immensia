package ru.immensia.utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.items.DataParser;
import ru.immensia.items.PDC;
import ru.immensia.objects.Duo;
import ru.immensia.utils.strings.StringUtil;
import ru.immensia.utils.strings.TCUtil;
import ru.immensia.utils.versions.Nms;

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
            if (compare(p.getInventory().getContents()[i], item, Stat.TYPE, Stat.NAME, Stat.MODEL)) {
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
    private static final StringUtil.Split[] seps = StringUtil.Split.values();
    private static final DataParser parsers = createParser();
    private static DataParser createParser() {
        final DataParser dataParser = new DataParser();
        dataParser.put(DataParser.PDC_TYPE, new DataParser.Parser<>() {
            public String write(final PDC.Data val) {
                final StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (final Duo<NamespacedKey, Serializable> en : val) {
                    if (first) first = false;
                    else sb.append(seps[1].get());
                    sb.append(en.key().asMinimalString())
                        .append(seps[2].get()).append(en.val());
                }
                return sb.toString();
            }

            public PDC.Data parse(final String str) {
                final String[] parts = seps[1].split(str);
                final PDC.Data bld = new PDC.Data();
                for (final String p : parts) {
                    final String[] mod = seps[2].split(p);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(NamespacedKey.fromString(mod[0]), mod[1]);
                }
                return bld;
            }
        });
        dataParser.put(DataComponentTypes.ATTACK_RANGE, new DataParser.Parser<>() {
            public String write(final AttackRange val) {
                return String.join(seps[1].get(), StringUtil.toSigFigs(val.minReach(), (byte) 2), StringUtil.toSigFigs(val.maxReach(), (byte) 2),
                    StringUtil.toSigFigs(val.minCreativeReach(), (byte) 2), StringUtil.toSigFigs(val.maxCreativeReach(), (byte) 2),
                    StringUtil.toSigFigs(val.hitboxMargin(), (byte) 2), StringUtil.toSigFigs(val.mobFactor(), (byte) 2));
            }

            public AttackRange parse(final String str) {
                final String[] parts = seps[1].split(str);
                final AttackRange.Builder bld = AttackRange.attackRange();
                if (!ClassUtil.check(parts, 6, false)) return bld.build();
                return bld.minReach(NumUtil.floatOf(parts[0], 0f)).maxReach(NumUtil.floatOf(parts[1], 3f))
                    .minCreativeReach(NumUtil.floatOf(parts[2], 0f)).maxCreativeReach(NumUtil.floatOf(parts[3], 5f))
                    .hitboxMargin(NumUtil.floatOf(parts[4], 0.3f)).mobFactor(NumUtil.floatOf(parts[5], 1.0f)).build();
            }
        });
        dataParser.put(DataComponentTypes.DEATH_PROTECTION, new DataParser.Parser<>() {
            public String write(final DeathProtection val) {
                return StringUtil.NA;
            }

            public DeathProtection parse(final String str) {
                return DeathProtection.deathProtection().build();
            }
        });
        dataParser.put(DataComponentTypes.KINETIC_WEAPON, new DataParser.Parser<>() {
            public String write(final KineticWeapon val) {
                final Key snd = val.sound(), hsd = val.hitSound();
                return String.join(seps[1].get(), String.valueOf(val.delayTicks()), String.valueOf(val.contactCooldownTicks()),
                    StringUtil.toSigFigs(val.forwardMovement(), (byte) 2), StringUtil.toSigFigs(val.damageMultiplier(), (byte) 2),
                    snd == null ? StringUtil.NA : ofKey(snd), hsd == null ? StringUtil.NA : ofKey(hsd), fromCond(val.damageConditions()),
                    fromCond(val.dismountConditions()), fromCond(val.knockbackConditions()));
            }

            private String fromCond(final KineticWeapon.Condition cnd) {
                return cnd == null ? StringUtil.NA : String.join(seps[2].get(), String.valueOf(cnd.maxDurationTicks()),
                    StringUtil.toSigFigs(cnd.minSpeed(), (byte) 2), StringUtil.toSigFigs(cnd.minRelativeSpeed(), (byte) 2));
            }

            public KineticWeapon parse(final String str) {
                final String[] parts = seps[1].split(str);
                final KineticWeapon.Builder bld = KineticWeapon.kineticWeapon();
                if (!ClassUtil.check(parts, 9, false)) return bld.build();
                return bld.delayTicks(NumUtil.intOf(parts[0], 0)).contactCooldownTicks(NumUtil.intOf(parts[1], 0))
                    .forwardMovement(NumUtil.floatOf(parts[2], 0f)).damageMultiplier(NumUtil.floatOf(parts[3], 1f))
                    .sound(StringUtil.isNA(parts[4]) ? null : Key.key(parts[4])).sound(StringUtil.isNA(parts[5]) ? null : Key.key(parts[5]))
                    .damageConditions(parceCond(parts[6])).dismountConditions(parceCond(parts[7])).knockbackConditions(parceCond(parts[8])).build();
            }

            private KineticWeapon.Condition parceCond(final String cnd) {
                if (StringUtil.isNA(cnd)) return null;
                final String[] mods = seps[2].split(cnd);
                return KineticWeapon.condition(NumUtil.intOf(mods[0], 0),
                    NumUtil.floatOf(mods[1], 0f), NumUtil.floatOf(mods[2], 0f));
            }
        });
        dataParser.put(DataComponentTypes.SWING_ANIMATION, new DataParser.Parser<>() {
            public String write(final SwingAnimation val) {
                return val.type().name() + seps[1].get() + val.duration();
            }

            public SwingAnimation parse(final String str) {
                final String[] parts = seps[1].split(str);
                final SwingAnimation.Builder bld = SwingAnimation.swingAnimation();
                if (!ClassUtil.check(parts, 2, false)) return bld.build();
                return bld.type(SwingAnimation.Animation.valueOf(parts[0]))
                    .duration(NumUtil.intOf(parts[1], 0)).build();
            }
        });
        dataParser.put(DataComponentTypes.MINIMUM_ATTACK_CHARGE, new DataParser.Parser<>() {
            public String write(final Float val) {
                return val.toString();
            }

            public Float parse(final String str) {
                return NumUtil.floatOf(str, 0f);
            }
        });
        dataParser.put(DataComponentTypes.TOOLTIP_STYLE, new DataParser.Parser<>() {
            public String write(final Key val) {
                return val.asMinimalString();
            }

            public Key parse(final String str) {
                return Key.key(str);
            }
        });
        dataParser.put(DataComponentTypes.ITEM_MODEL, new DataParser.Parser<>() {
            public String write(final Key val) {
                return val.asMinimalString();
            }

            public Key parse(final String str) {
                return Key.key(str);
            }
        });
        dataParser.put(DataComponentTypes.ATTRIBUTE_MODIFIERS, new DataParser.Parser<>() {
            public String write(final ItemAttributeModifiers val) {
                final StringBuilder sb = new StringBuilder();
                for (final ItemAttributeModifiers.Entry ie : val.modifiers()) {
                    final AttributeModifier mod = ie.modifier();
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), ofRegKey(Main.registries.ATTRIBS, ie.attribute()), ofKey(mod),
                        StringUtil.toSigFigs(mod.getAmount(), (byte) 4), mod.getOperation().name(), mod.getSlotGroup().toString()));
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }

            public ItemAttributeModifiers parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemAttributeModifiers.Builder bld = ItemAttributeModifiers.itemAttributes();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 5, false)) continue;
                    bld.addModifier(IStrap.get(Key.key(mod[0]), LUCK),
                        new AttributeModifier(NamespacedKey.fromString(mod[1]), NumUtil.doubleOf(mod[2], 0d),
                            AttributeModifier.Operation.valueOf(mod[3]), EquipmentSlotGroup.getByName(mod[4])));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.DAMAGE, new DataParser.Parser<>() {
            public String write(final Integer val) {
                return val.toString();
            }

            public Integer parse(final String str) {
                return NumUtil.intOf(str, 0);
            }
        });
        dataParser.put(DataComponentTypes.ITEM_NAME, new DataParser.Parser<>() {
            public String write(final Component val) {
                return TCUtil.deform(val);
            }

            public Component parse(final String str) {
                return TCUtil.form(str);
            }
        });
        dataParser.put(DataComponentTypes.CUSTOM_NAME, new DataParser.Parser<>() {
            public String write(final Component val) {
                return TCUtil.deform(val);
            }

            public Component parse(final String str) {
                return TCUtil.form(str);
            }
        });
        dataParser.put(DataComponentTypes.LORE, new DataParser.Parser<>() {
            public String write(final ItemLore val) {
                return String.join(seps[1].get(), val.lines().stream().map(TCUtil::deform).toArray(i -> new String[i]));
            }

            public ItemLore parse(final String str) {
                return ItemLore.lore(Arrays.stream(seps[1].split(str)).map(TCUtil::form).toList());
            }
        });
        dataParser.put(DataComponentTypes.DYED_COLOR, new DataParser.Parser<>() {
            public String write(final DyedItemColor val) {
                return String.valueOf(val.color().asARGB());
            }

            public DyedItemColor parse(final String str) {
                final String[] parts = seps[1].split(str);
                final DyedItemColor.Builder bld = DyedItemColor.dyedItemColor();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                return bld.color(Color.fromARGB(NumUtil.intOf(parts[parts.length - 1], 0))).build();
            }
        });
        dataParser.put(DataComponentTypes.CONSUMABLE, new DataParser.Parser<>() {
            public String write(final Consumable val) {
                return String.join(seps[1].get(), val.animation().name(),
                    StringUtil.toSigFigs(val.consumeSeconds(), (byte) 2),
                    String.valueOf(val.hasConsumeParticles()));
            }

            public Consumable parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Consumable.Builder bld = Consumable.consumable();
                if (!ClassUtil.check(parts, 3, false)) return bld.build();
                return bld.animation(ItemUseAnimation.valueOf(parts[0]))
                    .consumeSeconds(NumUtil.floatOf(parts[1], 0f))
                    .hasConsumeParticles(Boolean.parseBoolean(parts[2])).build();
            }
        });
        dataParser.put(DataComponentTypes.DAMAGE_RESISTANT, new DataParser.Parser<>() {
            public String write(final DamageResistant val) {
                return ofKey(val.types());
            }

            public DamageResistant parse(final String str) {
                return DamageResistant.damageResistant(TagKey.create(RegistryKey.DAMAGE_TYPE, Key.key(str)));
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTABLE, new DataParser.Parser<>() {
            public String write(final Enchantable val) {
                return String.valueOf(val.value());
            }

            public Enchantable parse(final String str) {
                return Enchantable.enchantable(NumUtil.intOf(str, 0));
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, new DataParser.Parser<>() {
            public String write(final Boolean val) {
                return val.toString();
            }

            public Boolean parse(final String str) {
                return Boolean.parseBoolean(str);
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTMENTS, new DataParser.Parser<>() {
            public String write(final ItemEnchantments val) {
                final StringBuilder sb = new StringBuilder();
                for (final Map.Entry<Enchantment, Integer> ie : val.enchantments().entrySet()) {
                    sb.append(seps[1].get()).append(ofKey(ie.getKey()))
                        .append(seps[2].get()).append(ie.getValue().intValue());
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }

            public ItemEnchantments parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemEnchantments.Builder bld = ItemEnchantments.itemEnchantments();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(IStrap.get(Key.key(mod[0]), Enchantment.AQUA_AFFINITY), NumUtil.intOf(mod[1], 0));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.STORED_ENCHANTMENTS, new DataParser.Parser<>() {
            public String write(final ItemEnchantments val) {
                final StringBuilder sb = new StringBuilder();
                for (final Map.Entry<Enchantment, Integer> ie : val.enchantments().entrySet()) {
                    sb.append(seps[1].get()).append(ofKey(ie.getKey()))
                        .append(seps[2].get()).append(ie.getValue().intValue());
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }

            public ItemEnchantments parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemEnchantments.Builder bld = ItemEnchantments.itemEnchantments();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(IStrap.get(Key.key(mod[0]), Enchantment.AQUA_AFFINITY), NumUtil.intOf(mod[1], 0));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.EQUIPPABLE, new DataParser.Parser<>() {
            public String write(final Equippable val) {
                final Key model = val.assetId();
                return String.join(seps[1].get(), val.slot().name(), model == null ? StringUtil.NA : model.asMinimalString(), String.valueOf(val.damageOnHurt()),
                    String.valueOf(val.dispensable()), String.valueOf(val.swappable()), val.equipSound().asMinimalString());
            }

            public Equippable parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Equippable.Builder bld = Equippable.equippable(EquipmentSlot.valueOf(parts[0]));
                if (!ClassUtil.check(parts, 6, false)) return bld.build();
                if (!StringUtil.isNA(parts[1])) bld.assetId(Key.key(parts[1]));
                return bld.damageOnHurt(Boolean.parseBoolean(parts[2])).dispensable(Boolean.parseBoolean(parts[3]))
                    .swappable(Boolean.parseBoolean(parts[4])).equipSound(Key.key(parts[5])).build();
            }
        });
        dataParser.put(DataComponentTypes.FIREWORKS, new DataParser.Parser<>() {
            public String write(final Fireworks val) {
                final StringBuilder sb = new StringBuilder();
                sb.append(val.flightDuration());
                for (final FireworkEffect fe : val.effects()) {
                    final List<Color> clrs = fe.getColors();
                    final Color clr = clrs.isEmpty() ? Color.WHITE : clrs.getFirst();
                    final List<Color> fds = fe.getFadeColors();
                    final Color fd = fds.isEmpty() ? clr : fds.getFirst();
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), fe.getType().name(), String.valueOf(clr.asARGB()),
                        String.valueOf(fd.asARGB()), String.valueOf(fe.hasFlicker()), String.valueOf(fe.hasTrail())));
                }
                return sb.toString();
            }

            public Fireworks parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Fireworks.Builder bld = Fireworks.fireworks();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                bld.flightDuration(NumUtil.intOf(parts[0], 1));
                for (int i = 1; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 5, false)) continue;
                    bld.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.valueOf(mod[0]))
                        .withColor(Color.fromARGB(NumUtil.intOf(mod[1], 0))).withFade(Color.fromARGB(NumUtil.intOf(mod[2], 0)))
                        .flicker(Boolean.parseBoolean(mod[3])).trail(Boolean.parseBoolean(mod[4])).build());
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.FOOD, new DataParser.Parser<>() {
            public String write(final FoodProperties val) {
                return String.join(seps[1].get(), String.valueOf(val.nutrition()),
                    StringUtil.toSigFigs(val.saturation(), (byte) 2), String.valueOf(val.canAlwaysEat()));
            }

            public FoodProperties parse(final String str) {
                final String[] parts = seps[1].split(str);
                final FoodProperties.Builder bld = FoodProperties.food();
                if (!ClassUtil.check(parts, 3, false)) return bld.build();
                return bld.nutrition(NumUtil.intOf(parts[0], 0)).saturation(NumUtil.floatOf(parts[1], 0f))
                    .canAlwaysEat(Boolean.parseBoolean(parts[2])).build();
            }
        });
        dataParser.put(DataComponentTypes.USE_COOLDOWN, new DataParser.Parser<>() {
            public String write(final UseCooldown val) {
                final Key key = val.cooldownGroup();
                return val.seconds() + seps[1].get() + (key == null ? StringUtil.NA : key.asMinimalString());
            }

            public UseCooldown parse(final String str) {
                final String[] parts = seps[1].split(str);
                final UseCooldown.Builder bld = UseCooldown.useCooldown(NumUtil.floatOf(parts[0], 0f));
                if (!ClassUtil.check(parts, 2, false)) return bld.build();
                if (!StringUtil.isNA(parts[1])) bld.cooldownGroup(Key.key(parts[1]));
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.TRIM, new DataParser.Parser<>() {
            public String write(final ItemArmorTrim val) {
                return ofKey(val.armorTrim().getMaterial()) + seps[1].get() + ofKey(val.armorTrim().getPattern());
            }

            public ItemArmorTrim parse(final String str) {
                final String[] parts = seps[1].split(str);
                if (!ClassUtil.check(parts, 2, true)) return null;
                return ItemArmorTrim.itemArmorTrim(new ArmorTrim(IStrap.get(Key.key(parts[parts.length - 2]), TrimMaterial.IRON),
                    IStrap.get(Key.key(parts[parts.length - 1]), TrimPattern.COAST))).build();
            }
        });
        dataParser.put(DataComponentTypes.MAX_DAMAGE, new DataParser.Parser<>() {
            public String write(final Integer val) {
                return val.toString();
            }

            public Integer parse(final String str) {
                return NumUtil.intOf(str, 1);
            }
        });
        dataParser.put(DataComponentTypes.POTION_CONTENTS, new DataParser.Parser<>() {
            public String write(final PotionContents val) {
                final StringBuilder sb = new StringBuilder();
                final Color clr = val.customColor();
                sb.append(ofKey(val.potion())).append(seps[1].get()).append(clr == null ? StringUtil.NA : clr.asARGB());
                for (final PotionEffect pe : val.customEffects()) {
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), ofKey(pe.getType()), String.valueOf(pe.getDuration()),
                        String.valueOf(pe.getAmplifier()), String.valueOf(pe.hasParticles() && pe.hasIcon())));
                }
                return sb.toString();
            }

            public PotionContents parse(final String str) {
                final String[] parts = seps[1].split(str);
                final PotionContents.Builder bld = PotionContents.potionContents();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                bld.potion(Registry.POTION.get(Key.key(parts[0])));
                if (!StringUtil.isNA(parts[1])) bld.customColor(Color.fromARGB(NumUtil.intOf(parts[1], 0)));
                for (int i = 2; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 4, false)) continue;
                    final boolean vis = Boolean.parseBoolean(mod[3]);
                    bld.addCustomEffect(new PotionEffect(Registry.POTION_EFFECT_TYPE.get(Key.key(mod[0])),
                        NumUtil.intOf(mod[1], 0), NumUtil.intOf(mod[2], 0), !vis, vis, vis));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.MAX_STACK_SIZE, new DataParser.Parser<>() {
            public String write(final Integer val) {
                return val.toString();
            }

            public Integer parse(final String str) {
                return NumUtil.intOf(str, 1);
            }
        });
        dataParser.put(DataComponentTypes.REPAIRABLE, new DataParser.Parser<>() {
            public String write(final Repairable val) {
                return String.join(seps[1].get(), val.types().values().stream()
                    .map(tk -> ofKey(tk)).toArray(i -> new String[i]));
            }

            public Repairable parse(final String str) {
                return Repairable.repairable(IStrap.regSetOf(Arrays.stream(seps[1].split(str))
                    .map(Key::key).toList(), RegistryKey.ITEM));
            }
        });
        dataParser.put(DataComponentTypes.RARITY, new DataParser.Parser<>() {
            public String write(final ItemRarity val) {
                return val.name();
            }

            public ItemRarity parse(final String str) {
                return ItemRarity.valueOf(str);
            }
        });
        dataParser.put(DataComponentTypes.TOOL, new DataParser.Parser<>() {
            public String write(final Tool val) {
                final StringBuilder sb = new StringBuilder();
                sb.append(StringUtil.toSigFigs(val.defaultMiningSpeed(), (byte) 2))
                    .append(seps[1].get()).append(val.damagePerBlock());
                for (final Tool.Rule rl : val.rules()) {
                    if (rl.speed() == null) continue;
                    final List<String> rls = new ArrayList<>();
                    rls.add(StringUtil.toSigFigs(rl.speed(), (byte) 2));
                    rls.add(rl.correctForDrops().name());
                    rls.addAll(rl.blocks().values().stream().map(tk -> ofKey(tk)).toList());
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), rls.toArray(new String[0])));
                }
                return sb.toString();
            }

            public Tool parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Tool.Builder bld = Tool.tool();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                bld.defaultMiningSpeed(NumUtil.floatOf(parts[0], 1)).damagePerBlock(NumUtil.intOf(parts[1], 0));
                for (int i = 2; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, true)) continue;
                    final List<Key> bks = new ArrayList<>(mod.length - 2);
                    for (int j = 2; j != mod.length; j++) {
                        bks.add(Key.key(mod[j]));
                    }
                    bld.addRule(Tool.rule(IStrap.regSetOf(bks, RegistryKey.BLOCK),
                        NumUtil.floatOf(mod[0], 0f), TriState.valueOf(mod[1])));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.TOOLTIP_DISPLAY, new DataParser.Parser<>() {
            public String write(final TooltipDisplay val) {
                final StringBuilder sb = new StringBuilder(String.valueOf(val.hideTooltip()));
                for (final DataComponentType dtc : val.hiddenComponents()) {
                    sb.append(seps[1].get()).append(ofKey(dtc));
                }
                return sb.toString();
            }

            public TooltipDisplay parse(final String str) {
                final String[] parts = seps[1].split(str);
                final TooltipDisplay.Builder bld = TooltipDisplay.tooltipDisplay();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                bld.hideTooltip(Boolean.parseBoolean(parts[0]));
                final Set<DataComponentType> dtcs = new HashSet<>();
                for (int i = 1; i != parts.length; i++) {
                    dtcs.add(IStrap.get(Key.key(parts[i]), DataComponentTypes.BREAK_SOUND));
                }
                return bld.hiddenComponents(dtcs).build();
            }
        });
        dataParser.put(DataComponentTypes.PROFILE, new DataParser.Parser<>() {
            public String write(final ResolvableProfile val) {
                final StringBuilder sb = new StringBuilder(val.name() == null ? StringUtil.NA : val.name());
                sb.append(seps[1].get()).append(val.uuid() == null ? StringUtil.NA : val.uuid().toString());
                for (final ProfileProperty pp : val.properties()) {
                    final String sig = pp.getSignature();
                    if (sig == null)
                        sb.append(seps[1].get()).append(String.join(seps[2].get(), pp.getName(), pp.getValue()));
                    else sb.append(seps[1].get()).append(String.join(seps[2].get(), pp.getName(), pp.getValue(), sig));
                }
                return sb.toString();
            }

            public ResolvableProfile parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ResolvableProfile.Builder bld = ResolvableProfile.resolvableProfile();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                if (!StringUtil.isNA(parts[0])) bld.name(parts[0]);
                if (!StringUtil.isNA(parts[1])) bld.uuid(UUID.fromString(parts[1]));
                final List<ProfileProperty> dtcs = new ArrayList<>();
                for (int i = 2; i != parts.length; i++) {
                    final String[] pps = seps[2].split(parts[i]);
                    if (!ClassUtil.check(pps, 2, true)) return bld.build();
                    dtcs.add(new ProfileProperty(pps[0], pps[1],
                        pps.length == 3 ? pps[2] : null));
                }
                return bld.addProperties(dtcs).build();
            }
        });
        dataParser.put(DataComponentTypes.BLOCKS_ATTACKS, new DataParser.Parser<>() {
            public String write(final BlocksAttacks val) {
                return String.join(seps[1].get(), String.valueOf(val.blockDelaySeconds()),
                    String.valueOf(val.disableCooldownScale()),
                    val.bypassedBy() == null ? StringUtil.NA : val.bypassedBy().key().asMinimalString(),
                    val.blockSound() == null ? StringUtil.NA : val.blockSound().asMinimalString(),
                    val.disableSound() == null ? StringUtil.NA : val.disableSound().asMinimalString());
            }

            public BlocksAttacks parse(final String str) {
                final String[] parts = seps[1].split(str);
                final BlocksAttacks.Builder bld = BlocksAttacks.blocksAttacks();
                if (!ClassUtil.check(parts, 5, false)) return bld.build();
                bld.blockDelaySeconds(NumUtil.floatOf(parts[0], 0f));
                bld.disableCooldownScale(NumUtil.floatOf(parts[1], 1f));
                if (!StringUtil.isNA(parts[2])) bld.bypassedBy(TagKey
                    .create(RegistryKey.DAMAGE_TYPE, Key.key(parts[2])));
                if (!StringUtil.isNA(parts[3])) bld.blockSound(Key.key(parts[3]));
                if (!StringUtil.isNA(parts[4])) bld.disableSound(Key.key(parts[4]));
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.WEAPON, new DataParser.Parser<>() {
            public String write(final Weapon val) {
                return val.itemDamagePerAttack()
                    + seps[1].get() + val.disableBlockingForSeconds();
            }

            public Weapon parse(final String str) {
                final String[] parts = seps[1].split(str, true);
                final Weapon.Builder bld = Weapon.weapon();
                if (!ClassUtil.check(parts, 2, false)) return bld.build();
                return bld.itemDamagePerAttack(NumUtil.intOf(parts[0], 1))
                    .disableBlockingForSeconds(NumUtil.floatOf(parts[1], 0f)).build();
            }
        });
        return dataParser;
    }

    public static String write(final @Nullable ItemStack is) {
        if (is == null || ItemType.AIR.equals(is.getType().asItemType())) return "air";
        final StringBuilder res = new StringBuilder(ofKey(is.getType().asItemType()) + StringUtil.Split.MEDIUM.get() + is.getAmount());
        for (final DataComponentType dtc : is.getDataTypes()) {
            if (!is.isDataOverridden(dtc)) continue;
            switch (dtc) {
                case final DataComponentType.NonValued nvd -> {
                    if (nvd.key().value().equals(OLD_PDC)) continue;
                    res.append(StringUtil.Split.LARGE.get()).append(ofKey(nvd));
                }
                case final DataComponentType.Valued<?> vld -> append(is, res, vld);
                default -> {}
            }
        }
        final PersistentDataContainerView pdc = is.getPersistentDataContainer();
        if (pdc.isEmpty()) return res.toString();
        try {
            res.append(StringUtil.Split.LARGE.get()).append(PDC.ID)
                .append(StringUtil.Split.MEDIUM.get())
                .append(Base64Coder.encode(pdc.serializeToBytes()));
        } catch (IOException e) {}
        return res.toString();
    }

    public static ItemStack parse(final @Nullable String str) {
        if (str == null || str.startsWith("air")) return ItemType.AIR.createItemStack();
        final String[] split = seps[0].split(str);
        final String[] idt = seps[1].split(split[0]);
        final ItemType tp;
        try {
            tp = IStrap.get(Key.key(idt[0]), ItemType.AIR);
        } catch (InvalidKeyException e) {
            Main.log_err("Couldn't parse type for " + str);
            e.printStackTrace();
            return ItemType.AIR.createItemStack();
        }
        if (tp == ItemType.AIR) {
            Main.log_err("Failed parsing item type for " + str);
            return tp.createItemStack();
        }
        final ItemStack it = tp.createItemStack(idt.length == 2 ? NumUtil.intOf(idt[1], 1) : 1);
        String data = null;
        try {
            for (int i = 1; i != split.length; i++) {
                data = split[i];
                final String[] dsp = seps[1].split(data, true);
                if (dsp.length == 1) {
                    if (Registry.DATA_COMPONENT_TYPE.get(Key.key(data))
                        instanceof final DataComponentType.NonValued nvd) {
                        it.setData(nvd);
                    }
                    continue;
                }
                if (PDC.ID.equals(dsp[0])) {
                    it.editPersistentDataContainer(pdc -> {
                        try {pdc.readFromBytes(Base64Coder.decode(dsp[1]));}
                        catch (IOException | IllegalArgumentException e) {
                            Main.log_warn("Couldnt parse pdc of " + dsp[1] + ", trying old");
                            final DataParser.Parser<PDC.Data> prs = parsers.get(DataParser.PDC_TYPE);
                            if (prs != null) Nms.setCustomData(it, prs.parse(dsp[1]));
                        }
                    });
                    continue;
                }
                if (Registry.DATA_COMPONENT_TYPE.get(Key.key(dsp[0]))
                    instanceof final DataComponentType.Valued<?> vld) {
                    append(it, dsp[1], vld);
                }
            }
        } catch (NullPointerException | IllegalArgumentException | InvalidKeyException e) {
            Main.log_err("Couldn't parse data " + data);
            e.printStackTrace();
            return it;
        }
        return it;
    }

    private static <D> void append(final ItemStack it, final StringBuilder sb, final DataComponentType.Valued<D> dtc) {
        final D val = it.getData(dtc); if (val == null) return;
        final DataParser.Parser<D> prs = parsers.get(dtc); if (prs == null) return;
        sb.append(seps[0].get()).append(ofKey(dtc)).append(seps[1].get()).append(prs.write(val));
    }

    private static <D> void append(final ItemStack it, final String data, final DataComponentType.Valued<D> dtc) {
        final DataParser.Parser<D> prs = parsers.get(dtc); if (prs == null) return;
        final D pd = prs.parse(data);
        if (pd == null) {
            Main.log_warn("Couldnt parse '" + data + "' for " + dtc.key().asMinimalString());
            return;
        }
        it.setData(dtc, pd);
    }

    private static <K extends net.kyori.adventure.key.Keyed> @Nullable String ofKey(final @Nullable K k) {
        if (k == null) return null;
        return k.key().asMinimalString();
    }

    private static <K extends Keyed> @Nullable String ofRegKey(final Registry<K> reg, final @Nullable K k) {
        if (k == null) return null;
        final NamespacedKey key = reg.getKey(k);
        return key == null ? null : key.asMinimalString();
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