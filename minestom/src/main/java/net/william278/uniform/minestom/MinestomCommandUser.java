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

import net.kyori.adventure.audience.Audience;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MinestomCommandUser(@NotNull CommandSender sender, @NotNull MinestomUniform uniform) implements CommandUser {

    @Override
    @NotNull
    public Audience getAudience() {
        return sender;
    }

    @Override
    public String getName() {
        return sender instanceof Player player ? player.getUsername() : "Console";
    }

    @Override
    @Nullable
    public UUID getUuid() {
        return sender instanceof Player player ? player.getUuid() : null;
    }

    @Override
    public boolean checkPermission(@NotNull Permission permission) {
        final boolean perm = uniform.permissionHandler.test(sender, permission.node());
        if (perm) {
            return true;
        }
        return permission.defaultValue().check(sender instanceof ConsoleSender);
    }

}
