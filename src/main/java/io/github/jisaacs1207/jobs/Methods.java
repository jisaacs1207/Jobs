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
	
	public static void inspectSkill(Player sender, String playerName, String value){
		if(playerFileExists(playerName)){
			PlayerConfig pConfig = new PlayerConfig();
			pConfig = populateObjectFromPfile(playerName);
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
	
	private static PlayerConfig populateObjectFromPfile(String playerName){
		PlayerConfig pConfig = new PlayerConfig();
		if(playerFileExists(playerName)){
			File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+playerName);
			YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);
            pConfig.name = playerfileyaml.getString("identity.name");
            pConfig.nick = playerfileyaml.getString("identity.nick");
            pConfig.uuid = playerfileyaml.getInt("identity.uuid");
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
	
	public static void populateMapFromPFile(String playerName){
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = populateObjectFromPfile(playerName);
		Jobs.playerStats.put(playerName, pConfig);
	}
	
	public static void updateLastJoin(String playerName){
		PlayerConfig pConfig = new PlayerConfig();
		pConfig = populateObjectFromPfile(playerName);
		pConfig.infolastjoined=(int) System.currentTimeMillis();
		Jobs.playerStats.put(playerName, pConfig);
		saveMapToPFile(playerName);
	}
	
	public static void generateNewPlayerFile(String playerName){
		
		String player = playerName;
		File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+player);
		YamlConfiguration playerfileyaml = YamlConfiguration.loadConfiguration(playerfile);
		
		// vip
		playerfileyaml.set("vip.level", 0);
		playerfileyaml.set("vip.teacher", 0);
		
		// info
		playerfileyaml.set("info.firstjoined", System.currentTimeMillis());
		playerfileyaml.set("info.lastjoined", System.currentTimeMillis());
		playerfileyaml.set("info.playtime", 0);
		
		// skillpoints
		playerfileyaml.set("skillpoints.cap",1200);
		playerfileyaml.set("skillpoints.current",0);
		playerfileyaml.set("reading", 0);
		playerfileyaml.set("readingbegan", 0);
		
		// primary crafting
		playerfileyaml.set("skills.primary.crafting.crafting", 0);                  //1
		playerfileyaml.set("skills.primary.crafting.taming", 0);                    //2
		playerfileyaml.set("skills.primary.crafting.stabling", 0);                  //3
		playerfileyaml.set("skills.primary.crafting.creaturecontrol", 0);           //4
		playerfileyaml.set("skills.primary.crafting.skinning", 0);                  //5
		playerfileyaml.set("skills.primary.crafting.weaponcrafting", 0);            //6
		playerfileyaml.set("skills.primary.crafting.legendaryweaponcrafting", 0);   //7
		playerfileyaml.set("skills.primary.crafting.armorcrafting", 0);             //8
		playerfileyaml.set("skills.primary.crafting.legendaryweaponcrafting", 0);   //9
		playerfileyaml.set("skills.primary.crafting.alchemy", 0);                   //10
		playerfileyaml.set("skills.primary.crafting.transmutation", 0);             //11
		playerfileyaml.set("skills.primary.crafting.bowcrafting", 0);               //12
		playerfileyaml.set("skills.primary.crafting.legendarybowcrafting", 0);      //13
		playerfileyaml.set("skills.primary.crafting.fletching", 0);                 //14
		playerfileyaml.set("skills.primary.crafting.engineering", 0);               //15
		playerfileyaml.set("skills.primary.crafting.tinkering", 0);                 //16
		playerfileyaml.set("skills.primary.crafting.amateurwriting", 0);            //17
		playerfileyaml.set("skills.primary.crafting.originalwriting", 0);           //18
		playerfileyaml.set("skills.primary.crafting.authorship", 0);                //19
		
        // primary botany
		
		playerfileyaml.set("skills.primary.botany.botany", 0);                      //20
		playerfileyaml.set("skills.primary.botany.herbalism", 0);                   //21
		playerfileyaml.set("skills.primary.botany.dendrology", 0);                  //22
		
		// primary defense
		
		playerfileyaml.set("skills.primary.defense.defense", 0);                    //23
		playerfileyaml.set("skills.primary.defense.defensivestance", 0);            //24
		playerfileyaml.set("skills.primary.defense.shield", 0);                     //25
		playerfileyaml.set("skills.primary.defense.shieldwall", 0);                 //26
		playerfileyaml.set("skills.primary.defense.twohandeddefense", 0);           //27
		playerfileyaml.set("skills.primary.defense.twohandedcleave", 0);            //28
		playerfileyaml.set("skills.primary.defense.parry", 0);                      //29
		playerfileyaml.set("skills.primary.defense.dodge", 0);                      //30
		playerfileyaml.set("skills.primary.defense.riposte", 0);                    //31
		
		// primary weapons
		
		playerfileyaml.set("skills.primary.defense.weapons", 0);                    //32
		playerfileyaml.set("skills.primary.weapons.twohanded", 0);                  //33
		playerfileyaml.set("skills.primary.defense.brawling", 0);                   //34
		playerfileyaml.set("skills.primary.defense.shieldfighting", 0);             //35
		playerfileyaml.set("skills.primary.defense.archery", 0);                    //36
		playerfileyaml.set("skills.primary.defense.onehanded", 0);                  //37
		
		// primary movement
		
		playerfileyaml.set("skills.primary.movement.movement", 0);                  //38
		playerfileyaml.set("skills.primary.movement.athletics", 0);                 //39
		playerfileyaml.set("skills.primary.movement.riding", 0);                    //40
		playerfileyaml.set("skills.primary.movement.damageavoidance", 0);           //41
		playerfileyaml.set("skills.primary.movement.endurance", 0);                 //42
		playerfileyaml.set("skills.primary.movement.passiveregeneration", 0);       //43
		playerfileyaml.set("skills.primary.movement.balance", 0);                   //44
		
		// primary zoology
		
		playerfileyaml.set("skills.primary.zoology.zoology", 0);                    //45
		playerfileyaml.set("skills.primary.zoology.mimicry", 0);                    //46
		playerfileyaml.set("skills.primary.zoology.disguise", 0);                  
		playerfileyaml.set("skills.primary.zoology.animalcall", 0);                 
		playerfileyaml.set("skills.primary.zoology.terrestria", 0);                 
		playerfileyaml.set("skills.primary.zoology.etheria", 0);                 
		playerfileyaml.set("skills.primary.zoology.spiraria", 0);

		// secondary crafting
		
		playerfileyaml.set("skills.secondary.crafting.beginnerwriting", 0);
		playerfileyaml.set("skills.secondary.crafting.intermediarywriting", 0);
		playerfileyaml.set("skills.secondary.crafting.advancedwriting", 0);
		playerfileyaml.set("skills.secondary.crafting.mounting", 0);
		playerfileyaml.set("skills.secondary.crafting.creaturecommands", 0);
		playerfileyaml.set("skills.secondary.crafting.improvedweaponcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.advancedweaponcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.improvedarmorcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.advancedarmorcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.improvedalchemy", 0);
		playerfileyaml.set("skills.secondary.crafting.advancedalchemy", 0);
		playerfileyaml.set("skills.secondary.crafting.improvedbowcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.advancedbowcrafting", 0);
		playerfileyaml.set("skills.secondary.crafting.deconstruct", 0);
		
		// secondary botany
		
		playerfileyaml.set("skills.secondary.botany.mycology", 0);
		playerfileyaml.set("skills.secondary.botany.redmushroom", 0);
		playerfileyaml.set("skills.secondary.botany.brownmushroom", 0);
		playerfileyaml.set("skills.secondary.botany.mooshroom", 0);
		playerfileyaml.set("skills.secondary.botany.greenery", 0);
		playerfileyaml.set("skills.secondary.botany.flower", 0);
		playerfileyaml.set("skills.secondary.botany.vegetable", 0);
		playerfileyaml.set("skills.secondary.botany.fruit", 0);
		playerfileyaml.set("skills.secondary.botany.melon", 0);
		playerfileyaml.set("skills.secondary.botany.oak", 0);
		playerfileyaml.set("skills.secondary.botany.spruce", 0);
		playerfileyaml.set("skills.secondary.botany.birch", 0);
		playerfileyaml.set("skills.secondary.botany.jungle", 0);
		playerfileyaml.set("skills.secondary.botany.acacia", 0);
		
		// secondary defense
		
		playerfileyaml.set("skills.secondary.defense.shieldblock", 0);
		playerfileyaml.set("skills.secondary.defense.shieldbash", 0);
		playerfileyaml.set("skills.secondary.defense.twohandedblock", 0);
		playerfileyaml.set("skills.secondary.defense.offhandparry", 0);
		playerfileyaml.set("skills.secondary.defense.sidestep", 0);
		
		// secondary weapons
		
		playerfileyaml.set("skills.secondary.weapons.diamond", 0);
		playerfileyaml.set("skills.secondary.weapons.iron", 0);
		playerfileyaml.set("skills.secondary.weapons.gold", 0);
		playerfileyaml.set("skills.secondary.weapons.stone", 0);
		playerfileyaml.set("skills.secondary.weapons.wood", 0);
		playerfileyaml.set("skills.secondary.weapons.sword", 0);
		playerfileyaml.set("skills.secondary.weapons.axe", 0);
		playerfileyaml.set("skills.secondary.weapons.hoe", 0);
		playerfileyaml.set("skills.secondary.weapons.pickaxe", 0);
		playerfileyaml.set("skills.secondary.weapons.shovel", 0);
		playerfileyaml.set("skills.secondary.weapons.fists", 0);
		
		// secondary movement
		
		playerfileyaml.set("skills.secondary.movement.mountedspeed", 0);
		playerfileyaml.set("skills.secondary.movement.mountedfighting", 0);
		playerfileyaml.set("skills.secondary.movement.mountedarchery", 0);
		playerfileyaml.set("skills.secondary.movement.foodspeed", 0);
		playerfileyaml.set("skills.secondary.movement.breathingtechniques", 0);
		playerfileyaml.set("skills.secondary.movement.jumping", 0);
		playerfileyaml.set("skills.secondary.movement.swimming", 0);
		playerfileyaml.set("skills.secondary.movement.landingcontrol", 0);
		playerfileyaml.set("skills.secondary.movement.thickskin", 0);
		playerfileyaml.set("skills.secondary.movement.activeregeneration", 0);
		
		// secondary zoology
		
		playerfileyaml.set("skills.secondary.zoology.livestock", 0);
		playerfileyaml.set("skills.secondary.zoology.ocean", 0);
		playerfileyaml.set("skills.secondary.zoology.arachnid", 0);
		playerfileyaml.set("skills.secondary.zoology.canine", 0);
		playerfileyaml.set("skills.secondary.zoology.feline", 0);
		playerfileyaml.set("skills.secondary.zoology.humanoid", 0);
		playerfileyaml.set("skills.secondary.zoology.undead", 0);
		playerfileyaml.set("skills.secondary.zoology.dragon", 0);
		playerfileyaml.set("skills.secondary.zoology.construct", 0);
		playerfileyaml.set("skills.secondary.zoology.elemental", 0);
		
		try {
			  playerfileyaml.save(playerfile);
			} catch(IOException e) {
			  e.printStackTrace();
			}
	}
}
