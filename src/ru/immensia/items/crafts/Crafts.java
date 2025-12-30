package ru.immensia.items.crafts;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.RecipeChoice.ExactChoice;
import ru.immensia.Main;
import ru.immensia.boot.IStrap;
import ru.immensia.items.ItemBuilder;
import ru.immensia.items.ItemManager;
import ru.immensia.items.SpecialItem;
import ru.immensia.utils.ClassUtil;
import ru.immensia.utils.ItemUtil;
import ru.immensia.utils.strings.TCUtil;


public final class Crafts implements Listener {

    public static final Map<NamespacedKey, Craft> crafts = new HashMap<>();
    public static final String MENU_TITLE = "Recipe ";
    public static final int MENU_SIZE = 27;

    public Crafts() {
        reload();
    }

    public void reload() {
        HandlerList.unregisterAll(this);

        final Iterator<NamespacedKey> rki = Crafts.crafts.keySet().iterator();
        while (rki.hasNext()) {
            Bukkit.removeRecipe(rki.next());
            rki.remove();
        }

        Bukkit.getPluginManager().registerEvents(this, Main.plug);
        Crafts.loadCrafts();
        new CraftCmd();

        Main.log("§2Крафты запущены!");
    }

    public void onDisable() {
        Crafts.crafts.clear();
        Main.log("§6Крафты выключены!");
    }

    public static final Path CRAFT_DIR = Path.of(Main.plug.getDataFolder().getAbsolutePath(), "crafts");
    public static final Path DEF_FILE = CRAFT_DIR.resolve("craft.yml");

    public static void loadCrafts() {
        //крафты
        try {
            Files.createDirectories(CRAFT_DIR);
            Files.createFile(DEF_FILE);
        } catch (IOException e) {
            Main.log("<amber>Craft files are already created");
        }
        try (final Stream<Path> pths = Files.list(CRAFT_DIR)) {
            pths.forEach(pth -> {
                if (!pth.endsWith(".yml")) return;
                final YamlConfiguration crf = YamlConfiguration.loadConfiguration(pth.toFile());
                Main.log("<yellow>Found craft file " + pth.getFileName().toString());
                final Set<String> crfts = crf.getKeys(false);
                if (crfts.isEmpty()) {
                    Main.log("<amber>File is empty...");
                    return;
                }
                Main.log("<green>Recipes found: " + crfts.size() + "!");
                for (final String key : crfts) {
                    readCraft(crf.getConfigurationSection(key));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readCraft(final ConfigurationSection cs) {
        //ConfigurationSection cs = craftConfig.getConfigurationSection("crafts");
        final ItemStack resultItem = ItemUtil.parse(cs.getString("result"));
        final NamespacedKey nsk = new NamespacedKey(IStrap.space, cs.getName());
        //cs = craftConfig.getConfigurationSection("crafts." + c + ".recipe");
        final Recipe recipe;
        final ItemStack it;
        switch (cs.getString("type")) {//(craftConfig.getString("crafts." + c + ".type")) {
            case "smoker":
                if (ItemUtil.isBlank((it = ItemUtil.parse(cs.getString("recipe.a"))), false)) return;
                recipe = new SmokingRecipe(nsk, resultItem, IdChoice.of(it), 0.5f, 100);
                break;
            case "blaster":
                if (ItemUtil.isBlank((it = ItemUtil.parse(cs.getString("recipe.a"))), false)) return;
                recipe = new BlastingRecipe(nsk, resultItem, IdChoice.of(it), 0.5f, 100);
                break;
            case "campfire":
                if (ItemUtil.isBlank((it = ItemUtil.parse(cs.getString("recipe.a"))), false)) return;
                recipe = new CampfireRecipe(nsk, resultItem, IdChoice.of(it), 0.5f, 500);
                break;
            case "furnace":
                if (ItemUtil.isBlank((it = ItemUtil.parse(cs.getString("recipe.a"))), false)) return;
                recipe = new FurnaceRecipe(nsk, resultItem, IdChoice.of(it), 0.5f, 200);
                break;
            case "cutter":
                if (ItemUtil.isBlank((it = ItemUtil.parse(cs.getString("recipe.a"))), false)) return;
                recipe = new StonecuttingRecipe(nsk, resultItem, IdChoice.of(it));
                break;
            case "smith":
                it = ItemUtil.parse(cs.getString("recipe.a"));
                final ItemStack scd = ItemUtil.parse(cs.getString("recipe.b"));
                if (ItemUtil.isBlank(it, false) || ItemUtil.isBlank(scd, false)) return;
                recipe = new SmithingTransformRecipe(nsk, resultItem, IdChoice.of(ItemUtil.parse(cs.getString("recipe.c"))),
                    IdChoice.of(it), IdChoice.of(scd), !it.hasData(DataComponentTypes.DAMAGE));
                break;
            case "noshape":
                recipe = new ShapelessRecipe(nsk, resultItem);
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                    final ItemStack ii = ItemUtil.parse(cs.getString("recipe." + s));
                    if (!ii.getType().isAir()) {
                        ((ShapelessRecipe) recipe).addIngredient(IdChoice.of(ItemUtil.parse(cs.getString("recipe." + s))));
                    }
                }
                break;
            case "shaped":
            default:
                recipe = new ShapedRecipe(nsk, resultItem);
                final String shp = cs.getString("shape");
                ((ShapedRecipe) recipe).shape(shp == null ? new String[]{"abc", "def", "ghi"} : shp.split(":"));
                for (final String s : cs.getConfigurationSection("recipe").getKeys(false)) {
                    ((ShapedRecipe) recipe).setIngredient(s.charAt(0), IdChoice.of(ItemUtil.parse(cs.getString("recipe." + s))));
                }
                break;
        }
        Bukkit.addRecipe(recipe);
        crafts.put(nsk, new Craft(recipe, p -> true));

    }

    @SuppressWarnings("unchecked")
    public static <G extends Recipe> G getRecipe(final NamespacedKey key, final Class<G> cls) {
        if (!key.getNamespace().equals(IStrap.space)) return null;
        final Craft rc = crafts.get(key);
        if (rc != null && cls.isAssignableFrom(rc.rec.getClass())) return (G) rc.rec;
        return null;
    }

    public static boolean rmvRecipe(final NamespacedKey key) {
        return crafts.remove(key) != null;
    }

    public static Recipe fakeRec(final Recipe rc) {
        if (rc instanceof Keyed) {
            final String ks = ((Keyed) rc).getKey().getKey();
            switch (rc) {
                case ShapedRecipe src:
                    final ShapedRecipe drc = new ShapedRecipe(new NamespacedKey(IStrap.space, ks), rc.getResult());
                    drc.shape(src.getShape());
                    for (final Entry<Character, RecipeChoice> en : src.getChoiceMap().entrySet()) {
                        if (en.getValue() == null) continue;
                        drc.setIngredient(en.getKey(), new ExactChoice(((IdChoice) en.getValue()).getItemStack()));
                    }
                    return drc;
                case ShapelessRecipe src:
                    final ShapelessRecipe lrc = new ShapelessRecipe(new NamespacedKey(IStrap.space, ks), rc.getResult());
                    for (final RecipeChoice ch : src.getChoiceList()) {
                        if (ch == null) continue;
                        lrc.addIngredient(new ExactChoice(((IdChoice) ch).getItemStack()));
                    }
                    return lrc;
                case final FurnaceRecipe src:
                    return new FurnaceRecipe(new NamespacedKey(IStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final SmokingRecipe src:
                    return new SmokingRecipe(new NamespacedKey(IStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final BlastingRecipe src:
                    return new BlastingRecipe(new NamespacedKey(IStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                case final CampfireRecipe src:
                    return new CampfireRecipe(new NamespacedKey(IStrap.space, ks), src.getResult(),
                        new ExactChoice(((IdChoice) src.getInputChoice()).getItemStack()), src.getExperience(), src.getCookingTime());
                default:
                    return null;
            }
        }
        return null;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        discRecs(e.getPlayer());
    }

    public static void discRecs(final Player p) {
        final List<NamespacedKey> rls = new ArrayList<>();
        for (final Entry<NamespacedKey, Craft> en : crafts.entrySet()) {
            if (en.getValue().canSee.test(p)) rls.add(en.getKey());
        }
        p.discoverRecipes(rls);
    }

    public record Craft(Recipe rec, Predicate<Player> canSee) {}

    @EventHandler
    public void onRecipe(final PrepareItemCraftEvent e) {
        final Recipe rc = e.getRecipe();
        if (rc == null) return;
        if (!e.isRepair() && rc instanceof Keyed) {
            switch (rc) {
                case ComplexRecipe cxr -> {
                    for (final ItemStack it : e.getInventory().getMatrix()) {
                        if (!ItemManager.isCustom(it)) continue;
                        e.getInventory().setResult(ItemUtil.air);
                        return;
                    }
                }
                case ShapedRecipe shr -> {
                    final ShapedRecipe src = Crafts.getRecipe(shr.getKey(), ShapedRecipe.class);
                    final ItemStack[] mtx = e.getInventory().getMatrix();
                    if (src == null) {
                        for (final ItemStack it : mtx) {
                            if (!ItemManager.isCustom(it)) continue;
                            e.getInventory().setResult(ItemUtil.air);
                            return;
                        }
                    } else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-8 2x3-4 3x2-4 3x3-2 магия крч
                        final Collection<RecipeChoice> rcs = src.getChoiceMap().values();
                        rcs.removeIf(c -> c == null);
                        for (final ItemStack it : mtx) {
                            if (!ItemUtil.isBlank(it, false)) {
                                final Iterator<RecipeChoice> rci = rcs.iterator();
                                while (rci.hasNext()) {
                                    if (rci.next().test(it)) {
                                        rci.remove();
                                        break;
                                    }
                                }
                            }
                        }

                        final CraftingInventory inv = e.getInventory();
                        if (rcs.size() != 0) {
                            inv.setResult(ItemUtil.air);
                            Bukkit.removeRecipe(src.getKey());
                            final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().getFirst();
                            if (pl == null) return;
                            inv.setResult(Bukkit.craftItem(ClassUtil.scale(mtx, 3, 3), pl.getWorld(), (Player) pl));
                            Bukkit.addRecipe(src);
                        }
                    }
                }
                case ShapelessRecipe slr -> {
                    final ShapelessRecipe src = Crafts.getRecipe(slr.getKey(), ShapelessRecipe.class);
                    final ItemStack[] mtx = e.getInventory().getMatrix();
                    if (src == null) {
                        for (final ItemStack it : mtx) {
                            if (!ItemManager.isCustom(it)) continue;
                            e.getInventory().setResult(ItemUtil.air);
                            return;
                        }
                    } else {//1x1-9 2x1-12 1x2-6 3x1-6 1x3-3 2x2-4 2x3-4 3x2-4 3x3-2 магия крч
                        final List<RecipeChoice> rcs = src.getChoiceList();
                        for (final ItemStack ti : mtx) {
                            final Iterator<RecipeChoice> ri = rcs.iterator();
                            while (ri.hasNext()) {
                                final RecipeChoice chs = ri.next();
                                if ((chs == null && ItemUtil.isBlank(ti, false)) || chs.test(ti)) {
                                    ri.remove();
                                    break;
                                }
                            }
                        }

                        final CraftingInventory inv = e.getInventory();
                        if (rcs.size() != 0) {
                            inv.setResult(ItemUtil.air);
                            Bukkit.removeRecipe(src.getKey());
                            final HumanEntity pl = e.getViewers().isEmpty() ? null : e.getViewers().getFirst();
                            if (pl == null) return;
                            inv.setResult(Bukkit.craftItem(ClassUtil.scale(mtx, 3, 3), pl.getWorld(), (Player) pl));
                            Bukkit.addRecipe(src);
                        }
                    }
                }
                default -> {
                }
            }

            final ItemStack fin = rc.getResult();
            final SpecialItem si = SpecialItem.get(fin);
            if (si != null && si.crafted()) {
                e.getInventory().setResult(ItemUtil.air);
                for (final HumanEntity he : e.getViewers()) {
                    he.sendMessage(TCUtil.form(Main.PREFIX + "<red>Эта реликвия уже создана!"));
                }
            }
        }
    }

    //FurnaceBurnEvent change burn time
    @EventHandler
    public void onCook(final FurnaceSmeltEvent e) {
        final Recipe rc = e.getRecipe();
        if (rc == null) return;
        if (e.getBlock().getState() instanceof Furnace) {
            final CookingRecipe<?> src = Crafts.getRecipe(((Keyed) rc).getKey(), CookingRecipe.class);
            final ItemStack ti = e.getSource();
            if (src != null) {
                if (src.getInputChoice().test(ti)) return;
                Bukkit.removeRecipe(src.getKey());
                final Class<?> cls = rc.getClass();
                final Iterator<Recipe> rci = Bukkit.recipeIterator();
                while (rci.hasNext()) {
                    final Recipe orc = rci.next();
                    if (orc.getClass() == cls && ((CookingRecipe<?>) orc).getInputChoice().test(ti)) {
                        e.setResult(orc.getResult());
                        break;
                    }
                }
                Bukkit.addRecipe(src);
            }
        }
    }

    //FurnaceBurnEvent change burn time
    @EventHandler
    public void onStCook(final FurnaceStartSmeltEvent e) {
        final Recipe rc = e.getRecipe();
        if (e.getBlock().getState() instanceof Furnace) {
            final CookingRecipe<?> src = Crafts.getRecipe(((Keyed) rc).getKey(), CookingRecipe.class);
            final ItemStack ti = e.getSource();
            if (src == null) {
                if (!ItemManager.isCustom(ti)) return;
                e.setTotalCookTime(Integer.MAX_VALUE);
            }
        }
    }

    @EventHandler
    public void onCamp(final PrepareSmithingEvent e) {
        final SmithingInventory si = e.getInventory();
        final Recipe rc = si.getRecipe();
        if (!(rc instanceof SmithingRecipe)) return;
        final SmithingRecipe src = Crafts.getRecipe(((Keyed) rc).getKey(), SmithingRecipe.class);
        final ItemStack ti = si.getInputMineral();
        if (src == null) {
            if (!ItemManager.isCustom(ti)) return;
        } else if (ti != null) {
            if (src.getAddition().test(ti)) return;
        }
        si.setResult(ItemUtil.air);
    }

    @EventHandler
    public void onSCut(final PlayerStonecutterRecipeSelectEvent e) {
        final StonecuttingRecipe rc = e.getStonecuttingRecipe();
        final StonecuttingRecipe src = Crafts.getRecipe(rc.getKey(), StonecuttingRecipe.class);
        final StonecutterInventory sci = e.getStonecutterInventory();
        final ItemStack ti = sci.getInputItem();
        if (src == null) {
            if (!ItemManager.isCustom(ti)) return;
        } else if (ti != null) {
            if (src.getInputChoice().test(ti)) return;
        }
        sci.setResult(ItemUtil.air);
        e.setCancelled(true);
    }

    /*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRecipeBook(final PlayerRecipeBookClickEvent e) {
        final Recipe rc = Crafts.getRecipe(e.getRecipe(), Recipe.class);
        if (rc == null) return;
        e.setCancelled(true);
        final Player p = e.getPlayer();
        final InventoryView iv = p.getOpenInventory();
        switch (iv.getType()) {
            case CRAFTING, WORKBENCH:
                final int start = 1;
                final CraftingInventory cri = (CraftingInventory) iv.getTopInventory();
                int ix = 0;
                for (final ItemStack is : cri) {
                    if ((ix++) < start) continue;
                    if (!ItemUtil.isBlank(is, false)) {
                        giveItemAmt(p, is, is.getAmount());
                        is.setAmount(0);
                    }
                }

                if (rc instanceof ShapedRecipe) {//магия бля

                    final HashMap<IdChoice, String> gridIts = new HashMap<>();
                    for (final Entry<Character, RecipeChoice> en : ((ShapedRecipe) rc).getChoiceMap().entrySet()) {
                        final RecipeChoice ch = en.getValue();
                        if (ch instanceof IdChoice) {
                            final String gs = gridIts.get(ch);
                            gridIts.put((IdChoice) ch, gs == null ?
                                String.valueOf(en.getKey()) : gs + en.getKey());
                        }
                    }

                    final HashMap<IdChoice, Integer> has = new HashMap<>();
                    for (final IdChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                        final Iterator<Entry<IdChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                            final Entry<IdChoice, Integer> en = eni.next();
                            if (en.getKey().test(it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                    final String shp = String.join(":", ((ShapedRecipe) rc).getShape());
                    final int rl = shp.indexOf(':') + 1;
                    final Iterator<Entry<IdChoice, String>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                        final Entry<IdChoice, String> en = eni.next();
                        final Integer his = has.get(en.getKey());
                        final String slots = en.getValue();
                        final ItemStack kst = en.getKey().getItemStack();
                        final int split = Math.min(e.isMakeAll() ?
                            kst.getType().getMaxStackSize() : 1, his / slots.length());
                        giveItemAmt(p, kst, his - (split * slots.length()));
                        if (split == 0) continue;
                        for (final char c : slots.toCharArray()) {
                            cri.setItem(getCharIx(shp, rl, c) + start, kst.asQuantity(split));
                        }
                        eni.remove();
                    }

                    if (gridIts.size() != 0) {
                        e.setCancelled(false);
                        int ir = 0;
                        for (final ItemStack is : cri) {
                            if ((ir++) < start) continue;
                            if (!ItemUtil.isBlank(is, false)) {
                                giveItemAmt(p, is, is.getAmount());
                                is.setAmount(0);
                            }
                        }
                        return;
                    }

                } else if (rc instanceof ShapelessRecipe) {//магия бля
                    final HashMap<IdChoice, Integer> gridIts = new HashMap<>();
                    for (final RecipeChoice ch : ((ShapelessRecipe) rc).getChoiceList()) {
                        if (ch instanceof IdChoice) {
                            final Integer gs = gridIts.get(ch);
                            gridIts.put((IdChoice) ch, gs == null ? 1 : gs + 1);
                        }
                    }

                    int mix = start;
                    final HashMap<IdChoice, Integer> has = new HashMap<>();
                    for (final IdChoice chs : gridIts.keySet()) has.put(chs, 0);
                    for (final ItemStack it : p.getInventory()) {
                        final Iterator<Entry<IdChoice, Integer>> eni = has.entrySet().iterator();
                        while (eni.hasNext()) {
                            final Entry<IdChoice, Integer> en = eni.next();
                            if (en.getKey().test(it)) {
                                en.setValue(en.getValue() + it.getAmount());
                                it.setAmount(0);
                            }
                        }
                    }

                    final Iterator<Entry<IdChoice, Integer>> eni = gridIts.entrySet().iterator();
                    while (eni.hasNext()) {
                        final Entry<IdChoice, Integer> en = eni.next();
                        final Integer his = has.get(en.getKey());
                        final int slots = en.getValue();
                        final ItemStack kst = en.getKey().getItemStack();
                        final int split = Math.min(e.isMakeAll() ?
                            kst.getType().getMaxStackSize() : 1, his / slots);
                        giveItemAmt(p, kst, his - (split * slots));
                        if (split == 0) continue;
                        for (int i = slots; i > 0; i--) {
                            cri.setItem(mix, kst.asQuantity(split));
                            mix++;
                        }
                        eni.remove();
                    }

                    if (gridIts.size() != 0) {
                        e.setCancelled(false);
                        int ir = 0;
                        for (final ItemStack is : cri) {
                            if ((ir++) < start) continue;
                            if (!ItemUtil.isBlank(is, false)) {
                                giveItemAmt(p, is, is.getAmount());
                                is.setAmount(0);
                            }
                        }
                        return;
                    }


                }
                break;
            case FURNACE, BLAST_FURNACE, SMOKER:
                final FurnaceInventory fni = (FurnaceInventory) iv.getTopInventory();
                if (rc instanceof CookingRecipe) {
                    final IdChoice chs = (IdChoice) ((CookingRecipe<?>) rc).getInputChoice();
                    final ItemStack in = fni.getSmelting();
                    if (!ItemUtil.isBlank(in, false)) {
                        giveItemAmt(p, in, in.getAmount());
                        fni.setSmelting(ItemUtil.air);
                    }

                    int invCnt = 0;
                    for (final ItemStack it : p.getInventory()) {
                        if (chs.test(it)) {
                            invCnt += it.getAmount();
                            it.setAmount(0);
                        }
                    }

                    if (invCnt == 0) {
                        e.setCancelled(false);
                        return;
                    }

                    final ItemStack cit = chs.getItemStack();
                    final int back = invCnt - cit.getType().getMaxStackSize();
                    if (back > 0) {
                        fni.setSmelting(cit.asQuantity(cit.getType().getMaxStackSize()));
                        giveItemAmt(p, cit, back);
                    } else {
                        fni.setSmelting(cit.asQuantity(invCnt));
                    }
                }
                break;
            default:
                e.setCancelled(false);
        }
    }

    private static int getCharIx(final String shp, final int rl, final char c) {
        final int ci = shp.indexOf(c);
        if (rl < 1) return ci;
        return ci / rl * 3 + ci % rl;
    }

    private static void giveItemAmt(final Player p, final ItemStack it, final int amt) {
        if (amt == 0) return;
        final int sts = it.getType().getMaxStackSize();
        final ItemStack[] its = new ItemStack[amt / sts + 1];
        for (int i = its.length - 1; i > 0; i--) {
            its[i] = it.asQuantity(sts);
        }
        its[0] = it.asQuantity(amt % sts);
        for (final ItemStack i : p.getInventory().addItem(its).values()) {
            p.getWorld().dropItem(p.getLocation(), i);
        }
    }*/

    private static final int rad = 3;
    public static void openEditor(final Player pl, final String name, final Recipe recipe) {
        if (recipe == null) return;
        final ItemType tp;
        final ItemStack[] mtx = new ItemStack[9];
        switch (recipe) {
            case final ShapedRecipe rec:
                tp = ItemType.CHEST;
                final String[] shp = rec.getShape();
                final Map<Character, RecipeChoice> imp = rec.getChoiceMap();
                for (int r = 0; r != rad; r++) {
                    final String sr = shp.length > r ? shp[r] : "";
                    for (int c = 0; c != rad; c++) {
                        final RecipeChoice chs = imp.get(sr.length() > c ? sr.charAt(c) : 'w');
                        mtx[r * 3 + c] = chs instanceof final IdChoice ic ? ic.getItemStack() : null;
                    }
                }
                for (int i = 0; i != mtx.length; i++)
                    if (mtx[i] == null) mtx[i] = ItemUtil.air;
                break;
            case final ShapelessRecipe rec:
                tp = ItemType.ENDER_CHEST;
                final Iterator<RecipeChoice> rci = rec.getChoiceList().iterator();
                for (int i = 0; i != mtx.length; i++) {
                    if (rci.hasNext() && rci.next() instanceof IdChoice ic) {
                        mtx[i] = ic.getItemStack();
                    }
                }
                for (int i = 0; i != mtx.length; i++)
                    if (mtx[i] == null) mtx[i] = ItemUtil.air;
                break;
            case final CookingRecipe<?> rec:
                tp = switch (rec) {
                    case final CampfireRecipe cr -> ItemType.CAMPFIRE;
                    case final BlastingRecipe cr -> ItemType.BLAST_FURNACE;
                    case final SmokingRecipe cr -> ItemType.SMOKER;
                    default -> ItemType.FURNACE;
                };
                mtx[4] = choiceIt(rec.getInputChoice());
                break;
            case final SmithingTransformRecipe rec:
                tp = ItemType.SMITHING_TABLE;
                mtx[1] = choiceIt(rec.getTemplate());
                mtx[3] = choiceIt(rec.getBase());
                mtx[5] = choiceIt(rec.getAddition());
                break;
            case final StonecuttingRecipe rec:
                tp = ItemType.STONECUTTER;
                mtx[4] = choiceIt(rec.getInputChoice());
                break;
            case null:
            default:
                tp = ItemType.CHEST;
                break;
        }
        openEditor(pl, name, tp, recipe.getResult(), mtx);
    }

    private static @Nullable ItemStack choiceIt(final RecipeChoice rc) {
        return rc instanceof final IdChoice idc ? idc.getItemStack() : null;
    }

    private static final int TYPE_SLOT = 9, RES_SLOT = 14, RDY_SLOT = 16, ARR_SLOT = 13, MTX_FST = 1, DMG_TAG = 1337;
    public static  void openEditor(final Player pl, final String name, final ItemType type, final ItemStack result, final ItemStack... mtx) {
        final ItemStack[] invIts = new ItemStack[MENU_SIZE];
        Arrays.fill(invIts, new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE)
            .name("§0.").maxDamage(DMG_TAG).build());
        for (int i = 0; i != mtx.length; i++) {
            if (mtx[i] == null) continue;
            invIts[i / 3 * 9 + i % 3 + MTX_FST] = mtx[i];
        }
        invIts[RES_SLOT] = result;
        invIts[TYPE_SLOT] = makeIcon(type);
        invIts[ARR_SLOT] = new ItemBuilder(ItemType.IRON_NUGGET).name("§7->").maxDamage(DMG_TAG).build();
        invIts[RDY_SLOT] = new ItemBuilder(ItemType.GREEN_CONCRETE_POWDER).name("§aГотово!").maxDamage(DMG_TAG).build();
        final Inventory cri = Bukkit.createInventory(null, MENU_SIZE, TCUtil.form("<yellow>" + MENU_TITLE + name));
        cri.setContents(invIts);
        pl.openInventory(cri);
    }

    public static boolean clickEditor(final HumanEntity pl, final Inventory inv, final String key, final int slot) {
        final ItemStack cli = inv.getItem(slot);
        if (cli != null && Integer.valueOf(DMG_TAG)
            .equals(cli.getData(DataComponentTypes.MAX_DAMAGE))) {
            if (slot != RDY_SLOT) return true;
        } else return false;

        final ItemStack rst = inv.getItem(RES_SLOT);
        if (ItemUtil.isBlank(rst, false)) {
            pl.sendMessage("§cFinish the recipe first!");
            return true;
        }

        //запоминание крафта
        final YamlConfiguration crCfg = YamlConfiguration.loadConfiguration(DEF_FILE.toFile());
        crCfg.set(key, null);
        crCfg.set(key + ".result", ItemUtil.write(rst));
        final ItemStack tis = inv.getItem(TYPE_SLOT);
        final ItemType tp = tis == null
            ? ItemType.CHEST : tis.getType().asItemType();
        crCfg.set(key + ".type", getRecType(tp));
        final ConfigurationSection cs = crCfg.getConfigurationSection(key);
        final NamespacedKey nKey = new NamespacedKey(IStrap.space, key);
        final Recipe nrc;
        final ItemStack it;
        final String[] shp;
        if (ItemType.FURNACE.equals(tp)) {
            it = inv.getItem(11);
            if (ItemUtil.isBlank(it, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            nrc = new FurnaceRecipe(nKey, rst, IdChoice.of(it), 0.5f, 200);
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.SMOKER.equals(tp)) {
            it = inv.getItem(11);
            if (ItemUtil.isBlank(it, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            nrc = new SmokingRecipe(nKey, rst, IdChoice.of(it), 0.5f, 100);
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.BLAST_FURNACE.equals(tp)) {
            it = inv.getItem(11);
            if (ItemUtil.isBlank(it, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            nrc = new BlastingRecipe(nKey, rst, IdChoice.of(it), 0.5f, 100);
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.CAMPFIRE.equals(tp)) {
            it = inv.getItem(11);
            if (ItemUtil.isBlank(it, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            nrc = new CampfireRecipe(nKey, rst, IdChoice.of(it), 0.5f, 500);
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.SMITHING_TABLE.equals(tp)) {
            it = inv.getItem(10);
            final ItemStack scd = inv.getItem(12);
            final ItemStack tpl = inv.getItem(2);
            if (ItemUtil.isBlank(it, false) || ItemUtil.isBlank(scd, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            cs.set("recipe.b", ItemUtil.write(scd));
            cs.set("recipe.c", ItemUtil.write(tpl));
            nrc = new SmithingTransformRecipe(nKey, rst, IdChoice.of(tpl), IdChoice.of(it),
                IdChoice.of(scd), !it.hasData(DataComponentTypes.DAMAGE));
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.STONECUTTER.equals(tp)) {
            it = inv.getItem(11);
            if (ItemUtil.isBlank(it, false)) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }
            cs.set("recipe.a", ItemUtil.write(it));
            nrc = new StonecuttingRecipe(nKey, rst, IdChoice.of(it));
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else if (ItemType.ENDER_CHEST.equals(tp)) {
            final ShapelessRecipe lrs = new ShapelessRecipe(nKey, rst);
            shp = new String[]{"abc", "def", "ghi"};
            for (byte cy = 0; cy < 3; cy++) {
                for (byte cx = 1; cx < 4; cx++) {
                    final ItemStack ti = inv.getItem(cy * 9 + cx);
                    if (!ItemUtil.isBlank(ti, false)) {
                        lrs.addIngredient(IdChoice.of(ti));
                        cs.set("recipe." + shp[cy].charAt(cx - 1), ItemUtil.write(ti));
                    }
                }
            }
            nrc = lrs;
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(nrc);
        } else {//if ItemType.CHEST.equals(tp) - тоже магия
            final ShapedRecipe srs = new ShapedRecipe(nKey, rst);
            final ItemStack[] rcs = new ItemStack[rad * rad];
            int xMin = -1, xMax = -1, yMin = -1, yMax = -1;
            for (int cx = 0; cx < rad; cx++) {
                for (int cy = 0; cy < rad; cy++) {
                    final ItemStack ti = inv.getItem(cy * 9 + cx + 1);
                    if (!ItemUtil.isBlank(ti, false)) {
                        if (xMin == -1 || xMin > cx) xMin = cx;
                        if (yMin == -1 || yMin > cy) yMin = cy;
                        if (xMax < cx) xMax = cx;
                        if (yMax < cy) yMax = cy;
                    }
                    rcs[cy * rad + cx] = ti;
                }
            }

            if (xMin == -1 || yMin == -1) {
                pl.sendMessage("§cFinish the recipe first!");
                return true;
            }

            shp = makeShape(xMax + 1 - xMin, yMax + 1 - yMin);
            final StringBuilder sb = new StringBuilder(shp.length * (xMax + 1 - xMin));
            for (final String s : shp) sb.append(":").append(s);
            cs.set("shape", sb.substring(1));
            srs.shape(shp);

            for (int cx = xMax; cx >= xMin; cx--) {
                for (int cy = yMax; cy >= yMin; cy--) {
                    final ItemStack ti = rcs[cy * rad + cx];
                    if (!ItemUtil.isBlank(ti, false)) {
                        srs.setIngredient(shp[cy - yMin].charAt(cx - xMin), IdChoice.of(ti));
                        cs.set("recipe." + shp[cy - yMin].charAt(cx - xMin), ItemUtil.write(ti));
                    }
                }
            }
            nrc = srs;
            Bukkit.removeRecipe(nKey);
            Bukkit.addRecipe(srs);
        }

        Crafts.crafts.put(nKey, new Craft(nrc, p -> true));

        try {
            crCfg.save(DEF_FILE.toFile());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        pl.sendMessage(TCUtil.form(Main.PREFIX + "§7Recipe §к" + key + " §7is complete!"));
        pl.closeInventory();
        return true;
    }

    private static final String dsp = "abcdefghi";
    private static String[] makeShape(final int dX, final int dY) {
        final String[] sp = new String[dY];
        for (int i = 0; i < dY; i++) {
            sp[i] = dsp.substring(i * dX, i * dX + dX);
        }
        return sp;
    }

    private static  ItemStack makeIcon(final ItemType mt) {
        if (ItemType.ENDER_CHEST.equals(mt)) return new ItemBuilder(ItemType.ENDER_CHEST).name("§5Formless").build();
        if (ItemType.FURNACE.equals(mt)) return new ItemBuilder(ItemType.FURNACE).name("§6Furnace").build();
        if (ItemType.SMOKER.equals(mt)) return new ItemBuilder(ItemType.SMOKER).name("§cSmoking").build();
        if (ItemType.BLAST_FURNACE.equals(mt)) return new ItemBuilder(ItemType.BLAST_FURNACE).name("§7Blasting").build();
        if (ItemType.CAMPFIRE.equals(mt)) return new ItemBuilder(ItemType.CAMPFIRE).name("§eCampfire").build();
        if (ItemType.SMITHING_TABLE.equals(mt)) return new ItemBuilder(ItemType.SMITHING_TABLE).name("§fSmithing").build();
        if (ItemType.STONECUTTER.equals(mt)) return new ItemBuilder(ItemType.STONECUTTER).name("§7Cutting").build();
        /*if ItemType.CHEST.equals(tp)*/ return new ItemBuilder(ItemType.CHEST).name("§dShaped").build();
    }

    private static  String getRecType(final ItemType tp) {
        if (ItemType.ENDER_CHEST.equals(tp)) return "noshape";
        if (ItemType.FURNACE.equals(tp)) return "furnace";
        if (ItemType.SMOKER.equals(tp)) return "smoker";
        if (ItemType.BLAST_FURNACE.equals(tp)) return "blaster";
        if (ItemType.CAMPFIRE.equals(tp)) return "campfire";
        if (ItemType.SMITHING_TABLE.equals(tp)) return "smith";
        if (ItemType.STONECUTTER.equals(tp)) return "cutter";
        /*if ItemType.CHEST.equals(tp)*/ return "shaped";
    }
}
