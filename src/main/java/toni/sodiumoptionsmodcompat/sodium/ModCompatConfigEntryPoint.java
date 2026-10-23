package toni.sodiumoptionsmodcompat.sodium;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;

/**
 * Sodium 0.8 entry point for Sodium Options Mod Compat.
 * <p>
 * Each integration lives in its own class and is only touched when its mod is loaded, so a
 * missing mod never causes a class-loading error here. Each one is also isolated with its own
 * try/catch: one broken integration can't prevent the others (or Sodium) from loading.
 */
public class ModCompatConfigEntryPoint implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        register(builder, "continuity", "Continuity", () -> ContinuityCompat.register(builder));
        register(builder, "dynamic_fps", "Dynamic FPS", () -> DynamicFpsCompat.register(builder));
        register(builder, "entity_texture_features", "Entity Texture Features", () -> EtfCompat.register(builder));
    }

    private static void register(ConfigBuilder builder, String modId, String label, Runnable registration) {
        if (!SodiumPages.isLoaded(modId)) {
            return;
        }
        if (SodiumPages.hasNativeSodiumConfig(modId)) {
            SodiumPages.LOGGER.info("[Sodium Options Mod Compat] " + label + " registers its own Sodium options; skipping the compat pages");
            return;
        }
        try {
            registration.run();
        } catch (Throwable t) {
            SodiumPages.LOGGER.error("[Sodium Options Mod Compat] Could not add Sodium options for " + label, t);
        }
    }
}
