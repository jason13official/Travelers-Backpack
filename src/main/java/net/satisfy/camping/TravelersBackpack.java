package net.satisfy.camping;

import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.fluids.EffectFluidRegistry;
import net.satisfy.camping.handlers.*;
import net.satisfy.camping.init.*;
import net.satisfy.camping.util.ResourceUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TravelersBackpack implements ModInitializer
{
	public static final String MODID = "camping";
	public static final Logger LOGGER = LogManager.getLogger();

	private static boolean trinketsLoaded;
	public static boolean craftingTweaksLoaded;

	public static boolean dehydrationloaded;
	public static boolean comfortsLoaded;
	public static boolean universalGravesLoaded;

	@Override
	public void onInitialize()
	{
		TravelersBackpackConfig.register();
		ModItemGroups.registerItemGroup();
		ModBlocks.init();
		ModItems.init();
		ModBlockEntityTypes.init();
		ModBlockEntityTypes.initSidedStorage();
		ModScreenHandlerTypes.init();
		ModRecipeSerializers.init();
		ModNetwork.initServer();
		ModCommands.registerCommands();
		ModLootConditions.registerLootConditions();
		EntityItemHandler.registerListeners();
		LootHandler.registerListeners();
		TradeOffersHandler.init();
		RightClickHandler.registerListeners();
		SleepHandler.registerListener();

		ModItems.addBackpacksToList();
		ResourceUtils.createTextureLocations();
		ResourceUtils.createSleepingBagTextureLocations();
		ModItemGroups.addItemGroup();

		trinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		craftingTweaksLoaded = FabricLoader.getInstance().isModLoaded("craftingtweaks");

		dehydrationloaded = FabricLoader.getInstance().isModLoaded("dehydration");
		comfortsLoaded = FabricLoader.getInstance().isModLoaded("comforts");

		universalGravesLoaded = FabricLoader.getInstance().isModLoaded("universal-graves");

		EffectFluidRegistry.initEffects();
	}

	public static boolean enableTrinkets()
	{
		return trinketsLoaded && TravelersBackpackConfig.getConfig().backpackSettings.trinketsIntegration;
	}

	public static boolean isAnyGraveModInstalled()
	{
		return TravelersBackpack.universalGravesLoaded;
	}
}