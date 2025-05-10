/*
 * This file is part of Uniform, licensed under the GNU General Public License v3.0.
 *
 *  Copyright (c) Tofaa2
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.william278.uniform.minestom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.Permission;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public class MinestomCommand extends BaseCommand<CommandSender> {

	private @Nullable Permission permission;

	public MinestomCommand(@NotNull Command command) {
		super(command);
		this.permission = command.getPermission().orElse(null);
	}

	public MinestomCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
		super(name, description, aliases);
	}

	public MinestomCommand(@NotNull String name, @NotNull List<String> aliases) {
		super(name, aliases);
	}

	public static MinestomCommandBuilder builder(@NotNull String name) {
		return new MinestomCommandBuilder(name);
	}

	@NotNull
	Impl getImpl(@NotNull Uniform uniform) {
		return new Impl(uniform, this);
	}

	static final class Impl extends net.minestom.server.command.builder.Command {
		private static final int COMMAND_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;
		private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
		private final @Nullable Permission permission;

		public Impl(@NotNull Uniform uniform, @NotNull MinestomCommand command) {
			super(command.getName(), command.getAliases().toArray(new String[0]));
			this.dispatcher.register(command.createBuilder());
			this.permission = command.permission;

			if (permission != null && !permission.node().isBlank()) {
				setCondition((sender, commandString) ->
						((MinestomUniform) uniform).permissionHandler.test(sender, permission.node()));
			}

			var params = ArgumentType.StringArray("params");
			params.setSuggestionCallback((sender, context, suggestion) -> {
				String input = context.getInput();
				String[] split = input.split(" ", 2);
				String args = split.length > 1 ? split[1] : "";

				var suggestions = dispatcher.getCompletionSuggestions(
						dispatcher.parse(args, sender),
						args.length()
				).join();

				for (Suggestion s : suggestions.getList()) {
					suggestion.addEntry(new SuggestionEntry(s.getText()));
				}
			});

			this.setDefaultExecutor((sender, context) ->
					process(sender, context.getCommandName(), new String[0]));

			this.addSyntax((sender, context) ->
							process(sender, context.getCommandName(), context.get(params)),
					params);
		}

		private void process(@NotNull CommandSender sender, @NotNull String commandName, String @NotNull [] args) {
			try {
				String input = args.length == 0 ? commandName :
						"%s %s".formatted(commandName, String.join(" ", args));

				dispatcher.execute(input, sender);
			} catch (CommandSyntaxException e) {
				sender.sendMessage(Component
						.translatable("command.context.parse_error", NamedTextColor.RED)
						.args(
								Component.text(e.getRawMessage().getString()),
								Component.text(e.getCursor()),
								Component.text(e.getContext())
						));
			} catch (Exception e) {
				sender.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
			}
		}
	}

	@Override
	public void addSubCommand(@NotNull Command command) {
		addSubCommand(new MinestomCommand(command));
	}

	@Override
	public Uniform getUniform() {
		return MinestomUniform.INSTANCE;
	}

	public static class MinestomCommandBuilder extends BaseCommandBuilder<CommandSender, MinestomCommandBuilder> {

		public MinestomCommandBuilder(@NotNull String name) {
			super(name);
		}

		public final MinestomCommandBuilder addSubCommand(@NotNull Command command) {
			subCommands.add(new MinestomCommand(command));
			return this;
		}

		@Override
		public @NotNull MinestomCommand build() {
			var command = new MinestomCommand(name, description, aliases);
			command.addPermissions(permissions);
			subCommands.forEach(command::addSubCommand);
			command.setDefaultExecutor(defaultExecutor);
			command.syntaxes.addAll(syntaxes);
			command.setExecutionScope(executionScope);
			command.setCondition(condition);

			return command;
		}

		public MinestomCommand register(CommandManager commandManager, BiPredicate<CommandSender, String> permissionHandler) {
			final MinestomCommand builtCmd = build();
			MinestomUniform.getInstance(commandManager, permissionHandler).register(builtCmd);
			return builtCmd;
		}
	}
}
