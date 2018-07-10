package net.sf.odinms.tools;

public class KoreanDateUtil {

    private final static int ITEM_YEAR2000 = -1085019342;
    private final static long REAL_YEAR2000 = 946681229830l;
    private final static int QUEST_UNIXAGE = 27111908;
    private final static long FT_UT_OFFSET = 116444736000000000L;

    private KoreanDateUtil() {
    }

    public static long getTempBanTimestamp(long realTimestamp) {
        return ((realTimestamp * 10000) + FT_UT_OFFSET);
    }

    public static int getItemTimestamp(long realTimestamp) {
        int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60);
        return (int) (time * 35.762787) + ITEM_YEAR2000;
    }

    public static int getQuestTimestamp(long realTimestamp) {
        int time = (int) (realTimestamp / 1000 / 60);
        return (int) (time * 0.1396987) + QUEST_UNIXAGE;
    }
}
