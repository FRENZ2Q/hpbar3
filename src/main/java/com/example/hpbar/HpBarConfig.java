package com.example.hpbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HpBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hpbar.json");
    private static HpBarConfig instance = new HpBarConfig();

    // --- что показывать ---
    public boolean enabled = true;
    public boolean players = true;      // другие игроки
    public boolean hostile = true;      // враждебные мобы (зомби, скелеты, крипер...)
    public boolean animals = true;      // животные (коровы, овцы, волки...)
    public boolean otherMobs = true;    // остальные мобы (жители, големы, рыбы...)
    public boolean armorStands = false; // стойки для брони
    public boolean self = false;        // свой персонаж (вид от 3-го лица)

    // --- как показывать ---
    public boolean showInvisible = true; // показывать над невидимыми
    public boolean throughWalls = true;  // видно сквозь стены
    public boolean showText = true;      // цифры "12/20"
    public boolean onlyDamaged = false;  // только если здоровье не полное
    public boolean showPlayerNames = true; // ник игрока над полоской
    public boolean showMobNames = false;   // имя моба над полоской
    public int maxDistance = 64;
    public float scale = 1.0f;

    /** Ручной список скрытых типов, например ["minecraft:bat", "minecraft:villager"] */
    public List<String> hiddenTypes = new ArrayList<>();

    public static HpBarConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                HpBarConfig loaded = GSON.fromJson(Files.readString(FILE), HpBarConfig.class);
                if (loaded != null) {
                    if (loaded.hiddenTypes == null) loaded.hiddenTypes = new ArrayList<>();
                    instance = loaded;
                }
            } else {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldShow(LivingEntity e, MinecraftClient mc) {
        if (e.isInvisible() && !showInvisible) return false;
        if (onlyDamaged && e.getHealth() >= e.getMaxHealth()) return false;

        if (!hiddenTypes.isEmpty()) {
            String id = Registries.ENTITY_TYPE.getId(e.getType()).toString();
            if (hiddenTypes.contains(id)) return false;
        }

        if (e instanceof ArmorStandEntity) return armorStands;
        if (e instanceof PlayerEntity) {
            if (e == mc.player) return self;
            return players;
        }
        if (e instanceof Monster) return hostile;
        if (e instanceof AnimalEntity) return animals;
        return otherMobs;
    }
}
