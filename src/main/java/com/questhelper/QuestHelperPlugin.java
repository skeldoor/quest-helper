/*
 * Copyright (c) 2020, Zoinkwiz <https://github.com/Zoinkwiz>
 * Copyright (c) 2019, Trevor <https://github.com/Trevor159>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.questhelper;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.questhelper.bank.banktab.BankTabItems;
import com.questhelper.managers.NewVersionManager;
import com.questhelper.managers.QuestBankManager;
import com.questhelper.managers.QuestManager;
import com.questhelper.managers.QuestMenuHandler;
import com.questhelper.managers.QuestOverlayManager;
import com.questhelper.panel.QuestHelperPanel;
import com.questhelper.questhelpers.QuestHelper;
import com.questhelper.questinfo.PlayerQuests;
import com.questhelper.questinfo.QuestHelperQuest;
import com.questhelper.requirements.item.ItemRequirement;
import com.questhelper.requirements.runelite.PlayerQuestStateRequirement;
import com.questhelper.requirements.util.Operation;
import com.questhelper.runeliteobjects.Cheerer;
import com.questhelper.runeliteobjects.GlobalFakeObjects;
import com.questhelper.statemanagement.PlayerStateManager;
import com.questhelper.runeliteobjects.RuneliteConfigSetter;
import com.questhelper.runeliteobjects.extendedruneliteobjects.RuneliteObjectManager;
import com.google.inject.Module;
import com.questhelper.tools.QuestWidgets;
import com.questhelper.util.worldmap.WorldMapAreaManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import com.questhelper.tools.Icon;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.NpcID;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.ScriptID;
import net.runelite.api.Tile;
import net.runelite.api.VarPlayer;
import net.runelite.api.WorldType;
import net.runelite.api.events.BeforeMenuRender;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Quest Helper",
	description = "Helps you with questing",
	tags = { "quest", "helper", "overlay" }
)
@Slf4j
public class QuestHelperPlugin extends Plugin
{
	@Getter
	@Inject
	@Named("developerMode")
	private boolean developerMode;

	@Getter
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Getter
	@Inject
	private BankSearch bankSearch;

	@Getter
	@Inject
	private ItemManager itemManager;

	@Getter
	@Inject
	ChatMessageManager chatMessageManager;

	@Getter
	@Inject
	private QuestHelperConfig config;

	@Getter
	@Inject
	RuneliteObjectManager runeliteObjectManager;

	@Inject
	private QuestOverlayManager questOverlayManager;

	@Inject
	private QuestBankManager questBankManager;

	@Inject
	private QuestManager questManager;

	@Inject
	private WorldMapAreaManager worldMapAreaManager;

	@Inject
	private QuestMenuHandler questMenuHandler;

	@Inject
	private NewVersionManager newVersionManager;

	@Getter
	@Inject
	private ColorPickerManager colorPickerManager;

	@Getter
	@Inject
	ConfigManager configManager;

	@Getter
	@Inject
	PlayerStateManager playerStateManager;

	@Inject
	public SkillIconManager skillIconManager;

	private QuestHelperPanel panel;

	private NavigationButton navButton;

	boolean profileChanged;


	// TODO: Use this for item checks
	@Getter
	private int lastTickInventoryUpdated = -1;

	@Getter
	private int lastTickBankUpdated = -1;

	private final Collection<String> configEvents = Arrays.asList("orderListBy", "filterListBy", "questDifficulty", "showCompletedQuests");
	private final Collection<String> configItemEvents = Arrays.asList("highlightNeededQuestItems", "highlightNeededMiniquestItems", "highlightNeededAchievementDiaryItems");

	@Provides
	QuestHelperConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuestHelperConfig.class);
	}

	@Override
	protected void startUp() throws IOException
	{
		questBankManager.startUp(injector, eventBus);
		eventBus.register(worldMapAreaManager);

		injector.injectMembers(playerStateManager);
		eventBus.register(playerStateManager);
		playerStateManager.startUp();

		eventBus.register(runeliteObjectManager);
		runeliteObjectManager.startUp();

		scanAndInstantiate();

		questOverlayManager.startUp();

		final BufferedImage icon = Icon.QUEST_ICON.getImage();

		panel = new QuestHelperPanel(this, questManager, configManager);
		questManager.startUp(panel);
		navButton = NavigationButton.builder()
			.tooltip("Quest Helper")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				questManager.setupRequirements();
				questManager.setupOnLogin();
				GlobalFakeObjects.createNpcs(client, runeliteObjectManager, configManager, config);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		runeliteObjectManager.shutDown();

		eventBus.unregister(playerStateManager);
		eventBus.unregister(runeliteObjectManager);
		eventBus.unregister(worldMapAreaManager);
		questOverlayManager.shutDown();

		clientToolbar.removeNavigation(navButton);
		questManager.shutDown();
		questBankManager.shutDown(eventBus);

		GlobalFakeObjects.setInitialized(false);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		questBankManager.loadInitialStateFromConfig(client);
		questManager.updateQuestState();

		ModelData modelData = client.loadModelData(7815);
		if (modelData != null){
			//print out the short array modelData.getFaceColors() as a string
			modelData.recolor((short) 6364, (short) 65343);
			modelData.recolor((short) 5314, (short) 65343);
		}

		clientThread.invokeAtTickEnd(() ->
		{
			Scene scene = client.getScene();
			Tile[][][] tiles = scene.getTiles();
			// Search every tile for an object with the ID 837
			for (Tile[][] tile : tiles)
			{
				for (Tile[] tile1 : tile)
				{
					for (Tile tile2 : tile1)
					{
						if (tile2 != null)
						{
							GameObject[] gameObjects = tile2.getGameObjects();
							if (gameObjects != null)
							{
								for (GameObject gameObject : gameObjects)
								{
									if (gameObject != null && gameObject.getId() == 837)
									{
										if (gameObject.getWorldLocation().getX() == 3205 && gameObject.getWorldLocation().getY() == 3222) continue;
										axecab = gameObject;
									}
								}
							}
						}
					}
				}
			}

			if (axecab != null)
			{
				//print the axecab world location in a nice format
				log.debug("Axecab is at: " + axecab.getWorldLocation().toString());


				Renderable renderable = axecab.getRenderable();
				Model model = verifyModel(renderable);
				if (model == null)
				{
					log.debug("recolorGameObject returned null!");
					return;
				}
				applyColor(model);
				model.setSceneId(0);
			} else {
				log.debug("axecab is null!");
			}
		});
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getItemContainer() == client.getItemContainer(InventoryID.BANK))
		{
			lastTickBankUpdated = client.getTickCount();
			questBankManager.updateLocalBank(event.getItemContainer());
		}

		if (event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY))
		{
			lastTickInventoryUpdated = client.getTickCount();
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		final GameState state = event.getGameState();

		if (state == GameState.LOGIN_SCREEN)
		{
			questBankManager.saveBankToConfig();
			SwingUtilities.invokeLater(() -> panel.refresh(Collections.emptyList(), true, new HashMap<>()));
			questBankManager.emptyState();
			questManager.shutDownQuest(true);
			profileChanged = true;
		}

		if (state == GameState.LOGGED_IN && profileChanged)
		{
			profileChanged = false;
			questManager.shutDownQuest(true);
			GlobalFakeObjects.createNpcs(client, runeliteObjectManager, configManager, config);
			newVersionManager.updateChatWithNotificationIfNewVersion();
			questBankManager.setUnknownInitialState();
			clientThread.invokeAtTickEnd(() -> {
				questManager.setupRequirements();
				questManager.setupOnLogin();
			});
		}

		if(event.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeAtTickEnd(() ->
			{
				if (axecab != null)
				{
					//Renderable renderable = axecab.getRenderable();
					//Model model = verifyModel(renderable);
					//if (model == null)
					//{
					//	log.debug("recolorGameObject returned null!");
					//	return;
					//}
					int[] f1;
					int[] f2;
					int[] f3;
					f1 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					f2 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					f3 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					//applyColor(model, f1, f2, f3);
					//model.setSceneId(0);
				}
			});
		}
	}

	private Model verifyModel(Renderable renderable)
	{
		if (renderable instanceof Model)
		{
			return (Model) renderable;
		}
		else
		{
			Model model = renderable.getModel();
			if (model == null)
			{
				log.debug("verifyModel returned null!");
				return null;
			}
			return model;
		}
	}

	@Subscribe
	private void onRuneScapeProfileChanged(RuneScapeProfileChanged ev)
	{
		profileChanged = true;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!(client.getGameState() == GameState.LOGGED_IN))
		{
			return;
		}

		if (client.getWorldType().contains(WorldType.QUEST_SPEEDRUNNING)
			&& event.getVarpId() == VarPlayer.IN_RAID_PARTY
			&& event.getValue() == 0
			&& client.getGameState() == GameState.LOGGED_IN)
		{
			questBankManager.updateBankForQuestSpeedrunningWorld();
		}

		questManager.handleVarbitChanged();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		questManager.handleConfigChanged();

		if (event.getGroup().equals(QuestHelperConfig.QUEST_BACKGROUND_GROUP))
		{
			clientThread.invokeLater(questManager::updateQuestList);
			SwingUtilities.invokeLater(panel::refreshSkillFiltering);
		}

		if (!event.getGroup().equals(QuestHelperConfig.QUEST_HELPER_GROUP))
		{
			return;
		}

		if (event.getKey().equals("showRuneliteObjects") && client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() -> {
				if (config.showRuneliteObjects())
				{
					GlobalFakeObjects.createNpcs(client, runeliteObjectManager, configManager, config);
				}
				else
				{
					GlobalFakeObjects.disableNpcs(runeliteObjectManager);
				}
			});
		}

		if (configEvents.contains(event.getKey()) || event.getKey().contains("skillfilter"))
		{
			clientThread.invokeLater(questManager::updateQuestList);
		}

		if (configItemEvents.contains(event.getKey()))
		{
			questManager.updateAllItemsHelper();
		}

		if ("highlightItemsBackground".equals(event.getKey()))
		{
			questManager.updateAllItemsBackgroundHelper(event.getNewValue());
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if (developerMode && commandExecuted.getCommand().equals("questhelperdebug"))
		{
			if (commandExecuted.getArguments().length == 0 ||
				(Arrays.stream(commandExecuted.getArguments()).toArray()[0]).equals("disable"))
			{
				questOverlayManager.removeDebugOverlay();
			}
			else if ((Arrays.stream(commandExecuted.getArguments()).toArray()[0]).equals("enable"))
			{
				questOverlayManager.addDebugOverlay();
			}
		}
		else if (commandExecuted.getCommand().equals("reset-cooks-helper"))
		{
			String step = (String) (Arrays.stream(commandExecuted.getArguments()).toArray()[0]);
			new RuneliteConfigSetter(configManager, QuestHelperQuest.COOKS_HELPER.getPlayerQuests().getConfigValue(), step).setConfigValue();
		}
		else if (commandExecuted.getCommand().equals("reset-black-axe"))
		{
			String step = (String) (Arrays.stream(commandExecuted.getArguments()).toArray()[0]);
			new RuneliteConfigSetter(configManager, QuestHelperQuest.THE_BLACK_AXE.getPlayerQuests().getConfigValue(), step).setConfigValue();
		}
		else if (developerMode && commandExecuted.getCommand().equals("qh-inv"))
		{
			ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
			StringBuilder inv = new StringBuilder();
			if (inventory != null)
			{
				for (Item item : inventory.getItems())
				{
					inv.append(item.getId()).append("\n");
				}
			}
			System.out.println(inv);
		}
	}

	@Subscribe(priority = 100)
	private void onClientShutdown(ClientShutdown e)
	{
		questBankManager.saveBankToConfig();
	}

	public void refreshBank()
	{
		clientThread.invokeLater(() -> questBankManager.refreshBankTab());
	}

	public List<BankTabItems> getPluginBankTagItemsForSections()
	{
		return questBankManager.getBankTagService().getPluginBankTagItemsForSections(false);
	}

	public QuestHelper getSelectedQuest()
	{
		return questManager.getSelectedQuest();
	}

	public Map<String, QuestHelper> getBackgroundHelpers()
	{
		return questManager.backgroundHelpers;
	}

	public Map<QuestHelperQuest, List<ItemRequirement>> getItemRequirements()
	{
		return questManager.itemRequirements;
	}

	public Map<QuestHelperQuest, List<ItemRequirement>> getItemRecommended()
	{
		return questManager.itemRecommended;
	}

	public List<Integer> itemsToTag()
	{
		return questBankManager.getBankTagService().itemsToTag();
	}

	private void addCheerer()
	{
		Cheerer.activateCheerer(client, chatMessageManager);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		int widgetIndex = event.getActionParam0();
		int widgetID = event.getActionParam1();
		MenuEntry[] menuEntries = client.getMenuEntries();
		String option = event.getOption();

		String target = Text.removeTags(event.getTarget());

		questMenuHandler.setupQuestMenuOptions(menuEntries, widgetIndex, widgetID, target, option);
	}

	GameObject axecab = null;

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned){
		GameObject gameObject = gameObjectSpawned.getGameObject();
		if (gameObject.getId() == 837){
			axecab = gameObject;
		}
	}

	// Sorts the list according to increasing original Y position
	// This value is between 0 and 3000 so cant be used as the index for a list
	private Widget[] orderListOnOriginalY(Widget[] list){
		Arrays.sort(list, (a, b) -> {
			int aY = a.getOriginalY();
			int bY = b.getOriginalY();
			return aY - bY;
		});
		return list;
	}

	//This function will find the alphabetical order for the quest by searching for bone voyage, replacing it with the black axe, and then putting bone voyage into the next available slot, repeating for the entire list
	// this function doesnt work because the quest list is not in the same order as the quest widget list
	// the quest list in game is sorted by f2p then p2p in alphabetical order but the quest widget list is sorted by addition to the game
	private void determineBlackAxePositionAndShuffle(){
		Widget[] sortedWidgets = orderListOnOriginalY(client.getWidget(26148871).getDynamicChildren());

		//print out all the sorted widgets names in a pretty format all on one line
		for (Widget questWidget : sortedWidgets){
			System.out.print(questWidget.getName() + ", ");
		}

		boolean blackAxePositionDetermined = false;
		boolean boneVoyagePositionDetermined = false;
		String savedQuestText = "";
		String savedQuestName = "";
		for (Widget questWidget : sortedWidgets){

			if (!blackAxePositionDetermined && !questWidget.getText().contains("Bone Voyage")){
				continue;
			}
			if (!blackAxePositionDetermined && questWidget.getText().contains("Bone Voyage")){
				questWidget.setText("The Black Axe");
				questWidget.setName("The Black Axe");
				blackAxePositionDetermined = true;
				savedQuestText = questWidget.getText();
				savedQuestName = questWidget.getName();
			}
			if (blackAxePositionDetermined)
			{3
				String currentQuestText = questWidget.getText();
				String currentQuestName = questWidget.getName();
				if (!boneVoyagePositionDetermined){
					boneVoyagePositionDetermined = true;
					currentQuestText = "bne coyage";
					currentQuestName = "bne coyage";
				}

				questWidget.setText(savedQuestText);
				questWidget.setName(savedQuestName);
				savedQuestText = currentQuestText;
				savedQuestName = currentQuestName;
			}
			if (questWidget.getText().contains("While Guthix Sleeps")){
				break;
			}
		}
	}


	@Subscribe
	public void onBeforeRender(BeforeRender beforeRender)
	{

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			determineBlackAxePositionAndShuffle();
		}


		try {
			if (Objects.equals(new RuneliteConfigSetter(configManager, QuestHelperQuest.THE_BLACK_AXE.getPlayerQuests().getConfigValue(), "blah").getConfigValue(), "7")){
				client.getWidget(9764864).getDynamicChildren()[0].setModelZoom(400);
				client.getWidget(9764864).getDynamicChildren()[0].setItemId(ItemID.BLACK_BATTLEAXE);
				client.getWidget(9764864).getDynamicChildren()[0].setItemQuantity(1);
				client.getWidget(9764864).getDynamicChildren()[0].setItemQuantityMode(1);
				client.getWidget(9764864).getDynamicChildren()[0].setName("<col=FF9040>Black thrownaxe");
			} else if (Objects.equals(new RuneliteConfigSetter(configManager, QuestHelperQuest.THE_BLACK_AXE.getPlayerQuests().getConfigValue(), "blah").getConfigValue(), "5")){
				client.getWidget(9764864).getDynamicChildren()[0].setModelZoom(400);
				client.getWidget(9764864).getDynamicChildren()[0].setItemId(ItemID.CHARCOAL);
				client.getWidget(9764864).getDynamicChildren()[0].setName("<col=FF9040>Black lump");
				client.getWidget(9764864).getDynamicChildren()[0].setItemQuantityMode(0);
			} else {
				client.getWidget(9764864).getDynamicChildren()[0].setItemId(-1);
				client.getWidget(9764864).getDynamicChildren()[0].setName("");
			}
		} catch (Exception ignored) {
		}

		try {
			if (Objects.equals(new RuneliteConfigSetter(configManager, QuestHelperQuest.THE_BLACK_AXE.getPlayerQuests().getConfigValue(), "blah").getConfigValue(), "7")){

				//Model model = client.loadModel(1160);

				int[] f1;
				int[] f2;
				int[] f3;
				f1 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				f2 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				f3 = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				//applyColor(model, f1, f2, f3);

				short[] shortstofind = new short[1];
				shortstofind[0] = 61;
				short[] shortstoreplace = new short[1];
				shortstoreplace[0] = 7;
				client.loadModel(1160, shortstofind, shortstoreplace);


			}
		} catch (Exception ignored){
		}





		try {
			if (client.getWidget(10616869).getDynamicChildren()[3].getText().contains("Thurgo leaves a lump of Black metal slag outside the nearby furnace.")){
				client.getWidget(10616869).getDynamicChildren()[3].setRelativeX(72);
				client.getWidget(10616869).getDynamicChildren()[2].setRelativeX(72);
			}
		} catch (Exception ignored) {
		}

		try {
			if (client.getWidget(10616869).getDynamicChildren()[3].getText().contains("You hand the lump of black metal to Ben.")){
				client.getWidget(10616869).getDynamicChildren()[3].setRelativeX(72);
				client.getWidget(10616869).getDynamicChildren()[2].setRelativeX(72);
			}
		} catch (Exception ignored) {
		}

		try {
			if (client.getWidget(10616869).getDynamicChildren()[3].getText().contains("Ben stomps away muttering")){
				client.getWidget(10616869).getDynamicChildren()[3].setRelativeX(72);
				client.getWidget(10616869).getDynamicChildren()[2].setRelativeX(72);
			}
		} catch (Exception ignored) {
		}

		try {
			if (client.getWidget(10616869).getDynamicChildren()[3].getText().contains("and returns, carrying a Black thrownaxe")){
				client.getWidget(10616869).getDynamicChildren()[3].setRelativeX(72);
				client.getWidget(10616869).getDynamicChildren()[2].setRelativeX(72);
			}
		} catch (Exception ignored) {
		}

	}

	//Searches an array of shorts for a value and replaces all instances of that value with another
	public void replaceIntValue(int[] array, int find, int replace)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] < find)
			{
				array[i] = replace;
			}
		}
	}


	// applies the colors to a model
	public void applyColor(Model model)
	{

		int[] faceColors = model.getFaceColors1();
		int[] faceColors2 = model.getFaceColors2();
		int[] faceColors3 = model.getFaceColors3();

		//Create a new faceColors array with the same length as the original and copy it
		int[] newFaceColors = new int[faceColors.length];
		newFaceColors = new int[]{7945, 7945, 7945, 7945, 7942, 7942, 7942, 7949, 7949, 7949, 7949, 5767, 5767, 5767, 5767, 5763, 5763, 5769, 5769, 58, 58, 58, 58, 39, 30, 30, 30, 30, 30, 30, 30, 35, 4, 4, 8, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 36, 53, 54, 2, 2, 22, 7047, 7047, 7047, 7047, 7047, 7044, 7044, 7044, 7057, 7057, 7057, 7059, 7059, 7059, 7059, 7059, 7059, 7056, 7056, 7046, 7046, 7059, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7053, 7053, 7053, 7053, 7053, 7045, 7045, 7045, 7057, 7057, 7057, 7067, 7067, 7067, 7067, 7067, 7055, 7064, 7064, 7046, 7046, 7055, 31, 31, 31, 31, 31, 31, 31, 31, 31, 31, 20, 30, 30, 46, 46, 46, 40, 40, 40, 40, 28, 29, 29, 29, 29, 29, 29, 29, 13, 59, 59, 36, 7057, 7057, 7057, 7057, 7057, 7057, 7051, 7051, 7048, 7048, 7048, 7060, 7060, 7060, 7060, 7044, 7044, 7044, 7056, 7067, 7066, 7066, 21, 21, 21, 21, 21, 21, 14, 14, 14, 15, 15, 42, 42, 42, 42, 20, 17, 17, 27, 42, 42, 42, 48, 48, 20, 20, 9, 9, 24, 24, 43, 43, 55, 35, 23, 23, 23, 7, 8834, 8834, 3, 3, 8834, 8834, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5771, 7951, 8834, 8834, 8834, 8834, 5762, 5765, 5763, 5763, 5763, 5763, 5763, 5762, 5762, 8836, 8836, 8836, 5762, 5762, 5762, 5762, 5764, 5764, 5762, 5763, 5762, 8834, 8834, 3, 3, 8834, 8834, 8834, 5764, 5764, 5764, 5762};

		int[] newFaceColors2 = new int[faceColors2.length];
		newFaceColors2 = new int[]{7945, 7945, 7945, 7941, 7941, 7938, 7946, 7946, 7951, 7948, 7945, 5763, 5763, 5763, 5763, 5763, 5762, 5765, 5763, 39, 30, 58, 35, 35, 35, 21, 4, 8, 26, 30, 40, 58, 40, 30, 30, 36, 53, 54, 30, 30, 22, 2, 2, 19, 30, 30, 30, 30, 30, 30, 30, 7050, 7046, 7044, 7057, 7059, 7059, 7059, 7054, 7054, 7067, 7067, 7067, 7067, 7059, 7056, 7050, 7056, 7056, 7046, 7056, 7059, 7067, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7059, 7046, 7045, 7057, 7067, 7067, 7055, 7045, 7045, 7060, 7067, 7067, 7060, 7055, 7064, 7059, 7057, 7057, 7046, 7057, 7055, 7060, 20, 30, 30, 54, 46, 30, 19, 23, 2, 2, 2, 2, 2, 54, 23, 19, 28, 29, 38, 13, 13, 13, 7, 20, 36, 39, 59, 59, 38, 59, 36, 59, 7051, 7048, 7056, 7067, 7067, 7066, 7066, 7060, 7060, 7044, 7056, 7056, 7067, 7059, 7054, 7054, 7059, 7054, 7054, 7054, 7067, 7067, 14, 35, 31, 20, 27, 17, 17, 15, 15, 17, 28, 20, 43, 57, 27, 31, 27, 44, 57, 55, 35, 23, 23, 30, 30, 7, 7, 11, 11, 15, 15, 34, 34, 34, 34, 15, 7, 15, 8838, 8838, 3, 3, 8834, 8836, 5762, 5762, 5762, 5762, 5762, 5767, 5767, 5762, 5762, 5762, 5765, 5765, 7948, 8836, 8834, 8834, 8834, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5762, 5763, 8839, 8834, 8834, 5762, 5762, 5762, 5762, 5762, 5767, 5767, 5767, 5767, 8839, 8838, 3, 3, 8834, 8834, 8834, 5762, 5762, 5767, 5762};

		int[] newFaceColors3 = new int[faceColors3.length];
		newFaceColors3 = new int[]{7945, 7945, 7941, 7942, 7938, 7946, 7949, 7951, 7948, 7945, 7945, 5763, 5767, 5763, 5763, 5762, 5762, 5763, 5767, 30, 58, 35, 39, 30, 21, 4, 8, 26, 30, 40, 58, 21, 30, 8, 26, 53, 54, 30, 30, 22, 2, 2, 19, 30, 36, 53, 54, 19, 30, 2, 2, 7046, 7044, 7057, 7059, 7044, 7059, 7054, 7057, 7067, 7067, 7059, 7067, 7059, 7056, 7050, 7047, 7056, 7046, 7050, 7059, 7044, 7054, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7046, 7045, 7057, 7067, 7045, 7055, 7045, 7057, 7060, 7067, 7067, 7060, 7055, 7064, 7059, 7053, 7064, 7046, 7059, 7055, 7045, 7045, 30, 30, 54, 46, 30, 19, 23, 2, 2, 20, 30, 30, 2, 23, 19, 30, 29, 38, 13, 28, 29, 7, 20, 36, 39, 59, 59, 38, 7, 36, 20, 39, 7048, 7056, 7067, 7067, 7066, 7051, 7060, 7048, 7044, 7056, 7060, 7067, 7059, 7054, 7044, 7059, 7054, 7056, 7067, 7059, 7067, 7060, 35, 31, 20, 27, 17, 14, 15, 15, 35, 28, 15, 43, 57, 27, 20, 43, 44, 28, 44, 35, 23, 48, 30, 20, 7, 9, 11, 24, 15, 43, 34, 55, 35, 23, 15, 7, 30, 11, 8838, 8834, 3, 3, 8836, 8838, 5762, 5762, 5762, 5762, 5767, 5762, 5762, 5762, 5762, 5765, 5771, 5769, 7951, 8834, 8834, 8834, 8838, 5765, 5763, 5762, 5763, 5762, 5763, 5762, 5763, 5762, 8834, 8834, 8834, 5762, 5762, 5762, 5764, 5767, 5762, 5763, 5762, 5762, 8838, 8834, 3, 3, 8834, 8834, 8838, 5762, 5767, 5762, 5762};



//		//Create a new faceColors array with the same length as the original and copy it
//		int[] newFaceColors = new int[faceColors.length];
//		System.arraycopy(faceColors, 0, newFaceColors, 0, faceColors.length);
//		replaceIntValue(newFaceColors, 100, 0);
//
//		int[] newFaceColors2 = new int[faceColors2.length];
//		System.arraycopy(faceColors2, 0, newFaceColors2, 0, faceColors2.length);
//		replaceIntValue(newFaceColors2, 100, 0);
//
//		int[] newFaceColors3 = new int[faceColors3.length];
//		System.arraycopy(faceColors3, 0, newFaceColors3, 0, faceColors3.length);
//		replaceIntValue(newFaceColors3, 100, 0);



		if (newFaceColors.length <= faceColors.length && newFaceColors2.length <= faceColors2.length && newFaceColors3.length <= faceColors3.length)
		{
			System.arraycopy(newFaceColors, 0, faceColors, 0, newFaceColors.length);
			System.arraycopy(newFaceColors2, 0, faceColors2, 0, newFaceColors2.length);
			System.arraycopy(newFaceColors3, 0, faceColors3, 0, newFaceColors3.length);
		}
		else
		{
			log.debug("FaceColor has the wrong length.");
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked){
		log.warn(menuOptionClicked.getMenuOption());
		log.warn(menuOptionClicked.getMenuTarget());
		if (menuOptionClicked.getMenuOption().equals("Wield") && menuOptionClicked.getMenuTarget().contains("Black") && menuOptionClicked.getMenuTarget().contains("thrownaxe")){
			runeliteObjectManager.createChatboxMessage("Are you mad? This is the only black thrownaxe in the game, it's too precious to equip! What if you were to drop it?");
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		log.warn(chatMessage.getMessage());
		if (chatMessage.getMessage().contains("A finely balanced throwing axe")){
			log.warn("setting");
			chatMessage.setMessage("A black axe that can be used in battle and you could throw it, if you tried hard enough.");
			chatMessage.getMessageNode().setValue("A black axe that can be used in battle and you could throw it, if you tried hard enough.");
		}
		if (config.showFan() && chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (chatMessage.getMessage().contains("Congratulations! Quest complete!") ||
				chatMessage.getMessage().contains("you've completed a quest"))
			{
				addCheerer();
			}
		}
		if (config.autoStartQuests() && chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (questManager.getSelectedQuest() == null && chatMessage.getMessage().contains("You've started a new quest"))
			{
				String questName = chatMessage.getMessage().substring(chatMessage.getMessage().indexOf(">") + 1);
				questName = questName.substring(0, questName.indexOf("<"));
				questMenuHandler.startUpQuest(questName);
			}
		}
	}

	public void displayPanel()
	{
		SwingUtilities.invokeLater(() -> {
			clientToolbar.openPanel(navButton);
		});
	}

	private void scanAndInstantiate()
	{
		for (QuestHelperQuest qhq : QuestHelperQuest.values())
		{
			instantiate(qhq);
		}
	}

	private void instantiate(QuestHelperQuest quest)
	{
		QuestHelper questHelper = quest.getQuestHelper();

		Module questModule = (Binder binder) ->
		{
			binder.bind(QuestHelper.class).toInstance(questHelper);
			binder.install(questHelper);
		};
		Injector questInjector = RuneLite.getInjector().createChildInjector(questModule);
		injector.injectMembers(questHelper);
		questHelper.setInjector(questInjector);
		questHelper.setQuest(quest);
		questHelper.setConfig(config);
		questHelper.setQuestHelperPlugin(this);

		log.debug("Loaded quest helper {}", quest.name());
	}
}
