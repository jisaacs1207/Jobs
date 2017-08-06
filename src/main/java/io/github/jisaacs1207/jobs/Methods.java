package io.github.jisaacs1207.jobs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
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
					for(Entry<String, Integer> entry:sortedSkillMap.entrySet()){
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
			int firstLogin = pConfig.infofirstjoined;
			int time = (int) System.currentTimeMillis();
			int charAge = time-firstLogin;
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
				for(Field skillName:pConfig.getClass().getDeclaredFields()){	
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
		playerfileyaml.set("vip.level", pConfig.viplevel);
		playerfileyaml.set("vip.teacher", pConfig.vipteacher);
		playerfileyaml.set("info.firstjoined", pConfig.infofirstjoined);
		playerfileyaml.set("info.lastjoined", pConfig.infolastjoined);
		playerfileyaml.set("info.playtime", pConfig.infoplaytime);
		playerfileyaml.set("skillpoints.cap", pConfig.skillpointscap);
		playerfileyaml.set("skillpoints.current", pConfig.skillpointscurrent);
		playerfileyaml.set("reading", pConfig.reading);
		playerfileyaml.set("readingbegan", pConfig.readingbegan);
		playerfileyaml.set("skills.primary.crafting.crafting", pConfig.crafting);
		playerfileyaml.set("skills.primary.crafting.taming", pConfig.taming);
		playerfileyaml.set("skills.primary.crafting.stabling", pConfig.stabling);
		playerfileyaml.set("skills.primary.crafting.creaturecontrol", pConfig.creaturecontrol);
		playerfileyaml.set("skills.primary.crafting.skinning", pConfig.skinning);
		playerfileyaml.set("skills.primary.crafting.weaponcrafting", pConfig.weaponcrafting);
		playerfileyaml.set("skills.primary.crafting.legendaryweaponcrafting", pConfig.legendaryweaponcrafting);
		playerfileyaml.set("skills.primary.crafting.armorcrafting", pConfig.armorcrafting);
		playerfileyaml.set("skills.primary.crafting.legendaryarmorcrafting", pConfig.legendaryarmorcrafting);
		playerfileyaml.set("skills.primary.crafting.alchemy", pConfig.alchemy);
		playerfileyaml.set("skills.primary.crafting.transmutation", pConfig.transmutation);
		playerfileyaml.set("skills.primary.crafting.bowcrafting", pConfig.bowcrafting);
		playerfileyaml.set("skills.primary.crafting.legendarybowcrafting", pConfig.legendarybowcrafting);
		playerfileyaml.set("skills.primary.crafting.fletching", pConfig.fletching);
		playerfileyaml.set("skills.primary.crafting.engineering", pConfig.engineering);
		playerfileyaml.set("skills.primary.crafting.tinkering", pConfig.tinkering);
		playerfileyaml.set("skills.primary.crafting.amateurwriting", pConfig.amateurwriting);
		playerfileyaml.set("skills.primary.crafting.originalwriting", pConfig.originalwriting);
		playerfileyaml.set("skills.primary.crafting.authorship", pConfig.authorship);
		playerfileyaml.set("skills.primary.botany.botany", pConfig.botany);
		playerfileyaml.set("skills.primary.botany.herbalism", pConfig.herbalism);
		playerfileyaml.set("skills.primary.botany.dendrology", pConfig.dendrology);
		playerfileyaml.set("skills.primary.defense.defense", pConfig.defense);
		playerfileyaml.set("skills.primary.defense.defensivestance", pConfig.defensivestance);
		playerfileyaml.set("skills.primary.defense.shield", pConfig.shield);
		playerfileyaml.set("skills.primary.defense.shieldwall", pConfig.shieldwall);
		playerfileyaml.set("skills.primary.defense.twohandeddefense", pConfig.twohandeddefense);
		playerfileyaml.set("skills.primary.defense.twohandedcleave", pConfig.twohandedcleave);
		playerfileyaml.set("skills.primary.defense.parry", pConfig.parry);
		playerfileyaml.set("skills.primary.defense.dodge", pConfig.dodge);
		playerfileyaml.set("skills.primary.defense.riposte", pConfig.riposte);
		playerfileyaml.set("skills.primary.defense.weapons", pConfig.weapons);
		playerfileyaml.set("skills.primary.weapons.twohanded", pConfig.twohanded);
		playerfileyaml.set("skills.primary.defense.brawling", pConfig.brawling);
		playerfileyaml.set("skills.primary.defense.shieldfighting", pConfig.shieldfighting);
		playerfileyaml.set("skills.primary.defense.archery", pConfig.archery);
		playerfileyaml.set("skills.primary.defense.onehanded", pConfig.onehanded);
		playerfileyaml.set("skills.primary.movement.movement", pConfig.movement);
		playerfileyaml.set("skills.primary.movement.athletics", pConfig.athletics);
		playerfileyaml.set("skills.primary.movement.riding", pConfig.riding);
		playerfileyaml.set("skills.primary.movement.damageavoidance", pConfig.damageavoidance);
		playerfileyaml.set("skills.primary.movement.endurance", pConfig.endurance);
		playerfileyaml.set("skills.primary.movement.passiveregeneration", pConfig.passiveregeneration);
		playerfileyaml.set("skills.primary.movement.balance", pConfig.balance);
		playerfileyaml.set("skills.primary.zoology.zoology", pConfig.zoology);
		playerfileyaml.set("skills.primary.zoology.mimicry", pConfig.mimicry);
		playerfileyaml.set("skills.primary.zoology.disguise", pConfig.disguise);
		playerfileyaml.set("skills.primary.zoology.animalcall", pConfig.animalcall);
		playerfileyaml.set("skills.primary.zoology.terrestria", pConfig.terrestria);
		playerfileyaml.set("skills.primary.zoology.etheria", pConfig.etheria);
		playerfileyaml.set("skills.primary.zoology.spiraria", pConfig.spiraria);
		playerfileyaml.set("skills.secondary.crafting.beginnerwriting", pConfig.beginnerwriting);
		playerfileyaml.set("skills.secondary.crafting.intermediarywriting", pConfig.intermediarywriting);
		playerfileyaml.set("skills.secondary.crafting.advancedwriting", pConfig.advancedwriting);
		playerfileyaml.set("skills.secondary.crafting.mounting", pConfig.mounting);
		playerfileyaml.set("skills.secondary.crafting.creaturecommands", pConfig.creaturecommands);
		playerfileyaml.set("skills.secondary.crafting.improvedweaponcrafting", pConfig.improvedweaponcrafting);
		playerfileyaml.set("skills.secondary.crafting.advancedweaponcrafting", pConfig.advancedweaponcrafting);
		playerfileyaml.set("skills.secondary.crafting.improvedarmorcrafting", pConfig.improvedarmorcrafting);
		playerfileyaml.set("skills.secondary.crafting.advancedarmorcrafting", pConfig.advancedarmorcrafting);
		playerfileyaml.set("skills.secondary.crafting.improvedalchemy", pConfig.improvedalchemy);
		playerfileyaml.set("skills.secondary.crafting.advancedalchemy", pConfig.advancedalchemy);
		playerfileyaml.set("skills.secondary.crafting.improvedbowcrafting", pConfig.improvedbowcrafting);
		playerfileyaml.set("skills.secondary.crafting.advancedbowcrafting", pConfig.advancedbowcrafting);
		playerfileyaml.set("skills.secondary.crafting.deconstruct", pConfig.deconstruct);
		playerfileyaml.set("skills.secondary.botany.mycology", pConfig.mycology);
		playerfileyaml.set("skills.secondary.botany.redmushroom", pConfig.redmushroom);
		playerfileyaml.set("skills.secondary.botany.brownmushroom", pConfig.brownmushroom);
		playerfileyaml.set("skills.secondary.botany.mooshroom", pConfig.mooshroom);
		playerfileyaml.set("skills.secondary.botany.greenery", pConfig.greenery);
		playerfileyaml.set("skills.secondary.botany.flower", pConfig.flower);
		playerfileyaml.set("skills.secondary.botany.vegetable", pConfig.vegetable);
		playerfileyaml.set("skills.secondary.botany.fruit", pConfig.fruit);
		playerfileyaml.set("skills.secondary.botany.melon", pConfig.melon);
		playerfileyaml.set("skills.secondary.botany.oak", pConfig.oak);
		playerfileyaml.set("skills.secondary.botany.spruce", pConfig.spruce);
		playerfileyaml.set("skills.secondary.botany.birch", pConfig.birch);
		playerfileyaml.set("skills.secondary.botany.jungle", pConfig.jungle);
		playerfileyaml.set("skills.secondary.botany.acacia", pConfig.acacia);
		playerfileyaml.set("skills.secondary.defense.shieldblock", pConfig.shieldblock);
		playerfileyaml.set("skills.secondary.defense.shieldbash", pConfig.shieldbash);
		playerfileyaml.set("skills.secondary.defense.twohandedblock", pConfig.twohandedblock);
		playerfileyaml.set("skills.secondary.defense.offhandparry", pConfig.offhandparry);
		playerfileyaml.set("skills.secondary.defense.sidestep", pConfig.sidestep);
		playerfileyaml.set("skills.secondary.weapons.diamond", pConfig.diamond);
		playerfileyaml.set("skills.secondary.weapons.iron", pConfig.iron);
		playerfileyaml.set("skills.secondary.weapons.gold", pConfig.gold);
		playerfileyaml.set("skills.secondary.weapons.stone", pConfig.stone);
		playerfileyaml.set("skills.secondary.weapons.wood", pConfig.wood);
		playerfileyaml.set("skills.secondary.weapons.sword", pConfig.sword);
		playerfileyaml.set("skills.secondary.weapons.axe", pConfig.axe);
		playerfileyaml.set("skills.secondary.weapons.hoe", pConfig.hoe);
		playerfileyaml.set("skills.secondary.weapons.pickaxe", pConfig.pickaxe);
		playerfileyaml.set("skills.secondary.weapons.shovel", pConfig.shovel);
		playerfileyaml.set("skills.secondary.weapons.fists", pConfig.fists);
		playerfileyaml.set("skills.secondary.movement.mountedspeed", pConfig.mountedspeed);
		playerfileyaml.set("skills.secondary.movement.mountedfighting", pConfig.mountedfighting);
		playerfileyaml.set("skills.secondary.movement.mountedarchery", pConfig.mountedarchery);
		playerfileyaml.set("skills.secondary.movement.foodspeed", pConfig.foodspeed);
		playerfileyaml.set("skills.secondary.movement.breathingtechniques", pConfig.breathingtechniques);
		playerfileyaml.set("skills.secondary.movement.jumping", pConfig.jumping);
		playerfileyaml.set("skills.secondary.movement.swimming", pConfig.swimming);
		playerfileyaml.set("skills.secondary.movement.landingcontrol", pConfig.landingcontrol);
		playerfileyaml.set("skills.secondary.movement.thickskin", pConfig.thickskin);
		playerfileyaml.set("skills.secondary.movement.activeregeneration", pConfig.activeregeneration);
		playerfileyaml.set("skills.secondary.zoology.livestock", pConfig.livestock);
		playerfileyaml.set("skills.secondary.zoology.ocean", pConfig.ocean);
		playerfileyaml.set("skills.secondary.zoology.arachnid", pConfig.arachnid);
		playerfileyaml.set("skills.secondary.zoology.canine", pConfig.canine);
		playerfileyaml.set("skills.secondary.zoology.feline", pConfig.feline);
		playerfileyaml.set("skills.secondary.zoology.humanoid", pConfig.humanoid);
		playerfileyaml.set("skills.secondary.zoology.undead", pConfig.undead);
		playerfileyaml.set("skills.secondary.zoology.dragon", pConfig.dragon);
		playerfileyaml.set("skills.secondary.zoology.construct", pConfig.construct);
		playerfileyaml.set("skills.secondary.zoology.elemental", pConfig.elemental);
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
			pConfig.viplevel=playerfileyaml.getInt("vip.level");
			pConfig.vipteacher=playerfileyaml.getInt("vip.teacher");
			pConfig.infofirstjoined=playerfileyaml.getInt("info.firstjoined");
			pConfig.infolastjoined=playerfileyaml.getInt("info.lastjoined");
			pConfig.infoplaytime=playerfileyaml.getInt("info.playtime");
			pConfig.skillpointscap=playerfileyaml.getInt("skillpoints.cap");
			pConfig.skillpointscurrent=playerfileyaml.getInt("skillpoints.current");
			pConfig.reading=playerfileyaml.getInt("reading");
			pConfig.readingbegan=playerfileyaml.getInt("readingbegan");
			pConfig.crafting=playerfileyaml.getInt("skills.primary.crafting.crafting");
			pConfig.taming=playerfileyaml.getInt("skills.primary.crafting.taming");
			pConfig.stabling=playerfileyaml.getInt("skills.primary.crafting.stabling");
			pConfig.creaturecontrol=playerfileyaml.getInt("skills.primary.crafting.creaturecontrol");
			pConfig.skinning=playerfileyaml.getInt("skills.primary.crafting.skinning");
			pConfig.weaponcrafting=playerfileyaml.getInt("skills.primary.crafting.weaponcrafting");
			pConfig.legendaryweaponcrafting=playerfileyaml.getInt("skills.primary.crafting.legendaryweaponcrafting");
			pConfig.armorcrafting=playerfileyaml.getInt("skills.primary.crafting.armorcrafting");
			pConfig.legendaryarmorcrafting=playerfileyaml.getInt("skills.primary.crafting.legendaryarmorcrafting");
			pConfig.alchemy=playerfileyaml.getInt("skills.primary.crafting.alchemy");
			pConfig.transmutation=playerfileyaml.getInt("skills.primary.crafting.transmutation");
			pConfig.bowcrafting=playerfileyaml.getInt("skills.primary.crafting.bowcrafting");
			pConfig.legendarybowcrafting=playerfileyaml.getInt("skills.primary.crafting.legendarybowcrafting");
			pConfig.fletching=playerfileyaml.getInt("skills.primary.crafting.fletching");
			pConfig.engineering=playerfileyaml.getInt("skills.primary.crafting.engineering");
			pConfig.tinkering=playerfileyaml.getInt("skills.primary.crafting.tinkering");
			pConfig.amateurwriting=playerfileyaml.getInt("skills.primary.crafting.amateurwriting");
			pConfig.originalwriting=playerfileyaml.getInt("skills.primary.crafting.originalwriting");
			pConfig.authorship=playerfileyaml.getInt("skills.primary.crafting.authorship");
			pConfig.botany=playerfileyaml.getInt("skills.primary.botany.botany");
			pConfig.herbalism=playerfileyaml.getInt("skills.primary.botany.herbalism");
			pConfig.dendrology=playerfileyaml.getInt("skills.primary.botany.dendrology");
			pConfig.defense=playerfileyaml.getInt("skills.primary.defense.defense");
			pConfig.defensivestance=playerfileyaml.getInt("skills.primary.defense.defensivestance");
			pConfig.shield=playerfileyaml.getInt("skills.primary.defense.shield");
			pConfig.shieldwall=playerfileyaml.getInt("skills.primary.defense.shieldwall");
			pConfig.twohandeddefense=playerfileyaml.getInt("skills.primary.defense.twohandeddefense");
			pConfig.twohandedcleave=playerfileyaml.getInt("skills.primary.defense.twohandedcleave");
			pConfig.parry=playerfileyaml.getInt("skills.primary.defense.parry");
			pConfig.dodge=playerfileyaml.getInt("skills.primary.defense.dodge");
			pConfig.riposte=playerfileyaml.getInt("skills.primary.defense.riposte");
			pConfig.weapons=playerfileyaml.getInt("skills.primary.defense.weapons");
			pConfig.twohanded=playerfileyaml.getInt("skills.primary.weapons.twohanded");
			pConfig.brawling=playerfileyaml.getInt("skills.primary.defense.brawling");
			pConfig.shieldfighting=playerfileyaml.getInt("skills.primary.defense.shieldfighting");
			pConfig.archery=playerfileyaml.getInt("skills.primary.defense.archery");
			pConfig.onehanded=playerfileyaml.getInt("skills.primary.defense.onehanded");
			pConfig.movement=playerfileyaml.getInt("skills.primary.movement.movement");
			pConfig.athletics=playerfileyaml.getInt("skills.primary.movement.athletics");
			pConfig.riding=playerfileyaml.getInt("skills.primary.movement.riding");
			pConfig.damageavoidance=playerfileyaml.getInt("skills.primary.movement.damageavoidance");
			pConfig.endurance=playerfileyaml.getInt("skills.primary.movement.endurance");
			pConfig.passiveregeneration=playerfileyaml.getInt("skills.primary.movement.passiveregeneration");
			pConfig.balance=playerfileyaml.getInt("skills.primary.movement.balance");
			pConfig.zoology=playerfileyaml.getInt("skills.primary.zoology.zoology");
			pConfig.mimicry=playerfileyaml.getInt("skills.primary.zoology.mimicry");
			pConfig.disguise=playerfileyaml.getInt("skills.primary.zoology.disguise");
			pConfig.animalcall=playerfileyaml.getInt("skills.primary.zoology.animalcall");
			pConfig.terrestria=playerfileyaml.getInt("skills.primary.zoology.terrestria");
			pConfig.etheria=playerfileyaml.getInt("skills.primary.zoology.etheria");
			pConfig.spiraria=playerfileyaml.getInt("skills.primary.zoology.spiraria");
			pConfig.beginnerwriting=playerfileyaml.getInt("skills.secondary.crafting.beginnerwriting");
			pConfig.intermediarywriting=playerfileyaml.getInt("skills.secondary.crafting.intermediarywriting");
			pConfig.advancedwriting=playerfileyaml.getInt("skills.secondary.crafting.advancedwriting");
			pConfig.mounting=playerfileyaml.getInt("skills.secondary.crafting.mounting");
			pConfig.creaturecommands=playerfileyaml.getInt("skills.secondary.crafting.creaturecommands");
			pConfig.improvedweaponcrafting=playerfileyaml.getInt("skills.secondary.crafting.improvedweaponcrafting");
			pConfig.advancedweaponcrafting=playerfileyaml.getInt("skills.secondary.crafting.advancedweaponcrafting");
			pConfig.improvedarmorcrafting=playerfileyaml.getInt("skills.secondary.crafting.improvedarmorcrafting");
			pConfig.advancedarmorcrafting=playerfileyaml.getInt("skills.secondary.crafting.advancedarmorcrafting");
			pConfig.improvedalchemy=playerfileyaml.getInt("skills.secondary.crafting.improvedalchemy");
			pConfig.advancedalchemy=playerfileyaml.getInt("skills.secondary.crafting.advancedalchemy");
			pConfig.improvedbowcrafting=playerfileyaml.getInt("skills.secondary.crafting.improvedbowcrafting");
			pConfig.advancedbowcrafting=playerfileyaml.getInt("skills.secondary.crafting.advancedbowcrafting");
			pConfig.deconstruct=playerfileyaml.getInt("skills.secondary.crafting.deconstruct");
			pConfig.mycology=playerfileyaml.getInt("skills.secondary.botany.mycology");
			pConfig.redmushroom=playerfileyaml.getInt("skills.secondary.botany.redmushroom");
			pConfig.brownmushroom=playerfileyaml.getInt("skills.secondary.botany.brownmushroom");
			pConfig.mooshroom=playerfileyaml.getInt("skills.secondary.botany.mooshroom");
			pConfig.greenery=playerfileyaml.getInt("skills.secondary.botany.greenery");
			pConfig.flower=playerfileyaml.getInt("skills.secondary.botany.flower");
			pConfig.vegetable=playerfileyaml.getInt("skills.secondary.botany.vegetable");
			pConfig.fruit=playerfileyaml.getInt("skills.secondary.botany.fruit");
			pConfig.melon=playerfileyaml.getInt("skills.secondary.botany.melon");
			pConfig.oak=playerfileyaml.getInt("skills.secondary.botany.oak");
			pConfig.spruce=playerfileyaml.getInt("skills.secondary.botany.spruce");
			pConfig.birch=playerfileyaml.getInt("skills.secondary.botany.birch");
			pConfig.jungle=playerfileyaml.getInt("skills.secondary.botany.jungle");
			pConfig.acacia=playerfileyaml.getInt("skills.secondary.botany.acacia");
			pConfig.shieldblock=playerfileyaml.getInt("skills.secondary.defense.shieldblock");
			pConfig.shieldbash=playerfileyaml.getInt("skills.secondary.defense.shieldbash");
			pConfig.twohandedblock=playerfileyaml.getInt("skills.secondary.defense.twohandedblock");
			pConfig.offhandparry=playerfileyaml.getInt("skills.secondary.defense.offhandparry");
			pConfig.sidestep=playerfileyaml.getInt("skills.secondary.defense.sidestep");
			pConfig.diamond=playerfileyaml.getInt("skills.secondary.weapons.diamond");
			pConfig.iron=playerfileyaml.getInt("skills.secondary.weapons.iron");
			pConfig.gold=playerfileyaml.getInt("skills.secondary.weapons.gold");
			pConfig.stone=playerfileyaml.getInt("skills.secondary.weapons.stone");
			pConfig.wood=playerfileyaml.getInt("skills.secondary.weapons.wood");
			pConfig.sword=playerfileyaml.getInt("skills.secondary.weapons.sword");
			pConfig.axe=playerfileyaml.getInt("skills.secondary.weapons.axe");
			pConfig.hoe=playerfileyaml.getInt("skills.secondary.weapons.hoe");
			pConfig.pickaxe=playerfileyaml.getInt("skills.secondary.weapons.pickaxe");
			pConfig.shovel=playerfileyaml.getInt("skills.secondary.weapons.shovel");
			pConfig.fists=playerfileyaml.getInt("skills.secondary.weapons.fists");
			pConfig.mountedspeed=playerfileyaml.getInt("skills.secondary.movement.mountedspeed");
			pConfig.mountedfighting=playerfileyaml.getInt("skills.secondary.movement.mountedfighting");
			pConfig.mountedarchery=playerfileyaml.getInt("skills.secondary.movement.mountedarchery");
			pConfig.foodspeed=playerfileyaml.getInt("skills.secondary.movement.foodspeed");
			pConfig.breathingtechniques=playerfileyaml.getInt("skills.secondary.movement.breathingtechniques");
			pConfig.jumping=playerfileyaml.getInt("skills.secondary.movement.jumping");
			pConfig.swimming=playerfileyaml.getInt("skills.secondary.movement.swimming");
			pConfig.landingcontrol=playerfileyaml.getInt("skills.secondary.movement.landingcontrol");
			pConfig.thickskin=playerfileyaml.getInt("skills.secondary.movement.thickskin");
			pConfig.activeregeneration=playerfileyaml.getInt("skills.secondary.movement.activeregeneration");
			pConfig.livestock=playerfileyaml.getInt("skills.secondary.zoology.livestock");
			pConfig.ocean=playerfileyaml.getInt("skills.secondary.zoology.ocean");
			pConfig.arachnid=playerfileyaml.getInt("skills.secondary.zoology.arachnid");
			pConfig.canine=playerfileyaml.getInt("skills.secondary.zoology.canine");
			pConfig.feline=playerfileyaml.getInt("skills.secondary.zoology.feline");
			pConfig.humanoid=playerfileyaml.getInt("skills.secondary.zoology.humanoid");
			pConfig.undead=playerfileyaml.getInt("skills.secondary.zoology.undead");
			pConfig.dragon=playerfileyaml.getInt("skills.secondary.zoology.dragon");
			pConfig.construct=playerfileyaml.getInt("skills.secondary.zoology.construct");
			pConfig.elemental=playerfileyaml.getInt("skills.secondary.zoology.elemental");
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
