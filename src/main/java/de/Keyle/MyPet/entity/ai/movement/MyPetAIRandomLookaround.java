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
import de.Keyle.MyPet.entity.types.EntityMyPet;

public class MyPetAIRandomLookaround extends MyPetAIGoal
{
    private EntityMyPet petEntity;
    private double directionX;
    private double directionZ;
    private int ticksUntilStopLookingAround = 0;

    public MyPetAIRandomLookaround(EntityMyPet petEntity)
    {
        this.petEntity = petEntity;
    }

    @Override
    public boolean shouldStart()
    {
        if (this.petEntity.getAITarget() != null && !this.petEntity.getAITarget().isDead)
        {
            return false;
        }
        return this.petEntity.getRNG().nextFloat() < 0.02F;
    }

    @Override
    public boolean shouldFinish()
    {
        return this.ticksUntilStopLookingAround <= 0;
    }

    @Override
    public void start()
    {
        double circumference = 6.283185307179586D * this.petEntity.getRNG().nextDouble();
        this.directionX = Math.cos(circumference);
        this.directionZ = Math.sin(circumference);
        this.ticksUntilStopLookingAround = (20 + this.petEntity.getRNG().nextInt(20));
    }

    @Override
    public void tick()
    {
        this.ticksUntilStopLookingAround--;
        this.petEntity.getLookHelper().setLookPosition(this.petEntity.posX + this.directionX, this.petEntity.posY + this.petEntity.getEyeHeight(), this.petEntity.posZ + this.directionZ, 10.0F, this.petEntity.getVerticalFaceSpeed());
    }
}
