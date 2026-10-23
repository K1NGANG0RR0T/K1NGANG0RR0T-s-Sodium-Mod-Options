package toni.sodiumoptionsmodcompat.sodium;

import dynamic_fps.impl.DynamicFPSMod;
import dynamic_fps.impl.PowerState;
import dynamic_fps.impl.config.Config;
import dynamic_fps.impl.config.DynamicFPSConfig;
import dynamic_fps.impl.config.option.GraphicsState;
import dynamic_fps.impl.util.VariableStepTransformer;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Mirrors Dynamic FPS' own (Cloth Config) screen: a General page plus one page per configurable
 * power state. Saving goes through {@link DynamicFPSMod#onConfigChanged()}, which is exactly what
 * Dynamic FPS' own screen calls.
 */
final class DynamicFpsCompat {
    private static final int NO_FRAME_RATE_LIMIT = 260;

    private DynamicFpsCompat() {
    }

    private static Component t(String key) {
        return Component.translatable("config.dynamic_fps." + key);
    }

    /** Same slider steps Dynamic FPS uses in its own screen (fine steps at low FPS, coarse at high). */
    private static VariableStepTransformer fpsSteps() {
        VariableStepTransformer steps = new VariableStepTransformer();
        steps.addStep(1, 20);
        steps.addStep(2, 72);
        steps.addStep(3, 75);
        steps.addStep(5, 100);
        steps.addStep(10, NO_FRAME_RATE_LIMIT);
        return steps;
    }

    static void register(ConfigBuilder b) {
        ModOptionsBuilder mod = SodiumPages.registerFor(b, "dynamic_fps", "Dynamic FPS");
        StorageEventHandler storage = SodiumPages.storage("Dynamic FPS config", DynamicFPSMod::onConfigChanged);

        List<Supplier<? extends PageBuilder>> pages = new ArrayList<>();
        pages.add(() -> general(b, storage));
        for (PowerState state : PowerState.values()) {
            if (state.configurabilityLevel == PowerState.ConfigurabilityLevel.NONE) {
                continue; // FOCUSED is never throttled, Dynamic FPS doesn't expose it either
            }
            pages.add(() -> powerState(b, storage, state));
        }
        SodiumPages.addPages(b, mod, "Dynamic FPS", pages);
    }

    private static OptionPageBuilder general(ConfigBuilder b, StorageEventHandler storage) {
        DynamicFPSConfig config = DynamicFPSConfig.INSTANCE;
        return b.createOptionPage()
                .setName(t("category.general"))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(SodiumPages.bool(b, "dynamic_fps/enabled",
                                t("enabled"), t("enabled"), true, storage,
                                config::setEnabled, config::enabled))
                        .addOption(SodiumPages.bool(b, "dynamic_fps/uncap_menu_frame_rate",
                                t("uncap_menu_frame_rate"), t("uncap_menu_frame_rate_tooltip"), false, storage,
                                config::setUncapMenuFrameRate, config::uncapMenuFrameRate)));
    }

    private static OptionPageBuilder powerState(ConfigBuilder b, StorageEventHandler storage, PowerState state) {
        String name = state.name().toLowerCase(Locale.ROOT);
        String prefix = "dynamic_fps/" + name + "/";
        Supplier<Config> config = () -> DynamicFPSConfig.INSTANCE.get(state);
        VariableStepTransformer steps = fpsSteps();
        ControlValueFormatter fpsLabel = step -> {
            int fps = steps.toValue(step);
            return fps == NO_FRAME_RATE_LIMIT
                    ? Component.translatable("options.framerateLimit.max")
                    : Component.translatable("options.framerate", fps);
        };

        OptionPageBuilder page = b.createOptionPage()
                .setName(t("category." + name))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(SodiumPages.integer(b, prefix + "frame_rate_target",
                                t("frame_rate_target"), t("frame_rate_target"),
                                steps.toStep(60), 0, steps.toStep(NO_FRAME_RATE_LIMIT), 1, fpsLabel, storage,
                                step -> config.get().setFrameRateTarget(steps.toValue(step)),
                                () -> steps.toStep(config.get().frameRateTarget())))
                        .addOption(SodiumPages.bool(b, prefix + "enable_vsync",
                                Component.translatable("options.vsync"), Component.translatable("options.vsync"),
                                false, storage,
                                v -> config.get().setEnableVsync(v),
                                () -> config.get().enableVsync())));

        if (state.configurabilityLevel == PowerState.ConfigurabilityLevel.SOME) {
            return page; // "On Battery" only exposes frame rate + vsync in Dynamic FPS too
        }

        OptionGroupBuilder volume = b.createOptionGroup().setName(t("volume_multiplier"));
        for (SoundSource source : SoundSource.values()) {
            volume.addOption(SodiumPages.integer(b, prefix + "volume_" + source.getName(),
                    Component.translatable("soundCategory." + source.getName()), t("volume_multiplier"),
                    100, 0, 100, 1, v -> Component.literal(v + "%"), storage,
                    v -> config.get().setVolumeMultiplier(source, v / 100f),
                    () -> Math.round(config.get().rawVolumeMultiplier(source) * 100f)));
        }
        page.addOptionGroup(volume);

        page.addOptionGroup(b.createOptionGroup()
                .addOption(SodiumPages.enumeration(b, prefix + "graphics_state", GraphicsState.class,
                        t("graphics_state"), t("graphics_state_minimal_tooltip"), GraphicsState.DEFAULT,
                        v -> t("graphics_state_" + v.name().toLowerCase(Locale.ROOT)), storage,
                        v -> config.get().setGraphicsState(v),
                        () -> config.get().graphicsState()))
                .addOption(SodiumPages.bool(b, prefix + "show_toasts",
                        t("show_toasts"), t("show_toasts_tooltip"), true, storage,
                        v -> config.get().setShowToasts(v),
                        () -> config.get().showToasts()))
                .addOption(SodiumPages.bool(b, prefix + "run_garbage_collector",
                        t("run_garbage_collector"), t("run_garbage_collector_tooltip"), false, storage,
                        v -> config.get().setRunGarbageCollector(v),
                        () -> config.get().runGarbageCollector())));
        return page;
    }
}
