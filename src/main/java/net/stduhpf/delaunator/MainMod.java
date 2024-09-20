package net.stduhpf.delaunator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.stduhpf.delaunator.gui.DeGui;
import net.stduhpf.delaunator.gui.DeScreen;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	private static KeyBinding guiKey = new KeyBinding(
			"key.delaunator.gui", // The translation key of the keybinding's name
			InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
			GLFW.GLFW_KEY_B, // The keycode of the key
			"category.delaunator.test" // The translation key of the keybinding's category.
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		LOGGER.info("Hello world!");
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("degui").executes(context -> {
				LOGGER.info("Useless command was sent!");
				MinecraftClient instance = MinecraftClient.getInstance();
				instance.player.setPos(0,260,0);
				return 0;
			}));
		});
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (guiKey.wasPressed()) {
				DeScreen screen = new DeScreen(new DeGui());
				client.player.sendMessage(Text.literal("Key 1 was pressed!"), false);
				MinecraftClient.getInstance().setScreen(screen);
			}
		});

	}
}
