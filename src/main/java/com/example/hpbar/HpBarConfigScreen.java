package com.example.hpbar;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HpBarConfigScreen extends Screen {
    private static final int[] DISTANCES = {16, 32, 64, 128, 256};
    private static final float[] SCALES = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

    private final Screen parent;

    public HpBarConfigScreen(Screen parent) {
        super(Text.literal("HP Bar — настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        HpBarConfig c = HpBarConfig.get();
        int bw = 150, bh = 20, step = 24;
        int left = width / 2 - bw - 3;
        int right = width / 2 + 3;
        int top = 40;

        toggle(width / 2 - bw / 2, top - 4, "Мод", () -> c.enabled, v -> c.enabled = v);
        top += 26;

        // левая колонка — КТО
        toggle(left, top, "Игроки", () -> c.players, v -> c.players = v);
        toggle(left, top + step, "Враждебные", () -> c.hostile, v -> c.hostile = v);
        toggle(left, top + step * 2, "Животные", () -> c.animals, v -> c.animals = v);
        toggle(left, top + step * 3, "Остальные мобы", () -> c.otherMobs, v -> c.otherMobs = v);
        toggle(left, top + step * 4, "Стойки для брони", () -> c.armorStands, v -> c.armorStands = v);
        toggle(left, top + step * 5, "Я сам (3-е лицо)", () -> c.self, v -> c.self = v);

        // правая колонка — КАК
        toggle(right, top, "Над невидимыми", () -> c.showInvisible, v -> c.showInvisible = v);
        toggle(right, top + step, "Сквозь стены", () -> c.throughWalls, v -> c.throughWalls = v);
        toggle(right, top + step * 2, "Цифры HP", () -> c.showText, v -> c.showText = v);
        toggle(right, top + step * 3, "Только если ранен", () -> c.onlyDamaged, v -> c.onlyDamaged = v);

        addDrawableChild(ButtonWidget.builder(Text.literal("Дистанция: " + c.maxDistance), btn -> {
            int idx = 0;
            for (int i = 0; i < DISTANCES.length; i++) if (DISTANCES[i] == c.maxDistance) idx = i;
            c.maxDistance = DISTANCES[(idx + 1) % DISTANCES.length];
            btn.setMessage(Text.literal("Дистанция: " + c.maxDistance));
            HpBarConfig.save();
        }).dimensions(right, top + step * 4, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Размер: x" + c.scale), btn -> {
            int idx = 0;
            for (int i = 0; i < SCALES.length; i++) if (Math.abs(SCALES[i] - c.scale) < 0.01f) idx = i;
            c.scale = SCALES[(idx + 1) % SCALES.length];
            btn.setMessage(Text.literal("Размер: x" + c.scale));
            HpBarConfig.save();
        }).dimensions(right, top + step * 5, bw, bh).build());

        toggle(left, top + step * 6, "Ники игроков", () -> c.showPlayerNames, v -> c.showPlayerNames = v);
        toggle(right, top + step * 6, "Имена мобов", () -> c.showMobNames, v -> c.showMobNames = v);

        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), btn -> close())
            .dimensions(width / 2 - 75, top + step * 7 + 8, 150, bh).build());
    }

    private void toggle(int x, int y, String name, Supplier<Boolean> get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.get()), btn -> {
            set.accept(!get.get());
            btn.setMessage(label(name, get.get()));
            HpBarConfig.save();
        }).dimensions(x, y, 150, 20).build());
    }

    private static Text label(String name, boolean value) {
        return Text.literal(name + ": ")
            .append(Text.literal(value ? "ВКЛ" : "ВЫКЛ").formatted(value ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void close() {
        HpBarConfig.save();
        client.setScreen(parent);
    }
}
