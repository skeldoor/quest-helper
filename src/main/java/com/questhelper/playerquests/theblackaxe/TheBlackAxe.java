package com.questhelper.playerquests.theblackaxe;

import com.questhelper.panel.PanelDetails;
import com.questhelper.questhelpers.PlayerMadeQuestHelper;
import com.questhelper.questinfo.QuestHelperQuest;
import com.questhelper.requirements.Requirement;
import com.questhelper.requirements.conditional.Conditions;
import com.questhelper.requirements.quest.QuestRequirement;
import com.questhelper.requirements.runelite.PlayerQuestStateRequirement;
import com.questhelper.requirements.util.Operation;
import com.questhelper.requirements.zone.Zone;
import com.questhelper.requirements.zone.ZoneRequirement;
import com.questhelper.rewards.UnlockReward;
import com.questhelper.runeliteobjects.RuneliteConfigSetter;
import com.questhelper.runeliteobjects.dialog.RuneliteDialogStep;
import com.questhelper.runeliteobjects.dialog.RuneliteObjectDialogStep;
import com.questhelper.runeliteobjects.dialog.RunelitePlayerDialogStep;
import com.questhelper.runeliteobjects.extendedruneliteobjects.FaceAnimationIDs;
import com.questhelper.runeliteobjects.extendedruneliteobjects.FakeItem;
import com.questhelper.runeliteobjects.extendedruneliteobjects.FakeNpc;
import com.questhelper.runeliteobjects.extendedruneliteobjects.FakeObject;
import com.questhelper.runeliteobjects.extendedruneliteobjects.ReplacedObject;
import com.questhelper.steps.ConditionalStep;
import com.questhelper.steps.DetailedQuestStep;
import com.questhelper.steps.QuestStep;
import com.questhelper.steps.TileStep;
import com.questhelper.steps.playermadesteps.RuneliteObjectStep;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.NullObjectID;
import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TheBlackAxe extends PlayerMadeQuestHelper
{
	private RuneliteObjectStep talkToBen, talkToSmith, grabBlackOre, returnToBen;

	private DetailedQuestStep standNextToBen, standNextToSmith, standNextToBen2;

	private Requirement nearBen, nearSmith;

	private FakeNpc ben, smith;

	private FakeItem blacklump;

	private PlayerQuestStateRequirement talkedToBen, talkedToSmith, displayBlackLump, pickedBlackLump;



	/*




	 */



	@Override
	public QuestStep loadStep()
	{
		itemWidget = ItemID.BLACK_BATTLEAXE;
		rotationX = 1350;
		rotationY = 1900;
		rotationZ = 150;
		zoom = 300;

		setupRequirements();
		createRuneliteObjects();
		setupSteps();

		PlayerQuestStateRequirement req = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 0);

		ConditionalStep questSteps = new ConditionalStep(this, standNextToBen);
		questSteps.addStep(req.getNewState(4), new DetailedQuestStep(this, "Quest completed!"));
		questSteps.addStep(new Conditions(req.getNewState(3), nearBen), returnToBen);
		questSteps.addStep(req.getNewState(3), standNextToBen);
		questSteps.addStep(req.getNewState(2), grabBlackOre);
		questSteps.addStep(new Conditions(req.getNewState(1), nearSmith), talkToSmith);
		questSteps.addStep(req.getNewState(1), standNextToSmith);
		questSteps.addStep(nearBen, talkToBen);

		// Don't save to config until helper closes/client closing?

		return questSteps;
	}

	@Override
	protected void setupRequirements()
	{
		// NPCs should persist through quest steps unless actively removed? Dialog should be conditional on step (sometimes)
		// Hide/show NPCs/the runelite character when NPCs go on it/you go over it
		// Handle cancelling dialog boxes (even just moving a tile away should remove for example)
		// Properly handle removing NPC from screen when changing floors and such
		// Work out how to do proper priority on the npcs being clicked
		// Wandering NPCs?
		// Objects + items (basically same as NPCs)
		talkedToBen = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 1, Operation.GREATER_EQUAL);
		talkedToSmith = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 2, Operation.GREATER_EQUAL);
		displayBlackLump = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 2);
		pickedBlackLump = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 3, Operation.GREATER_EQUAL);
		nearBen = new ZoneRequirement(new Zone(new WorldPoint(3206, 3212, 0), new WorldPoint(3212, 3218, 0)));
		nearSmith = new ZoneRequirement(new Zone(new WorldPoint(3232, 3212, 0), new WorldPoint(3238, 3218, 0)));
	}

	public void setupSteps()
	{
		// TODO: Need a way to define the groupID of a runelite object to be a quest step without it being stuck
		// Add each step's groupID as a sub-group

		talkToBen = new RuneliteObjectStep(this, ben, "Talk to the brother of Bob of Bob's Brilliant Axes.");
		standNextToBen = new TileStep(this, new WorldPoint(3228, 3204, 0), "Start the Quest by talking to Ben, the brother of Bob of Bob's Brilliant Axes.");
		talkToBen.addSubSteps(standNextToBen);

		talkToSmith = new RuneliteObjectStep(this, smith, "Talk to Smith at the Lumbridge Furnace.");
		standNextToSmith = new TileStep(this, new WorldPoint(3236, 3215, 0), "Talk to Smith at the Lumbridge Furnace. 2");
		talkToSmith.addSubSteps(standNextToSmith);

		grabBlackOre = new RuneliteObjectStep(this, blacklump, "Get the black lump to the north of Smith, outside the Lumbridge Furnace.");

		returnToBen = new RuneliteObjectStep(this, ben, "Return to Ben at Bob's Brilliant Axes.");
		standNextToBen2 = new DetailedQuestStep(this, new WorldPoint(3228, 3204, 0), "Return to Ben at Bob's Brilliant Axes. 2");
		returnToBen.addSubSteps(standNextToBen2);
	}

	private void setupBobsBrother()
	{
		// Cook's Cousin
		ben = runeliteObjectManager.createFakeNpc(this.toString(), client.getNpcDefinition(NpcID.DOCK_WORKER).getModels(), new WorldPoint(3228, 3204, 0), 808);
		ben.setName("Ben");
		ben.setFace(NpcID.DOCK_WORKER);
		ben.setExamine("Ben is Bob's brother, a member of the Dover family");
		ben.addTalkAction(runeliteObjectManager);
		ben.addExamineAction(runeliteObjectManager);

		QuestRequirement hasDoneCooksAssistant = new QuestRequirement(QuestHelperQuest.COOKS_ASSISTANT, QuestState.FINISHED);

		RuneliteObjectDialogStep dontMeetReqDialog = ben.createDialogStepForNpc("Come talk to me once you've helped my cousin out.");
		ben.addDialogTree(null, dontMeetReqDialog);

//		RuneliteDialogStep dialog = cooksCousin.createDialogStepForNpc("Hey, you there! You helped out my cousin before right?");
//		dialog.addContinueDialog(new RunelitePlayerDialogStep(client, "I have yeah, what's wrong? Does he need some more eggs? Maybe I can just get him a chicken instead?"))
//			.addContinueDialog(cooksCousin.createDialogStepForNpc("No no, nothing like that. Have you seen that terribly dressed person outside the courtyard?", FaceAnimationIDs.FRIENDLY_QUESTIONING))
//			.addContinueDialog(cooksCousin.createDialogStepForNpc("I don't know who they are, but can you please get them to move along please?", FaceAnimationIDs.FRIENDLY_QUESTIONING))
//			.addContinueDialog(cooksCousin.createDialogStepForNpc("They seem to be attracting more troublemakers...."))
//			.addContinueDialog(new RunelitePlayerDialogStep(client, "You mean Hatius? If so it'd be my pleasure.").setStateProgression(talkedToCooksCousin.getSetter()));
		RuneliteDialogStep dialog = ben.createDialogStepForNpc("Hello, I'm Bob's brother Ben. How can I help you?", FaceAnimationIDs.FRIENDLY);
		dialog.addContinueDialog(new RunelitePlayerDialogStep(client, "I'm looking for a new axe but I'm having trouble finding exactly what I need.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(ben.createDialogStepForNpc("Well you're in the right place for axes! What kind of axe do you need?", FaceAnimationIDs.FRIENDLY_QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I'm looking to acquire a rare type of axe that no one's ever seen before."))
			.addContinueDialog(ben.createDialogStepForNpc("Okay that's vague, let's start at the basics. What do you want to use this axe for?", FaceAnimationIDs.QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "For battle! Specifically ranged combat, something I could throw, if that's possible.", FaceAnimationIDs.LAUGHING))
			.addContinueDialog(ben.createDialogStepForNpc("Yep, we can do you an axe you can throw in a ranged battle, a battle axe if you will.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Uh... I'm not sure if that's quite-", FaceAnimationIDs.WORRIED_SAD))
			.addContinueDialog(ben.createDialogStepForNpc("What type of wood would you like the handle of this axe to be made from?", FaceAnimationIDs.FRIENDLY_QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Oh, I've never thought about that, just \"wood\" I guess.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(ben.createDialogStepForNpc("Yeah, but what type of wo- Okay not to worry, I'll sort that out for you.", FaceAnimationIDs.ANNOYED))
			.addContinueDialog(ben.createDialogStepForNpc("How about the axe head, what type of metal would you like that to be made from?", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Black. Uh, black metal? Yes. Black metal, please.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(ben.createDialogStepForNpc("...", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(ben.createDialogStepForNpc("Are you having me on?", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Uh...", FaceAnimationIDs.WORRIED_SAD))
			.addContinueDialog(ben.createDialogStepForNpc("Right, look pal. If you can bring me a lump of this \"Black metal\" then sure, I'll make you your axe.", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(ben.createDialogStepForNpc("Maybe find a Master Smith to help you. Until then, please stay away from this shop.", FaceAnimationIDs.ANNOYED_2).setStateProgression(talkedToBen.getSetter()));
		ben.addDialogTree(hasDoneCooksAssistant, dialog);

		RuneliteConfigSetter endQuest = new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "4");
		RuneliteDialogStep benGiveMetalLumpDialog = ben.createDialogStepForNpc("Have you got the cabbage?");
		benGiveMetalLumpDialog
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I have! Here you go, why do you need it?"))
			.addContinueDialog(ben.createDialogStepForNpc("Nice! Now let's sort out this crasher..."))
			.addContinueDialog(ben.createDialogStepForNpc("Oi noob, take this!"))
			.addContinueDialog(new RuneliteObjectDialogStep("Hatius Cosaintus", "What on earth?", NpcID.HATIUS_COSAINTUS).setStateProgression(endQuest));
		ben.addDialogTree(pickedBlackLump, benGiveMetalLumpDialog);

	}



	private void setupSmith()
	{
		// Hopleez
		smith = runeliteObjectManager.createFakeNpc(this.toString(), client.getNpcDefinition(NpcID.DOCK_WORKER).getModels(), new WorldPoint(3235, 3215, 0), 808);
		smith.setName("Smith");
		smith.setFace(7481);
		smith.setExamine("He looks kinda shady, but he's got the stuff you need.");
		smith.addTalkAction(runeliteObjectManager);
		smith.addExamineAction(runeliteObjectManager);

		// Dialog
		RuneliteDialogStep hopleezDialogPreQuest = smith.createDialogStepForNpc("Hop noob.");
		hopleezDialogPreQuest.addContinueDialog(new RunelitePlayerDialogStep(client, "What? Also, what are you wearing?"))
			.addContinueDialog(smith.createDialogStepForNpc("Hop NOOB."));
		smith.addDialogTree(null, hopleezDialogPreQuest);

		RuneliteDialogStep hopleezDialog1 = smith.createDialogStepForNpc("Hop noob.", FaceAnimationIDs.ANNOYED);
		hopleezDialog1.addContinueDialog(new RunelitePlayerDialogStep(client, "What? The Cook's Cousin sent me to see what you were doing here.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(smith.createDialogStepForNpc("One moment I was relaxing in Zeah killing some crabs. I closed my eyes for a second, and suddenly I'm here."))
			.addContinueDialog(smith.createDialogStepForNpc("People would always try to steal my spot in Zeah, and it seems it's no different here!", FaceAnimationIDs.ANNOYED))
			.addContinueDialog(smith.createDialogStepForNpc("Not only is this guy crashing me, but he's trying to outdress me too!", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Hatius? I'm pretty sure he's been here much longer than you....", FaceAnimationIDs.QUESTIONING))
			.addContinueDialog(smith.createDialogStepForNpc("I swear he wasn't here when I first arrived, I went away for a second and suddenly he's here!", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(smith.createDialogStepForNpc("Help me teach him a lesson, get me that old cabbage from outside the The Sheared Ram."))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Umm, sure....", talkedToSmith.getSetter()));
		smith.addDialogTree(talkedToBen, hopleezDialog1);


		RuneliteDialogStep hopleezWaitingForCabbageDialog = smith.createDialogStepForNpc("Get me that cabbage!");
		smith.addDialogTree(talkedToSmith, hopleezWaitingForCabbageDialog);

		RuneliteConfigSetter endQuest = new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "4");
		RuneliteDialogStep hopleezGiveCabbageDialog = smith.createDialogStepForNpc("Have you got the cabbage?");
		hopleezGiveCabbageDialog
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I have! Here you go, why do you need it?"))
			.addContinueDialog(smith.createDialogStepForNpc("Nice! Now let's sort out this crasher..."))
			.addContinueDialog(smith.createDialogStepForNpc("Oi noob, take this!"))
			.addContinueDialog(new RuneliteObjectDialogStep("Hatius Cosaintus", "What on earth?", NpcID.HATIUS_COSAINTUS).setStateProgression(endQuest));
		smith.addDialogTree(pickedBlackLump, hopleezGiveCabbageDialog);
	}

	private void setupBlackLump()
	{
		// 48958
		blacklump = runeliteObjectManager.createFakeItem(this.toString(), new int[]{ 48958 }, new WorldPoint(3231, 3235, 0), -1);
		blacklump.setScaledModel(new int[]{48958 }, 150,20,150);
		blacklump.setName("Black lump");
		blacklump.setExamine("A lump of black metal, you think? It's hard to tell, it's so dark.");
		blacklump.addExamineAction(runeliteObjectManager);
		blacklump.setDisplayRequirement(displayBlackLump);
		blacklump.addTakeAction(runeliteObjectManager, new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "3"), "You pick up the black metal lump.");
		blacklump.setObjectToRemove(new ReplacedObject(NullObjectID.NULL_37348, new WorldPoint(3231, 3235, 0)));
	}

	private void setupSuspiciousMarkings()
	{
		WorldPoint[] positions = {
			new WorldPoint(3200, 3150, 0),
			new WorldPoint(3199, 3150, 0),
			new WorldPoint(3198, 3150, 0)
		};

		for (WorldPoint position : positions){
			FakeObject suspiciousMarkings = runeliteObjectManager.createFakeObject(this.toString(), new int[]{ 37348 }, position, -1);
			suspiciousMarkings.setName("Suspicious Markings");
			suspiciousMarkings.setExamine("Some suspicious markings on the ground.");
			suspiciousMarkings.addExamineAction(runeliteObjectManager);
			suspiciousMarkings.setObjectToRemove(new ReplacedObject(NullObjectID.NULL_37348, position));
			suspiciousMarkings.setDisplayRequirement(displayBlackLump);
		}
	}

	private void createRuneliteObjects()
	{
		setupBobsBrother();
		setupSmith();
		setupBlackLump();
		setupSuspiciousMarkings();
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		return Collections.singletonList(new QuestRequirement(QuestHelperQuest.COOKS_ASSISTANT, QuestState.FINISHED));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return new ArrayList<>(Arrays.asList(
			new UnlockReward("0 Quest Points"),
			new UnlockReward("Bob's brilliant axes restraining order"),
			new UnlockReward("Access to the Black Thrownaxe")
		));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		PanelDetails helpingTheCousinSteps = new PanelDetails("Getting a black thrownaxeaxe", Arrays.asList(talkToBen, talkToSmith, grabBlackOre, returnToBen));
		allSteps.add(helpingTheCousinSteps);

		return allSteps;
	}
}
