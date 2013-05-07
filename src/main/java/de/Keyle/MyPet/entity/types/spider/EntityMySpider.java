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

package de.Keyle.MyPet.entity.types.spider;

import de.Keyle.MyPet.entity.EntitySize;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import net.minecraft.world.World;

@EntitySize(width = 1.4F, height = 0.9F)
public class EntityMySpider extends EntityMyPet
{
    public EntityMySpider(World world, MyPet myPet)
    {
        super(world, myPet);
        this.texture = "/mob/spider.png";
    }

    // Obfuscated Methods -------------------------------------------------------------------------------------------

    protected void entityInit()
    {
        super.entityInit();
        this.getDataWatcher().addObject(16, new Byte((byte) 0)); // N/A
    }

    @Override
    protected void playStepSound(int i, int j, int k, int l)
    {
        playSound("mob.spider.step", 0.15F, 1.0F);
    }

    /**
     * Returns the default sound of the MyPet
     */
    protected String getLivingSound()
    {
        return !playIdleSound() ? "" : "mob.spider.say";
    }

    /**
     * Returns the sound that is played when the MyPet get hurt
     */
    @Override
    protected String getHurtSound()
    {
        return "mob.spider.say";
    }

    /**
     * Returns the sound that is played when the MyPet dies
     */
    @Override
    protected String getDeathSound()
    {
        return "mob.spider.death";
    }

    public void a(boolean flag)
    {
        byte b0 = this.getDataWatcher().getWatchableObjectByte(16);

        if (flag)
        {
            b0 = (byte) (b0 | 0x1);
        }
        else
        {
            b0 = (byte) (b0 & 0xFFFFFFFE);
        }
        this.getDataWatcher().updateObject(16, b0);
    }
}