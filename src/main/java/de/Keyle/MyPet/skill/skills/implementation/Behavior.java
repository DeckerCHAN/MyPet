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
import de.Keyle.MyPet.entity.types.enderman.MyEnderman;
import de.Keyle.MyPet.skill.ISkillActive;
import de.Keyle.MyPet.skill.ISkillStorage;
import de.Keyle.MyPet.skill.skills.info.BehaviorInfo;
import de.Keyle.MyPet.skill.skills.info.ISkillInfo;
import de.Keyle.MyPet.util.IScheduler;
import de.Keyle.MyPet.util.MyPetBukkitUtil;
import de.Keyle.MyPet.util.MyPetLanguage;
import de.Keyle.MyPet.util.MyPetPermissions;
import org.bukkit.ChatColor;
import org.spout.nbt.ByteTag;
import org.spout.nbt.CompoundMap;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.StringTag;

public class Behavior extends BehaviorInfo implements ISkillInstance, IScheduler, ISkillStorage, ISkillActive
{
    private BehaviorState behavior = BehaviorState.Normal;
    private boolean active = false;
    private MyPet myPet;

    public Behavior(boolean addedByInheritance)
    {
        super(addedByInheritance);
        behaviorActive.put(BehaviorState.Normal, true);
        behaviorActive.put(BehaviorState.Aggressive, false);
        behaviorActive.put(BehaviorState.Farm, false);
        behaviorActive.put(BehaviorState.Friendly, false);
        behaviorActive.put(BehaviorState.Raid, false);
        behaviorActive.put(BehaviorState.Duel, false);
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
        return active;
    }

    public void upgrade(ISkillInfo upgrade, boolean quiet)
    {
        if (upgrade instanceof BehaviorInfo)
        {
            active = true;
            boolean valuesEdit = false;
            String activeModes = "";
            if (upgrade.getProperties().getValue().containsKey("friend"))
            {
                behaviorActive.put(BehaviorState.Friendly, ((ByteTag) upgrade.getProperties().getValue().get("friend")).getBooleanValue());
                if (behaviorActive.get(BehaviorState.Friendly) && BehaviorState.Friendly.isActive())
                {
                    activeModes = ChatColor.GOLD + "Friendly" + ChatColor.RESET;
                }
                valuesEdit = true;
            }
            if (upgrade.getProperties().getValue().containsKey("aggro"))
            {
                behaviorActive.put(BehaviorState.Aggressive, ((ByteTag) upgrade.getProperties().getValue().get("aggro")).getBooleanValue());
                if (behaviorActive.get(BehaviorState.Aggressive) && BehaviorState.Aggressive.isActive())
                {
                    if (!activeModes.equalsIgnoreCase(""))
                    {
                        activeModes += ", ";
                    }
                    activeModes += ChatColor.GOLD + "Aggressive" + ChatColor.RESET;
                }
                valuesEdit = true;
            }
            if (upgrade.getProperties().getValue().containsKey("farm"))
            {
                behaviorActive.put(BehaviorState.Farm, ((ByteTag) upgrade.getProperties().getValue().get("farm")).getBooleanValue());
                if (behaviorActive.get(BehaviorState.Farm) && BehaviorState.Farm.isActive())
                {
                    if (!activeModes.equalsIgnoreCase(""))
                    {
                        activeModes += ", ";
                    }
                    activeModes += ChatColor.GOLD + "Farm" + ChatColor.RESET;
                }
                valuesEdit = true;
            }
            if (upgrade.getProperties().getValue().containsKey("raid"))
            {
                behaviorActive.put(BehaviorState.Raid, ((ByteTag) upgrade.getProperties().getValue().get("raid")).getBooleanValue());
                if (behaviorActive.get(BehaviorState.Raid) && BehaviorState.Raid.isActive())
                {
                    if (!activeModes.equalsIgnoreCase(""))
                    {
                        activeModes += ", ";
                    }
                    activeModes += ChatColor.GOLD + "Raid" + ChatColor.RESET;
                }
                valuesEdit = true;
            }
            if (upgrade.getProperties().getValue().containsKey("duel"))
            {
                behaviorActive.put(BehaviorState.Duel, ((ByteTag) upgrade.getProperties().getValue().get("duel")).getBooleanValue());
                if (behaviorActive.get(BehaviorState.Duel) && BehaviorState.Duel.isActive())
                {
                    if (!activeModes.equalsIgnoreCase(""))
                    {
                        activeModes += ", ";
                    }
                    activeModes += ChatColor.GOLD + "Duel" + ChatColor.RESET;
                }
                valuesEdit = true;
            }
            if (!quiet && valuesEdit)
            {
                myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_AddBehavior").replace("%petname%", myPet.petName)));
                myPet.sendMessageToOwner("  " + activeModes);
            }
        }
    }

    public String getFormattedValue()
    {
        String activeModes = ChatColor.GOLD + MyPetLanguage.getString("Name_Normal") + ChatColor.RESET;
        if (behaviorActive.get(BehaviorState.Friendly) && BehaviorState.Friendly.isActive())
        {

            activeModes += ", " + ChatColor.GOLD + MyPetLanguage.getString("Name_Friendly") + ChatColor.RESET;
        }
        if (behaviorActive.get(BehaviorState.Aggressive) && BehaviorState.Aggressive.isActive())
        {
            if (!activeModes.equalsIgnoreCase(""))
            {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + MyPetLanguage.getString("Name_Aggressive") + ChatColor.RESET;
        }
        if (behaviorActive.get(BehaviorState.Farm) && BehaviorState.Farm.isActive())
        {
            if (!activeModes.equalsIgnoreCase(""))
            {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + MyPetLanguage.getString("Name_Farm") + ChatColor.RESET;
        }
        if (behaviorActive.get(BehaviorState.Raid) && BehaviorState.Raid.isActive())
        {
            if (!activeModes.equalsIgnoreCase(""))
            {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + MyPetLanguage.getString("Name_Raid") + ChatColor.RESET;
        }
        if (behaviorActive.get(BehaviorState.Duel) && BehaviorState.Duel.isActive())
        {
            if (!activeModes.equalsIgnoreCase(""))
            {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + MyPetLanguage.getString("Name_Duel") + ChatColor.RESET;
        }
        return MyPetLanguage.getString("Name_Modes") + ": " + activeModes;
    }

    public void reset()
    {
        behavior = BehaviorState.Normal;
        behaviorActive.put(BehaviorState.Normal, true);
        behaviorActive.put(BehaviorState.Aggressive, false);
        behaviorActive.put(BehaviorState.Farm, false);
        behaviorActive.put(BehaviorState.Friendly, false);
        behaviorActive.put(BehaviorState.Raid, false);
        behaviorActive.put(BehaviorState.Duel, false);
        active = false;
    }

    public static enum BehaviorState
    {
        Normal(true), Friendly(true), Aggressive(true), Raid(true), Farm(true), Duel(true);

        boolean active;

        BehaviorState(boolean active)
        {
            this.active = active;
        }

        public void setActive(boolean active)
        {
            if (this != Normal)
            {
                this.active = active;
            }
        }

        public boolean isActive()
        {
            return this.active;
        }
    }


    public void setBehavior(BehaviorState behaviorState)
    {
        behavior = behaviorState;
        myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_BehaviorState")).replace("%petname%", myPet.petName).replace("%mode%", MyPetLanguage.getString("Name_" + behavior.name())));
        if (behavior == BehaviorState.Friendly)
        {
            myPet.getCraftPet().setTarget(null);
        }
    }

    public void activateBehavior(BehaviorState behaviorState)
    {
        if (active)
        {
            behavior = behaviorState;
            myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_BehaviorState")).replace("%petname%", myPet.petName).replace("%mode%", MyPetLanguage.getString("Name_" + behavior.name())));
            if (behavior == BehaviorState.Friendly)
            {
                myPet.getCraftPet().getHandle().setAttackTarget(null);
            }
        }
        else
        {
            myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_NoSkill")).replace("%petname%", myPet.petName).replace("%skill%", this.getName()));
        }
    }

    public BehaviorState getBehavior()
    {
        return behavior;
    }

    public boolean activate()
    {
        if (active)
        {
            if (behavior == BehaviorState.Normal)
            {
                if (BehaviorState.Friendly.isActive() && behaviorActive.get(BehaviorState.Friendly) && MyPetPermissions.hasExtended(myPet.getOwner().getPlayer(), "MyPet.user.extended.Behavior.Friendly"))
                {
                    behavior = BehaviorState.Friendly;
                    myPet.getCraftPet().setTarget(null);
                }
                else if (BehaviorState.Aggressive.isActive() && behaviorActive.get(BehaviorState.Aggressive) && MyPetPermissions.hasExtended(myPet.getOwner().getPlayer(), "MyPet.user.extended.Behavior.Aggressive"))
                {
                    behavior = BehaviorState.Aggressive;
                }
            }
            else if (behavior == BehaviorState.Friendly)
            {
                if (BehaviorState.Aggressive.isActive() && behaviorActive.get(BehaviorState.Aggressive) && MyPetPermissions.hasExtended(myPet.getOwner().getPlayer(), "MyPet.user.extended.Behavior.Aggressive"))
                {
                    behavior = BehaviorState.Aggressive;
                }
                else
                {
                    behavior = BehaviorState.Normal;
                }
            }
            else
            {
                behavior = BehaviorState.Normal;
            }
            myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_BehaviorState")).replace("%petname%", myPet.petName).replace("%mode%", MyPetLanguage.getString("Name_" + behavior.name())));
            return true;
        }
        else
        {
            myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_NoSkill")).replace("%petname%", myPet.petName).replace("%skill%", this.getName()));
            return false;
        }
    }

    public void load(CompoundTag compound)
    {
        if (compound.getValue().containsKey("Mode"))
        {
            behavior = BehaviorState.valueOf(((StringTag) compound.getValue().get("Mode")).getValue());
        }
    }

    public CompoundTag save()
    {
        CompoundTag nbtTagCompound = new CompoundTag(getName(), new CompoundMap());
        nbtTagCompound.getValue().put("Mode", new StringTag("Mode", behavior.name()));
        return nbtTagCompound;
    }

    public void schedule()
    {
        if (myPet instanceof MyEnderman)
        {
            MyEnderman myEnderman = (MyEnderman) myPet;
            if (behavior == BehaviorState.Aggressive)
            {
                if (!myEnderman.isScreaming())
                {
                    myEnderman.setScreaming(true);
                }
            }
            else
            {
                if (myEnderman.isScreaming())
                {
                    myEnderman.setScreaming(false);
                }
            }
        }
    }

    @Override
    public ISkillInstance cloneSkill()
    {
        Behavior newSkill = new Behavior(isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}