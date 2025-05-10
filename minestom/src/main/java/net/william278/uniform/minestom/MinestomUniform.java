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

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A class for registering commands with the Bukkit server's CommandMap
 *
 * @since 1.3.3
 */
@SuppressWarnings("unused")
public final class MinestomUniform implements Uniform {

    static MinestomUniform INSTANCE;
    final CommandManager commandManager;
    final BiPredicate<CommandSender, String> permissionHandler;

    @Getter
    @Setter
    Function<Object, CommandUser> commandUserSupplier = (user) -> new MinestomCommandUser((CommandSender) user, this);

    private MinestomUniform(CommandManager commandManager, BiPredicate<CommandSender, String> permissionHandler) {
        this.commandManager = commandManager;
        this.permissionHandler = permissionHandler;
    }

    /**
     * Get the BukkitUniform instance for registering commands
     *
     * @return BukkitUniform instance
     * @since 1.0
     */
    @NotNull
    public static MinestomUniform getInstance(CommandManager commandManager, BiPredicate<CommandSender, String> permissionHandler) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new MinestomUniform(commandManager, permissionHandler));
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @param <S>      The command source type
     * @param <T>      The command type
     * @since 1.0
     */
    @SafeVarargs
    @Override
    public final <S, T extends BaseCommand<S>> void register(T... commands) {
        commandManager.register(
            Arrays.stream(commands).map(c -> (MinestomCommand) c)
                .map(c -> (net.minestom.server.command.builder.Command) c.getImpl(this))
                    .toArray(net.minestom.server.command.builder.Command[]::new)
        );
    }

    /**
     * Register command(s) to be added to the server's command map
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(MinestomCommand::new).toArray(MinestomCommand[]::new));
    }

}
