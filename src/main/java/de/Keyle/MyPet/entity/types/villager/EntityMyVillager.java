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

package de.Keyle.MyPet.entity.types.villager;

import de.Keyle.MyPet.entity.EntitySize;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.Material;

@EntitySize(width = 0.6F, height = 0.8F)
public class EntityMyVillager extends EntityMyPet
{
    public static Material GROW_UP_ITEM = Material.POTION;

    public EntityMyVillager(World world, MyPet myPet)
    {
        super(world, myPet);
        this.texture = "/mob/villager/villager.png";
    }

    public void setMyPet(MyPet myPet)
    {
        if (myPet != null)
        {
            super.setMyPet(myPet);

            this.setProfession(((MyVillager) myPet).getProfession());
            this.setBaby(((MyVillager) myPet).isBaby());
        }
    }

    public int getProfession()
    {
        return ((MyVillager) myPet).profession;
    }

    public void setProfession(int value)
    {
        this.getDataWatcher().updateObject(16, value);
        ((MyVillager) myPet).profession = value;
    }

    public boolean isBaby()
    {
        return ((MyVillager) myPet).isBaby;
    }

    @SuppressWarnings("boxing")
    public void setBaby(boolean flag)
    {
        if (flag)
        {
            this.getDataWatcher().updateObject(12, Integer.valueOf(Integer.MIN_VALUE));
        }
        else
        {
            this.getDataWatcher().updateObject(12, new Integer(0));
        }
        ((MyVillager) myPet).isBaby = flag;
    }

    // Obfuscated Methods -------------------------------------------------------------------------------------------

    protected void entityInit()
    {
        super.entityInit();
        this.getDataWatcher().addObject(16, new Integer(0)); // profession
        this.getDataWatcher().addObject(12, new Integer(0)); // age
    }

    public boolean interact(EntityPlayer entityhuman)
    {
        if (super.interact(entityhuman))
        {
            return true;
        }

        ItemStack itemStack = entityhuman.inventory.getItemStack();

        if (getOwner().equals(entityhuman) && itemStack != null)
        {
            if (itemStack.itemID == GROW_UP_ITEM.getId())
            {
                if (isBaby())
                {
                    if (!entityhuman.capabilities.isCreativeMode)
                    {
                        if (--itemStack.stackSize <= 0)
                        {
                            entityhuman.inventory.setInventorySlotContents(entityhuman.inventory.currentItem, null);
                        }
                    }
                    this.setBaby(false);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the default sound of the MyPet
     */
    protected String getLivingSound()
    {
        return !playIdleSound() ? "" : "mob.villager.default";
    }

    /**
     * Returns the sound that is played when the MyPet get hurt
     */
    protected String getHurtSound()
    {
        return "mob.villager.defaulthurt";
    }

    /**
     * Returns the sound that is played when the MyPet dies
     */
    protected String getDeathSound()
    {
        return "mob.villager.defaultdeath";
    }
}