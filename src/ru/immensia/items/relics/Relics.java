package ru.immensia.items.relics;

import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import ru.immensia.items.ItemBuilder;
import ru.immensia.utils.colors.TCUtil;

public class Relics {

    public final AYEMace AYE_MACE = new AYEMace(new ItemBuilder(ItemType.MACE).glint(true).unbreak(true)
        .name("<red>Сплеш-Булава").lore("").lore(TCUtil.N + "Наносит урон сущностям в радиусе 3.6 бл.").build());

    public final AreaPick AREA_PICK = new AreaPick(new ItemBuilder(ItemType.DIAMOND_PICKAXE).glint(true).unbreak(true)
        .name("<stale>Кирка Троицы").lore("").lore(TCUtil.N + "Копает такие-же блоки площадью в 3x3").build());

    public final SmeltPick SMELT_PICK = new SmeltPick(new ItemBuilder(ItemType.NETHERITE_PICKAXE).glint(true).unbreak(true)
        .name("<gold>Плавленая Кирка").lore("").lore(TCUtil.N + "Переплавляет дропы из блоков").build());

    public final FreezeAxe FREEZE_AXE = new FreezeAxe(new ItemBuilder(ItemType.IRON_AXE).glint(true).unbreak(true)
        .name("<aqua>Замерзший Топор").lore("").lore(TCUtil.N + "Криты морозят на 30% сек от урона").build());

    public final TreeAxe TREE_AXE = new TreeAxe(new ItemBuilder(ItemType.WOODEN_AXE).glint(true).unbreak(true)
        .name("<amber>Лесоповал").lore("").lore(TCUtil.N + "Срубает ствол дерева до 40 бревен").build());

    public final VampSword VAMP_SWORD = new VampSword(new ItemBuilder(ItemType.GOLDEN_SWORD).glint(true).unbreak(true)
        .name("<dark_red>Кровожадный Меч").lore("").lore(TCUtil.N + "Впитывает 20% нанесенного урона как здоровье").build());

    public final HasteSword HASTE_SWORD = new HasteSword(new ItemBuilder(ItemType.IRON_SWORD).glint(true).unbreak(true)
        .name("<dark_red>Попутный Меч").lore("").lore(TCUtil.N + "Ускоряет носителя за комбо удары на 2 сек").build());

    public final Thurident THURIDENT = new Thurident(new ItemBuilder(ItemType.TRIDENT).unbreak(true)
        .name("<dark_aqua>Трезубец Грома").lore("").lore(TCUtil.N + "Более молниеносный вариант трезубца").build());

    public final SneakPants SNEAK_PANTS = new SneakPants(new ItemBuilder(ItemType.CHAINMAIL_LEGGINGS).unbreak(true)
        .name("<dark_gray>Штаны Ниндзя").lore("").lore(TCUtil.N + "Присаживание прячет носителя").build());

    public final JumpyBoots JUMPY_BOOTS = new JumpyBoots(new ItemBuilder(ItemType.IRON_BOOTS).unbreak(true)
        .name("<sky>Ботинки Ангела").lore("").lore(TCUtil.N + "Позволяют прыгать дважды").build());

    public final WardenChest WARDEN_CHEST = new WardenChest(new ItemBuilder(ItemType.DIAMOND_CHESTPLATE)
        .trim(TrimMaterial.DIAMOND, TrimPattern.SILENCE).name("<indigo>Нагрудник Хранителя")
        .unbreak(true).lore("").lore(TCUtil.N + "Получение урона создает разряд").build());

    public final ProtHelmet PROT_HELMET = new ProtHelmet(new ItemBuilder(ItemType.TURTLE_HELMET).unbreak(true)
        .name("<green>Черепаший Шлем").lore("").lore(TCUtil.N + "Поглощает криты и удары булавой").build());

    public final Telebow TELEBOW = new Telebow(new ItemBuilder(ItemType.BOW).unbreak(true)
        .name("<dark_purple>Лук-Телепортер").lore("").lore(TCUtil.N + "Стреляет жемчугом при " + TCUtil.bind(TCUtil.Input.SNEAK)).build());

    public final CrossWeb CROSS_WEB = new CrossWeb(new ItemBuilder(ItemType.CROSSBOW).unbreak(true)
        .name("<dark_purple>Паутинный Арбалет").lore("").lore(TCUtil.N + "Создает паутину при попадании").build());
}
