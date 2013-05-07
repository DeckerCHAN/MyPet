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

package de.Keyle.MyPet.skill.skills.implementation;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.skill.skills.info.ISkillInfo;
import de.Keyle.MyPet.skill.skills.info.RangedInfo;
import de.Keyle.MyPet.util.MyPetBukkitUtil;
import de.Keyle.MyPet.util.MyPetLanguage;
import org.spout.nbt.IntTag;
import org.spout.nbt.StringTag;

public class Ranged extends RangedInfo implements ISkillInstance
{
    private MyPet myPet;

    public Ranged(boolean addedByInheritance)
    {
        super(addedByInheritance);
    }

    public void setMyPet(MyPet myPet)
    {
        this.myPet = myPet;
    }

    public MyPet getMyPet()
    {
        return myPet;
    }

    public boolean isActive()
    {
        return damage > 0;
    }

    public void upgrade(ISkillInfo upgrade, boolean quiet)
    {
        if (upgrade instanceof RangedInfo)
        {
            boolean isPassive = damage <= 0;
            if (upgrade.getProperties().getValue().containsKey("damage"))
            {
                if (!upgrade.getProperties().getValue().containsKey("addset_damage") || ((StringTag) upgrade.getProperties().getValue().get("addset_damage")).getValue().equals("add"))
                {
                    damage += ((IntTag) upgrade.getProperties().getValue().get("damage")).getValue();
                }
                else
                {
                    damage = ((IntTag) upgrade.getProperties().getValue().get("damage")).getValue();
                }
                if (!quiet)
                {
                    myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_AddDamage")).replace("%petname%", myPet.petName).replace("%dmg%", "" + damage));
                }
            }
            if (isPassive != (damage <= 0))
            {
                if (myPet.getStatus() == PetState.Here)
                {
                    getMyPet().getCraftPet().getHandle().petPathfinderSelector.clearGoals();
                    getMyPet().getCraftPet().getHandle().petTargetSelector.clearGoals();
                    getMyPet().getCraftPet().getHandle().setPathfinder();
                    if (damage == 0)
                    {
                        getMyPet().getCraftPet().getHandle().setAttackTarget(null);
                    }
                }
            }
        }
    }

    public String getFormattedValue()
    {
        return "+" + damage;
    }

    public void reset()
    {
        damage = 0;
        if (myPet.getStatus() == PetState.Here)
        {
            getMyPet().getCraftPet().getHandle().petPathfinderSelector.clearGoals();
            getMyPet().getCraftPet().getHandle().petTargetSelector.clearGoals();
            getMyPet().getCraftPet().getHandle().setPathfinder();
            getMyPet().getCraftPet().getHandle().setAttackTarget(null);
        }
    }

    public int getDamage()
    {
        return damage;
    }

    public ISkillInstance cloneSkill()
    {
        Ranged newSkill = new Ranged(isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}