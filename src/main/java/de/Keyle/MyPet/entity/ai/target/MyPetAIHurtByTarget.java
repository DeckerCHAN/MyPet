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

package de.Keyle.MyPet.entity.ai.target;

import de.Keyle.MyPet.entity.ai.MyPetAIGoal;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.util.MyPetPvP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.entity.Player;

public class MyPetAIHurtByTarget extends MyPetAIGoal
{
    EntityMyPet petEntity;
    MyPet myPet;
    EntityLiving target = null;

    public MyPetAIHurtByTarget(EntityMyPet petEntity)
    {
        this.petEntity = petEntity;
        myPet = petEntity.getMyPet();
    }

    @Override
    public boolean shouldStart()
    {

        if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0)
        {
            return false;
        }
        if (petEntity.getLastAttackingEntity() == null)
        {
            return false;
        }
        if (target != petEntity.getLastAttackingEntity())
        {
            target = petEntity.getLastAttackingEntity();
        }
        if (target == petEntity)
        {
            return false;
        }
        if (target instanceof EntityPlayer)
        {
            Player targetPlayer = (Player) target.getBukkitEntity();

            if (targetPlayer == myPet.getOwner().getPlayer())
            {
                return false;
            }
            else if (!MyPetPvP.canHurt(myPet.getOwner().getPlayer(), targetPlayer))
            {
                return false;
            }
        }
        else if (target instanceof EntityMyPet)
        {
            MyPet targetMyPet = ((EntityMyPet) target).getMyPet();
            if (!MyPetPvP.canHurt(myPet.getOwner().getPlayer(), targetMyPet.getOwner().getPlayer()))
            {
                return false;
            }
        }
        else if (target instanceof EntityTameable)
        {
            EntityTameable tameable = (EntityTameable) target;
            if (tameable.isTamed() && tameable.getOwner() != null)
            {
                Player tameableOwner = (Player) tameable.getOwner().getBukkitEntity();
                if (myPet.getOwner().equals(tameableOwner))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldFinish()
    {
        EntityLiving entityliving = petEntity.getAITarget();

        if (!petEntity.canMove())
        {
            return true;
        }
        else if (entityliving == null)
        {
            return true;
        }
        else if (entityliving.isDead)
        {
            return true;
        }
        return false;
    }

    @Override
    public void start()
    {
        petEntity.setAttackTarget(this.target);
    }

    @Override
    public void finish()
    {
        petEntity.setAttackTarget(null);
    }
}