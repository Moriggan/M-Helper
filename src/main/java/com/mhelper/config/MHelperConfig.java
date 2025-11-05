package com.mhelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MHelperConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mhelper.json");

    public enum HudPalette {
        DEFAULT(0x1F6FE3, 0xD7A000, 0x9A0D0D),
        COLORBLIND_HIGH_CONTRAST(0x2D9CDB, 0xF2994A, 0xEB5757),
        COLORBLIND_GRAYSCALE(0x5C6BC0, 0xBDBDBD, 0xF44336);

        public final int notReady;
        public final int almost;
        public final int perfect;

        HudPalette(int notReady, int almost, int perfect) {
            this.notReady = notReady;
            this.almost = almost;
            this.perfect = perfect;
        }
    }

    public boolean autoEquipElytra = true;
    public double autoGlideThreshold = 2.5;
    public double perfectWindowStart = 0.15;
    public double perfectWindowEnd = 0.45;
    public boolean playPerfectChime = true;
    public double chimeVolume = 0.6;
    public boolean showAimAssist = true;
    public double timingBarOpacity = 0.8;
    public double hudScale = 1.0;
    public HudPalette hudPalette = HudPalette.DEFAULT;
    public boolean requireSneakForAuto = false;
    public double fallDistanceThreshold = 1.5;
    public boolean autoClickerEnabled = false;
    public boolean autoClickerHoldToFire = true;
    public double autoClickerCps = 12.0;
    public double autoClickerCooldownThreshold = 0.92;
    public boolean criticalHelperEnabled = true;
    public double criticalHelperCooldownGate = 0.9;
    public long criticalHelperJumpDelayMs = 65L;
    public boolean autoWaterMlgEnabled = true;
    public double autoWaterMlgFallDistance = 15.0;
    public boolean autoWaterMlgRefill = true;

    private static MHelperConfig instance;

    public static MHelperConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static MHelperConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                MHelperConfig config = GSON.fromJson(reader, MHelperConfig.class);
                return config != null ? config : new MHelperConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MHelperConfig config = new MHelperConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
