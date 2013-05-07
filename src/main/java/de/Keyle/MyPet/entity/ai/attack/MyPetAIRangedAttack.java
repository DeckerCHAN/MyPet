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

package de.Keyle.MyPet.entity.ai.attack;

import de.Keyle.MyPet.entity.ai.MyPetAIGoal;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.skill.skills.implementation.ranged.MyPetArrow;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;

public class MyPetAIRangedAttack extends MyPetAIGoal
{
    private MyPet myPet;
    private final EntityMyPet entityMyPet;
    private EntityLiving target;
    private int shootTimer;
    private float walkSpeedModifier;
    private int lastSeenTimer;
    private int fireRate;
    private float rangeSquared;

    public MyPetAIRangedAttack(EntityMyPet entityMyPet, float walkSpeedModifier, int fireRate, float range)
    {
        this.entityMyPet = entityMyPet;
        this.myPet = entityMyPet.getMyPet();
        this.shootTimer = -1;
        this.lastSeenTimer = 0;
        this.walkSpeedModifier = walkSpeedModifier;
        this.fireRate = fireRate;
        this.rangeSquared = (range * range);
    }

    @Override
    public boolean shouldStart()
    {
        if (myPet.getRangedDamage() <= 0)
        {
            return false;
        }
        EntityLiving goalTarget = this.entityMyPet.getAITarget();

        if (goalTarget == null || goalTarget.isDead || !entityMyPet.canMove())
        {
            return false;
        }
        double space = this.entityMyPet.getDistanceSq(goalTarget.posX, goalTarget.boundingBox.minY, goalTarget.posZ);
        if (myPet.getDamage() > 0 && space < 16)
        {
            return false;
        }
        this.target = goalTarget;
        return true;
    }

    @Override
    public boolean shouldFinish()
    {
        if (target == null || target.isDead || myPet.getRangedDamage() <= 0 || !entityMyPet.canMove())
        {
            return true;
        }
        if (myPet.getDamage() > 0 && this.entityMyPet.getDistanceSqToEntity(target) < 16)
        {
            return true;
        }
        return false;
    }

    @Override
    public void finish()
    {
        this.target = null;
        this.lastSeenTimer = 0;
        this.shootTimer = -1;
    }

    @Override
    public void tick()
    {
        double distanceToTarget = this.entityMyPet.getDistanceSqToEntity(this.target);
        boolean canSee = this.entityMyPet.getEntitySenses().canSee(this.target);

        if (canSee)
        {
            this.lastSeenTimer++;
        }
        else
        {
            this.lastSeenTimer = 0;
        }

        if ((distanceToTarget <= this.rangeSquared) && (this.lastSeenTimer >= 20))
        {
            this.entityMyPet.petNavigation.getParameters().removeSpeedModifier("RangedAttack");
            this.entityMyPet.petNavigation.stop();
        }
        else
        {
            this.entityMyPet.petNavigation.getParameters().addSpeedModifier("RangedAttack", walkSpeedModifier);
            this.entityMyPet.petNavigation.navigateTo(this.target);
        }

        this.entityMyPet.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

        if (--this.shootTimer <= 0)
        {
            if (distanceToTarget < this.rangeSquared && canSee)
            {
                shootProjectile(this.target, myPet.getRangedDamage());
                this.shootTimer = this.fireRate;
            }
        }
    }

    public void shootProjectile(EntityLiving target, float damage)
    {
        World world = target.worldObj;
        EntityArrow entityArrow = new MyPetArrow(world, entityMyPet, target, 1.6F, 1);
        entityArrow.setDamage(damage);
        entityMyPet.playSound("random.bow", 1.0F, 1.0F / (entityMyPet.getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntityInWorld(entityArrow);
    }
}
