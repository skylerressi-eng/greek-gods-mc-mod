package com.olympus.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.olympus.Olympus;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;

/**
 * Each legendary exists only once per server. Stored on the overworld (saved with the world).
 * Key: legendary item id path. A legendary destroyed by the void is removed so its event can drop it again.
 */
public final class LegendaryRegistry {
	public record Entry(UUID instance, String firstOwner, String lastHolder) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.CODEC.fieldOf("instance").forGetter(Entry::instance),
			Codec.STRING.fieldOf("first_owner").forGetter(Entry::firstOwner),
			Codec.STRING.optionalFieldOf("last_holder", "").forGetter(Entry::lastHolder)
		).apply(i, Entry::new));
	}

	private static final Codec<Map<String, Entry>> CODEC = Codec.unboundedMap(Codec.STRING, Entry.CODEC);
	public static final AttachmentType<Map<String, Entry>> TYPE = AttachmentRegistry.create(Olympus.id("legendaries"),
		b -> b.persistent(CODEC).initializer(Map::of));

	private LegendaryRegistry() {
	}

	private static Map<String, Entry> map(MinecraftServer server) {
		return server.overworld().getAttachedOrCreate(TYPE);
	}

	private static void put(MinecraftServer server, Map<String, Entry> map) {
		server.overworld().setAttached(TYPE, Map.copyOf(map));
	}

	public static Optional<Entry> get(MinecraftServer server, String id) {
		return Optional.ofNullable(map(server).get(id));
	}

	public static Map<String, Entry> all(MinecraftServer server) {
		return map(server);
	}

	public static void register(MinecraftServer server, String id, Entry entry) {
		Map<String, Entry> m = new HashMap<>(map(server));
		m.put(id, entry);
		put(server, m);
	}

	public static void remove(MinecraftServer server, String id) {
		Map<String, Entry> m = new HashMap<>(map(server));
		if (m.remove(id) != null) {
			put(server, m);
		}
	}

	public static void seen(MinecraftServer server, String id, String holder) {
		Entry e = map(server).get(id);
		if (e != null && !e.lastHolder().equals(holder)) {
			register(server, id, new Entry(e.instance(), e.firstOwner(), holder));
		}
	}

	public static void init() {
	}
}
