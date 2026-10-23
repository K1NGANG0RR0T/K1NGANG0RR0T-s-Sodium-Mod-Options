package toni.sodiumoptionsmodcompat.integration;

/**
 * Kept so the existing mod class can still call {@code Integrations.init()}.
 * <p>
 * The old implementation listened for Sodium Options API's {@code OptionGUIConstruction} event,
 * which can never fire on Sodium 0.8 because the GUI classes it hooked were removed. Pages are now
 * registered through Sodium's own config API instead; see
 * {@code toni.sodiumoptionsmodcompat.sodium.ModCompatConfigEntryPoint}.
 */
public class Integrations {
    public static void init() {
    }
}
