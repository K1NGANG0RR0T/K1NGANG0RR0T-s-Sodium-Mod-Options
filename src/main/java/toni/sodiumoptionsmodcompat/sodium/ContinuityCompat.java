package toni.sodiumoptionsmodcompat.sodium;

import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.config.Option;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

final class ContinuityCompat {
    private ContinuityCompat() {
    }

    static void register(ConfigBuilder b) {
        ModOptionsBuilder mod = SodiumPages.registerFor(b, "continuity", "Continuity");
        SodiumPages.addPages(b, mod, "Continuity", List.of(() -> page(b)));
    }

    private static OptionPageBuilder page(ConfigBuilder b) {
        StorageEventHandler storage = SodiumPages.storage("Continuity config", () -> ContinuityConfig.INSTANCE.save());
        OptionGroupBuilder group = b.createOptionGroup();
        group.addOption(option(b, storage, "connected_textures", () -> ContinuityConfig.INSTANCE.connectedTextures));
        group.addOption(option(b, storage, "emissive_textures", () -> ContinuityConfig.INSTANCE.emissiveTextures));
        group.addOption(option(b, storage, "custom_block_layers", () -> ContinuityConfig.INSTANCE.customBlockLayers));
        return b.createOptionPage()
                .setName(Component.translatable("options.sodiumoptionsmodcompat.general"))
                .addOptionGroup(group);
    }

    private static net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder option(
            ConfigBuilder b, StorageEventHandler storage, String key, Supplier<Option<Boolean>> option) {
        return SodiumPages.bool(b, "continuity/" + key,
                        Component.translatable("options.continuity." + key),
                        Component.translatable("options.continuity." + key + ".tooltip"),
                        true, storage,
                        value -> option.get().set(value),
                        () -> option.get().get())
                // same effect as Continuity's own screen: rebuild chunks so the change is visible
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD);
    }
}
