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

package de.Keyle.MyPet.entity.ai.movement;

import de.Keyle.MyPet.entity.ai.MyPetAIGoal;
import de.Keyle.MyPet.entity.ai.navigation.AbstractNavigation;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.util.MyPetBukkitUtil;
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;

public class MyPetAIFollowOwner extends MyPetAIGoal
{
    private EntityMyPet petEntity;
    private float walkSpeedModifier;
    private AbstractNavigation nav;
    private int setPathTimer = 0;
    private float stopDistance;
    private float startDistance;
    private float teleportDistance;
    private MyPetAIControl controlPathfinderGoal;
    private EntityPlayer owner;

    public MyPetAIFollowOwner(EntityMyPet entityMyPet, float walkSpeedModifier, float startDistance, float stopDistance, float teleportDistance)
    {
        this.petEntity = entityMyPet;
        this.walkSpeedModifier = walkSpeedModifier;
        this.nav = entityMyPet.petNavigation;
        this.startDistance = startDistance * startDistance;
        this.stopDistance = stopDistance * stopDistance;
        this.teleportDistance = teleportDistance * teleportDistance;
        this.owner = ((CraftPlayer) petEntity.getOwner().getPlayer()).getHandle();
    }

    @Override
    public boolean shouldStart()
    {
        if (controlPathfinderGoal == null)
        {
            if (petEntity.petPathfinderSelector.hasGoal("Control"))
            {
                controlPathfinderGoal = (MyPetAIControl) petEntity.petPathfinderSelector.getGoal("Control");
            }
        }
        if (controlPathfinderGoal == null)
        {
            return false;
        }
        if (!this.petEntity.canMove())
        {
            return false;
        }
        else if (this.petEntity.getAITarget() != null && !this.petEntity.getAITarget().isDead)
        {
            return false;
        }
        else if (this.petEntity.getOwner() == null)
        {
            return false;
        }
        else if (this.petEntity.getDistanceSqToEntity(owner) < this.startDistance)
        {
            return false;
        }
        else if (controlPathfinderGoal.moveTo != null)
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldFinish()
    {
        if (controlPathfinderGoal.moveTo != null)
        {
            return true;
        }
        else if (this.petEntity.getOwner() == null)
        {
            return true;
        }
        else if (this.petEntity.getDistanceSqToEntity(owner) < this.stopDistance)
        {
            return true;
        }
        else if (!this.petEntity.canMove())
        {
            return true;
        }
        else if (this.petEntity.getAITarget() != null && !this.petEntity.getAITarget().isDead)
        {
            return true;
        }
        return false;
    }

    @Override
    public void start()
    {
        nav.getParameters().addSpeedModifier("FollowOwner", walkSpeedModifier);
        this.setPathTimer = 0;
    }

    @Override
    public void finish()
    {
        nav.getParameters().removeSpeedModifier("FollowOwner");
        this.nav.stop();
    }

    @Override
    public void tick()
    {
        Location ownerLocation = this.petEntity.getMyPet().getOwner().getPlayer().getLocation();
        Location petLocation = this.petEntity.getMyPet().getLocation();
        if (ownerLocation.getWorld() != petLocation.getWorld())
        {
            return;
        }

        this.petEntity.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float) this.petEntity.getVerticalFaceSpeed());

        if (this.petEntity.canMove())
        {
            if (--this.setPathTimer <= 0)
            {
                this.setPathTimer = 10;

                if (!this.nav.navigateTo(owner))
                {
                    if (owner.onGround && this.petEntity.getDistanceSqToEntity(owner) > this.teleportDistance && controlPathfinderGoal.moveTo == null && petEntity.goalTarget == null && MyPetBukkitUtil.canSpawn(ownerLocation, this.petEntity))
                    {
                        this.petEntity.setPositionAndRotation(ownerLocation.getX(), ownerLocation.getY(), ownerLocation.getZ(), this.petEntity.rotationYaw, this.petEntity.cameraPitch);
                        this.nav.navigateTo(owner);
                    }
                }
            }
        }
    }
}