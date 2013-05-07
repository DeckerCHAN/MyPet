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

package de.Keyle.MyPet.chatcommands;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.chicken.MyChicken;
import de.Keyle.MyPet.entity.types.cow.MyCow;
import de.Keyle.MyPet.entity.types.creeper.MyCreeper;
import de.Keyle.MyPet.entity.types.enderman.MyEnderman;
import de.Keyle.MyPet.entity.types.irongolem.MyIronGolem;
import de.Keyle.MyPet.entity.types.magmacube.MyMagmaCube;
import de.Keyle.MyPet.entity.types.mooshroom.MyMooshroom;
import de.Keyle.MyPet.entity.types.ocelot.MyOcelot;
import de.Keyle.MyPet.entity.types.pig.MyPig;
import de.Keyle.MyPet.entity.types.pigzombie.MyPigZombie;
import de.Keyle.MyPet.entity.types.sheep.MySheep;
import de.Keyle.MyPet.entity.types.skeleton.MySkeleton;
import de.Keyle.MyPet.entity.types.slime.MySlime;
import de.Keyle.MyPet.entity.types.villager.MyVillager;
import de.Keyle.MyPet.entity.types.wolf.MyWolf;
import de.Keyle.MyPet.entity.types.zombie.MyZombie;
import de.Keyle.MyPet.skill.skills.implementation.Inventory;
import de.Keyle.MyPet.skill.skills.implementation.inventory.MyPetCustomInventory;
import de.Keyle.MyPet.util.*;
import de.Keyle.MyPet.util.logger.DebugLogger;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPigZombie;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftSkeleton;
import org.bukkit.entity.*;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class CommandRelease implements CommandExecutor, TabCompleter
{
    private static List<String> emptyList = new ArrayList<String>();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player petOwner = (Player) sender;
            if (MyPetList.hasMyPet(petOwner))
            {
                MyPet myPet = MyPetList.getMyPet(petOwner);

                if (!MyPetPermissions.has(petOwner, "MyPet.user.release"))
                {
                    return true;
                }
                if (myPet.getStatus() == PetState.Despawned)
                {
                    sender.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_CallFirst")).replace("%petname%", myPet.petName));
                    return true;
                }
                else if (myPet.getStatus() == PetState.Dead)
                {
                    sender.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_RespawnIn").replace("%petname%", myPet.petName).replace("%time%", "" + myPet.respawnTime)));
                    return true;
                }
                if (args.length < 1)
                {
                    return false;
                }
                String name = "";
                for (String arg : args)
                {
                    if (!name.equals(""))
                    {
                        name += " ";
                    }
                    name += arg;
                }
                if (myPet.petName.replaceAll("§[abcdefklmnor0-9]", "").equalsIgnoreCase(name))
                {
                    if (myPet.getSkills().isSkillActive("Inventory"))
                    {
                        MyPetCustomInventory inv = ((Inventory) myPet.getSkills().getSkill("Inventory")).inv;
                        inv.dropContentAt(myPet.getLocation());
                    }

                    if (!MyPetConfiguration.REMOVE_PETS_AFTER_RELEASE)
                    {
                        LivingEntity normalEntity = (LivingEntity) myPet.getLocation().getWorld().spawnEntity(myPet.getLocation(), myPet.getPetType().getEntityType());

                        if (myPet instanceof MyChicken)
                        {
                            if (((MyChicken) myPet).isBaby())
                            {
                                ((Chicken) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Chicken) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyCow)
                        {
                            if (((MyCow) myPet).isBaby())
                            {
                                ((Cow) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Cow) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyCreeper)
                        {
                            ((Creeper) normalEntity).setPowered(((MyCreeper) myPet).isPowered());
                        }
                        else if (myPet instanceof MyEnderman)
                        {
                            MaterialData materialData = new MaterialData(((MyEnderman) myPet).getBlockID(), (byte) ((MyEnderman) myPet).getBlockData());
                            ((Enderman) normalEntity).setCarriedMaterial(materialData);
                        }
                        else if (myPet instanceof MyIronGolem)
                        {
                            ((IronGolem) normalEntity).setPlayerCreated(true);
                        }
                        else if (myPet instanceof MyMooshroom)
                        {
                            if (((MyMooshroom) myPet).isBaby())
                            {
                                ((MushroomCow) normalEntity).setBaby();
                            }
                            else
                            {
                                ((MushroomCow) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyMagmaCube)
                        {
                            ((MagmaCube) normalEntity).setSize(((MyMagmaCube) myPet).getSize());
                        }
                        else if (myPet instanceof MyOcelot)
                        {
                            ((Ocelot) normalEntity).setCatType(Type.WILD_OCELOT);
                            ((Ocelot) normalEntity).setTamed(false);
                            if (((MyOcelot) myPet).isBaby())
                            {
                                ((Ocelot) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Ocelot) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyPig)
                        {
                            ((Pig) normalEntity).setSaddle(((MyPig) myPet).hasSaddle());
                            if (((MyPig) myPet).isBaby())
                            {
                                ((Pig) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Pig) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MySheep)
                        {
                            ((Sheep) normalEntity).setSheared(((MySheep) myPet).isSheared());
                            ((Sheep) normalEntity).setColor(((MySheep) myPet).getColor());
                            if (((MySheep) myPet).isBaby())
                            {
                                ((Sheep) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Sheep) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyVillager)
                        {
                            ((Villager) normalEntity).setProfession(Profession.getProfession(((MyVillager) myPet).getProfession()));
                            if (((MyVillager) myPet).isBaby())
                            {
                                ((Villager) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Villager) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MyWolf)
                        {
                            ((Wolf) normalEntity).setTamed(false);
                            if (((MyWolf) myPet).isBaby())
                            {
                                ((Wolf) normalEntity).setBaby();
                            }
                            else
                            {
                                ((Wolf) normalEntity).setAdult();
                            }
                        }
                        else if (myPet instanceof MySlime)
                        {
                            ((Slime) normalEntity).setSize(((MySlime) myPet).getSize());
                        }
                        else if (myPet instanceof MyZombie)
                        {
                            ((Zombie) normalEntity).setBaby(((MyZombie) myPet).isBaby());
                            World world = myPet.getCraftPet().getHandle().worldObj;
                            Location petLocation = myPet.getLocation();
                            for (ItemStack is : ((MyZombie) myPet).getEquipment())
                            {
                                if (is != null)
                                {
                                    EntityItem itemEntity = new EntityItem(world, petLocation.getX(), petLocation.getY(), petLocation.getZ(), is);
                                    itemEntity.delayBeforeCanPickup = 20;
                                    world.spawnEntityInWorld(itemEntity);
                                }
                            }
                        }
                        else if (myPet instanceof MySkeleton)
                        {
                            if (((MySkeleton) myPet).isWither())
                            {
                                ((Skeleton) normalEntity).setSkeletonType(SkeletonType.WITHER);
                                ((CraftSkeleton) normalEntity).getHandle().setCurrentItemOrArmor(0, new ItemStack(Item.swordStone));
                            }
                            else
                            {
                                ((CraftSkeleton) normalEntity).getHandle().setCurrentItemOrArmor(0, new ItemStack(Item.bow));
                            }
                            World world = myPet.getCraftPet().getHandle().worldObj;
                            Location petLocation = myPet.getLocation();
                            for (ItemStack is : ((MySkeleton) myPet).getEquipment())
                            {
                                if (is != null)
                                {
                                    EntityItem itemEntity = new EntityItem(world, petLocation.getX(), petLocation.getY(), petLocation.getZ(), is);
                                    itemEntity.delayBeforeCanPickup = 20;
                                    world.spawnEntityInWorld(itemEntity);
                                }
                            }
                        }
                        else if (myPet instanceof MyPigZombie)
                        {
                            ((CraftPigZombie) normalEntity).getHandle().setCurrentItemOrArmor(0, new ItemStack(Item.swordGold));
                            World world = myPet.getCraftPet().getHandle().worldObj;
                            Location petLocation = myPet.getLocation();
                            for (ItemStack is : ((MyPigZombie) myPet).getEquipment())
                            {
                                if (is != null)
                                {
                                    EntityItem itemEntity = new EntityItem(world, petLocation.getX(), petLocation.getY(), petLocation.getZ(), is);
                                    itemEntity.delayBeforeCanPickup = 10;
                                    world.spawnEntityInWorld(itemEntity);
                                }
                            }
                        }
                    }
                    myPet.removePet();

                    sender.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_Release")).replace("%petname%", myPet.petName));
                    MyPetList.removeInactiveMyPet(MyPetList.setMyPetInactive(myPet.getOwner()));
                    DebugLogger.info(sender.getName() + " released pet.");
                    if (MyPetConfiguration.STORE_PETS_ON_PET_RELEASE)
                    {
                        DebugLogger.info(MyPetPlugin.getPlugin().savePets(false) + " pet(s) saved.");
                    }
                    return true;
                }
                else
                {
                    sender.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_Name")).replace("%petname%", myPet.petName));
                    return true;
                }
            }
            else
            {
                sender.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_DontHavePet")));
            }
            return true;
        }
        sender.sendMessage("You can't use this command from server console!");
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (MyPetList.hasMyPet((Player) commandSender))
        {
            List<String> petnameList = new ArrayList<String>();
            petnameList.add(MyPetPlayer.getMyPetPlayer((Player) commandSender).getMyPet().getPetName());
            return petnameList;
        }
        return emptyList;
    }
}