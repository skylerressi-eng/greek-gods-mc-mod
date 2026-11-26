package com.olympus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.olympus.data.PlayerOlympusData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /ally add|remove|list: choose which players your relic buffs reach (and your powers spare).
 * Players on the same scoreboard team are always allies. Available to everyone.
 */
public final class AllyCommand {
	private AllyCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ally")
			.then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player()).executes(c -> change(c, true))))
			.then(Commands.literal("remove").then(Commands.argument("player", EntityArgument.player()).executes(c -> change(c, false))))
			.then(Commands.literal("list").executes(AllyCommand::list))
			.executes(AllyCommand::list));
	}

	private static int change(CommandContext<CommandSourceStack> c, boolean add) throws CommandSyntaxException {
		ServerPlayer self = c.getSource().getPlayerOrException();
		ServerPlayer other = EntityArgument.getPlayer(c, "player");
		if (other == self) {
			c.getSource().sendFailure(Component.translatable("olympus.ally.self"));
			return 0;
		}
		List<UUID> allies = new ArrayList<>(PlayerOlympusData.get(self).allies());
		if (add) {
			if (!allies.contains(other.getUUID())) {
				allies.add(other.getUUID());
			}
			other.sendSystemMessage(Component.translatable("olympus.ally.added_you", self.getDisplayName()).withStyle(ChatFormatting.GREEN));
		} else {
			allies.remove(other.getUUID());
		}
		PlayerOlympusData.set(self, PlayerOlympusData.get(self).withAllies(allies));
		c.getSource().sendSuccess(() -> Component.translatable(add ? "olympus.ally.added" : "olympus.ally.removed", other.getDisplayName())
			.withStyle(add ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		ServerPlayer self = c.getSource().getPlayerOrException();
		List<UUID> allies = PlayerOlympusData.get(self).allies();
		List<String> names = new ArrayList<>();
		for (UUID id : allies) {
			ServerPlayer p = c.getSource().getServer().getPlayerList().getPlayer(id);
			names.add(p != null ? p.getGameProfile().name() : id.toString().substring(0, 8));
		}
		String team = self.getTeam() != null ? self.getTeam().getName() : "-";
		c.getSource().sendSuccess(() -> Component.translatable("olympus.ally.list", names.isEmpty() ? "-" : String.join(", ", names), team), false);
		return allies.size();
	}
}
