package net.sf.odinms.net;

import net.sf.odinms.net.handler.KeepAliveHandler;
import net.sf.odinms.net.handler.LoginRequiringNoOpHandler;
import net.sf.odinms.net.channel.handler.*;
import net.sf.odinms.net.login.handler.*;
import org.slf4j.LoggerFactory;

public final class PacketProcessor {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PacketProcessor.class);

    public enum Mode {

        LOGINSERVER,
        CHANNELSERVER
    };
    private static PacketProcessor instance;
    private MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvPacketOpcode op : RecvPacketOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public MaplePacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        MaplePacketHandler handler = handlers[packetId];
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void registerHandler(RecvPacketOpcode code, MaplePacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            log.error("Check your Recv Packet Opcodes - Something is wrong");
        }
    }

    public synchronized static PacketProcessor getProcessor(Mode mode) {
        if (instance == null) {
            instance = new PacketProcessor();
            instance.reset(mode);
        }
        return instance;
    }

    public void reset(Mode mode) {
        handlers = new MaplePacketHandler[handlers.length];
        registerHandler(RecvPacketOpcode.PONG, new KeepAliveHandler());
        if (mode == Mode.LOGINSERVER) {
            registerHandler(RecvPacketOpcode.AFTER_LOGIN, new AfterLoginHandler());
            registerHandler(RecvPacketOpcode.SERVERLIST_REREQUEST, new ServerlistRequestHandler());
            registerHandler(RecvPacketOpcode.CHARLIST_REQUEST, new CharlistRequestHandler());
            registerHandler(RecvPacketOpcode.CHAR_SELECT, new CharSelectedHandler());
            registerHandler(RecvPacketOpcode.LOGIN_PASSWORD, new LoginPasswordHandler());
            registerHandler(RecvPacketOpcode.RELOG, new RelogRequestHandler());
            registerHandler(RecvPacketOpcode.SERVERLIST_REQUEST, new ServerlistRequestHandler());
            registerHandler(RecvPacketOpcode.SERVERSTATUS_REQUEST, new ServerStatusRequestHandler());
            registerHandler(RecvPacketOpcode.CHECK_CHAR_NAME, new CheckCharNameHandler());
            registerHandler(RecvPacketOpcode.CREATE_CHAR, new CreateCharHandler());
            registerHandler(RecvPacketOpcode.DELETE_CHAR, new DeleteCharHandler());
            registerHandler(RecvPacketOpcode.VIEW_ALL_CHAR, new ViewCharHandler());
            registerHandler(RecvPacketOpcode.PICK_ALL_CHAR, new PickCharHandler());
        } else if (mode == Mode.CHANNELSERVER) {
            registerHandler(RecvPacketOpcode.CHANGE_CHANNEL, new ChangeChannelHandler());
            registerHandler(RecvPacketOpcode.STRANGE_DATA, LoginRequiringNoOpHandler.getInstance());
            registerHandler(RecvPacketOpcode.GENERAL_CHAT, new GeneralchatHandler());
            registerHandler(RecvPacketOpcode.WHISPER, new WhisperHandler());
            registerHandler(RecvPacketOpcode.NPC_TALK, new NPCTalkHandler());
            registerHandler(RecvPacketOpcode.NPC_TALK_MORE, new NPCMoreTalkHandler());
            registerHandler(RecvPacketOpcode.QUEST_ACTION, new QuestActionHandler());
            registerHandler(RecvPacketOpcode.NPC_SHOP, new NPCShopHandler());
            registerHandler(RecvPacketOpcode.ITEM_SORT, new ItemSortHandler());
            registerHandler(RecvPacketOpcode.ITEM_MOVE, new ItemMoveHandler());
            registerHandler(RecvPacketOpcode.MESO_DROP, new MesoDropHandler());
            registerHandler(RecvPacketOpcode.PLAYER_LOGGEDIN, new PlayerLoggedinHandler());
            registerHandler(RecvPacketOpcode.CHANGE_MAP, new ChangeMapHandler());
            registerHandler(RecvPacketOpcode.MOVE_LIFE, new MoveLifeHandler());
            registerHandler(RecvPacketOpcode.CLOSE_RANGE_ATTACK, new CloseRangeDamageHandler());
            registerHandler(RecvPacketOpcode.RANGED_ATTACK, new RangedAttackHandler());
            registerHandler(RecvPacketOpcode.MAGIC_ATTACK, new MagicDamageHandler());
            registerHandler(RecvPacketOpcode.TAKE_DAMAGE, new TakeDamageHandler());
            registerHandler(RecvPacketOpcode.MOVE_PLAYER, new MovePlayerHandler());
            registerHandler(RecvPacketOpcode.USE_CASH_ITEM, new UseCashItemHandler());
            registerHandler(RecvPacketOpcode.USE_ITEM, new UseItemHandler());
            registerHandler(RecvPacketOpcode.USE_RETURN_SCROLL, new UseItemHandler());
            registerHandler(RecvPacketOpcode.USE_UPGRADE_SCROLL, new ScrollHandler());
            registerHandler(RecvPacketOpcode.USE_SUMMON_BAG, new UseSummonBag());
            registerHandler(RecvPacketOpcode.FACE_EXPRESSION, new FaceExpressionHandler());
            registerHandler(RecvPacketOpcode.HEAL_OVER_TIME, new HealOvertimeHandler());
            registerHandler(RecvPacketOpcode.ITEM_PICKUP, new ItemPickupHandler());
            registerHandler(RecvPacketOpcode.CHAR_INFO_REQUEST, new CharInfoRequestHandler());
            registerHandler(RecvPacketOpcode.SPECIAL_MOVE, new SpecialMoveHandler());
            registerHandler(RecvPacketOpcode.USE_INNER_PORTAL, new InnerPortalHandler());
            registerHandler(RecvPacketOpcode.CANCEL_BUFF, new CancelBuffHandler());
            registerHandler(RecvPacketOpcode.CANCEL_ITEM_EFFECT, new CancelItemEffectHandler());
            registerHandler(RecvPacketOpcode.PLAYER_INTERACTION, new PlayerInteractionHandler());
            registerHandler(RecvPacketOpcode.DISTRIBUTE_AP, new DistributeAPHandler());
            registerHandler(RecvPacketOpcode.DISTRIBUTE_SP, new DistributeSPHandler());
            registerHandler(RecvPacketOpcode.CHANGE_KEYMAP, new KeymapChangeHandler());
            registerHandler(RecvPacketOpcode.CHANGE_MAP_SPECIAL, new ChangeMapSpecialHandler());
            registerHandler(RecvPacketOpcode.STORAGE, new StorageHandler());
            registerHandler(RecvPacketOpcode.GIVE_FAME, new GiveFameHandler());
            registerHandler(RecvPacketOpcode.PARTY_OPERATION, new PartyOperationHandler());
            registerHandler(RecvPacketOpcode.DENY_PARTY_REQUEST, new DenyPartyRequestHandler());
            registerHandler(RecvPacketOpcode.PARTYCHAT, new PartychatHandler());
            registerHandler(RecvPacketOpcode.USE_DOOR, new DoorHandler());
            registerHandler(RecvPacketOpcode.ENTER_MTS, new EnterMTSHandler());
            registerHandler(RecvPacketOpcode.ENTER_CASH_SHOP, new EnterCashShopHandler());
            registerHandler(RecvPacketOpcode.DAMAGE_SUMMON, new DamageSummonHandler());
            registerHandler(RecvPacketOpcode.MOVE_SUMMON, new MoveSummonHandler());
            registerHandler(RecvPacketOpcode.SUMMON_ATTACK, new SummonDamageHandler());
            registerHandler(RecvPacketOpcode.BUDDYLIST_MODIFY, new BuddylistModifyHandler());
            registerHandler(RecvPacketOpcode.USE_ITEMEFFECT, new UseItemEffectHandler());
            registerHandler(RecvPacketOpcode.USE_CHAIR, new UseChairHandler());
            registerHandler(RecvPacketOpcode.CANCEL_CHAIR, new CancelChairHandler());
            registerHandler(RecvPacketOpcode.DAMAGE_REACTOR, new ReactorHitHandler());
            registerHandler(RecvPacketOpcode.GUILD_OPERATION, new GuildOperationHandler());
            registerHandler(RecvPacketOpcode.DENY_GUILD_REQUEST, new DenyGuildRequestHandler());
            registerHandler(RecvPacketOpcode.BBS_OPERATION, new BBSOperationHandler());
            registerHandler(RecvPacketOpcode.SKILL_EFFECT, new SkillEffectHandler());
            registerHandler(RecvPacketOpcode.MESSENGER, new MessengerHandler());
            registerHandler(RecvPacketOpcode.NPC_ACTION, new NPCAnimation());
            registerHandler(RecvPacketOpcode.TOUCHING_CS, new TouchingCashShopHandler());
            registerHandler(RecvPacketOpcode.BUY_CS_ITEM, new BuyCSItemHandler());
            registerHandler(RecvPacketOpcode.COUPON_CODE, new CouponCodeHandler());
            registerHandler(RecvPacketOpcode.SPAWN_PET, new SpawnPetHandler());
            registerHandler(RecvPacketOpcode.MOVE_PET, new MovePetHandler());
            registerHandler(RecvPacketOpcode.PET_CHAT, new PetChatHandler());
            registerHandler(RecvPacketOpcode.PET_COMMAND, new PetCommandHandler());
            registerHandler(RecvPacketOpcode.PET_FOOD, new PetFoodHandler());
            registerHandler(RecvPacketOpcode.PET_LOOT, new PetLootHandler());
            registerHandler(RecvPacketOpcode.AUTO_AGGRO, new AutoAggroHandler());
            registerHandler(RecvPacketOpcode.MONSTER_BOMB, new MonsterBombHandler());
            registerHandler(RecvPacketOpcode.CANCEL_DEBUFF, new CancelDebuffHandler());
            registerHandler(RecvPacketOpcode.USE_SKILL_BOOK, new SkillBookHandler());
            registerHandler(RecvPacketOpcode.SKILL_MACRO, new SkillMacroHandler());
            registerHandler(RecvPacketOpcode.NOTE_ACTION, new NoteActionHandler());
            registerHandler(RecvPacketOpcode.MAPLETV, new MapleTVHandler());
            registerHandler(RecvPacketOpcode.PLAYER_UPDATE, new PlayerUpdateHandler());
            registerHandler(RecvPacketOpcode.USE_CATCH_ITEM, new UseCatchItemHandler());
            registerHandler(RecvPacketOpcode.CLOSE_CHALKBOARD, new CloseChalkboardHandler());
            registerHandler(RecvPacketOpcode.DUEY_ACTION, new DueyActionHandler());
            registerHandler(RecvPacketOpcode.USE_MOUNT_FOOD, new UseMountFoodHandler());
            registerHandler(RecvPacketOpcode.MTS_OP, new MTSHandler());
            registerHandler(RecvPacketOpcode.RING_ACTION, new RingActionHandler());
            registerHandler(RecvPacketOpcode.SPOUSECHAT, new SpouseChatHandler());
            registerHandler(RecvPacketOpcode.PET_AUTO_POT, new PetAutoPotHandler());
//            registerHandler(RecvPacketOpcode.MONSTER_CARNIVAL, new MonsterCarnivalHandler());
        } else {
            throw new RuntimeException("Unknown packet processor mode");
        }
    }
}
