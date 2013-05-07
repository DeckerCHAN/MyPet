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
import de.Keyle.MyPet.skill.skills.implementation.Behavior;
import de.Keyle.MyPet.skill.skills.implementation.Behavior.BehaviorState;
import de.Keyle.MyPet.util.MyPetPvP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class MyPetAIAggressiveTarget extends MyPetAIGoal
{
    private MyPet myPet;
    private EntityMyPet petEntity;
    private EntityPlayer petOwnerEntity;
    private EntityLiving target;
    private float range;
    private Behavior behaviorSkill = null;

    public MyPetAIAggressiveTarget(EntityMyPet petEntity, float range)
    {
        this.petEntity = petEntity;
        this.myPet = petEntity.getMyPet();
        this.petOwnerEntity = ((CraftPlayer) myPet.getOwner().getPlayer()).getHandle();
        this.range = range;
        if (myPet.getSkills().hasSkill("Behavior"))
        {
            behaviorSkill = (Behavior) myPet.getSkills().getSkill("Behavior");
        }
    }

    @Override
    public boolean shouldStart()
    {
        if (behaviorSkill == null || !behaviorSkill.isActive() || behaviorSkill.getBehavior() != BehaviorState.Aggressive)
        {
            return false;
        }
        if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0)
        {
            return false;
        }
        if (!myPet.getCraftPet().canMove())
        {
            return false;
        }
        if (petEntity.getAITarget() != null && !petEntity.getAITarget().isDead)
        {
            return false;
        }

        for (Object entityObj : this.petEntity.worldObj.getEntitiesWithinAABB(EntityLiving.class, this.petOwnerEntity.boundingBox.expand((double) range, (double) range, (double) range)))
        {
            EntityLiving entityLiving = (EntityLiving) entityObj;

            if (petEntity.getEntitySenses().canSee(entityLiving) && entityLiving != petEntity && !entityLiving.isDead && petEntity.getDistanceSqToEntity(entityLiving) <= 91)
            {
                if (entityLiving instanceof EntityPlayer)
                {
                    Player targetPlayer = (Player) entityLiving.getBukkitEntity();
                    if (myPet.getOwner().equals(targetPlayer))
                    {
                        continue;
                    }
                    if (!MyPetPvP.canHurt(myPet.getOwner().getPlayer(), targetPlayer))
                    {
                        continue;
                    }
                }
                else if (entityLiving instanceof EntityMyPet)
                {
                    MyPet targetMyPet = ((EntityMyPet) entityLiving).getMyPet();
                    if (!MyPetPvP.canHurt(myPet.getOwner().getPlayer(), targetMyPet.getOwner().getPlayer()))
                    {
                        continue;
                    }
                }
                else if (entityLiving instanceof EntityTameable)
                {
                    EntityTameable tameable = (EntityTameable) entityLiving;
                    if (tameable.isTamed() && tameable.getOwner() != null)
                    {
                        Player tameableOwner = (Player) tameable.getOwner().getBukkitEntity();
                        if (myPet.getOwner().equals(tameableOwner))
                        {
                            continue;
                        }
                        else if (!MyPetPvP.canHurt(myPet.getOwner().getPlayer(), tameableOwner))
                        {
                            continue;
                        }
                    }
                }
                this.target = entityLiving;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldFinish()
    {
        if (!petEntity.canMove())
        {
            return true;
        }
        else if (petEntity.getAITarget() == null)
        {
            return true;
        }
        else if (petEntity.getAITarget().isDead)
        {
            return true;
        }
        else if (behaviorSkill.getBehavior() != BehaviorState.Aggressive)
        {
            return true;
        }
        else if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0)
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
        target = null;
    }
}