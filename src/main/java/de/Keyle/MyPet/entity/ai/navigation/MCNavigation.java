/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.ai.navigation;

import de.Keyle.MyPet.entity.types.EntityMyPet;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.Navigation;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCNavigation extends AbstractNavigation
{
    Navigation nav;

    public MCNavigation(EntityMyPet entityMyPet)
    {
        super(entityMyPet);
        nav = entityMyPet.getNavigation();
    }

    public MCNavigation(EntityMyPet entityMyPet, NavigationParameters parameters)
    {
        super(entityMyPet, parameters);
        nav = entityMyPet.getNavigation();
    }

    @Override
    public void stop()
    {
        nav.g();
    }

    @Override
    public boolean navigateTo(double x, double y, double z)
    {
        applyNavigationParameters();
        if (this.nav.a(x, y, z, parameters.speed() + parameters.speedModifier()))
        {
            applyNavigationParameters();
            return true;
        }
        return false;
    }

    @Override
    public boolean navigateTo(LivingEntity entity)
    {
        if (this.nav.a(((CraftLivingEntity) entity).getHandle(), parameters.speed() + parameters.speedModifier()))
        {
            applyNavigationParameters();
            return true;
        }
        return false;
    }

    @Override
    public boolean navigateTo(EntityLiving entity)
    {
        if (this.nav.a(entity, entityMyPet.getWalkSpeed()))
        {
            applyNavigationParameters();
            return true;
        }
        return false;
    }

    public void applyNavigationParameters()
    {
        this.nav.a(parameters.avoidWater());
        this.nav.a(parameters.speed() + parameters.speedModifier());
    }
}
