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
import net.runelite.api.MenuEntry;
import net.runelite.api.ModelData;
import net.runelite.api.NpcID;
import net.runelite.api.NullObjectID;
import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TheBlackAxe extends PlayerMadeQuestHelper
{
	private RuneliteObjectStep talkToBen, talkToThurgo, plantRedberryBush, talkToThurgo2, grabBlackOre, talkToBen2, talkToBen3;

	private DetailedQuestStep standNextToBen, standNextToThurgo, standNextToBen2;

	private Requirement nearBen, nearThurgo;

	private FakeNpc ben, thurgo;

	private FakeObject redberryBushPatch;
	private FakeObject redberryBush;

	private FakeItem blackLump;

	private PlayerQuestStateRequirement
		talkedToBen, talkedToThurgo,
		plantedRedberryBush, displayRedberryBush,
		talkedToThurgo2, displayBlackLump, 
		handedInBlackLump,
		retrievedBlackLump, retrievedBlackThrownaxe,
		handedInBlackAxe;
	

	private RuneliteObjectStep talkToCook, talkToHopleez, grabCabbage, returnToHopleez;

	private DetailedQuestStep standNextToCook, standNextToHopleez, standNextToHopleez2;

	private Requirement nearCook, nearHopleez;

	private FakeNpc cooksCousin, hopleez;

	private FakeItem cabbage;

	private PlayerQuestStateRequirement talkedToCooksCousin, talkedToHopleez, displayCabbage, pickedCabbage;

	/*
	Speak to Ben, the brother of Bob of Bob's Brilliant Axes.
	Speak to Thurgo at the Lumbridge Furnace.
	Thurgo will give you a black lump in return for a redberry bush planted in his garden.
	"I don't know what Black is, but I thought I could sweeten the deal with some redberry pie."
	When talking about redberry pies, Thurgo mentions he wishes there was an infinite supply of redberries.
	"Oh I love redberries, sometimes I wish I had an infinite supply!"
	I tell him "I can help with that."
	"Oh yeah, how are you going to do that? Redberries don't grow on trees you know."
	"Well funnily enough, they kinda do. I could plant a redberry bush in your garden if you'd like."
	"picks axes mate, I'd love that! In return, I'll give you some raw Black metal for your axe."
	Plant a redberry bush in Thurgo's garden.
	Return to Thurgo and he will give you a black lump.
	Return to Ben with the black lump.
	Ben will craft you a black thrownaxe.
	The axe is unusable.
	Your character decides to return the axe to Ben who frames it in his shop.
	"Sorry to be a pain Ben, but I can't bring myself to use this axe. It's too precious."
	"Ah, don't worry about it mate. I'll hang it up in the shop, it'll be a great conversation piece."
	"Thanks Ben, I'd like that."
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
//		questSteps.addStep(req.getNewState(7), new DetailedQuestStep(this, "Quest completed!"));
//		questSteps.addStep(req.getNewState(6), talkToBen3);
//		questSteps.addStep(req.getNewState(5), talkToBen2);
//		questSteps.addStep(req.getNewState(4), grabBlackOre);
//		questSteps.addStep(req.getNewState(3), talkToThurgo2);
//		questSteps.addStep(req.getNewState(2), plantRedberryBush);
//		questSteps.addStep(nearThurgo, talkToThurgo);
		questSteps.addStep(nearBen, talkToBen);



		return questSteps;
	}

	@Override
	protected void setupRequirements()
	{
		talkedToBen = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 1, Operation.GREATER_EQUAL);
		talkedToThurgo = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 2, Operation.GREATER_EQUAL);
		plantedRedberryBush = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 3, Operation.GREATER_EQUAL);
		displayRedberryBush = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 3);
		talkedToThurgo2 = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 4, Operation.GREATER_EQUAL);
		displayBlackLump = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 4);
		retrievedBlackLump = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 5, Operation.GREATER_EQUAL);
		handedInBlackLump = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 6, Operation.GREATER_EQUAL);
		retrievedBlackThrownaxe = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 7, Operation.GREATER_EQUAL);
		handedInBlackAxe = new PlayerQuestStateRequirement(configManager, getQuest().getPlayerQuests(), 8, Operation.GREATER_EQUAL);
		nearBen = new ZoneRequirement(new Zone(new WorldPoint(3206, 3212, 0), new WorldPoint(3212, 3218, 0)));
		nearThurgo = new ZoneRequirement(new Zone(new WorldPoint(3230, 3210, 0), new WorldPoint(3242, 3220, 0)));
	}

	public void setupSteps()
	{
//		// TODO: Need a way to define the groupID of a runelite object to be a quest step without it being stuck
//		// Add each step's groupID as a sub-group
//
		talkToBen = new RuneliteObjectStep(this, ben, "Talk to the brother of Bob of Bob's Brilliant Axes.");
		standNextToBen = new TileStep(this, new WorldPoint(3228, 3204, 0), "Start the Quest by talking to Ben, the brother of Bob of Bob's Brilliant Axes.");
		talkToBen.addSubSteps(standNextToBen);
//
//		talkToThurgo = new RuneliteObjectStep(this, thurgo, "Talk to Thurgo at the Lumbridge Furnace.");
//		standNextToThurgo = new TileStep(this, new WorldPoint(3236, 3215, 0), "Talk to Smith at the Lumbridge Furnace. 2");
//		talkToThurgo.addSubSteps(standNextToThurgo);
//
//		grabBlackOre = new RuneliteObjectStep(this, redberryBush, "Get the black lump to the north of Smith, outside the Lumbridge Furnace.");
//
//		returnToBen = new RuneliteObjectStep(this, ben, "Return to Ben at Bob's Brilliant Axes.");
//		standNextToBen2 = new DetailedQuestStep(this, new WorldPoint(3228, 3204, 0), "Return to Ben at Bob's Brilliant Axes. 2");
//		returnToBen.addSubSteps(standNextToBen2);
	}

	private void setupBobsBrother()
	{
		ben = runeliteObjectManager.createFakeNpc(this.toString(), client.getNpcDefinition(NpcID.DOCK_WORKER).getModels(), new WorldPoint(3228, 3204, 0), 808);
		ben.setName("Ben");
		ben.setFace(NpcID.DOCK_WORKER);
		ben.setExamine("Ben is Bob's brother, a member of the Dover family");
		ben.addTalkAction(runeliteObjectManager);
		ben.addExamineAction(runeliteObjectManager);

		QuestRequirement hasDoneCooksAssistant = new QuestRequirement(QuestHelperQuest.COOKS_ASSISTANT, QuestState.FINISHED);

		// Dialog
		RuneliteDialogStep benDialogPreQuest = ben.createDialogStepForNpc("Hop noob.");
		benDialogPreQuest.addContinueDialog(new RunelitePlayerDialogStep(client, "What? Also, what are you wearing?"))
			.addContinueDialog(ben.createDialogStepForNpc("Hop NOOB."));
		ben.addDialogTree(null, benDialogPreQuest);

		RuneliteDialogStep BenDialog = ben.createDialogStepForNpc("Hello, I'm Bob's brother Ben. How can I help you?", FaceAnimationIDs.FRIENDLY);
		BenDialog.addContinueDialog(new RunelitePlayerDialogStep(client, "I'm looking for a new axe but I'm having trouble finding exactly what I need.", FaceAnimationIDs.QUIZZICAL))
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
			.addContinueDialog(ben.createDialogStepForNpc("Maybe find a Master Smith to help you. I hear there's a new guy, a short little fella who's good with metals over at the local furnace.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(ben.createDialogStepForNpc("Until then, please stay away from this shop.", FaceAnimationIDs.ANNOYED_2).setStateProgression(talkedToBen.getSetter()));
		ben.addDialogTree(hasDoneCooksAssistant, BenDialog);



		RuneliteDialogStep BenDialog2 = ben.createDialogStepForNpc("Hey, not you again. I thought I told you to stay away.", FaceAnimationIDs.ANNOYED_2);
		BenDialog2.addContinueDialog(new RunelitePlayerDialogStep(client, "You said not to return until I found you a lump of Black metal to work with.", FaceAnimationIDs.CHATTY))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I spoke with the Master Smith you recommended and well, here I am, ready for my axe.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(ben.createDialogStepForNpc("Wow, I didn't expect you to ever return, never mind so fast. Let's have a look then.", FaceAnimationIDs.QUESTIONING))
			.addContinueDialog(new RuneliteDialogStep("", "You hand the lump of black metal to Ben.", -1, -1)).setStateProgression(handedInBlackLump.getSetter())
			.addContinueDialog(ben.createDialogStepForNpc("Ah yeah, this is some classic black metal alright, seen plenty of this stuff. Hard to work with.", FaceAnimationIDs.SHORT_LAUGH))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "You said you had no idea what Black metal was.", FaceAnimationIDs.BIG_LAUGH))
			.addContinueDialog(ben.createDialogStepForNpc("Right, listen here you little...", FaceAnimationIDs.ANNOYED_2))
			.addContinueDialog(new RuneliteDialogStep("", "Ben stomps away muttering under his breath...", -1, -1))
			.addContinueDialog(new RuneliteDialogStep("", "...returning a short while later carrying a Black thrownaxe.", -1, -1))
			.addContinueDialog(ben.createDialogStepForNpc("Here you go mate, one Black thrownaxe. Bespoke. One of a kind.", FaceAnimationIDs.CHATTY))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Ben, this is perfect. Thank you so much.", FaceAnimationIDs.FRIENDLY_2))
			.addContinueDialog(ben.createDialogStepForNpc("Hopefully you'll leave me alone now. </br>Good bye picks axes.", FaceAnimationIDs.WORRIED_SAD)).setStateProgression(retrievedBlackThrownaxe.getSetter());
		ben.addDialogTree(retrievedBlackLump, BenDialog2);



		RuneliteDialogStep BenDialog3 = ben.createDialogStepForNpc("Am I going to need some sort of restraining order?", FaceAnimationIDs.FRIENDLY);
		BenDialog3.addContinueDialog(new RunelitePlayerDialogStep(client, "Sorry to be a pain Ben, but I can't bring myself to use this axe. It's too precious.", FaceAnimationIDs.WORRIED_SAD))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Is there some way we could preserve it?", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(ben.createDialogStepForNpc("Yeah, I think we can do something for you.", FaceAnimationIDs.YES))
			.addContinueDialog(ben.createDialogStepForNpc("On the wall behind you we store our best axes, our finest pieces of work.", FaceAnimationIDs.NORMAL))


			.addContinueDialog(new RunelitePlayerDialogStep(client, "Ben, this is perfect. Thank you so much.", FaceAnimationIDs.FRIENDLY_2))
			.addContinueDialog(ben.createDialogStepForNpc("Hopefully you'll leave me alone now. </br>Good bye picks axes.", FaceAnimationIDs.WORRIED_SAD)).setStateProgression(retrievedBlackThrownaxe.getSetter());
		ben.addDialogTree(retrievedBlackThrownaxe, BenDialog3);









		RuneliteConfigSetter endQuest = new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "4");
		RuneliteDialogStep benGiveMetalLumpDialog = ben.createDialogStepForNpc("Have you got the cabbage?");
		benGiveMetalLumpDialog
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I have! Here you go, why do you need it?"))
			.addContinueDialog(ben.createDialogStepForNpc("Nice! Now let's sort out this crasher..."))
			.addContinueDialog(ben.createDialogStepForNpc("Oi noob, take this!"))
			.addContinueDialog(new RuneliteObjectDialogStep("Hatius Cosaintus", "What on earth?", NpcID.HATIUS_COSAINTUS).setStateProgression(endQuest));
		//ben.addDialogTree(pickedBlackLump, benGiveMetalLumpDialog);

	}



	private void setupThurgo()
	{
		for (int modelid : client.getNpcDefinition(NpcID.THURGO).getModels()){
			ModelData modelData = client.loadModelData(modelid);
			if (modelData == null)
				continue;
			modelData.recolor((short) 4550, (short) 5314);
		}
		// Thurgo
		thurgo = runeliteObjectManager.createFakeNpc(this.toString(), client.getNpcDefinition(NpcID.THURGO).getModels(), new WorldPoint(3224, 3257, 0), 101);

		thurgo.setName("Thurgo");
		thurgo.setFace(NpcID.THURGO);
		thurgo.setExamine("Dwarvish.");
		thurgo.addTalkAction(runeliteObjectManager);
		thurgo.addExamineAction(runeliteObjectManager);

		// Dialog
		RuneliteDialogStep thurgoDialogPreQuest = thurgo.createDialogStepForNpc("Hop noob.");
		thurgoDialogPreQuest.addContinueDialog(new RunelitePlayerDialogStep(client, "What? Also, what are you wearing?"))
			.addContinueDialog(thurgo.createDialogStepForNpc("Hop NOOB."));
		thurgo.addDialogTree(null, thurgoDialogPreQuest);

		RuneliteDialogStep thurgoDialog1 = thurgo.createDialogStepForNpc("Oh hello again picks axes, I hope I did a good job with that Faladian knight's sword.", FaceAnimationIDs.FRIENDLY);
		thurgoDialog1.addContinueDialog(new RunelitePlayerDialogStep(client, "Thurgo! What are you doing here?", FaceAnimationIDs.CHATTY))
			.addContinueDialog(thurgo.createDialogStepForNpc("Well I've heard rumours that more Imcandorian settlements have been discovered across the realm.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(thurgo.createDialogStepForNpc("I’ve spent all this time thinking I was the last EVER Imcando dwarf, and then it turns out there’s one living right around the corner.", FaceAnimationIDs.SAD))
			.addContinueDialog(thurgo.createDialogStepForNpc("I suppose I should get over meself and go say hello, but it’s been so long. I wouldn’t know what to say.", FaceAnimationIDs.SAD))
			.addContinueDialog(thurgo.createDialogStepForNpc("I thought sharing my culture's knowledge of metalworking with the humans might help bridge the gap.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(thurgo.createDialogStepForNpc("So I'm here teaching Smithing to the people of Lumbridge, in return for all the pies I can eat.", FaceAnimationIDs.YES))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "It's good to hear you're getting out more, but I did actually have a Smithing question for you.", FaceAnimationIDs.QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I'm looking to have new weapon made. The only issue is, I'd like it to be made from Black.", FaceAnimationIDs.QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I have a weapons smith willing to help me but I can't for the life of me figure out what Black actually is.", FaceAnimationIDs.WORRIED_SAD))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "And that's why I've come to you. Do you have any knowledge on the Black metal?", FaceAnimationIDs.FRIENDLY_QUESTIONING))
			.addContinueDialog(thurgo.createDialogStepForNpc("Ah, metal: a subject very close to my heart, and one which I could talk about for hours.", FaceAnimationIDs.LAUGHING))
			.addContinueDialog(thurgo.createDialogStepForNpc("I like metal almost as much as pie!", FaceAnimationIDs.SHORT_LAUGH))
			.addContinueDialog(thurgo.createDialogStepForNpc("White and Black metals are actually just coloured forms of steel, created by differing forging techniques.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(thurgo.createDialogStepForNpc("If you'd like, I could give you some raw Black metal that your weapons smith can work with.", FaceAnimationIDs.FRIENDLY_QUESTIONING))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Thurgo, that would be fantastic!", FaceAnimationIDs.LAUGHING))
			.addContinueDialog(thurgo.createDialogStepForNpc("Of course I'd need something to sweeten the deal.", FaceAnimationIDs.BIG_LAUGH))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "There it is. Would a redberry pie suffice?", FaceAnimationIDs.SHORT_LAUGH))
			.addContinueDialog(thurgo.createDialogStepForNpc("Ah, redberry pie: I like pie almost as much as metal!", FaceAnimationIDs.BIG_LAUGH))
			.addContinueDialog(thurgo.createDialogStepForNpc("Sometimes I wish I had my own infinite supply of redberries.", FaceAnimationIDs.SHORT_LAUGH))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I could help with that.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(thurgo.createDialogStepForNpc("Oh yeah, how are you going to help? Redberries don't grow on trees you know.", FaceAnimationIDs.SHORT_LAUGH))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Well funnily enough, they kinda do. I could plant a redberry bush in your garden if you'd like.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(thurgo.createDialogStepForNpc("picks axes mate, I'd love that! In return, I'll prepare some raw Black metal for your axe.", FaceAnimationIDs.QUIZZICAL))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "I'll be be back soon Thurgo.", talkedToThurgo.getSetter()));
		thurgo.addDialogTree(talkedToBen, thurgoDialog1);



		RuneliteDialogStep thurgoDialog2 = thurgo.createDialogStepForNpc("picks axes, how goes that redberry business?", FaceAnimationIDs.FRIENDLY_QUESTIONING);
		thurgoDialog2.addContinueDialog(new RunelitePlayerDialogStep(client, "You'll be happy to know that your redberry bush is ready and waiting for you at home.", FaceAnimationIDs.CHATTY))
			.addContinueDialog(thurgo.createDialogStepForNpc("Mate, you've warmed my steel heart. I'll get that Black metal ready for you.", FaceAnimationIDs.FRIENDLY))
			.addContinueDialog(new RuneliteDialogStep("", "Thurgo leaves a lump of Black metal slag outside the nearby furnace.", -1, -1))
			.addContinueDialog(new RunelitePlayerDialogStep(client, "Thanks Thurgo, you've really saved this episode of Picks Axes, the hit Youtube series from Skeldoor.", talkedToThurgo2.getSetter()))
			.addContinueDialog(thurgo.createDialogStepForNpc("Wait what?", FaceAnimationIDs.ANNOYED));
		thurgo.addDialogTree(plantedRedberryBush, thurgoDialog2);



//		RuneliteConfigSetter endQuest = new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "4");
//		RuneliteDialogStep hopleezGiveCabbageDialog = thurgo.createDialogStepForNpc("Have you got the cabbage?");
//		hopleezGiveCabbageDialog
//			.addContinueDialog(new RunelitePlayerDialogStep(client, "I have! Here you go, why do you need it?"))
//			.addContinueDialog(thurgo.createDialogStepForNpc("Nice! Now let's sort out this crasher..."))
//			.addContinueDialog(thurgo.createDialogStepForNpc("Oi noob, take this!"))
//			.addContinueDialog(new RuneliteObjectDialogStep("Hatius Cosaintus", "What on earth?", NpcID.HATIUS_COSAINTUS).setStateProgression(endQuest));
		//thurgo.addDialogTree(pickedBlackLump, hopleezGiveCabbageDialog);
	}


	private void setupRedberryPatch()
	{
		redberryBushPatch = runeliteObjectManager.createFakeObject(this.toString(), new int[]{1076}, new WorldPoint(2999, 3141, 0), -1);
		redberryBushPatch.setName("Bush patch");
		redberryBushPatch.setExamine("This earth is particularly fertile.");
		redberryBushPatch.addExamineAction(runeliteObjectManager);
		redberryBushPatch.setDisplayRequirement(talkedToThurgo);
		redberryBushPatch.addAction("Plant", new Consumer<MenuEntry>()
		{
			@Override
			public void accept(MenuEntry menuEntry)
			{
				new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "3").setConfigValue();
				client.getLocalPlayer().setAnimation(830);
				client.getLocalPlayer().setAnimationFrame(0);
			}
		});
	}

	private void setupRedberryBush()
	{
		redberryBush = runeliteObjectManager.createFakeObject(this.toString(), new int[]{ 7826,7767,7815}, new WorldPoint(2999, 3141, 0), -1);
		redberryBush.setName("Redberry bush");
		redberryBush.setExamine("Thurgo's redberry bush is full of tasty berries.");
		redberryBush.addExamineAction(runeliteObjectManager);
		redberryBush.setDisplayRequirement(displayRedberryBush);
		redberryBush.getRuneliteObject().setRadius(100);
	}

	private void setupCabbage()
	{
		// Old cabbage
		blackLump = runeliteObjectManager.createFakeItem(this.toString(), new int[]{ 48958 }, new WorldPoint(3227, 3255, 0), -1);
		blackLump.setScaledModel(new int[]{48958}, 150,20,150);
		blackLump.setName("Black lump");
		blackLump.setExamine("A heavy lump of raw Black metal.");
		blackLump.addExamineAction(runeliteObjectManager);
		blackLump.setDisplayRequirement(displayBlackLump);
		blackLump.addTakeAction(runeliteObjectManager, new RuneliteConfigSetter(configManager, getQuest().getPlayerQuests().getConfigValue(), "5"),
			"You pick up the lump of black metal.");

		blackLump.setObjectToRemove(new ReplacedObject(NullObjectID.NULL_37348, new WorldPoint(3227, 3255, 0)));
	}

	private void createRuneliteObjects()
	{
		setupBobsBrother();
		setupThurgo();
		setupRedberryPatch();
		setupRedberryBush();
		setupCabbage();
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
		PanelDetails helpingTheCousinSteps = new PanelDetails("Getting a black thrownaxeaxe", Arrays.asList(talkToBen, talkToThurgo, grabBlackOre));
		allSteps.add(helpingTheCousinSteps);

		return allSteps;
	}
}
