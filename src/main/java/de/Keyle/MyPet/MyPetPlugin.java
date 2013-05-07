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

package de.Keyle.MyPet;

import de.Keyle.MyPet.chatcommands.*;
import de.Keyle.MyPet.entity.types.IMyPet;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.entity.types.bat.EntityMyBat;
import de.Keyle.MyPet.entity.types.blaze.EntityMyBlaze;
import de.Keyle.MyPet.entity.types.cavespider.EntityMyCaveSpider;
import de.Keyle.MyPet.entity.types.chicken.EntityMyChicken;
import de.Keyle.MyPet.entity.types.cow.EntityMyCow;
import de.Keyle.MyPet.entity.types.creeper.EntityMyCreeper;
import de.Keyle.MyPet.entity.types.enderman.EntityMyEnderman;
import de.Keyle.MyPet.entity.types.ghast.EntityMyGhast;
import de.Keyle.MyPet.entity.types.giant.EntityMyGiant;
import de.Keyle.MyPet.entity.types.irongolem.EntityMyIronGolem;
import de.Keyle.MyPet.entity.types.magmacube.EntityMyMagmaCube;
import de.Keyle.MyPet.entity.types.mooshroom.EntityMyMooshroom;
import de.Keyle.MyPet.entity.types.ocelot.EntityMyOcelot;
import de.Keyle.MyPet.entity.types.pig.EntityMyPig;
import de.Keyle.MyPet.entity.types.pigzombie.EntityMyPigZombie;
import de.Keyle.MyPet.entity.types.sheep.EntityMySheep;
import de.Keyle.MyPet.entity.types.silverfish.EntityMySilverfish;
import de.Keyle.MyPet.entity.types.skeleton.EntityMySkeleton;
import de.Keyle.MyPet.entity.types.slime.EntityMySlime;
import de.Keyle.MyPet.entity.types.snowman.EntityMySnowman;
import de.Keyle.MyPet.entity.types.spider.EntityMySpider;
import de.Keyle.MyPet.entity.types.villager.EntityMyVillager;
import de.Keyle.MyPet.entity.types.witch.EntityMyWitch;
import de.Keyle.MyPet.entity.types.wither.EntityMyWither;
import de.Keyle.MyPet.entity.types.wolf.EntityMyWolf;
import de.Keyle.MyPet.entity.types.zombie.EntityMyZombie;
import de.Keyle.MyPet.listeners.*;
import de.Keyle.MyPet.skill.*;
import de.Keyle.MyPet.skill.skills.implementation.*;
import de.Keyle.MyPet.skill.skills.info.*;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoader;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderJSON;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderNBT;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderYAML;
import de.Keyle.MyPet.util.*;
import de.Keyle.MyPet.util.MyPetCaptureHelper.CaptureHelperMode;
import de.Keyle.MyPet.util.configuration.NBT_Configuration;
import de.Keyle.MyPet.util.configuration.YAML_Configuration;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;
import org.spout.nbt.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class MyPetPlugin extends JavaPlugin implements IScheduler
{
    private static MyPetPlugin plugin;
    private File NBTPetFile;
    private boolean isReady = false;
    private int autoSaveTimer = 0;

    public static MyPetPlugin getPlugin()
    {
        return plugin;
    }

    public void onDisable()
    {
        if (isReady)
        {
            int petCount = savePets(true);
            MyPetLogger.write("" + ChatColor.YELLOW + petCount + ChatColor.RESET + " pet(s) saved");
            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                myPet.removePet();
            }
            MyPetList.clearList();
        }
        MyPetTimer.reset();
        MyPetLogger.setConsole(null);
        Bukkit.getServer().getScheduler().cancelTasks(getPlugin());
        DebugLogger.info("MyPet disabled!");
    }

    public void onEnable()
    {
        plugin = this;
        this.isReady = false;
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "skilltrees" + File.separator).mkdirs();
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "backups" + File.separator).mkdirs();

        MyPetVersion.reset();
        MyPetLogger.setConsole(getServer().getConsoleSender());
        MyPetPvP.reset();
        MyPetEconomy.reset();
        MyPetConfiguration.config = this.getConfig();
        MyPetConfiguration.setDefault();
        MyPetConfiguration.loadConfiguration();

        DebugLogger.setup(MyPetConfiguration.USE_DEBUG_LOGGER);

        String minecraftVersion = ((CraftServer) getServer()).getHandle().getServerInstance().getMinecraftVersion();

        if (!MyPetVersion.getMinecraftVersion().equalsIgnoreCase(minecraftVersion))
        {
            MyPetLogger.write(ChatColor.RED + "---------------------------------------------------------");
            MyPetLogger.write(ChatColor.RED + "This version of MyPet only works with:");
            MyPetLogger.write(ChatColor.RED + "   Minecraft " + MyPetVersion.getMinecraftVersion());
            MyPetLogger.write(ChatColor.RED + "MyPet disabled!");
            MyPetLogger.write(ChatColor.RED + "---------------------------------------------------------");
            this.setEnabled(false);
            return;
        }

        DebugLogger.info("----------- loading MyPet ... -----------");
        DebugLogger.info("MyPet " + MyPetVersion.getMyPetVersion() + " build: " + MyPetVersion.getMyPetBuild());
        DebugLogger.info("Bukkit " + getServer().getVersion());

        DebugLogger.info("Plugins: " + Arrays.toString(getServer().getPluginManager().getPlugins()));

        List<String> nodeList = new ArrayList<String>();
        String allNodes = "";
        MyPetConfiguration.getConfigOptionList(nodeList, "MyPet");
        for (String node : nodeList)
        {
            allNodes += "\n   " + node;
        }
        DebugLogger.info("Config: -------------------------------" + allNodes);
        DebugLogger.info("---------------------------------------");

        DebugLogger.info("MobEXP table: -------------------------");
        for (MyPetMonsterExperience monsterExperience : MyPetMonsterExperience.mobExp.values())
        {
            DebugLogger.info("   " + monsterExperience.toString());
        }
        DebugLogger.info("MobEXP table end ----------------------");

        MyPetPlayerListener playerListener = new MyPetPlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);

        MyPetVehicleListener vehicleListener = new MyPetVehicleListener();
        getServer().getPluginManager().registerEvents(vehicleListener, this);

        MyPetEntityListener entityListener = new MyPetEntityListener();
        getServer().getPluginManager().registerEvents(entityListener, this);

        MyPetLevelUpListener levelupListener = new MyPetLevelUpListener();
        getServer().getPluginManager().registerEvents(levelupListener, this);

        MyPetWeatherListener weatherListener = new MyPetWeatherListener();
        getServer().getPluginManager().registerEvents(weatherListener, this);

        MyPetBlockListener blockListener = new MyPetBlockListener();
        getServer().getPluginManager().registerEvents(blockListener, this);

        getCommand("petname").setExecutor(new CommandName());
        getCommand("petcall").setExecutor(new CommandCall());
        getCommand("petsendaway").setExecutor(new CommandSendAway());
        getCommand("petstop").setExecutor(new CommandStop());
        getCommand("petrelease").setExecutor(new CommandRelease());
        getCommand("mypet").setExecutor(new CommandHelp());
        getCommand("petinventory").setExecutor(new CommandInventory());
        getCommand("petpickup").setExecutor(new CommandPickup());
        getCommand("petbehavior").setExecutor(new CommandBehavior());
        getCommand("petinfo").setExecutor(new CommandInfo());
        getCommand("petadmin").setExecutor(new CommandAdmin());
        getCommand("petskill").setExecutor(new CommandSkill());
        getCommand("petskilltree").setExecutor(new CommandShowSkillTree());
        getCommand("petchooseskilltree").setExecutor(new CommandChooseSkilltree());
        getCommand("petbeacon").setExecutor(new CommandBeacon());
        getCommand("petrespawn").setExecutor(new CommandRespawn());
        getCommand("pettype").setExecutor(new CommandPetType());
        getCommand("petcapturehelper").setExecutor(new CommandCaptureHelper());

        registerSkillsInfo();
        registerSkills();

        File defaultSkillConfigNBT = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.st");
        File defaultSkillConfigYAML = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.yml");
        File defaultSkillConfigJSON = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.json");

        if (!defaultSkillConfigNBT.exists() && !defaultSkillConfigYAML.exists() && !defaultSkillConfigJSON.exists())
        {
            try
            {
                InputStream template = getPlugin().getResource("skilltrees/default.st");
                OutputStream out = new FileOutputStream(defaultSkillConfigNBT);

                byte[] buf = new byte[1024];
                int len;
                while ((len = template.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                template.close();
                out.close();
                MyPetLogger.write("Default skilltree configfile created.");
                DebugLogger.info("created default.st");
            }
            catch (IOException ex)
            {
                MyPetLogger.write(ChatColor.RED + "Unable" + ChatColor.RESET + " to create the default.st!");
                DebugLogger.info("unable to create default.st");
            }
        }

        String[] petTypes = new String[MyPetType.values().length];
        for (int i = 0 ; i < MyPetType.values().length ; i++)
        {
            petTypes[i] = MyPetType.values()[i].getTypeName();
        }

        MyPetSkillTreeMobType.clearMobTypes();
        MyPetSkillTreeLoaderNBT.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
        MyPetSkillTreeLoaderYAML.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
        MyPetSkillTreeLoaderJSON.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);

        for (MyPetType mobType : MyPetType.values())
        {
            MyPetSkillTreeMobType skillTreeMobType = MyPetSkillTreeMobType.getMobTypeByName(mobType.getTypeName());
            MyPetSkillTreeLoader.addDefault(skillTreeMobType);
            MyPetSkillTreeLoader.manageInheritance(skillTreeMobType);
        }

        try
        {
            Method addMapping = EntityList.class.getDeclaredMethod("addMapping", Class.class, String.class, Integer.TYPE);
            addMapping.setAccessible(true);

            // https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/EntityTypes.java
            addMapping.invoke(addMapping, EntityMyCreeper.class, "Creeper", 50);
            addMapping.invoke(addMapping, EntityCreeper.class, "Creeper", 50);
            addMapping.invoke(addMapping, EntityMySkeleton.class, "Skeleton", 51);
            addMapping.invoke(addMapping, EntitySkeleton.class, "Skeleton", 51);
            addMapping.invoke(addMapping, EntityMySpider.class, "Spider", 52);
            addMapping.invoke(addMapping, EntitySpider.class, "Spider", 52);
            addMapping.invoke(addMapping, EntityMyGiant.class, "Giant", 53);
            addMapping.invoke(addMapping, EntityGiantZombie.class, "Giant", 53);
            addMapping.invoke(addMapping, EntityMyZombie.class, "Zombie", 54);
            addMapping.invoke(addMapping, EntityZombie.class, "Zombie", 54);
            addMapping.invoke(addMapping, EntityMySlime.class, "Slime", 55);
            addMapping.invoke(addMapping, EntitySlime.class, "Slime", 55);
            addMapping.invoke(addMapping, EntityMyGhast.class, "Ghast", 56);
            addMapping.invoke(addMapping, EntityGhast.class, "Ghast", 56);
            addMapping.invoke(addMapping, EntityMyPigZombie.class, "PigZombie", 57);
            addMapping.invoke(addMapping, EntityPigZombie.class, "PigZombie", 57);
            addMapping.invoke(addMapping, EntityMyEnderman.class, "Enderman", 58);
            addMapping.invoke(addMapping, EntityEnderman.class, "Enderman", 58);
            addMapping.invoke(addMapping, EntityMyCaveSpider.class, "CaveSpider", 59);
            addMapping.invoke(addMapping, EntityCaveSpider.class, "CaveSpider", 59);
            addMapping.invoke(addMapping, EntityMySilverfish.class, "Silverfish", 60);
            addMapping.invoke(addMapping, EntitySilverfish.class, "Silverfish", 60);
            addMapping.invoke(addMapping, EntityMyBlaze.class, "Blaze", 61);
            addMapping.invoke(addMapping, EntityBlaze.class, "Blaze", 61);
            addMapping.invoke(addMapping, EntityMyMagmaCube.class, "LavaSlime", 62);
            addMapping.invoke(addMapping, EntityMagmaCube.class, "LavaSlime", 62);
            addMapping.invoke(addMapping, EntityMyWither.class, "WitherBoss", 64);
            addMapping.invoke(addMapping, EntityWither.class, "WitherBoss", 64);
            addMapping.invoke(addMapping, EntityMyBat.class, "Bat", 65);
            addMapping.invoke(addMapping, EntityBat.class, "Bat", 65);
            addMapping.invoke(addMapping, EntityMyWitch.class, "Witch", 66);
            addMapping.invoke(addMapping, EntityWitch.class, "Witch", 66);
            addMapping.invoke(addMapping, EntityMyPig.class, "Pig", 90);
            addMapping.invoke(addMapping, EntityPig.class, "Pig", 90);
            addMapping.invoke(addMapping, EntityMySheep.class, "Sheep", 91);
            addMapping.invoke(addMapping, EntitySheep.class, "Sheep", 91);
            addMapping.invoke(addMapping, EntityMyCow.class, "Cow", 92);
            addMapping.invoke(addMapping, EntityCow.class, "Cow", 92);
            addMapping.invoke(addMapping, EntityMyChicken.class, "Chicken", 93);
            addMapping.invoke(addMapping, EntityChicken.class, "Chicken", 93);
            addMapping.invoke(addMapping, EntityMyWolf.class, "Wolf", 95);
            addMapping.invoke(addMapping, EntityWolf.class, "Wolf", 95);
            addMapping.invoke(addMapping, EntityMyMooshroom.class, "MushroomCow", 96);
            addMapping.invoke(addMapping, EntityMooshroom.class, "MushroomCow", 96);
            addMapping.invoke(addMapping, EntityMySnowman.class, "SnowMan", 97);
            addMapping.invoke(addMapping, EntitySnowman.class, "SnowMan", 97);
            addMapping.invoke(addMapping, EntityMyOcelot.class, "Ozelot", 98);
            addMapping.invoke(addMapping, EntityOcelot.class, "Ozelot", 98);
            addMapping.invoke(addMapping, EntityMyIronGolem.class, "VillagerGolem", 99);
            addMapping.invoke(addMapping, EntityIronGolem.class, "VillagerGolem", 99);
            addMapping.invoke(addMapping, EntityMyVillager.class, "Villager", 120);
            addMapping.invoke(addMapping, EntityVillager.class, "Villager", 120);

            DebugLogger.info("registered MyPet entities.");
        }
        catch (Exception e)
        {
            MyPetLogger.write("version " + MyPetPlugin.plugin.getDescription().getVersion() + ChatColor.RED + " NOT ENABLED");
            DebugLogger.severe(Arrays.toString(e.getStackTrace()));
            DebugLogger.severe("error while registering MyPet entity.");
            DebugLogger.severe(e.getMessage());
            setEnabled(false);
            return;
        }

        DebugLogger.info("Pet type: ----------");
        for (MyPetType myPetType : MyPetType.values())
        {
            DebugLogger.info("  " + myPetType.getTypeName() + " { " +
                    "startHP:" + MyPet.getStartHP(myPetType.getMyPetClass()) + ", " +
                    "speed:" + MyPet.getStartSpeed(myPetType.getMyPetClass()) + ", " +
                    "food:" + MyPet.getFood(myPetType.getMyPetClass()) + ", " +
                    "leashFlags:" + MyPet.getLeashFlags(myPetType.getMyPetClass()) + " }");
        }

        MyPetLanguage.load(new YAML_Configuration(getPlugin().getDataFolder().getPath() + File.separator + "lang.yml"));

        File NBTWolfFile = new File(getPlugin().getDataFolder().getPath() + File.separator + "Wolves.MyWolf");
        if (NBTWolfFile.exists())
        {
            NBTWolfFile.renameTo(new File(getPlugin().getDataFolder().getPath() + File.separator + "Wolves.MyWolf.old"));
            NBTWolfFile = new File(getPlugin().getDataFolder().getPath() + File.separator + "Wolves.MyWolf.old");
            loadMyWolfWolves(NBTWolfFile);
        }
        NBTPetFile = new File(getPlugin().getDataFolder().getPath() + File.separator + "My.Pets");

        if (MyPetBackup.MAKE_BACKUPS)
        {
            new MyPetBackup(NBTPetFile, new File(getPlugin().getDataFolder().getPath() + File.separator + "backups" + File.separator));
        }

        loadPets(NBTPetFile);

        MyPetTimer.startTimer();

        try
        {
            Metrics metrics = new Metrics(this);

            Graph graphPercent = metrics.createGraph("Percentage of every MyPet type");
            Graph graphCount = metrics.createGraph("Counted MyPets per type");
            Graph graphTotalCount = metrics.createGraph("Total MyPets");

            for (final MyPetType petType : MyPetType.values())
            {
                Plotter plotter = new Metrics.Plotter(petType.getTypeName())
                {
                    final MyPetType type = petType;

                    @Override
                    public int getValue()
                    {
                        return MyPetList.countMyPets(type);
                    }
                };
                graphPercent.addPlotter(plotter);
                graphCount.addPlotter(plotter);
            }

            Plotter plotter = new Metrics.Plotter("Total MyPets")
            {
                @Override
                public int getValue()
                {
                    return MyPetList.countMyPets();
                }
            };
            graphTotalCount.addPlotter(plotter);

            boolean metricsActive = metrics.start();
            DebugLogger.info("Metrics " + (metricsActive ? "" : "not ") + "activated");
        }
        catch (IOException e)
        {
            MyPetLogger.write(e.getMessage());
        }

        MyPetLogger.write("version " + MyPetVersion.getMyPetVersion() + "-b" + MyPetVersion.getMyPetBuild() + ChatColor.GREEN + " ENABLED");

        for (Player player : getServer().getOnlinePlayers())
        {
            if (MyPetPlayer.isMyPetPlayer(player))
            {
                MyPetPlayer myPetPlayer = MyPetPlayer.getMyPetPlayer(player);

                if (!myPetPlayer.hasMyPet() && myPetPlayer.hasInactiveMyPets())
                {
                    IMyPet myPet = MyPetList.getLastActiveMyPet(myPetPlayer);
                    if (!(myPetPlayer.hasLastActiveMyPet() && myPetPlayer.getLastActiveMyPetUUID() == null))
                    {
                        if (myPetPlayer.getLastActiveMyPetUUID() == null)
                        {
                            if (myPetPlayer.hasInactiveMyPets())
                            {
                                MyPetList.setMyPetActive(myPetPlayer.getInactiveMyPets()[0]);
                            }
                        }
                        else if (myPet != null && myPet instanceof InactiveMyPet)
                        {
                            MyPetList.setMyPetActive((InactiveMyPet) myPet);
                        }
                    }
                }
                if (myPetPlayer.hasMyPet())
                {
                    MyPet myPet = MyPetList.getMyPet(player);
                    if (myPet.getStatus() == PetState.Dead)
                    {
                        player.sendMessage(MyPetBukkitUtil.setColors(MyPetLanguage.getString("Msg_RespawnIn").replace("%petname%", myPet.petName).replace("%time%", "" + myPet.respawnTime)));
                    }
                    else if (myPet.getLocation().getWorld() == player.getLocation().getWorld() && myPet.getLocation().distance(player.getLocation()) < 75)
                    {
                        myPet.createPet();
                    }
                    else
                    {
                        myPet.status = PetState.Despawned;
                    }
                }
            }
        }
        this.isReady = true;
        savePets(false);
        MyPetTimer.addTask(this);
        DebugLogger.info("----------- MyPet ready -----------");
    }

    public static void registerSkills()
    {
        MyPetSkills.registerSkill(Inventory.class);
        MyPetSkills.registerSkill(HPregeneration.class);
        MyPetSkills.registerSkill(Pickup.class);
        MyPetSkills.registerSkill(Behavior.class);
        MyPetSkills.registerSkill(Damage.class);
        MyPetSkills.registerSkill(Control.class);
        MyPetSkills.registerSkill(HP.class);
        MyPetSkills.registerSkill(Poison.class);
        MyPetSkills.registerSkill(Ride.class);
        MyPetSkills.registerSkill(Thorns.class);
        MyPetSkills.registerSkill(Fire.class);
        MyPetSkills.registerSkill(Beacon.class);
        MyPetSkills.registerSkill(Wither.class);
        MyPetSkills.registerSkill(Lightning.class);
        MyPetSkills.registerSkill(Slow.class);
        MyPetSkills.registerSkill(Knockback.class);
        MyPetSkills.registerSkill(Ranged.class);
        MyPetSkills.registerSkill(Sprint.class);
    }

    public static void registerSkillsInfo()
    {
        MyPetSkillsInfo.registerSkill(InventoryInfo.class);
        MyPetSkillsInfo.registerSkill(HPregenerationInfo.class);
        MyPetSkillsInfo.registerSkill(PickupInfo.class);
        MyPetSkillsInfo.registerSkill(BehaviorInfo.class);
        MyPetSkillsInfo.registerSkill(DamageInfo.class);
        MyPetSkillsInfo.registerSkill(ControlInfo.class);
        MyPetSkillsInfo.registerSkill(HPInfo.class);
        MyPetSkillsInfo.registerSkill(PoisonInfo.class);
        MyPetSkillsInfo.registerSkill(RideInfo.class);
        MyPetSkillsInfo.registerSkill(ThornsInfo.class);
        MyPetSkillsInfo.registerSkill(FireInfo.class);
        MyPetSkillsInfo.registerSkill(BeaconInfo.class);
        MyPetSkillsInfo.registerSkill(WitherInfo.class);
        MyPetSkillsInfo.registerSkill(LightningInfo.class);
        MyPetSkillsInfo.registerSkill(SlowInfo.class);
        MyPetSkillsInfo.registerSkill(KnockbackInfo.class);
        MyPetSkillsInfo.registerSkill(RangedInfo.class);
        MyPetSkillsInfo.registerSkill(SprintInfo.class);
    }

    int loadPets(File f)
    {
        if (!f.exists())
        {
            MyPetLogger.write(ChatColor.YELLOW + "0" + ChatColor.RESET + " pet(s) loaded");
            return 0;
        }
        int petCount = 0;

        NBT_Configuration nbtConfiguration = new NBT_Configuration(f);
        if (!nbtConfiguration.load())
        {
            return 0;
        }
        ListTag petList = (ListTag) nbtConfiguration.getNBTCompound().getValue().get("Pets");
        if (nbtConfiguration.getNBTCompound().getValue().containsKey("CleanShutdown"))
        {
            DebugLogger.info("Clean shutdown: " + ((ByteTag) nbtConfiguration.getNBTCompound().getValue().get("CleanShutdown")).getBooleanValue());
        }

        DebugLogger.info("Loading players -------------------------");
        if (nbtConfiguration.getNBTCompound().getValue().containsKey("Players"))
        {
            DebugLogger.info(loadPlayers(nbtConfiguration) + " PetPlayer(s) loaded");
        }
        DebugLogger.info("-----------------------------------------");

        DebugLogger.info("loading Pets: -----------------------------");
        for (int i = 0 ; i < petList.getValue().size() ; i++)
        {
            CompoundTag myPetNBT = (CompoundTag) petList.getValue().get(i);
            CompoundTag locationNBT = (CompoundTag) myPetNBT.getValue().get("Location");

            double petX = ((DoubleTag) locationNBT.getValue().get("X")).getValue();
            double petY = ((DoubleTag) locationNBT.getValue().get("Y")).getValue();
            double petZ = ((DoubleTag) locationNBT.getValue().get("Z")).getValue();
            float petYaw = 1F;
            if (locationNBT.getValue().containsKey("Yaw"))
            {
                petYaw = ((FloatTag) locationNBT.getValue().get("Yaw")).getValue();
            }
            float petPitch = 1F;
            if (locationNBT.getValue().containsKey("Pitch"))
            {
                petPitch = ((FloatTag) locationNBT.getValue().get("Pitch")).getValue();
            }
            UUID petUuid = null;
            if (myPetNBT.getValue().containsKey("UUID"))
            {
                petUuid = UUID.fromString(((StringTag) myPetNBT.getValue().get("UUID")).getValue());
            }
            String petWorld = ((StringTag) locationNBT.getValue().get("World")).getValue();
            double petExp = ((DoubleTag) myPetNBT.getValue().get("Exp")).getValue();
            int petHealthNow = ((IntTag) myPetNBT.getValue().get("Health")).getValue();
            int petRespawnTime = ((IntTag) myPetNBT.getValue().get("Respawntime")).getValue();
            String petName = ((StringTag) myPetNBT.getValue().get("Name")).getValue();
            String petOwner = ((StringTag) myPetNBT.getValue().get("Owner")).getValue();
            String skillTree = null;
            if (myPetNBT.getValue().containsKey("Skilltree"))
            {
                skillTree = ((StringTag) myPetNBT.getValue().get("Skilltree")).getValue();
            }
            int petHunger = 100;
            if (myPetNBT.getValue().containsKey("Hunger"))
            {
                petHunger = ((IntTag) myPetNBT.getValue().get("Hunger")).getValue();
            }
            String petType = "Wolf";
            if (myPetNBT.getValue().containsKey("Type"))
            {
                petType = ((StringTag) myPetNBT.getValue().get("Type")).getValue();
            }

            InactiveMyPet inactiveMyPet = new InactiveMyPet(MyPetPlayer.getMyPetPlayer(petOwner));

            inactiveMyPet.setUUID(petUuid);
            inactiveMyPet.setLocation(new Location(Bukkit.getServer().getWorld(petWorld) != null ? Bukkit.getServer().getWorld(petWorld) : Bukkit.getServer().getWorlds().get(0), petX, petY, petZ, petYaw, petPitch));
            inactiveMyPet.setHealth(petHealthNow);
            inactiveMyPet.setHungerValue(petHunger);
            inactiveMyPet.setRespawnTime(petRespawnTime);
            inactiveMyPet.setPetName(petName);
            inactiveMyPet.setExp(petExp);
            inactiveMyPet.setSkills((CompoundTag) myPetNBT.getValue().get("Skills"));
            inactiveMyPet.setPetType(MyPetType.valueOf(petType));
            inactiveMyPet.setInfo((CompoundTag) myPetNBT.getValue().get("Info"));
            if (skillTree != null)
            {
                if (MyPetSkillTreeMobType.getMobTypeByPetType(inactiveMyPet.getPetType()) != null)
                {
                    MyPetSkillTreeMobType mobType = MyPetSkillTreeMobType.getMobTypeByPetType(inactiveMyPet.getPetType());

                    if (mobType.hasSkillTree(skillTree))
                    {
                        inactiveMyPet.setSkillTree(mobType.getSkillTree(skillTree));
                    }
                }
            }

            MyPetList.addInactiveMyPet(inactiveMyPet);

            DebugLogger.info("   " + inactiveMyPet.toString());

            petCount++;
        }
        MyPetLogger.write("" + ChatColor.YELLOW + petCount + ChatColor.RESET + " pet(s) loaded");
        return petCount;
    }

    int loadMyWolfWolves(File f)
    {
        int wolfCount = 0;

        NBT_Configuration nbtConfiguration = new NBT_Configuration(f);
        if (!nbtConfiguration.load())
        {
            return 0;
        }
        ListTag wolfList = (ListTag) nbtConfiguration.getNBTCompound().getValue().get("Wolves");

        DebugLogger.info("loading Wolves: -----------------------------");
        for (int i = 0 ; i < wolfList.getValue().size() ; i++)
        {
            CompoundTag myMolfNBT = (CompoundTag) wolfList.getValue().get(i);

            CompoundTag locationNBT = (CompoundTag) myMolfNBT.getValue().get("Location");
            double wolfX = ((DoubleTag) locationNBT.getValue().get("X")).getValue();
            double wolfY = ((DoubleTag) locationNBT.getValue().get("Y")).getValue();
            double wolfZ = ((DoubleTag) locationNBT.getValue().get("Z")).getValue();
            String wolfWorld = ((StringTag) locationNBT.getValue().get("World")).getValue();

            double wolfExp = ((DoubleTag) myMolfNBT.getValue().get("Exp")).getValue();
            int wolfHealthNow = ((IntTag) myMolfNBT.getValue().get("Health")).getValue();
            String wolfName = ((StringTag) myMolfNBT.getValue().get("Name")).getValue();
            String wolfOwner = ((StringTag) myMolfNBT.getValue().get("Owner")).getValue();

            InactiveMyPet inactiveMyPet = new InactiveMyPet(MyPetPlayer.getMyPetPlayer(wolfOwner));

            inactiveMyPet.setLocation(new Location(Bukkit.getServer().getWorld(wolfWorld) != null ? Bukkit.getServer().getWorld(wolfWorld) : Bukkit.getServer().getWorlds().get(0), wolfX, wolfY, wolfZ));
            inactiveMyPet.setHealth(wolfHealthNow);
            inactiveMyPet.setPetName(wolfName);
            inactiveMyPet.setExp(wolfExp);
            inactiveMyPet.setPetType(MyPetType.Wolf);

            MyPetList.addInactiveMyPet(inactiveMyPet);

            DebugLogger.info("   " + inactiveMyPet.toString());

            wolfCount++;
        }
        MyPetLogger.write("" + ChatColor.YELLOW + wolfCount + ChatColor.RESET + " wolf/wolves converted");
        return wolfCount;
    }

    public int savePets(boolean shutdown)
    {
        /*
        if (!isReady)
        {
            MyPetLogger.write(ChatColor.RED + "Plugin tried to save MyPets but it isn't ready! new pet will not be saved to protect the database.");
            return 0;
        }
        */
        autoSaveTimer = MyPetConfiguration.AUTOSAVE_TIME;
        int petCount = 0;
        NBT_Configuration nbtConfiguration = new NBT_Configuration(NBTPetFile);
        List<CompoundTag> petList = new ArrayList<CompoundTag>();

        for (MyPet myPet : MyPetList.getAllActiveMyPets())
        {
            CompoundTag petNBT = new CompoundTag(null, new CompoundMap());
            CompoundTag locationNBT = new CompoundTag("Location", new CompoundMap());

            locationNBT.getValue().put("X", new DoubleTag("X", myPet.getLocation().getX()));
            locationNBT.getValue().put("Y", new DoubleTag("Y", myPet.getLocation().getY()));
            locationNBT.getValue().put("Z", new DoubleTag("Z", myPet.getLocation().getY()));
            locationNBT.getValue().put("Yaw", new FloatTag("Yaw", myPet.getLocation().getYaw()));
            locationNBT.getValue().put("Pitch", new FloatTag("Pitch", myPet.getLocation().getPitch()));
            locationNBT.getValue().put("World", new StringTag("World", myPet.getLocation().getWorld().getName()));

            petNBT.getValue().put("UUID", new StringTag("UUID", myPet.getUUID().toString()));
            petNBT.getValue().put("Type", new StringTag("Type", myPet.getPetType().getTypeName()));
            petNBT.getValue().put("Owner", new StringTag("Owner", myPet.getOwner().getName()));
            petNBT.getValue().put("Location", locationNBT);
            petNBT.getValue().put("Health", new IntTag("Health", myPet.getHealth()));
            petNBT.getValue().put("Respawntime", new IntTag("Respawntime", myPet.getRespawnTime()));
            petNBT.getValue().put("Hunger", new IntTag("Hunger", myPet.getHungerValue()));
            petNBT.getValue().put("Name", new StringTag("Name", myPet.getPetName()));
            petNBT.getValue().put("Exp", new DoubleTag("Exp", myPet.getExp()));
            petNBT.getValue().put("Info", myPet.getExtendedInfo());
            if (myPet.getSkillTree() != null)
            {
                petNBT.getValue().put("Skilltree", new StringTag("Skilltree", myPet.getSkillTree().getName()));
            }

            CompoundTag skillsNBT = new CompoundTag("Skills", new CompoundMap());
            Collection<ISkillInstance> skillList = myPet.getSkills().getSkills();
            if (skillList.size() > 0)
            {
                for (ISkillInstance skill : skillList)
                {
                    if (skill instanceof ISkillStorage)
                    {
                        ISkillStorage storageSkill = (ISkillStorage) skill;
                        CompoundTag s = storageSkill.save();
                        if (s != null)
                        {
                            skillsNBT.getValue().put(skill.getName(), s);
                        }
                    }
                }
            }
            petNBT.getValue().put("Skills", skillsNBT);
            petList.add(petNBT);
            petCount++;
        }
        for (InactiveMyPet inactiveMyPet : MyPetList.getAllInactiveMyPets())
        {
            CompoundTag petNBT = new CompoundTag(null, new CompoundMap());
            CompoundTag locationNBT = new CompoundTag("Location", new CompoundMap());

            locationNBT.getValue().put("X", new DoubleTag("X", inactiveMyPet.getLocation().getX()));
            locationNBT.getValue().put("Y", new DoubleTag("Y", inactiveMyPet.getLocation().getY()));
            locationNBT.getValue().put("Z", new DoubleTag("Z", inactiveMyPet.getLocation().getY()));
            locationNBT.getValue().put("Yaw", new FloatTag("Yaw", inactiveMyPet.getLocation().getYaw()));
            locationNBT.getValue().put("Pitch", new FloatTag("Pitch", inactiveMyPet.getLocation().getPitch()));
            locationNBT.getValue().put("World", new StringTag("World", inactiveMyPet.getLocation().getWorld().getName()));

            petNBT.getValue().put("UUID", new StringTag("UUID", inactiveMyPet.getUUID().toString()));
            petNBT.getValue().put("Type", new StringTag("Type", inactiveMyPet.getPetType().getTypeName()));
            petNBT.getValue().put("Owner", new StringTag("Owner", inactiveMyPet.getOwner().getName()));
            petNBT.getValue().put("Location", locationNBT);
            petNBT.getValue().put("Health", new IntTag("Health", inactiveMyPet.getHealth()));
            petNBT.getValue().put("Respawntime", new IntTag("Respawntime", inactiveMyPet.getRespawnTime()));
            petNBT.getValue().put("Hunger", new IntTag("Hunger", inactiveMyPet.getHungerValue()));
            petNBT.getValue().put("Name", new StringTag("Name", inactiveMyPet.getPetName()));
            petNBT.getValue().put("Exp", new DoubleTag("Exp", inactiveMyPet.getExp()));
            petNBT.getValue().put("Info", inactiveMyPet.getInfo());
            if (inactiveMyPet.getSkillTree() != null)
            {
                petNBT.getValue().put("Skilltree", new StringTag("Skilltree", inactiveMyPet.getSkillTree().getName()));
            }

            petNBT.getValue().put("Skills", inactiveMyPet.getSkills());

            petList.add(petNBT);
            petCount++;
        }
        nbtConfiguration.getNBTCompound().getValue().put("Version", new StringTag("Version", MyPetVersion.getMyPetVersion()));
        nbtConfiguration.getNBTCompound().getValue().put("Build", new StringTag("Build", MyPetVersion.getMyPetBuild()));
        nbtConfiguration.getNBTCompound().getValue().put("CleanShutdown", new ByteTag("CleanShutdown", shutdown));
        nbtConfiguration.getNBTCompound().getValue().put("Pets", new ListTag<CompoundTag>("Pets", CompoundTag.class, petList));
        nbtConfiguration.getNBTCompound().getValue().put("Players", savePlayers());
        nbtConfiguration.save();
        return petCount;
    }

    private ListTag savePlayers()
    {
        List<CompoundTag> playerList = new ArrayList<CompoundTag>();
        for (MyPetPlayer myPetPlayer : MyPetPlayer.getMyPetPlayers())
        {
            if (myPetPlayer.hasCustomData())
            {
                CompoundTag playerNBT = new CompoundTag(myPetPlayer.getName(), new CompoundMap());

                playerNBT.getValue().put("Name", new StringTag("Name", myPetPlayer.getName()));
                playerNBT.getValue().put("AutoRespawn", new ByteTag("AutoRespawn", myPetPlayer.hasAutoRespawnEnabled()));
                playerNBT.getValue().put("AutoRespawnMin", new IntTag("AutoRespawnMin", myPetPlayer.getAutoRespawnMin()));
                playerNBT.getValue().put("ExtendedInfo", myPetPlayer.getExtendedInfo());
                playerNBT.getValue().put("CaptureMode", new StringTag("CaptureMode", myPetPlayer.getCaptureHelperMode().name()));
                if (myPetPlayer.getLastActiveMyPetUUID() != null)
                {
                    playerNBT.getValue().put("LastActiveMyPetUUID", new StringTag("LastActiveMyPetUUID", myPetPlayer.getLastActiveMyPetUUID().toString()));
                }
                else
                {
                    playerNBT.getValue().put("LastActiveMyPetUUID", new StringTag("LastActiveMyPetUUID", ""));
                }

                playerList.add(playerNBT);
            }
        }
        return new ListTag<CompoundTag>("Players", CompoundTag.class, playerList);
    }

    private int loadPlayers(NBT_Configuration nbtConfiguration)
    {
        int playerCount = 0;
        ListTag playerList = (ListTag) nbtConfiguration.getNBTCompound().getValue().get("Players");

        for (int i = 0 ; i < playerList.getValue().size() ; i++)
        {
            CompoundTag myplayerNBT = (CompoundTag) playerList.getValue().get(i);
            MyPetPlayer petPlayer = MyPetPlayer.getMyPetPlayer(((StringTag) myplayerNBT.getValue().get("Name")).getValue());

            if (myplayerNBT.getValue().containsKey("AutoRespawn"))
            {
                petPlayer.setAutoRespawnEnabled(((ByteTag) myplayerNBT.getValue().get("AutoRespawn")).getBooleanValue());
            }
            if (myplayerNBT.getValue().containsKey("AutoRespawnMin"))
            {
                petPlayer.setAutoRespawnMin(((IntTag) myplayerNBT.getValue().get("AutoRespawnMin")).getValue());
            }
            if (myplayerNBT.getValue().containsKey("CaptureMode"))
            {
                String mode = ((StringTag) myplayerNBT.getValue().get("CaptureMode")).getValue();
                petPlayer.setCaptureHelperMode(CaptureHelperMode.valueOf(mode));
            }
            if (myplayerNBT.getValue().containsKey("LastActiveMyPetUUID"))
            {
                String lastActive = ((StringTag) myplayerNBT.getValue().get("LastActiveMyPetUUID")).getValue();
                if (!lastActive.equalsIgnoreCase(""))
                {
                    petPlayer.setLastActiveMyPetUUID(UUID.fromString(lastActive));
                }
                else
                {
                    petPlayer.setLastActiveMyPetUUID(null);
                }
            }
            if (myplayerNBT.getValue().containsKey("ExtendedInfo"))
            {
                petPlayer.setExtendedInfo((CompoundTag) myplayerNBT.getValue().get("ExtendedInfo"));
            }

            playerCount++;
            DebugLogger.info("   " + petPlayer);
        }
        return playerCount;
    }

    @Override
    public void schedule()
    {
        if (MyPetConfiguration.AUTOSAVE_TIME > 0 && autoSaveTimer-- <= 0)
        {
            MyPetPlugin.getPlugin().savePets(false);
            autoSaveTimer = MyPetConfiguration.AUTOSAVE_TIME;
        }
    }
}