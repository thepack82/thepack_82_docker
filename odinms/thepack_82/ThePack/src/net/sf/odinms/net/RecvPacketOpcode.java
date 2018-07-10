package net.sf.odinms.net;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public enum RecvPacketOpcode implements WritableIntValueHolder {

    PONG,
    AFTER_LOGIN,
    MTS_TAB,
    SERVERLIST_REQUEST,
    SERVERLIST_REREQUEST,
    CHARLIST_REQUEST,
    CHAR_SELECT,
    CHECK_CHAR_NAME,
    CREATE_CHAR,
    DELETE_CHAR,
    LOGIN_PASSWORD,
    RELOG,
    SERVERSTATUS_REQUEST,
    VIEW_ALL_CHAR,
    PICK_ALL_CHAR,
    PET_AUTO_POT,
    CHANGE_CHANNEL,
    CHAR_INFO_REQUEST,
    CLOSE_RANGE_ATTACK,
    RANGED_ATTACK,
    MAGIC_ATTACK,
    FACE_EXPRESSION,
    HEAL_OVER_TIME,
    ITEM_SORT,
    ITEM_MOVE,
    ITEM_PICKUP,
    CHANGE_MAP,
    MESO_DROP,
    MOVE_LIFE,
    MOVE_PLAYER,
    NPC_SHOP,
    NPC_TALK,
    NPC_TALK_MORE,
    PLAYER_LOGGEDIN,
    QUEST_ACTION,
    TAKE_DAMAGE,
    USE_CASH_ITEM,
    USE_ITEM,
    USE_RETURN_SCROLL,
    USE_UPGRADE_SCROLL,
    USE_SUMMON_BAG,
    GENERAL_CHAT,
    WHISPER,
    SPECIAL_MOVE,
    USE_INNER_PORTAL,
    CANCEL_BUFF,
    PLAYER_INTERACTION,
    CANCEL_ITEM_EFFECT,
    DISTRIBUTE_AP,
    DISTRIBUTE_SP,
    CHANGE_KEYMAP,
    CHANGE_MAP_SPECIAL,
    STORAGE,
    STRANGE_DATA,
    GIVE_FAME,
    PARTY_OPERATION,
    DENY_PARTY_REQUEST,
    PARTYCHAT,
    USE_DOOR,
    ENTER_MTS,
    ENTER_CASH_SHOP,
    DAMAGE_SUMMON,
    MOVE_SUMMON,
    SUMMON_ATTACK,
    BUDDYLIST_MODIFY,
    USE_ITEMEFFECT,
    USE_CHAIR,
    SKILL_EFFECT,
    CANCEL_CHAIR,
    DAMAGE_REACTOR,
    GUILD_OPERATION,
    DENY_GUILD_REQUEST,
    BBS_OPERATION,
    MESSENGER,
    NPC_ACTION,
    TOUCHING_CS,
    BUY_CS_ITEM,
    COUPON_CODE,
    SPAWN_PET,
    MOVE_PET,
    PET_CHAT,
    PET_COMMAND,
    PET_FOOD,
    PET_LOOT,
    AUTO_AGGRO,
    MONSTER_BOMB,
    CANCEL_DEBUFF,
    USE_SKILL_BOOK,
    SKILL_MACRO,
    NOTE_ACTION,
    MAPLETV,
    PLAYER_UPDATE,
    USE_CATCH_ITEM,
    CLOSE_CHALKBOARD,
    RING_ACTION,
    DUEY_ACTION,
    MONSTER_CARNIVAL,
    MTS_OP,
    USE_MOUNT_FOOD,
    TROCK_ADD_MAP,
    SPOUSECHAT,;
    private int code = -2;

    public void setValue(int code) {
        this.code = code;
    }

    @Override
    public int getValue() {
        return code;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(System.getProperty("net.sf.odinms.recvops"));
        props.load(fis);
        fis.close();
        return props;
    }


    static {
        try {
            ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recvops", e);
        }
    }
}
