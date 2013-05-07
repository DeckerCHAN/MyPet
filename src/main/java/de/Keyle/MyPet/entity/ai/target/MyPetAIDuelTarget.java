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
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;

public class MyPetAIDuelTarget extends MyPetAIGoal
{
    private MyPet myPet;
    private EntityMyPet petEntity;
    private EntityPlayer petOwnerEntity;
    private EntityMyPet target;
    private EntityMyPet duelOpponent = null;
    private float range;
    private Behavior behaviorSkill = null;

    public MyPetAIDuelTarget(EntityMyPet petEntity, float range)
    {
        this.petEntity = petEntity;
        this.petOwnerEntity = ((CraftPlayer) petEntity.getOwner().getPlayer()).getHandle();
        this.myPet = petEntity.getMyPet();
        this.range = range;
        if (myPet.getSkills().hasSkill("Behavior"))
        {
            behaviorSkill = (Behavior) myPet.getSkills().getSkill("Behavior");
        }
    }

    @Override
    public boolean shouldStart()
    {
        if (behaviorSkill == null || !behaviorSkill.isActive() || behaviorSkill.getBehavior() != BehaviorState.Duel)
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
        if (duelOpponent != null)
        {
            this.target = duelOpponent;
            return true;
        }

        for (Object entityObj : this.petEntity.worldObj.getEntitiesWithinAABB(EntityMyPet.class, this.petOwnerEntity.boundingBox.expand((double) range, (double) range, (double) range)))
        {
            EntityMyPet entityMyPet = (EntityMyPet) entityObj;
            MyPet targetMyPet = entityMyPet.getMyPet();

            if (petEntity.getEntitySenses().canSee(entityMyPet) && entityMyPet != petEntity && !entityMyPet.isDead)
            {
                if (!targetMyPet.getSkills().isSkillActive("Behavior") || !targetMyPet.getCraftPet().canMove())
                {
                    continue;
                }
                Behavior targetbehavior = (Behavior) targetMyPet.getSkills().getSkill("Behavior");
                if (targetbehavior.getBehavior() != BehaviorState.Duel)
                {
                    continue;
                }
                if (targetMyPet.getDamage() == 0)
                {
                    continue;
                }
                this.target = entityMyPet;
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
        else if (behaviorSkill.getBehavior() != BehaviorState.Duel)
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
        petEntity.setRevengeTarget(this.target);
        setDuelOpponent(this.target);
        if (target.petTargetSelector.hasGoal("DuelTarget"))
        {
            MyPetAIDuelTarget duelGoal = (MyPetAIDuelTarget) target.petTargetSelector.getGoal("DuelTarget");
            duelGoal.setDuelOpponent(this.petEntity);
        }
    }

    @Override
    public void finish()
    {
        petEntity.setAttackTarget(null);
        duelOpponent = null;
        target = null;
    }

    public EntityMyPet getDuelOpponent()
    {
        return duelOpponent;
    }

    public void setDuelOpponent(EntityMyPet opponent)
    {
        this.duelOpponent = opponent;
    }
}