package toni.sodiumoptionsmodcompat.sodium;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helpers for registering pages through Sodium 0.8's native config API.
 * <p>
 * Every value getter/setter that touches another mod's config is wrapped so that a
 * signature change in that mod logs a warning instead of crashing Sodium's config
 * system (which would take the whole game down with it).
 */
public final class SodiumPages {
    public static final String NAMESPACE = "sodiumoptionsmodcompat";
    public static final Logger LOGGER = LogManager.getLogger("SodiumOptionsModCompat");
    private static final String CONFIG_API_KEY = "sodium:config_api_user";
    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<>());

    private SodiumPages() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    public static void warnOnce(String what, Throwable t) {
        if (WARNED.add(what)) {
            LOGGER.warn("[Sodium Options Mod Compat] '" + what + "' failed, falling back to a safe value", t);
        }
    }

    public static <V> Supplier<V> guardLoad(String what, Supplier<V> loader, V fallback) {
        return () -> {
            try {
                V value = loader.get();
                return value != null ? value : fallback;
            } catch (Throwable t) {
                warnOnce("load " + what, t);
                return fallback;
            }
        };
    }

    public static <V> Consumer<V> guardSave(String what, Consumer<V> saver) {
        return value -> {
            try {
                saver.accept(value);
            } catch (Throwable t) {
                warnOnce("save " + what, t);
            }
        };
    }

    public static StorageEventHandler storage(String what, Runnable flush) {
        return () -> {
            try {
                flush.run();
            } catch (Throwable t) {
                warnOnce("flush " + what, t);
            }
        };
    }

    public static BooleanOptionBuilder bool(ConfigBuilder b, String id, Component name, Component tooltip,
                                            boolean def, StorageEventHandler storage,
                                            Consumer<Boolean> save, Supplier<Boolean> load) {
        return b.createBooleanOption(id(id))
                .setName(name)
                .setTooltip(tooltip)
                .setDefaultValue(def)
                .setStorageHandler(storage)
                .setBinding(guardSave(id, save), guardLoad(id, load, def));
    }

    public static IntegerOptionBuilder integer(ConfigBuilder b, String id, Component name, Component tooltip,
                                               int def, int min, int max, int step, ControlValueFormatter formatter,
                                               StorageEventHandler storage,
                                               Consumer<Integer> save, Supplier<Integer> load) {
        return b.createIntegerOption(id(id))
                .setName(name)
                .setTooltip(tooltip)
                .setRange(min, max, step)
                .setValueFormatter(formatter)
                .setDefaultValue(def)
                .setStorageHandler(storage)
                .setBinding(guardSave(id, save), guardLoad(id, load, def));
    }

    public static <E extends Enum<E>> EnumOptionBuilder<E> enumeration(ConfigBuilder b, String id, Class<E> type,
                                                                      Component name, Component tooltip, E def,
                                                                      Function<E, Component> names,
                                                                      StorageEventHandler storage,
                                                                      Consumer<E> save, Supplier<E> load) {
        return b.createEnumOption(id(id), type)
                .setName(name)
                .setTooltip(tooltip)
                .setElementNameProvider(names)
                .setDefaultValue(def)
                .setStorageHandler(storage)
                .setBinding(guardSave(id, save), guardLoad(id, load, def));
    }

    /**
     * Adds pages one at a time. A page that fails to build is skipped and logged; if every page
     * fails, a placeholder is added so Sodium never sees an empty mod entry (which is fatal).
     */
    public static void addPages(ConfigBuilder builder, ModOptionsBuilder mod, String label,
                                List<Supplier<? extends PageBuilder>> pages) {
        int added = 0;
        for (Supplier<? extends PageBuilder> page : pages) {
            try {
                mod.addPage(page.get());
                added++;
            } catch (Throwable t) {
                LOGGER.error("[Sodium Options Mod Compat] Skipping a " + label + " options page that failed to build", t);
            }
        }
        if (added == 0) {
            mod.addPage(builder.createExternalPage()
                    .setName(Component.literal(label + " (unavailable, see log)"))
                    .setScreenConsumer(screen -> {
                    }));
        }
    }

    public static boolean isLoaded(String modId) {
        try {
            return ModList.get().isLoaded(modId);
        } catch (Throwable t) {
            return false;
        }
    }

    private static IModInfo info(String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).orElse(null);
    }

    public static String displayName(String modId, String fallback) {
        try {
            IModInfo info = info(modId);
            String name = info != null ? info.getDisplayName() : null;
            return name == null || name.isBlank() ? fallback : name;
        } catch (Throwable t) {
            return fallback;
        }
    }

    public static String version(String modId) {
        try {
            IModInfo info = info(modId);
            String version = info != null && info.getVersion() != null ? info.getVersion().toString() : null;
            return version == null || version.isBlank() ? "unknown" : version;
        } catch (Throwable t) {
            return "unknown";
        }
    }

    /** True if the mod already registers its own pages with Sodium, in which case we stay out of the way. */
    public static boolean hasNativeSodiumConfig(String modId) {
        try {
            IModInfo info = info(modId);
            return info != null && info.getModProperties() != null && info.getModProperties().containsKey(CONFIG_API_KEY);
        } catch (Throwable t) {
            return false;
        }
    }

    /** Registers a mod entry under a compat-owned config id, so it can never collide with the mod's own id. */
    public static ModOptionsBuilder registerFor(ConfigBuilder builder, String modId, String fallbackName) {
        return builder.registerModOptions(NAMESPACE + "_" + modId, displayName(modId, fallbackName), version(modId));
    }
}
