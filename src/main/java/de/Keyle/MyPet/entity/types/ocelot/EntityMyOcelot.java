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

package de.Keyle.MyPet.entity.types.ocelot;

import de.Keyle.MyPet.entity.EntitySize;
import de.Keyle.MyPet.entity.ai.movement.MyPetAISit;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.entity.Ocelot.Type;

@EntitySize(width = 0.6F, height = 0.8F)
public class EntityMyOcelot extends EntityMyPet
{
    public static org.bukkit.Material GROW_UP_ITEM = org.bukkit.Material.POTION;

    private MyPetAISit sitPathfinder;

    public EntityMyOcelot(World world, MyPet myPet)
    {
        super(world, myPet);
        this.texture = "/mob/ozelot.png";
    }

    public void setPathfinder()
    {
        super.setPathfinder();
        petPathfinderSelector.addGoal("Sit", 2, sitPathfinder);
    }

    public void setMyPet(MyPet myPet)
    {
        if (myPet != null)
        {
            this.sitPathfinder = new MyPetAISit(this);

            super.setMyPet(myPet);

            this.setSitting(((MyOcelot) myPet).isSitting());
            this.setBaby(((MyOcelot) myPet).isBaby());
            this.setCatType(((MyOcelot) myPet).getCatType().getId());
        }
    }

    public boolean canMove()
    {
        return !isSitting();
    }

    public void setSitting(boolean flag)
    {
        this.sitPathfinder.setSitting(flag);
    }

    public boolean isSitting()
    {
        return this.sitPathfinder.isSitting();
    }

    public void applySitting(boolean flag)
    {
        int i = this.getDataWatcher().getWatchableObjectByte(16);
        if (flag)
        {
            this.getDataWatcher().updateObject(16, (byte) (i | 0x1));
        }
        else
        {
            this.getDataWatcher().updateObject(16, (byte) (i & 0xFFFFFFFE));
        }
        ((MyOcelot) myPet).isSitting = flag;
    }

    public Type getCatType()
    {
        return ((MyOcelot) myPet).catType;
    }

    public void setCatType(int value)
    {
        this.getDataWatcher().updateObject(18, (byte) value);
        ((MyOcelot) myPet).catType = Type.getType(value);
    }

    public boolean isBaby()
    {
        return ((MyOcelot) myPet).isBaby;
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
        ((MyOcelot) myPet).isBaby = flag;
    }

    // Obfuscated Methods -------------------------------------------------------------------------------------------

    protected void entityInit()
    {
        super.entityInit();
        this.getDataWatcher().addObject(12, new Integer(0));     // age
        this.getDataWatcher().addObject(16, new Byte((byte) 0)); // tamed/sitting
        this.getDataWatcher().addObject(18, new Byte((byte) 0)); // cat type

    }

    /**
     * Is called when player rightclicks this MyPet
     * return:
     * true: there was a reaction on rightclick
     * false: no reaction on rightclick
     */
    public boolean interact(EntityPlayer entityhuman)
    {
        if (super.interact(entityhuman))
        {
            return true;
        }

        ItemStack itemStack = entityhuman.inventory.getItemStack();

        if (getOwner().equals(entityhuman))
        {
            if (itemStack != null)
            {
                if (itemStack.itemID == 351)
                {
                    if (itemStack.getItemDamage() == 11)
                    {
                        ((MyOcelot) myPet).setCatType(Type.WILD_OCELOT);
                        return true;
                    }
                    else if (itemStack.getItemDamage() == 0)
                    {
                        ((MyOcelot) myPet).setCatType(Type.BLACK_CAT);
                        return true;
                    }
                    else if (itemStack.getItemDamage() == 14)
                    {
                        ((MyOcelot) myPet).setCatType(Type.RED_CAT);
                        return true;
                    }
                    else if (itemStack.getItemDamage() == 7)
                    {
                        ((MyOcelot) myPet).setCatType(Type.SIAMESE_CAT);
                        return true;
                    }
                }
                else if (itemStack.itemID == GROW_UP_ITEM.getId())
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
            this.sitPathfinder.toogleSitting();
            return true;
        }
        return false;
    }

    /**
     * Returns the default sound of the MyPet
     */
    protected String getLivingSound()
    {
        return !playIdleSound() ? "" : this.rand.nextInt(4) == 0 ? "mob.cat.purreow" : "mob.cat.meow";
    }

    /**
     * Returns the sound that is played when the MyPet get hurt
     */
    protected String getHurtSound()
    {
        return "mob.cat.hitt";
    }

    /**
     * Returns the sound that is played when the MyPet dies
     */
    protected String getDeathSound()
    {
        return "mob.cat.hitt";
    }
}