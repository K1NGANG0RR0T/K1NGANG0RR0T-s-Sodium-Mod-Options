## 1.0.0-k1ng.sodium0.8 (K1NGANG0RR0T fork: Sodium 0.8 port, NeoForge 1.21.1)
- Renamed to K1NGANG0RR0T's Sodium Mod Options; authors credit K1NGANG0RR0T (fork), Toni and DeeChael (original authors); license field corrected to MIT to match LICENSE.md.
- Sodium 0.8 removed the options GUI classes (`OptionPage`, `OptionGroup`, `OptionImpl`, `OptionStorage`, `SodiumOptionsGUI`) that every page here was built on, and Sodium Options API's events can no longer fire. All pages are now registered through Sodium's native `ConfigEntryPoint` (`toni.sodiumoptionsmodcompat.sodium`), so they also appear in Reese's Sodium Options.
- Continuity: Connected Textures, Emissive Textures, Custom Block Layers (renderer reload on change, like Continuity's own screen).
- Dynamic FPS (3.11.x, `dynamic_fps.impl`): General page plus one page per configurable power state, using `DynamicFPSConfig.INSTANCE.get(state)` and `DynamicFPSMod.onConfigChanged()`; frame-rate slider uses Dynamic FPS' own step mapping. Replaces the broken `DynamicFPSConfigAccessor` mixin, which targeted a field that no longer exists.
- ETF (7.2.x): Textures and Misc pages; changes trigger a resource reload like ETF's own screen.
- EMF integration dropped.
- Each integration registers under its own config id (`sodiumoptionsmodcompat_<modid>`) and option namespace, is skipped if the target mod already registers native Sodium options, and wraps every getter/setter so a version mismatch logs a warning instead of crashing Sodium's config system.
- Sodium Options API is now optional.

## Changelog
- Initial release