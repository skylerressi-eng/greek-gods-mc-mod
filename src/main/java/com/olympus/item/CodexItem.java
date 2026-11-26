package com.olympus.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

/** Codex of Olympus: the in-game guide. Pages are translated text (olympus.codex.page.N). */
public class CodexItem extends WrittenBookItem {
	public static final int PAGES = 32;

	public CodexItem(Properties properties) {
		super(properties);
	}

	public static WrittenBookContent content() {
		List<Filterable<Component>> pages = new ArrayList<>();
		for (int i = 1; i <= PAGES; i++) {
			pages.add(Filterable.passThrough(Component.translatable("olympus.codex.page." + i)));
		}
		return new WrittenBookContent(Filterable.passThrough("Codex of Olympus"), "The Muses", 0, pages, true);
	}
}
