package io.github.jisaacs1207.jobs;


import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class Methods implements Listener {
	
	public static boolean isInt(String s){
	    try {
	        Integer.parseInt(s);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;	
	}
	
	public static int getSkillPoints(String playerName) {
		int skillPoints = 0;
		if(playerFileExists(playerName)){
			File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+playerName);
            YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);
            for(String key : playerfileyaml.getConfigurationSection("skills.primary").getKeys(true)){
				int keyValue = playerfileyaml.getInt("skills.primary."+key);
				skillPoints = keyValue+skillPoints;
			}
		}
		return skillPoints;
	}
	public static boolean playerFileExists(String playerName){
		boolean playerExists = false;
		File folder = new File(Jobs.plugin.getDataFolder()+"/players/");
		File[] listOfFiles = folder.listFiles();
		for(File file : listOfFiles){
	    	String fileName= file.getName().toString();
	    	if(playerName.equalsIgnoreCase(fileName)){
	    		playerExists=true;
	    	}
		}
		return playerExists;
	}
	
	public static boolean skillExists(String testPlayer, String skill){
		boolean exists = false;
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = Jobs.playerStats.get(testPlayer);
		for(Field skillName:pConfig.getClass().getDeclaredFields()){
			if(skill.equalsIgnoreCase(skillName.getName().toString())){
				exists=true;
			}
		}
		
		return exists;
	}
	
	public static void displayPlayerSkills(Player sender, String playerName, String pageNumber){
		if(playerFileExists(playerName)){
			if(isInt(pageNumber)){
				PlayerConfig pConfig = new PlayerConfig();
				pConfig = Jobs.playerStats.get(playerName);
				int skillNumber=0;
				int pageCount=0;
				int pageNumberInt = Integer.valueOf(pageNumber);
				HashMap<String, Integer> skillMap = new HashMap<String, Integer>();
				for(Field field:pConfig.getClass().getDeclaredFields()){
					String skillName = field.getName();
					if((!skillName.equalsIgnoreCase("viplevel"))&&(!skillName.equalsIgnoreCase("vipteacher"))&&(!skillName.equalsIgnoreCase("skillpointscap"))
							&&(!skillName.equalsIgnoreCase("skillpointscurrent"))&&(!skillName.equalsIgnoreCase("reading"))
							&&(!skillName.equalsIgnoreCase("readingbegan"))&&(!skillName.equalsIgnoreCase("infofirstjoined"))
							&&(!skillName.equalsIgnoreCase("infolastjoined"))&&(!skillName.equalsIgnoreCase("infoplaytime"))) {
						skillNumber++;
						field.setAccessible(true);
						try {
							skillMap.put(field.getName(), field.getInt(pConfig));
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				double tempSkill = skillNumber;
				double tempCount = tempSkill/10;
				pageCount = (int) Math.ceil(tempCount);
				if((pageNumberInt<=pageCount)&&(pageNumberInt>0)){
					skillNumber=0;
					int skillRangeMin=(pageNumberInt*10)-9;
					int skillRangeMax=(pageNumberInt*10);
					TreeMap<String, Integer> sortedSkillMap = new TreeMap<String, Integer>(skillMap);
					sender.sendMessage("************************************************");
					sender.sendMessage(playerName.toUpperCase() + " SKILLS LISTING (Page " + pageNumber + "/" + String.valueOf(pageCount) + ")");
					sender.sendMessage("************************************************");
                    for (Map.Entry<String, Integer> entry : sortedSkillMap.entrySet()) {
                        skillNumber++;
						if ((skillNumber>=skillRangeMin) && (skillNumber <= skillRangeMax)){
							sender.sendMessage(entry.getKey() + ": " + entry.getValue());
						}			
					}
					sender.sendMessage("View more with '/sb admin inspect player skills [pagenumber]");
				} else sender.sendMessage("Not a valid page number.");
			} else sender.sendMessage("Page number must be a valid integer.");
		} else sender.sendMessage("Player not found!");
	}
	
	public static void setSkillLevel(Player sender, String playerName, String skill, String level, Boolean notify){
		
		// Pretty sloppy coding here, but functions. I'll get back to prettifying it later.
		
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = Jobs.playerStats.get(playerName);
		boolean worked = false;
		boolean levelIsInt = true;
		boolean skillFound = false;
		boolean playerFound=false;
		int skillLevel = 50;

		if(playerFileExists(playerName)) playerFound = true;
		// Ensures a catch if level is not a number or if it doesn't fall in to 0-100
		if(isInt(level)) skillLevel = Integer.valueOf(level);
		if((skillLevel<0)||(skillLevel>100)) {
			level = "potato";
		}
		if(!isInt(level)){
			levelIsInt=false;
		}
		// If a player is found, tries to find the skill, if it does, it writes the value to the hashmap.
		else{
			if(playerFound==true){
				for(Field key: pConfig.getClass().getDeclaredFields()){
					String name = key.getName();
					if(name.equalsIgnoreCase(skill)){
						key.setAccessible(true);
						try {
							key.set(pConfig, Integer.valueOf(level));
							Jobs.playerStats.put(playerName, pConfig);
							worked=true;
							skillFound=true;
							
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Jobs.plugin.getServer().broadcastMessage("SB IllegalArgumentException!");
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Jobs.plugin.getServer().broadcastMessage("SB IllegalAccessException!");
						}
					}
				}
				
				// The sketchy catches begin.
				if(skillFound==false){
					if(notify==true){
						sender.sendMessage(skill + " isn't a recognized skill.");
					}
				}
			}
			else{
				if(notify==true){
					sender.sendMessage("Player not found!");
				}
			}
			
		}
		if(worked==true){
			if(notify==true){
				sender.sendMessage("Successfully set " + playerName + "'s " + skill + " level to " + level + "!");
			}		
			saveMapToPFile(playerName);
		}
		if((levelIsInt==false)&&(playerFound==true)){
			if(notify==true){
				sender.sendMessage("Skill level must be a valid integer between 0 and 100.");
			}
		}
	}
	
	public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }
	
	public static void inspectPlayer(Player sender, String playerName){
		if(playerFileExists(playerName)){
			PlayerConfig pConfig = new PlayerConfig();
			pConfig = Jobs.playerStats.get(playerName);
            long firstLogin = pConfig.infofirstjoined;
            int time = (int) System.currentTimeMillis();
            long charAge = time - firstLogin;
            int playTime = (int) TimeUnit.SECONDS.toMillis(pConfig.infoplaytime);
			String isTeacher = "No";
			if(pConfig.vipteacher>0) isTeacher = "Yes";
			String charAgeString = getDurationBreakdown(charAge);
			String playTimeString = getDurationBreakdown(playTime);
			sender.sendMessage("***********************************");
			sender.sendMessage(playerName + "'s Statistics");
			sender.sendMessage("***********************************");
			sender.sendMessage("Character Age: " + charAgeString);
			sender.sendMessage("Character Playtime: " + playTimeString);
			sender.sendMessage("SkillPoints Used: " + getSkillPoints(playerName) + "/1200");
			sender.sendMessage("VIP Level: " + pConfig.viplevel);
			sender.sendMessage("Teacher: " + isTeacher);
		} else sender.sendMessage("Player not found!");
	}

    public static void inspectSkill(Player sender, Player player, String value) {
        String playerName = player.getName();
        if(playerFileExists(playerName)){
			PlayerConfig pConfig = new PlayerConfig();
            pConfig = populateObjectFromPfile(player);
            if(skillExists(playerName, value)){
                for (Field skillName : pConfig.getClass().getDeclaredFields()) {
                    String skillNameString = skillName.getName().toString();
					if(skillNameString.equalsIgnoreCase(value)){
						skillName.setAccessible(true);
						int skillValue=0;
						try {
							skillValue = skillName.getInt(pConfig);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sender.sendMessage("The level of " + playerName + "'s " + value + " skill is " + skillValue + ".");
					}
				}
			} else sender.sendMessage("Skill not found!");
		} else sender.sendMessage("Player not found!");
	}
	
	public static void setSkillAll(Player sender, String playerName, String level){
		if(playerFileExists(playerName)){
			if(isInt(level)){
				if((Integer.valueOf(level)<=100)||(Integer.valueOf(level)>=0)){
					PlayerConfig pConfig = new PlayerConfig();
					pConfig = Jobs.playerStats.get(playerName);
					for(Field key: pConfig.getClass().getDeclaredFields()){
						String skillName = key.getName();
						if((!skillName.equalsIgnoreCase("viplevel"))&&(!skillName.equalsIgnoreCase("vipteacher"))&&(!skillName.equalsIgnoreCase("skillpointscap"))
								&&(!skillName.equalsIgnoreCase("skillpointscurrent"))&&(!skillName.equalsIgnoreCase("reading"))
								&&(!skillName.equalsIgnoreCase("readingbegan"))&&(!skillName.equalsIgnoreCase("infofirstjoined"))
								&&(!skillName.equalsIgnoreCase("infolastjoined"))&&(!skillName.equalsIgnoreCase("infoplaytime"))) setSkillLevel(sender, playerName, skillName, level, false);
					}
					sender.sendMessage("All of " + playerName + "'s skills successfully set to " + level + ".");
				}
				else sender.sendMessage("Skill level must be a valid integer between 0 and 100.");
			}
			else sender.sendMessage("Skill level must be a valid integer!");
		}
		else sender.sendMessage("Player not found!");
	}
	
	public static void addToPlaytime(String playerName){
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = Jobs.playerStats.get(playerName);
		pConfig.infoplaytime=pConfig.infoplaytime+1;
		Jobs.playerStats.put(playerName, pConfig);
	}
	
	// Saves the hashmap to the player file.
	public static void saveMapToPFile(String playerName){
		File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+playerName);
		YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = Jobs.playerStats.get(playerName);
        playerfileyaml.set("identity.name", pConfig.name);
        playerfileyaml.set("identity.nick", pConfig.nick);
        playerfileyaml.set("identity.uuid", pConfig.uuid);
        playerfileyaml.set("extra.staff", pConfig.staff);
        playerfileyaml.set("extra.viplevel", pConfig.viplevel);
        playerfileyaml.set("extra.vipteacher", pConfig.vipteacher);
        playerfileyaml.set("stats.infolastjoined", pConfig.infolastjoined);
        playerfileyaml.set("stats.infofirstjoined", pConfig.infofirstjoined);
        playerfileyaml.set("stats.infoplaytime", pConfig.infoplaytime);
        playerfileyaml.set("stats.joins", pConfig.joins);
        playerfileyaml.set("stats.kicked", pConfig.kicked);
        playerfileyaml.set("stats.warned", pConfig.warned);
        playerfileyaml.set("stats.banned", pConfig.banned);
        playerfileyaml.set("stats.votes", pConfig.votes);
        playerfileyaml.set("stats.complimented", pConfig.complimented);
        playerfileyaml.set("stats.spoken", pConfig.spoken);
        playerfileyaml.set("stats.pkils", pConfig.pkills);
        playerfileyaml.set("stats.pkillstreak", pConfig.pkillstreak);
        playerfileyaml.set("stats.tpkillstreak", pConfig.tpkillstreak);
        playerfileyaml.set("stats.mkills", pConfig.mkills);
        playerfileyaml.set("stats.damagedone", pConfig.damagedone);
        playerfileyaml.set("stats.damagetaken", pConfig.damagetaken);
        playerfileyaml.set("stats.pdeaths", pConfig.pdeaths);
        playerfileyaml.set("stats.mdeaths", pConfig.mdeaths);
        playerfileyaml.set("stats.mcaught", pConfig.mcaught);
        playerfileyaml.set("stats.ethrown", pConfig.ethrown);
        playerfileyaml.set("stats.fcaught", pConfig.fcaught);
        playerfileyaml.set("stats.crafted", pConfig.crafted);
        playerfileyaml.set("stats.tbroken", pConfig.tbroken);
        playerfileyaml.set("stats.experience", pConfig.experience);
        playerfileyaml.set("stats.trades", pConfig.trades);
        playerfileyaml.set("stats.walked", pConfig.walked);
        playerfileyaml.set("stats.broken", pConfig.broken);
        playerfileyaml.set("stats.placed", pConfig.placed);
        playerfileyaml.set("stats.stolen", pConfig.stolen);
        playerfileyaml.set("protection.isProtected", pConfig.isProtected);
        playerfileyaml.set("protection.hasLifeInsurance", pConfig.hasLifeInsurance);
        playerfileyaml.set("classes.alchemist", pConfig.alchemist);
        playerfileyaml.set("classes.artisan", pConfig.artisan);
        playerfileyaml.set("classes.beastmaster", pConfig.beastmaster);
        playerfileyaml.set("classes.cleric", pConfig.cleric);
        playerfileyaml.set("classes.laborer", pConfig.laborer);
        playerfileyaml.set("classes.mage", pConfig.mage);
        playerfileyaml.set("classes.merchant", pConfig.merchant);
        playerfileyaml.set("classes.psionic", pConfig.psionic);
        playerfileyaml.set("classes.ranger", pConfig.ranger);
        playerfileyaml.set("classes.rogue", pConfig.rogue);
        playerfileyaml.set("classes.scribe", pConfig.scribe);
        playerfileyaml.set("classes.thief", pConfig.thief);
        playerfileyaml.set("classes.tinkerer", pConfig.tinkerer);
        playerfileyaml.set("classes.warrior", pConfig.warrior);
        try{
			playerfileyaml.save(playerfile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		
	}

    private static PlayerConfig populateObjectFromPfile(Player player) {
        String playerName = player.getName();
        PlayerConfig pConfig = new PlayerConfig();
		if(playerFileExists(playerName)){
			File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+playerName);
			YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);
            pConfig.name = playerfileyaml.getString("identity.name");
            pConfig.nick = playerfileyaml.getString("identity.nick");
            pConfig.uuid = playerfileyaml.getString("identity.uuid");
            pConfig.staff = playerfileyaml.getInt("extra.staff");
            pConfig.viplevel = playerfileyaml.getInt("extra.viplevel");
            pConfig.vipteacher = playerfileyaml.getInt("extra.vipteacher");
            pConfig.infolastjoined = playerfileyaml.getLong("stats.infolastjoined");
            pConfig.infofirstjoined = playerfileyaml.getLong("stats.infofirstjoined");
            pConfig.infoplaytime = playerfileyaml.getLong("stats.infoplaytime");
            pConfig.joins = playerfileyaml.getInt("stats.joins");
            pConfig.kicked = playerfileyaml.getInt("stats.kicked");
            pConfig.warned = playerfileyaml.getInt("stats.warned");
            pConfig.banned = playerfileyaml.getInt("stats.banned");
            pConfig.votes = playerfileyaml.getInt("stats.votes");
            pConfig.complimented = playerfileyaml.getInt("stats.complimented");
            pConfig.spoken = playerfileyaml.getLong("stats.spoken");
            pConfig.pkills = playerfileyaml.getInt("stats.pkils");
            pConfig.pkillstreak = playerfileyaml.getInt("stats.pkillstreak");
            pConfig.tpkillstreak = playerfileyaml.getInt("stats.tpkillstreak");
            pConfig.mkills = playerfileyaml.getInt("stats.mkills");
            pConfig.damagedone = playerfileyaml.getLong("stats.damagedone");
            pConfig.damagetaken = playerfileyaml.getLong("stats.damagetaken");
            pConfig.pdeaths = playerfileyaml.getInt("stats.pdeaths");
            pConfig.mdeaths = playerfileyaml.getInt("stats.mdeaths");
            pConfig.mcaught = playerfileyaml.getInt("stats.mcaught");
            pConfig.ethrown = playerfileyaml.getInt("stats.ethrown");
            pConfig.fcaught = playerfileyaml.getInt("stats.fcaught");
            pConfig.crafted = playerfileyaml.getInt("stats.crafted");
            pConfig.tbroken = playerfileyaml.getInt("stats.tbroken");
            pConfig.experience = playerfileyaml.getLong("stats.experience");
            pConfig.trades = playerfileyaml.getInt("stats.trades");
            pConfig.walked = playerfileyaml.getLong("stats.walked");
            pConfig.broken = playerfileyaml.getLong("stats.broken");
            pConfig.placed = playerfileyaml.getLong("stats.placed");
            pConfig.stolen = playerfileyaml.getInt("stats.stolen");
            pConfig.isProtected = playerfileyaml.getBoolean("protection.isProtected");
            pConfig.hasLifeInsurance = playerfileyaml.getBoolean("protection.hasLifeInsurance");
            pConfig.alchemist = playerfileyaml.getInt("classes.alchemist");
            pConfig.artisan = playerfileyaml.getInt("classes.artisan");
            pConfig.beastmaster = playerfileyaml.getInt("classes.beastmaster");
            pConfig.cleric = playerfileyaml.getInt("classes.cleric");
            pConfig.laborer = playerfileyaml.getInt("classes.laborer");
            pConfig.mage = playerfileyaml.getInt("classes.mage");
            pConfig.merchant = playerfileyaml.getInt("classes.merchant");
            pConfig.psionic = playerfileyaml.getInt("classes.psionic");
            pConfig.ranger = playerfileyaml.getInt("classes.ranger");
            pConfig.rogue = playerfileyaml.getInt("classes.rogue");
            pConfig.scribe = playerfileyaml.getInt("classes.scribe");
            pConfig.thief = playerfileyaml.getInt("classes.thief");
            pConfig.tinkerer = playerfileyaml.getInt("classes.tinkerer");
            pConfig.warrior = playerfileyaml.getInt("classes.warrior");
        }
		return pConfig;
	}

    public static void populateMapFromPFile(Player player) {
        String playerName = player.getName();
        PlayerConfig pConfig = new PlayerConfig();
        pConfig = populateObjectFromPfile(player);
        Jobs.playerStats.put(playerName, pConfig);
	}

    public static void updateLastJoin(Player player) {
        String playerName = player.getName();
        PlayerConfig pConfig = new PlayerConfig();
        pConfig = populateObjectFromPfile(player);
        pConfig.infolastjoined = System.currentTimeMillis();
        Jobs.playerStats.put(playerName, pConfig);
		saveMapToPFile(playerName);
	}

    public static void generateNewPlayerFile(Player player) {

        String playerName = player.getName();
        String playerNick = player.getDisplayName();
        String uuid = player.getUniqueId().toString();

        File playerfile = new File(Jobs.plugin.getDataFolder() + "/players/" + playerName);
        YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);

        // identity
        playerfileyaml.set("identity.name", playerName);
        playerfileyaml.set("identity.nick", playerNick);
        playerfileyaml.set("identity.uuid", uuid);

        //extra
        playerfileyaml.set("extra.staff", 0);
        playerfileyaml.set("extra.viplevel", 0);
        playerfileyaml.set("extra.vipteacher", 0);

        //stats
        playerfileyaml.set("stats.infolastjoined", System.currentTimeMillis());
        playerfileyaml.set("stats.infofirstjoined", System.currentTimeMillis());
        playerfileyaml.set("stats.infoplaytime", 0);
        playerfileyaml.set("stats.joins", 1);
        playerfileyaml.set("stats.kicked", 0);
        playerfileyaml.set("stats.warned", 0);
        playerfileyaml.set("stats.banned", 0);
        playerfileyaml.set("stats.votes", 0);
        playerfileyaml.set("stats.complimented", 0);
        playerfileyaml.set("stats.spoken", 0);
        playerfileyaml.set("stats.pkils", 0);
        playerfileyaml.set("stats.pkillstreak", 0);
        playerfileyaml.set("stats.tpkillstreak", 0);
        playerfileyaml.set("stats.mkills", 0);
        playerfileyaml.set("stats.damagedone", 0);
        playerfileyaml.set("stats.damagetaken", 0);
        playerfileyaml.set("stats.pdeaths", 0);
        playerfileyaml.set("stats.mdeaths", 0);
        playerfileyaml.set("stats.mcaught", 0);
        playerfileyaml.set("stats.ethrown", 0);
        playerfileyaml.set("stats.fcaught", 0);
        playerfileyaml.set("stats.crafted", 0);
        playerfileyaml.set("stats.tbroken", 0);
        playerfileyaml.set("stats.experience", 0);
        playerfileyaml.set("stats.trades", 0);
        playerfileyaml.set("stats.walked", 0);
        playerfileyaml.set("stats.broken", 0);
        playerfileyaml.set("stats.placed", 0);
        playerfileyaml.set("stats.stolen", 0);

        //protection
        playerfileyaml.set("protection.isProtected", false);
        playerfileyaml.set("protection.hasLifeInsurance", false);

        //classes
        playerfileyaml.set("classes.alchemist", 0);
        playerfileyaml.set("classes.artisan", 0);
        playerfileyaml.set("classes.beastmaster", 0);
        playerfileyaml.set("classes.cleric", 0);
        playerfileyaml.set("classes.laborer", 0);
        playerfileyaml.set("classes.mage", 0);
        playerfileyaml.set("classes.merchant", 0);
        playerfileyaml.set("classes.psionic", 0);
        playerfileyaml.set("classes.ranger", 0);
        playerfileyaml.set("classes.rogue", 0);
        playerfileyaml.set("classes.scribe", 0);
        playerfileyaml.set("classes.thief", 0);
        playerfileyaml.set("classes.tinkerer", 0);
        playerfileyaml.set("classes.warrior", 0);

        try {
			  playerfileyaml.save(playerfile);
			} catch(IOException e) {
			  e.printStackTrace();
			}
	}
}
