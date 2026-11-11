package de.donutsell;

import de.donutsell.config.DonutSellConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Sells the configured item one hotbar slot at a time, the way a player would by hand:
 * <ol>
 *   <li>open the inventory and move stacks of the item into empty hotbar slots
 *       (hover + number key, i.e. a SWAP click), one click at a time, then close it</li>
 *   <li>select each filled hotbar slot in turn and run {@code /ah sell <price>}</li>
 *   <li>repeat until no more of the item is left, the listing limit is reached or the player stops it</li>
 * </ol>
 * It only ever runs while the player is present: it stops on unfocus, on no mouse movement
 * for the AFK timeout, on movement/attack keys, on any foreign menu and on disconnect.
 */
public final class AutoSeller {
    public static final AutoSeller INSTANCE = new AutoSeller();

    /** InventoryMenu: slots 9..35 are the main inventory with the same index as {@link Inventory}. */
    private static final int MAIN_INV_START = 9;
    private static final int MAIN_INV_END = 35;
    private static final long CONFIRM_WAIT_MS = 2500;
    private static final long SOLD_CHECK_TIMEOUT_MS = 4000;
    private static final Set<String> CONFIRM_ITEMS = Set.of(
        "minecraft:lime_stained_glass_pane", "minecraft:green_stained_glass_pane",
        "minecraft:lime_stained_glass", "minecraft:green_stained_glass",
        "minecraft:lime_wool", "minecraft:green_wool",
        "minecraft:lime_concrete", "minecraft:green_concrete",
        "minecraft:lime_terracotta", "minecraft:green_terracotta",
        "minecraft:lime_dye", "minecraft:green_dye", "minecraft:emerald_block"
    );
    private static final List<String> CANCEL_WORDS = List.of("cancel", "abbrechen", "deny", "decline", "back", "zurück", "close");

    private enum State {
        IDLE,
        WAIT_FOR_SCREEN_CLOSE, // start requested from the OneConfig menu
        PLAN,
        FILL_OPEN, FILL_CLICK, FILL_CLOSE,
        SELL_SELECT, SELL_SEND, SELL_WAIT, SELL_CONFIRM_CLICK, SELL_AFTER
    }

    private State state = State.IDLE;
    private long nextActionAt;
    private long stateSince;

    // run state
    private String itemId;
    private long basePrice;
    private int listings;
    private int failedInARow;
    private int originalSlot;
    private final List<int[]> pendingSwaps = new ArrayList<>(); // {inventorySlot, hotbarSlot}
    private final List<Integer> sellQueue = new ArrayList<>();
    private int currentSlot = -1;
    private boolean commandSent;

    // presence tracking
    private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;
    private float lastYaw, lastPitch;
    private long lastInputAt;
    private boolean afkWarned;

    private AutoSeller() {
    }

    public boolean isRunning() {
        return state != State.IDLE;
    }

    public void toggle() {
        if (isRunning()) stop("Gestoppt.");
        else requestStart();
    }

    /** Validates the config and starts selling (after the current menu is closed). */
    public void requestStart() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        if (isRunning()) {
            info("Läuft bereits.", ChatFormatting.YELLOW);
            return;
        }
        itemId = configuredItemId();
        if (itemId == null) {
            info("Kein Item gewählt. Öffne OneConfig (Rechts-Shift) → Donut Sell → Item.", ChatFormatting.RED);
            return;
        }
        basePrice = Prices.parse(DonutSellConfig.price);
        if (basePrice <= 0) {
            info("Ungültiger Preis \"" + DonutSellConfig.price + "\". Beispiele: 1500, 2.5k, 1m", ChatFormatting.RED);
            return;
        }
        if (allowedHotbarSlots().isEmpty()) {
            info("Keine gültigen Hotbar-Slots eingestellt (z. B. \"1-9\").", ChatFormatting.RED);
            return;
        }
        if (countMatching(mc.player) == 0) {
            info("Du hast kein " + itemId + " im Inventar.", ChatFormatting.RED);
            return;
        }
        listings = 0;
        failedInARow = 0;
        originalSlot = mc.player.getInventory().getSelectedSlot();
        resetPresence(mc);
        info("Starte: " + itemId + " für $" + Prices.pretty(basePrice)
            + (DonutSellConfig.priceMode == 1 ? " pro Item" : " pro Angebot")
            + ". Drück die Start/Stopp-Taste oder lauf los, um abzubrechen.", ChatFormatting.GREEN);
        setState(mc.gui.screen() != null ? State.WAIT_FOR_SCREEN_CLOSE : State.PLAN, 0);
    }

    public void stop(String reason) {
        if (!isRunning()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            // close a menu we opened ourselves
            Screen screen = mc.gui.screen();
            if (screen instanceof AbstractContainerScreen<?> && isOwnScreenState()) player.closeContainer();
            if (originalSlot >= 0 && originalSlot < 9) player.getInventory().setSelectedSlot(originalSlot);
        }
        state = State.IDLE;
        pendingSwaps.clear();
        sellQueue.clear();
        info(reason + " (" + listings + " Angebot" + (listings == 1 ? "" : "e") + " erstellt)", ChatFormatting.GOLD);
    }

    // ------------------------------------------------------------------ events

    public void onChatMessage(String plain) {
        if (!isRunning() || state == State.WAIT_FOR_SCREEN_CLOSE) return;
        String lower = plain.toLowerCase(Locale.ROOT);
        for (String word : words(DonutSellConfig.stopWords)) {
            if (lower.contains(word)) {
                stop("Server-Nachricht enthält \"" + word + "\" → gestoppt");
                return;
            }
        }
    }

    public void tick(Minecraft mc) {
        if (!isRunning()) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.getConnection() == null || mc.gameMode == null) {
            state = State.IDLE;
            return;
        }
        if (!checkPresence(mc, player)) return;

        long now = System.currentTimeMillis();
        if (now < nextActionAt) return;

        switch (state) {
            case WAIT_FOR_SCREEN_CLOSE -> {
                if (mc.gui.screen() == null) {
                    resetPresence(mc);
                    setState(State.PLAN, randomDelay(DonutSellConfig.switchDelayMin, DonutSellConfig.switchDelayMax));
                }
            }
            case PLAN -> plan(mc, player);
            case FILL_OPEN -> {
                if (mc.gameMode.isServerControlledInventory()) {
                    stop("Inventar kann gerade nicht geöffnet werden (reitest du?)");
                    return;
                }
                mc.gui.setScreen(new InventoryScreen(player));
                setState(State.FILL_CLICK, clickDelay());
            }
            case FILL_CLICK -> fillClick(mc, player);
            case FILL_CLOSE -> {
                if (mc.gui.screen() instanceof InventoryScreen) player.closeContainer();
                buildSellQueue(player);
                setState(State.SELL_SELECT, randomDelay(DonutSellConfig.switchDelayMin, DonutSellConfig.switchDelayMax));
            }
            case SELL_SELECT -> sellSelect(player);
            case SELL_SEND -> sellSend(player);
            case SELL_WAIT -> sellWait(mc, player, now);
            case SELL_CONFIRM_CLICK -> confirmClick(mc, player);
            case SELL_AFTER -> sellAfter(mc, player, now);
            default -> { }
        }
    }

    // ------------------------------------------------------------------ steps

    private void plan(Minecraft mc, LocalPlayer player) {
        if (maxReached()) {
            stop("Maximale Anzahl Angebote erreicht.");
            return;
        }
        Inventory inv = player.getInventory();
        List<Integer> allowed = allowedHotbarSlots();

        // hotbar slots that already hold the item are sold first, empty allowed slots get refilled
        List<Integer> emptySlots = new ArrayList<>();
        for (int slot : allowed) if (inv.getItem(slot).isEmpty()) emptySlots.add(slot);
        List<Integer> sources = new ArrayList<>();
        for (int i = MAIN_INV_START; i <= MAIN_INV_END; i++) if (matches(inv.getItem(i))) sources.add(i);

        pendingSwaps.clear();
        int remainingBudget = DonutSellConfig.maxListings > 0 ? DonutSellConfig.maxListings - listings : Integer.MAX_VALUE;
        int alreadyInHotbar = 0;
        for (int slot : allowed) if (matches(inv.getItem(slot))) alreadyInHotbar++;
        int canFill = Math.max(0, Math.min(emptySlots.size(), remainingBudget - alreadyInHotbar));
        for (int i = 0; i < Math.min(canFill, sources.size()); i++) {
            pendingSwaps.add(new int[]{sources.get(i), emptySlots.get(i)});
        }

        if (!pendingSwaps.isEmpty()) {
            setState(State.FILL_OPEN, 0);
        } else if (alreadyInHotbar > 0) {
            buildSellQueue(player);
            setState(State.SELL_SELECT, 0);
        } else if (sources.isEmpty()) {
            stop("Fertig – kein " + itemId + " mehr im Inventar.");
        } else {
            stop("Keine freien erlaubten Hotbar-Slots zum Auffüllen.");
        }
    }

    private void fillClick(Minecraft mc, LocalPlayer player) {
        if (!(mc.gui.screen() instanceof InventoryScreen screen)) {
            stop("Inventar wurde geschlossen.");
            return;
        }
        AbstractContainerMenu menu = screen.getMenu();
        if (!menu.getCarried().isEmpty()) {
            stop("Du hältst ein Item mit der Maus – abgebrochen.");
            return;
        }
        if (pendingSwaps.isEmpty()) {
            setState(State.FILL_CLOSE, clickDelay());
            return;
        }
        int[] swap = pendingSwaps.removeFirst();
        int source = swap[0], hotbar = swap[1];
        Inventory inv = player.getInventory();
        if (matches(inv.getItem(source)) && inv.getItem(hotbar).isEmpty()) {
            // same packet as hovering the stack and pressing the hotbar number key
            mc.gameMode.handleContainerInput(menu.containerId, source, hotbar, ContainerInput.SWAP, player);
        }
        setState(pendingSwaps.isEmpty() ? State.FILL_CLOSE : State.FILL_CLICK, clickDelay());
    }

    private void buildSellQueue(LocalPlayer player) {
        sellQueue.clear();
        Inventory inv = player.getInventory();
        for (int slot : allowedHotbarSlots()) if (matches(inv.getItem(slot))) sellQueue.add(slot);
        if (DonutSellConfig.shuffleSlots) Collections.shuffle(sellQueue);
    }

    private void sellSelect(LocalPlayer player) {
        if (maxReached()) {
            stop("Maximale Anzahl Angebote erreicht.");
            return;
        }
        Inventory inv = player.getInventory();
        while (!sellQueue.isEmpty() && !matches(inv.getItem(sellQueue.getFirst()))) sellQueue.removeFirst();
        if (sellQueue.isEmpty()) {
            setState(State.PLAN, randomDelay(DonutSellConfig.switchDelayMin, DonutSellConfig.switchDelayMax));
            return;
        }
        currentSlot = sellQueue.removeFirst();
        // the carried-item packet is sent by MultiPlayerGameMode on the next tick, like a scroll/number key
        inv.setSelectedSlot(currentSlot);
        setState(State.SELL_SEND, randomDelay(DonutSellConfig.switchDelayMin, DonutSellConfig.switchDelayMax));
    }

    private void sellSend(LocalPlayer player) {
        Inventory inv = player.getInventory();
        ItemStack held = inv.getSelectedItem();
        if (inv.getSelectedSlot() != currentSlot || !matches(held)) {
            setState(State.SELL_SELECT, 0); // slot changed under us, pick the next one
            return;
        }
        long price = DonutSellConfig.priceMode == 1 ? basePrice * held.getCount() : basePrice;
        String command = DonutSellConfig.sellCommand.trim();
        if (command.startsWith("/")) command = command.substring(1);
        command = command.replace("{price}", Long.toString(price));
        player.connection.sendCommand(command);
        commandSent = true;
        setState(State.SELL_WAIT, 0);
    }

    private void sellWait(Minecraft mc, LocalPlayer player, long now) {
        Screen screen = mc.gui.screen();
        if (screen instanceof AbstractContainerScreen<?> && !(screen instanceof InventoryScreen)) {
            if (!DonutSellConfig.autoConfirm) {
                stop("Server hat ein Bestätigungs-Menü geöffnet (Auto-Bestätigen ist aus).");
                return;
            }
            setState(State.SELL_CONFIRM_CLICK, clickDelay() + 150);
            return;
        }
        if (screen != null) {
            stop("Unerwartetes Menü geöffnet.");
            return;
        }
        // no confirm menu: the listing is done once the stack left the slot
        if (player.getInventory().getItem(currentSlot).isEmpty() || now - stateSince > CONFIRM_WAIT_MS) {
            setState(State.SELL_AFTER, 0);
        }
    }

    private void confirmClick(Minecraft mc, LocalPlayer player) {
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> screen) || screen instanceof InventoryScreen) {
            setState(State.SELL_AFTER, 0);
            return;
        }
        AbstractContainerMenu menu = screen.getMenu();
        Slot target = findConfirmSlot(menu, player);
        if (target == null) {
            player.closeContainer();
            stop("Bestätigen-Knopf nicht gefunden – passe die Bestätigen-Wörter an.");
            return;
        }
        mc.gameMode.handleContainerInput(menu.containerId, target.index, 0, ContainerInput.PICKUP, player);
        setState(State.SELL_AFTER, 0);
    }

    private void sellAfter(Minecraft mc, LocalPlayer player, long now) {
        Screen screen = mc.gui.screen();
        boolean sold = !matches(player.getInventory().getItem(currentSlot));
        if (!sold && now - stateSince < SOLD_CHECK_TIMEOUT_MS) return; // wait for the server to take the item

        if (screen instanceof AbstractContainerScreen<?> && !(screen instanceof InventoryScreen)) {
            if (now - stateSince < SOLD_CHECK_TIMEOUT_MS) return; // menu still closing
            player.closeContainer();
        }
        if (sold) {
            listings++;
            failedInARow = 0;
            player.sendOverlayMessage(Component.literal("Donut Sell: " + listings + " Angebot" + (listings == 1 ? "" : "e"))
                .withStyle(ChatFormatting.GREEN));
        } else if (commandSent && ++failedInARow >= 2) {
            stop("Item wurde zweimal nicht angenommen (Limit erreicht?)");
            return;
        }
        commandSent = false;
        setState(State.SELL_SELECT, randomDelay(DonutSellConfig.sellDelayMin, DonutSellConfig.sellDelayMax));
    }

    // ------------------------------------------------------------------ presence / safety

    private boolean checkPresence(Minecraft mc, LocalPlayer player) {
        long now = System.currentTimeMillis();
        if (DonutSellConfig.stopOnUnfocus && !mc.isWindowActive() && state != State.WAIT_FOR_SCREEN_CLOSE) {
            stop("Minecraft-Fenster nicht mehr im Fokus → gestoppt");
            return false;
        }

        double mx = mc.mouseHandler.xpos(), my = mc.mouseHandler.ypos();
        float yaw = player.getYRot(), pitch = player.getXRot();
        if (mx != lastMouseX || my != lastMouseY || yaw != lastYaw || pitch != lastPitch) {
            lastMouseX = mx;
            lastMouseY = my;
            lastYaw = yaw;
            lastPitch = pitch;
            lastInputAt = now;
            afkWarned = false;
        }
        long timeoutMs = Math.max(10, DonutSellConfig.afkTimeoutSeconds) * 1000L;
        long idle = now - lastInputAt;
        if (idle > timeoutMs) {
            stop("AFK-Schutz: keine Maus-Bewegung seit " + DonutSellConfig.afkTimeoutSeconds + " s → gestoppt");
            return false;
        }
        if (!afkWarned && idle > timeoutMs - 10_000) {
            afkWarned = true;
            player.sendOverlayMessage(Component.literal("Donut Sell: Beweg die Maus, sonst stoppt der AFK-Schutz!")
                .withStyle(ChatFormatting.YELLOW));
        }

        if (DonutSellConfig.stopOnMovement && state != State.WAIT_FOR_SCREEN_CLOSE) {
            var o = mc.options;
            for (KeyMapping key : new KeyMapping[]{o.keyUp, o.keyDown, o.keyLeft, o.keyRight, o.keyJump, o.keyAttack, o.keyUse, o.keyDrop}) {
                if (key.isDown()) {
                    stop("Bewegung erkannt (" + key.getTranslatedKeyMessage().getString() + ") → gestoppt");
                    return false;
                }
            }
        }

        Screen screen = mc.gui.screen();
        if (screen != null && !isOwnScreenState() && state != State.WAIT_FOR_SCREEN_CLOSE) {
            stop("Menü geöffnet → gestoppt");
            return false;
        }
        return true;
    }

    private boolean isOwnScreenState() {
        return switch (state) {
            case FILL_CLICK, FILL_CLOSE, SELL_WAIT, SELL_CONFIRM_CLICK, SELL_AFTER -> true;
            default -> false;
        };
    }

    private void resetPresence(Minecraft mc) {
        lastInputAt = System.currentTimeMillis();
        lastMouseX = mc.mouseHandler.xpos();
        lastMouseY = mc.mouseHandler.ypos();
        if (mc.player != null) {
            lastYaw = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
        }
        afkWarned = false;
    }

    // ------------------------------------------------------------------ helpers

    private Slot findConfirmSlot(AbstractContainerMenu menu, LocalPlayer player) {
        List<String> confirmWords = words(DonutSellConfig.confirmWords);
        Slot byColour = null;
        for (Slot slot : menu.slots) {
            if (slot.container == player.getInventory() || !slot.hasItem()) continue;
            ItemStack stack = slot.getItem();
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (CANCEL_WORDS.stream().anyMatch(name::contains)) continue;
            for (String word : confirmWords) if (name.contains(word)) return slot;
            if (byColour == null && CONFIRM_ITEMS.contains(idOf(stack))) byColour = slot;
        }
        return byColour;
    }

    private boolean matches(ItemStack stack) {
        return !stack.isEmpty() && idOf(stack).equals(itemId);
    }

    private int countMatching(LocalPlayer player) {
        Inventory inv = player.getInventory();
        int count = 0;
        for (int i = 0; i <= MAIN_INV_END; i++) if (matches(inv.getItem(i))) count++;
        return count;
    }

    private static String idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private boolean maxReached() {
        return DonutSellConfig.maxListings > 0 && listings >= DonutSellConfig.maxListings;
    }

    /** First configured item id, stripped of any component/variant suffix the item picker may add. */
    static String configuredItemId() {
        List<String> items = DonutSellConfig.item;
        if (items == null || items.isEmpty() || items.getFirst() == null) return null;
        String raw = items.getFirst().trim();
        int cut = raw.length();
        for (char c : new char[]{'[', '{', '#', ' '}) {
            int i = raw.indexOf(c);
            if (i >= 0) cut = Math.min(cut, i);
        }
        raw = raw.substring(0, cut).toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) return null;
        return raw.contains(":") ? raw : "minecraft:" + raw;
    }

    /** Parses "1-9", "2,4,6-9" (1-based, as shown in game) into 0-based hotbar indices. */
    static List<Integer> parseSlots(String text) {
        List<Integer> result = new ArrayList<>();
        if (text == null) return result;
        for (String part : text.split("[,;\\s]+")) {
            if (part.isBlank()) continue;
            try {
                String[] range = part.split("-", 2);
                int from = Integer.parseInt(range[0].trim());
                int to = range.length > 1 ? Integer.parseInt(range[1].trim()) : from;
                for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
                    if (i >= 1 && i <= 9 && !result.contains(i - 1)) result.add(i - 1);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Collections.sort(result);
        return result;
    }

    private static List<Integer> allowedHotbarSlots() {
        return parseSlots(DonutSellConfig.hotbarSlots);
    }

    private static List<String> words(String csv) {
        List<String> out = new ArrayList<>();
        if (csv == null) return out;
        for (String w : csv.split(",")) {
            String t = w.trim().toLowerCase(Locale.ROOT);
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private void setState(State next, long delayMs) {
        state = next;
        stateSince = System.currentTimeMillis() + delayMs;
        nextActionAt = stateSince;
    }

    private static long clickDelay() {
        return randomDelay(DonutSellConfig.clickDelayMin, DonutSellConfig.clickDelayMax);
    }

    private static long randomDelay(int min, int max) {
        int lo = Math.max(0, Math.min(min, max)), hi = Math.max(min, max);
        return lo == hi ? lo : ThreadLocalRandom.current().nextLong(lo, hi + 1L);
    }

    private static void info(String text, ChatFormatting colour) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.gui.hud.getChat().addClientSystemMessage(
            Component.literal("[Donut Sell] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(text).withStyle(colour)));
    }
}
