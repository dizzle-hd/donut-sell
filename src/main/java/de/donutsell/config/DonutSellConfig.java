package de.donutsell.config;

import de.donutsell.AutoSeller;
import java.util.ArrayList;
import java.util.List;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.ItemList;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;

/**
 * The settings page that shows up in OneConfig / OneClient (Right Shift → "Donut Sell").
 * <p>
 * All values are static so the seller can read them without holding a reference to the config.
 * OneConfig saves them to {@code config/OneConfig/profiles/<profile>/donut-sell.json}.
 */
@SuppressWarnings("unused") // fields and buttons are read reflectively by OneConfig
public class DonutSellConfig extends Config {

    // ---------------------------------------------------------------- Verkauf

    @ItemList(
        title = "Item",
        description = "Das Item, das verkauft werden soll. Nur genau dieses Item wird angefasst.",
        category = "Verkauf",
        subcategory = "Was & wie teuer",
        maxEntries = 1,
        addText = "Item auswählen"
    )
    public static List<String> item = new ArrayList<>(List.of("minecraft:diamond"));

    @Text(
        title = "Preis",
        description = "z. B. 1500, 2.5k, 1m oder 1,2b. Wird als ganze Zahl an /ah sell geschickt.",
        category = "Verkauf",
        subcategory = "Was & wie teuer",
        placeholder = "z. B. 2.5k"
    )
    public static String price = "1k";

    @Dropdown(
        title = "Preis gilt für",
        description = "\"Ganzen Stack\": jedes Angebot kostet genau den Preis. "
            + "\"Pro Item\": Preis × Anzahl im Stack (bei angebrochenen Stacks wird es automatisch günstiger).",
        category = "Verkauf",
        subcategory = "Was & wie teuer",
        options = {"Ganzen Stack (pro Angebot)", "Pro Item"}
    )
    public static int priceMode = 0;

    @Text(
        title = "Verkaufs-Befehl",
        description = "Befehl ohne \"/\". {price} wird durch den Preis ersetzt.",
        category = "Verkauf",
        subcategory = "Server"
    )
    public static String sellCommand = "ah sell {price}";

    @Number(
        title = "Max. Angebote pro Durchlauf",
        description = "0 = so viele wie möglich (bis Inventar leer oder Server-Limit erreicht).",
        category = "Verkauf",
        subcategory = "Server",
        min = 0f,
        max = 500f
    )
    public static int maxListings = 0;

    @Switch(
        title = "Bestätigungs-Menü automatisch bestätigen",
        description = "Falls der Server nach /ah sell ein Bestätigungs-Menü öffnet, wird der Bestätigen-Knopf "
            + "(Name enthält ein Wort aus der Liste unten, oder grüne Glasscheibe/Wolle/Beton) geklickt.",
        category = "Verkauf",
        subcategory = "Server"
    )
    public static boolean autoConfirm = true;

    @Text(
        title = "Bestätigen-Wörter",
        description = "Kommagetrennt, Groß/Kleinschreibung egal.",
        category = "Verkauf",
        subcategory = "Server"
    )
    public static String confirmWords = "confirm, bestätigen, accept, yes, list";

    @Text(
        title = "Stopp-Wörter im Chat",
        description = "Kommt eine Server-Nachricht mit einem dieser Wörter, wird sofort gestoppt "
            + "(z. B. Angebots-Limit erreicht). Kommagetrennt.",
        category = "Verkauf",
        subcategory = "Server"
    )
    public static String stopWords = "maximum, limit, reached, can't, cannot, not allowed";

    @Text(
        title = "Brems-Wörter im Chat",
        description = "Meldet der Server so etwas (z. B. \"too fast\"), wird nicht gestoppt, sondern kurz pausiert "
            + "und für den Rest des Durchlaufs etwas langsamer gemacht. Kommagetrennt.",
        category = "Verkauf",
        subcategory = "Server"
    )
    public static String throttleWords = "too fast, slow down, cooldown, please wait, zu schnell, warte";

    // ---------------------------------------------------------------- Hotbar

    @Text(
        title = "Erlaubte Hotbar-Slots",
        description = "Welche Slots benutzt werden dürfen, z. B. \"1-9\" oder \"3-9\" oder \"2,4,6-9\". "
            + "Slots mit anderen Items (Schwert, Spitzhacke …) werden immer übersprungen.",
        category = "Hotbar",
        subcategory = "Slots"
    )
    public static String hotbarSlots = "1-9";

    @Switch(
        title = "Zufällige Slot-Reihenfolge",
        description = "Aus: Slot 1 → 9 nacheinander. An: die befüllten Slots werden durcheinander verkauft.",
        category = "Hotbar",
        subcategory = "Slots"
    )
    public static boolean shuffleSlots = false;

    // ---------------------------------------------------------------- Timing

    @Dropdown(
        title = "Geschwindigkeit",
        description = "Schnell ≈ 0,5 s pro Angebot (wie ein schneller Spieler). "
            + "\"Eigene\" benutzt die Pausen unten.",
        category = "Timing",
        subcategory = "Tempo",
        options = {"Schnell", "Normal", "Vorsichtig", "Eigene Werte"}
    )
    public static int speedPreset = 0;

    @Number(title = "Klick-Pause min.", unit = "ms", category = "Timing", subcategory = "Inventar",
        description = "Nur bei Geschwindigkeit \"Eigene Werte\". Pause zwischen zwei Inventar-Klicks beim Auffüllen der Hotbar.", min = 50f, max = 2000f)
    public static int clickDelayMin = 180;

    @Number(title = "Klick-Pause max.", unit = "ms", category = "Timing", subcategory = "Inventar", min = 50f, max = 3000f)
    public static int clickDelayMax = 380;

    @Number(title = "Slot-Wechsel-Pause min.", unit = "ms", category = "Timing", subcategory = "Verkauf",
        description = "Pause nach dem Wechsel auf den nächsten Hotbar-Slot, bevor verkauft wird.", min = 50f, max = 3000f)
    public static int switchDelayMin = 250;

    @Number(title = "Slot-Wechsel-Pause max.", unit = "ms", category = "Timing", subcategory = "Verkauf", min = 50f, max = 5000f)
    public static int switchDelayMax = 500;

    @Number(title = "Pause nach Verkauf min.", unit = "ms", category = "Timing", subcategory = "Verkauf",
        description = "Pause nach jedem /ah sell, bevor es weitergeht.", min = 200f, max = 10000f)
    public static int sellDelayMin = 900;

    @Number(title = "Pause nach Verkauf max.", unit = "ms", category = "Timing", subcategory = "Verkauf", min = 200f, max = 15000f)
    public static int sellDelayMax = 1600;

    // ---------------------------------------------------------------- Sicherheit

    @Number(
        title = "AFK-Schutz",
        unit = "s",
        description = "Stoppt, wenn du so lange weder Maus noch Tastatur benutzt hast. Du musst am PC sitzen!",
        category = "Sicherheit",
        subcategory = "Anwesenheit",
        min = 10f,
        max = 300f
    )
    public static int afkTimeoutSeconds = 45;

    @Switch(
        title = "Stopp wenn Fenster nicht im Fokus",
        description = "Stoppt sofort, wenn du aus Minecraft heraus tabbst.",
        category = "Sicherheit",
        subcategory = "Anwesenheit"
    )
    public static boolean stopOnUnfocus = true;

    @Switch(
        title = "Stopp bei Bewegung",
        description = "Stoppt, sobald du läufst, springst, schlägst oder ein eigenes Menü öffnest (Not-Aus).",
        category = "Sicherheit",
        subcategory = "Anwesenheit"
    )
    public static boolean stopOnMovement = true;

    // ---------------------------------------------------------------- Buttons

    @Button(title = "Verkauf starten", text = "Start", category = "Verkauf", subcategory = "Was & wie teuer",
        description = "Startet, sobald du dieses Menü schließt. Alternativ: Start/Stopp-Taste (Standard K, unter Optionen → Steuerung).")
    public void startButton() {
        AutoSeller.INSTANCE.requestStart();
    }

    @Button(title = "Verkauf stoppen", text = "Stopp", category = "Verkauf", subcategory = "Was & wie teuer")
    public void stopButton() {
        AutoSeller.INSTANCE.stop("Über das Menü gestoppt.");
    }

    public DonutSellConfig() {
        super("donut-sell.json", "Donut Sell", Category.UTILITY);
    }
}
