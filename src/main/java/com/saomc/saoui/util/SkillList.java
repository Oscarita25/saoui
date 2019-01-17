/*
 * Copyright (C) 2016-2019 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saomc.saoui.util;

import com.saomc.saoui.api.entity.ISkill;
import com.saomc.saoui.api.screens.Actions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Part of saoui
 *
 * @author Bluexin
 */
public class SkillList {
    private static SkillList instance;

    private final List<ISkill> skills = new ArrayList<>(3);
    private final boolean showRing;

    private SkillList(List<ISkill> skills, boolean showRing) {
        this.skills.addAll(skills);
        this.showRing = showRing;
    }

    public static void init(List<ISkill> skills, boolean showRing) {
        if (instance != null) throw new IllegalStateException("SkillList already got initialized!");
        instance = new SkillList(skills, showRing);
    }

    public static SkillList instance() {
        return instance;
    }

    public int size() {
        return skills.size();
    }

    public boolean isEmpty() {
        return skills.isEmpty();
    }

    public boolean contains(ISkill o) {
        return skills.contains(o);
    }

    public Iterator<ISkill> iterator() {
        return stream().iterator();
    }

    public boolean containsAll(Collection<?> c) {
        return skills.containsAll(c);
    }

    public int indexOf(ISkill o) {
        return skills.indexOf(o);
    }

    public int lastIndexOf(ISkill o) {
        return skills.lastIndexOf(o);
    }

    public Stream<ISkill> stream() {
        return skills.stream().filter(ISkill::visible);
    }

    public void forEach(Consumer<? super ISkill> action) {
        stream().forEach(action);
    }

    public boolean isRingShown() {
        return showRing;
    }

    public void hitInSkillRing(int index, Minecraft mc, GuiInventory parent, Actions action) {
        SkillList.instance().stream().filter(ISkill::shouldShowInRing).skip(index).findFirst().ifPresent(s -> s.activate(mc, parent, action));
    }
}
