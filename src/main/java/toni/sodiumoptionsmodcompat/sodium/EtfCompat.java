package toni.sodiumoptionsmodcompat.sodium;

import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.network.chat.Component;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.ETFConfig;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ETF pages. ETF's own screen reloads resource packs after saving, so every option here carries
 * {@link OptionFlag#REQUIRES_ASSET_RELOAD} to get the same behaviour from Sodium.
 */
final class EtfCompat {
    private EtfCompat() {
    }

    private static ETFConfig cfg() {
        return ETF.config().getConfig();
    }

    private static Component t(String key) {
        return Component.translatable("config.entity_texture_features." + key);
    }

    static void register(ConfigBuilder b) {
        ModOptionsBuilder mod = SodiumPages.registerFor(b, "entity_texture_features", "Entity Texture Features");
        StorageEventHandler storage = SodiumPages.storage("ETF config", () -> ETF.config().saveToFile());
        SodiumPages.addPages(b, mod, "Entity Texture Features", List.of(
                () -> textures(b, storage),
                () -> misc(b, storage)));
    }

    private static BooleanOptionBuilder bool(ConfigBuilder b, StorageEventHandler storage, String id, String title,
                                             String tooltip, boolean def, Consumer<Boolean> save, Supplier<Boolean> load) {
        return SodiumPages.bool(b, "etf/" + id, t(title), t(tooltip), def, storage, save, load)
                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD);
    }

    private static <E extends Enum<E>> EnumOptionBuilder<E> choice(ConfigBuilder b, StorageEventHandler storage, String id,
                                                                  Class<E> type, String title, String tooltip, E def,
                                                                  Function<E, Component> names,
                                                                  Consumer<E> save, Supplier<E> load) {
        return SodiumPages.enumeration(b, "etf/" + id, type, t(title), t(tooltip), def, names, storage, save, load)
                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD);
    }

    private static IntegerOptionBuilder number(ConfigBuilder b, StorageEventHandler storage, String id, String title,
                                               String tooltip, int def, int min, int max,
                                               Consumer<Integer> save, Supplier<Integer> load) {
        return SodiumPages.integer(b, "etf/" + id, t(title), t(tooltip), def, min, max, 1,
                        v -> Component.literal(Integer.toString(v)), storage, save, load)
                .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD);
    }

    private static OptionPageBuilder textures(ConfigBuilder b, StorageEventHandler s) {
        return b.createOptionPage()
                .setName(Component.translatable("options.sodiumoptionsmodcompat.etf.textures"))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(bool(b, s, "enable_custom_textures", "enable_custom_textures.title", "enable_custom_textures.tooltip", true,
                                v -> cfg().enableCustomTextures = v, () -> cfg().enableCustomTextures))
                        .addOption(choice(b, s, "texture_update_frequency", ETFConfig.UpdateFrequency.class,
                                "texture_update_frequency.title", "texture_update_frequency.tooltip", ETFConfig.UpdateFrequency.Fast,
                                v -> switch (v) {
                                    case Never -> t("update_frequency.never");
                                    case Slow -> t("update_frequency.slow");
                                    case Average -> t("update_frequency.average");
                                    case Fast -> t("update_frequency.fast");
                                    case Instant -> t("update_frequency.instant");
                                },
                                v -> cfg().textureUpdateFrequency_V2 = v, () -> cfg().textureUpdateFrequency_V2))
                        .addOption(bool(b, s, "custom_block_entity", "custom_block_entity.title", "custom_block_entity.tooltip", true,
                                v -> cfg().enableCustomBlockEntities = v, () -> cfg().enableCustomBlockEntities))
                        .addOption(bool(b, s, "disable_default_directory", "disable_default_directory.title", "disable_default_directory.tooltip", false,
                                v -> cfg().disableVanillaDirectoryVariantTextures = v, () -> cfg().disableVanillaDirectoryVariantTextures)))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(bool(b, s, "enable_emissive_textures", "enable_emissive_textures.title", "enable_emissive_textures.tooltip", true,
                                v -> cfg().enableEmissiveTextures = v, () -> cfg().enableEmissiveTextures))
                        .addOption(bool(b, s, "emissive_block_entity", "emissive_block_entity.title", "emissive_block_entity.tooltip", true,
                                v -> cfg().enableEmissiveBlockEntities = v, () -> cfg().enableEmissiveBlockEntities))
                        .addOption(choice(b, s, "emissive_mode", ETFConfig.EmissiveRenderModes.class,
                                "emissive_mode.title", "emissive_mode.tooltip", ETFConfig.EmissiveRenderModes.DULL,
                                v -> switch (v) {
                                    case DULL -> t("emissive_mode.dull");
                                    case BRIGHT -> t("emissive_mode.bright");
                                },
                                v -> cfg().emissiveRenderMode = v, () -> cfg().emissiveRenderMode))
                        .addOption(bool(b, s, "always_check_vanilla_emissive_suffix", "always_check_vanilla_emissive_suffix.title", "always_check_vanilla_emissive_suffix.tooltip", true,
                                v -> cfg().alwaysCheckVanillaEmissiveSuffix = v, () -> cfg().alwaysCheckVanillaEmissiveSuffix))
                        .addOption(bool(b, s, "armor_enable", "armor_enable", "armor_enable.tooltip", true,
                                v -> cfg().enableArmorAndTrims = v, () -> cfg().enableArmorAndTrims))
                        .addOption(bool(b, s, "enchanted_enable", "enchanted_enable", "enchanted_enable.tooltip", true,
                                v -> cfg().enableEnchantedTextures = v, () -> cfg().enableEnchantedTextures)))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(bool(b, s, "player_skin_features", "player_skin_features.title", "player_skin_features.tooltip", true,
                                v -> cfg().skinFeaturesEnabled = v, () -> cfg().skinFeaturesEnabled))
                        .addOption(choice(b, s, "transparent_skins", ETFConfig.SkinTransparencyMode.class,
                                "transparent_skins.title", "transparent_skins.tooltip", ETFConfig.SkinTransparencyMode.ETF_SKINS_ONLY,
                                v -> switch (v) {
                                    case VANILLA -> t("transparent_skins.vanilla");
                                    case ETF_SKINS_ONLY -> t("transparent_skins.etf");
                                    case ALL -> t("transparent_skins.all");
                                },
                                v -> cfg().skinTransparencyMode = v, () -> cfg().skinTransparencyMode))
                        .addOption(bool(b, s, "transparent_skins_extra", "transparent_skins_extra.title", "transparent_skins_extra.tooltip", false,
                                v -> cfg().skinTransparencyInExtraPixels = v, () -> cfg().skinTransparencyInExtraPixels))
                        .addOption(bool(b, s, "enemy_team_skin_features", "enable_enemy_team_players_skin_features.title", "enable_enemy_team_players_skin_features.tooltip", true,
                                v -> cfg().enableEnemyTeamPlayersSkinFeatures = v, () -> cfg().enableEnemyTeamPlayersSkinFeatures)))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(bool(b, s, "blinking", "blinking_mob_settings.title", "blinking_mob_settings.tooltip", true,
                                v -> cfg().enableBlinking = v, () -> cfg().enableBlinking))
                        .addOption(number(b, s, "blink_frequency", "blink_frequency.title", "blink_frequency.tooltip", 150, 1, 1024,
                                v -> cfg().blinkFrequency = v, () -> cfg().blinkFrequency))
                        .addOption(number(b, s, "blink_length", "blink_length.title", "blink_length.tooltip", 1, 1, 20,
                                v -> cfg().blinkLength = v, () -> cfg().blinkLength)));
    }

    private static OptionPageBuilder misc(ConfigBuilder b, StorageEventHandler s) {
        return b.createOptionPage()
                .setName(Component.translatable("options.sodiumoptionsmodcompat.etf.misc"))
                .addOptionGroup(b.createOptionGroup()
                        .addOption(choice(b, s, "illegal_texture_paths", ETFConfig.IllegalPathMode.class,
                                "allow_illegal_texture_paths.title", "allow_illegal_texture_paths.tooltip", ETFConfig.IllegalPathMode.None,
                                v -> switch (v) {
                                    case None -> Component.translatable("options.off");
                                    case Entity -> t("illegal_path_mode.entity");
                                    case All -> t("illegal_path_mode.all");
                                },
                                v -> cfg().illegalPathSupportMode = v, () -> cfg().illegalPathSupportMode))
                        .addOption(bool(b, s, "warden", "warden.title", "warden.tooltip", true,
                                v -> cfg().enableFullBodyWardenTextures = v, () -> cfg().enableFullBodyWardenTextures)));
    }
}
