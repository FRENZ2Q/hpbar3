package com.example.hpbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HpBarClient implements ClientModInitializer {
    public static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        HpBarConfig.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hpbar.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.hpbar"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                client.setScreen(new HpBarConfigScreen(client.currentScreen));
            }
        });

        // рисуем полоски после отрисовки всех сущностей
        WorldRenderEvents.AFTER_ENTITIES.register(HealthBarRenderer::onRender);
    }
}
