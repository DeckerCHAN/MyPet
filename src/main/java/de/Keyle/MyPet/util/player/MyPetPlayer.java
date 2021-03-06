/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2014 Keyle
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

package de.Keyle.MyPet.util.player;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.Keyle.MyPet.api.util.IScheduler;
import de.Keyle.MyPet.api.util.NBTStorage;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.util.BukkitUtil;
import de.Keyle.MyPet.util.Util;
import de.Keyle.MyPet.util.WorldGroup;
import de.Keyle.MyPet.util.locale.Locales;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.MyPet.util.support.Permissions;
import de.Keyle.MyPet.util.support.arenas.*;
import de.keyle.knbt.*;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class MyPetPlayer implements IScheduler, NBTStorage {
    public final static Set<UUID> onlinePlayerUUIDList = new HashSet<UUID>();
    public final static Set<String> onlinePlayerNamesList = new HashSet<String>();

    protected String lastKnownPlayerName;
    protected String lastLanguage = "en_US";
    protected UUID mojangUUID = null;
    protected UUID offlineUUID = null;

    protected boolean captureHelperMode = false;
    protected boolean autoRespawn = false;
    protected int autoRespawnMin = 1;

    protected BiMap<String, UUID> petWorldUUID = HashBiMap.create();
    protected BiMap<UUID, String> petUUIDWorld = petWorldUUID.inverse();
    protected TagCompound extendedInfo = new TagCompound();

    public String getName() {
        return lastKnownPlayerName;
    }

    public boolean hasCustomData() {
        if (autoRespawn || autoRespawnMin != 1) {
            return true;
        } else if (captureHelperMode) {
            return true;
        } else if (extendedInfo.getCompoundData().size() > 0) {
            return true;
        } else if (petWorldUUID.size() > 0) {
            return true;
        }
        return false;
    }

    // Custom Data -----------------------------------------------------------------

    public void setAutoRespawnEnabled(boolean flag) {
        autoRespawn = flag;
    }

    public boolean hasAutoRespawnEnabled() {
        return autoRespawn;
    }

    public void setAutoRespawnMin(int value) {
        autoRespawnMin = value;
    }

    public int getAutoRespawnMin() {
        return autoRespawnMin;
    }

    public boolean isCaptureHelperActive() {
        return captureHelperMode;
    }

    public void setCaptureHelperActive(boolean captureHelperMode) {
        this.captureHelperMode = captureHelperMode;
    }

    public void setMyPetForWorldGroup(String worldGroup, UUID myPetUUID) {
        if (worldGroup == null || worldGroup.equals("")) {
            return;
        }
        if (myPetUUID == null) {
            petWorldUUID.remove(worldGroup);
        } else {
            try {
                petWorldUUID.put(worldGroup, myPetUUID);
            } catch (IllegalArgumentException e) {
                DebugLogger.warning("There are two pets registered for one worldgroup or vice versa!");
            }
        }
    }

    public UUID getMyPetForWorldGroup(String worldGroup) {
        return petWorldUUID.get(worldGroup);
    }

    public String getWorldGroupForMyPet(UUID petUUID) {
        return petUUIDWorld.get(petUUID);
    }

    public boolean hasMyPetInWorldGroup(String worldGroup) {
        return petWorldUUID.containsKey(worldGroup);
    }

    public boolean hasInactiveMyPetInWorldGroup(String worldGroup) {
        for (InactiveMyPet inactiveMyPet : getInactiveMyPets()) {
            if (inactiveMyPet.getWorldGroup().equals(worldGroup)) {
                return true;
            }
        }
        return false;
    }

    public void setExtendedInfo(TagCompound compound) {
        if (extendedInfo.getCompoundData().size() == 0) {
            extendedInfo = compound;
        }
    }

    public void addExtendedInfo(String key, TagBase tag) {
        extendedInfo.getCompoundData().put(key, tag);
    }

    public TagBase getExtendedInfo(String key) {
        if (extendedInfo.getCompoundData().containsKey(key)) {
            return extendedInfo.getCompoundData().get(key);
        }
        return null;
    }

    public TagCompound getExtendedInfo() {
        return extendedInfo;
    }

    // -----------------------------------------------------------------------------

    public abstract boolean isOnline();

    public boolean isInExternalGames() {
        if (MobArena.isInMobArena(this) ||
                Minigames.isInMinigame(this) ||
                BattleArena.isInBattleArena(this) ||
                PvPArena.isInPvPArena(this) ||
                MyHungerGames.isInHungerGames(this) ||
                SurvivalGames.isInSurvivalGames(this)) {
            return true;
        }
        return false;
    }

    public UUID getPlayerUUID() {
        if (Bukkit.getOnlineMode()) {
            return mojangUUID;
        } else {
            return offlineUUID;
        }
    }

    public UUID getOfflineUUID() {
        return offlineUUID;
    }

    public UUID getMojangUUID() {
        return mojangUUID;
    }

    public String getLanguage() {
        if (isOnline()) {
            lastLanguage = BukkitUtil.getPlayerLanguage(getPlayer());
        }
        return lastLanguage;
    }

    public boolean isMyPetAdmin() {
        return isOnline() && Permissions.has(getPlayer(), "MyPet.admin", false);
    }

    public boolean hasMyPet() {
        return MyPetList.hasMyPet(this);
    }

    public MyPet getMyPet() {
        return MyPetList.getMyPet(this);
    }

    public boolean hasInactiveMyPets() {
        return MyPetList.hasInactiveMyPets(this);
    }

    public InactiveMyPet getInactiveMyPet(UUID petUUID) {
        for (InactiveMyPet inactiveMyPet : MyPetList.getInactiveMyPets(this)) {
            if (inactiveMyPet.getUUID().equals(petUUID)) {
                return inactiveMyPet;
            }
        }
        return null;
    }

    public List<InactiveMyPet> getInactiveMyPets() {
        return MyPetList.getInactiveMyPets(this);
    }

    public abstract Player getPlayer();

    public EntityPlayer getEntityPlayer() {
        Player p = getPlayer();
        if (p != null) {
            return ((CraftPlayer) p).getHandle();
        }
        return null;
    }

    public static MyPetPlayer getMyPetPlayer(UUID uuid) {
        if (OnlineMyPetPlayer.playerList.containsKey(uuid)) {
            return OnlineMyPetPlayer.playerList.get(uuid);
        } else {
            OnlineMyPetPlayer myPetPlayer = new OnlineMyPetPlayer(uuid);
            OnlineMyPetPlayer.playerList.put(uuid, myPetPlayer);
            return myPetPlayer;
        }
    }

    public static MyPetPlayer getMyPetPlayer(String name) {
        if (OfflineMyPetPlayer.playerList.containsKey(name)) {
            return OfflineMyPetPlayer.playerList.get(name);
        } else {
            OfflineMyPetPlayer myPetPlayer = new OfflineMyPetPlayer(name);
            OfflineMyPetPlayer.playerList.put(name, myPetPlayer);
            return myPetPlayer;
        }
    }

    public static MyPetPlayer getMyPetPlayer(Player player) {
        if (Bukkit.getOnlineMode()) {
            return MyPetPlayer.getMyPetPlayer(player.getUniqueId());
        } else {
            return MyPetPlayer.getMyPetPlayer(player.getName());
        }
    }

    public static boolean isMyPetPlayer(String name) {
        return OfflineMyPetPlayer.playerList.containsKey(name);
    }

    public static boolean isMyPetPlayer(Player player) {
        if (Bukkit.getOnlineMode()) {
            return OnlineMyPetPlayer.playerList.containsKey(player.getUniqueId());
        } else {
            return OfflineMyPetPlayer.playerList.containsKey(player.getName());
        }
    }

    public static MyPetPlayer[] getMyPetPlayers() {
        MyPetPlayer[] playerArray;
        int playerCounter = 0;
        if (Bukkit.getOnlineMode()) {
            playerArray = new MyPetPlayer[OnlineMyPetPlayer.playerList.size()];
            for (MyPetPlayer player : OnlineMyPetPlayer.playerList.values()) {
                playerArray[playerCounter++] = player;
            }
        } else {
            playerArray = new MyPetPlayer[OfflineMyPetPlayer.playerList.size()];
            for (MyPetPlayer player : OfflineMyPetPlayer.playerList.values()) {
                playerArray[playerCounter++] = player;
            }
        }

        return playerArray;
    }

    public static boolean checkRemovePlayer(MyPetPlayer myPetPlayer) {
        if (!myPetPlayer.isOnline() && !myPetPlayer.hasCustomData() && myPetPlayer.getMyPet() == null && myPetPlayer.getInactiveMyPets().size() == 0) {
            if (Bukkit.getOnlineMode()) {
                OnlineMyPetPlayer.playerList.remove(myPetPlayer.getPlayerUUID());
            } else {
                OfflineMyPetPlayer.playerList.remove(myPetPlayer.getName());
            }
            return true;
        }
        return false;
    }

    @Override
    public TagCompound save() {
        TagCompound playerNBT = new TagCompound();

        playerNBT.getCompoundData().put("Name", new TagString(getName()));
        playerNBT.getCompoundData().put("AutoRespawn", new TagByte(hasAutoRespawnEnabled()));
        playerNBT.getCompoundData().put("AutoRespawnMin", new TagInt(getAutoRespawnMin()));
        playerNBT.getCompoundData().put("AutoRespawnMin2", new TagInt(getAutoRespawnMin()));
        playerNBT.getCompoundData().put("ExtendedInfo", getExtendedInfo());
        playerNBT.getCompoundData().put("CaptureMode", new TagByte(isCaptureHelperActive()));

        if (offlineUUID != null) {
            playerNBT.getCompoundData().put("Offline-UUID", new TagString(offlineUUID.toString()));
        }
        if (mojangUUID != null) {
            playerNBT.getCompoundData().put("Mojang-UUID", new TagString(mojangUUID.toString()));
        }
        TagCompound multiWorldCompound = new TagCompound();
        for (String worldGroupName : petWorldUUID.keySet()) {
            multiWorldCompound.getCompoundData().put(worldGroupName, new TagString(petWorldUUID.get(worldGroupName).toString()));
        }
        playerNBT.getCompoundData().put("MultiWorld", multiWorldCompound);

        return playerNBT;
    }

    @Override
    public void load(TagCompound myplayerNBT) {
        if (myplayerNBT.getCompoundData().containsKey("UUID")) {
            offlineUUID = UUID.fromString(myplayerNBT.getAs("UUID", TagString.class).getStringData());
        }
        if (myplayerNBT.getCompoundData().containsKey("Offline-UUID")) {
            offlineUUID = UUID.fromString(myplayerNBT.getAs("Offline-UUID", TagString.class).getStringData());
        }
        if (myplayerNBT.getCompoundData().containsKey("Mojang-UUID")) {
            mojangUUID = UUID.fromString(myplayerNBT.getAs("Mojang-UUID", TagString.class).getStringData());
        }
        if (myplayerNBT.getCompoundData().containsKey("AutoRespawn")) {
            setAutoRespawnEnabled(myplayerNBT.getAs("AutoRespawn", TagByte.class).getBooleanData());
        }
        if (myplayerNBT.getCompoundData().containsKey("AutoRespawnMin")) {
            setAutoRespawnMin(myplayerNBT.getAs("AutoRespawnMin", TagInt.class).getIntData());
        }
        if (myplayerNBT.containsKeyAs("CaptureMode", TagString.class)) {
            if (!myplayerNBT.getAs("CaptureMode", TagString.class).getStringData().equals("Deactivated")) {
                setCaptureHelperActive(true);
            }
        } else if (myplayerNBT.containsKeyAs("CaptureMode", TagByte.class)) {
            setCaptureHelperActive(myplayerNBT.getAs("CaptureMode", TagByte.class).getBooleanData());
        }
        if (myplayerNBT.getCompoundData().containsKey("LastActiveMyPetUUID")) {
            String lastActive = myplayerNBT.getAs("LastActiveMyPetUUID", TagString.class).getStringData();
            if (!lastActive.equalsIgnoreCase("")) {
                UUID lastActiveUUID = UUID.fromString(lastActive);
                World newWorld = Bukkit.getServer().getWorlds().get(0);
                WorldGroup lastActiveGroup = WorldGroup.getGroupByWorld(newWorld.getName());
                this.setMyPetForWorldGroup(lastActiveGroup.getName(), lastActiveUUID);
            }
        }
        if (myplayerNBT.getCompoundData().containsKey("ExtendedInfo")) {
            setExtendedInfo(myplayerNBT.getAs("ExtendedInfo", TagCompound.class));
        }
        if (myplayerNBT.getCompoundData().containsKey("MultiWorld")) {
            TagCompound worldGroups = myplayerNBT.getAs("MultiWorld", TagCompound.class);
            for (String worldGroupName : worldGroups.getCompoundData().keySet()) {
                String petUUID = worldGroups.getAs(worldGroupName, TagString.class).getStringData();
                setMyPetForWorldGroup(worldGroupName, UUID.fromString(petUUID));
            }
        }
    }

    public void schedule() {
        if (!isOnline()) {
            return;
        }
        if (hasMyPet()) {
            MyPet myPet = getMyPet();
            if (myPet.getStatus() == PetState.Here) {
                if (myPet.getLocation().getWorld() != this.getPlayer().getLocation().getWorld() || myPet.getLocation().distance(this.getPlayer().getLocation()) > 40) {
                    myPet.removePet(true);
                    myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.Spawn.Despawn", getLanguage()), myPet.getPetName()));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof Player) {
            Player player = (Player) obj;
            if (Bukkit.getOnlineMode()) {
                return getPlayerUUID().equals(player.getUniqueId());
            } else {
                return getName().equals(player.getName());
            }
        } else if (obj instanceof OfflinePlayer) {
            OfflinePlayer offlinePlayer = (OfflinePlayer) obj;
            if (Bukkit.getOnlineMode()) {
                return getPlayerUUID().equals(offlinePlayer.getUniqueId());
            } else {
                return offlinePlayer.getName().equals(getName());
            }
        } else if (obj instanceof EntityHuman) {
            EntityHuman entityHuman = (EntityHuman) obj;
            if (Bukkit.getOnlineMode()) {
                return getPlayerUUID().equals(entityHuman.getUniqueID());
            } else {
                return entityHuman.getName().equals(getName());
            }
        } else if (obj instanceof AnimalTamer) {
            AnimalTamer animalTamer = (AnimalTamer) obj;
            return animalTamer.getName().equals(getName());
        } else if (obj instanceof MyPetPlayer) {
            return this == obj;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MyPetPlayer{name=" + getName() + ", mojang-uuid=" + mojangUUID + ", offline-uuid=" + offlineUUID + "}";
    }
}