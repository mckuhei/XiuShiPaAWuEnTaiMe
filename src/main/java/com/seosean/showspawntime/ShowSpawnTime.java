package com.seosean.showspawntime;

import com.seosean.showspawntime.commands.CommandCommon;
import com.seosean.showspawntime.commands.CommandSSTConfig;
import com.seosean.showspawntime.commands.CommandSSTHUD;
import com.seosean.showspawntime.config.LanguageConfiguration;
import com.seosean.showspawntime.config.MainConfiguration;
import com.seosean.showspawntime.handler.GameTickHandler;
import com.seosean.showspawntime.handler.LanguageDetector;
import com.seosean.showspawntime.handler.ScoreboardManager;
import com.seosean.showspawntime.modules.features.Renderer;
import com.seosean.showspawntime.modules.features.lrqueue.LRQueue;
import com.seosean.showspawntime.modules.features.spawntimes.SpawnNotice;
import com.seosean.showspawntime.modules.features.spawntimes.SpawnTimeRenderer;
import com.seosean.showspawntime.modules.features.spawntimes.SpawnTimes;
import com.seosean.showspawntime.modules.features.powerups.PowerupDetect;
import com.seosean.showspawntime.modules.features.powerups.PowerupRenderer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = ShowSpawnTime.MODID, version = ShowSpawnTime.VERSION,
        guiFactory = "com.seosean.showspawntime.config.gui.ShowSpawnTimeGuiFactory"

)
public class ShowSpawnTime
{
    public static final String MODID = "showspawntime";
    public static final String VERSION = "2.0";
    private Logger logger;
    private static Configuration config;
    public static final String EMOJI_REGEX = "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)";

    public static final String COLOR_REGEX = "§[a-zA-Z0-9]";

    public static final ScoreboardManager SCOREBOARD_MANAGER = new ScoreboardManager();
    public static ShowSpawnTime INSTANCE;
    private GameTickHandler gameTickHandler;
    private static PowerupDetect powerupDetect;
    private static PowerupRenderer powerupRenderer;
    private static SpawnTimeRenderer spawnTimeRenderer;
    private static LRQueue lrQueue;
    private static SpawnTimes spawnTimes;
    private static SpawnNotice spawnNotice;

    private static MainConfiguration mainConfiguration;
    private static LanguageConfiguration languageConfiguration;
    @EventHandler
    public void preinit(FMLPreInitializationEvent event){
        languageConfiguration = createMainConfig(event);
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        mainConfiguration = new MainConfiguration(config, logger);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(gameTickHandler = new GameTickHandler());
        MinecraftForge.EVENT_BUS.register(powerupDetect = new PowerupDetect());
        MinecraftForge.EVENT_BUS.register(spawnTimes = new SpawnTimes());
        MinecraftForge.EVENT_BUS.register(spawnNotice = new SpawnNotice());
        MinecraftForge.EVENT_BUS.register(powerupRenderer = new PowerupRenderer());
        MinecraftForge.EVENT_BUS.register(spawnTimeRenderer = new SpawnTimeRenderer());
        MinecraftForge.EVENT_BUS.register(lrQueue = new LRQueue());
        MinecraftForge.EVENT_BUS.register(SCOREBOARD_MANAGER);
        MinecraftForge.EVENT_BUS.register(mainConfiguration);
        MinecraftForge.EVENT_BUS.register(new LanguageDetector());
        
        ClientCommandHandler.instance.registerCommand(new CommandSSTHUD());
        ClientCommandHandler.instance.registerCommand(new CommandSSTConfig());
        ClientCommandHandler.instance.registerCommand(new CommandCommon());

        ClientRegistry.registerKeyBinding(MainConfiguration.keyToggleCountDown);
        ClientRegistry.registerKeyBinding(MainConfiguration.keyTogglePlayerInvisible);
        ClientRegistry.registerKeyBinding(MainConfiguration.keyOpenConfig);
    }

    public static ScoreboardManager getScoreboardManager() {
        return SCOREBOARD_MANAGER;
    }

    private static LanguageConfiguration createMainConfig(FMLPreInitializationEvent event) {
        File modConfigFolder = new File(event.getModConfigurationDirectory(), MODID);
        if (!modConfigFolder.exists()) {
            modConfigFolder.getParentFile().mkdirs();
        }
        File mainConfig = new File(modConfigFolder, MODID + ".txt");
        if (!mainConfig.exists()) {
            mainConfig.getParentFile().mkdirs();
        }
        LanguageConfiguration config = new LanguageConfiguration(mainConfig);
        config.load();
        return config;
    }

    public static LanguageConfiguration getLanguageConfiguration() {
        return languageConfiguration;
    }

    public static ShowSpawnTime getInstance() {
        return INSTANCE;
    }

    public GameTickHandler getGameTickHandler() {
        return gameTickHandler;
    }

    public PowerupDetect getPowerupDetect() {
        return powerupDetect;
    }

    public static SpawnTimes getSpawnTimes() {
        return spawnTimes;
    }

    public static PowerupRenderer getPowerupRenderer() {
        return powerupRenderer;
    }

    public static SpawnTimeRenderer getSpawnTimeRenderer() {
        return spawnTimeRenderer;
    }

    public static SpawnNotice getSpawnNotice() {
        return spawnNotice;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static MainConfiguration getMainConfiguration() {
        return mainConfiguration;
    }
}
