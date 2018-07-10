package net.sf.odinms.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.tools.StringUtil;

/**
 *
 * @author Lerk
 */
public class CashItemFactory {
	private static Map<Integer, Integer> snLookup = new HashMap<Integer,Integer>();
	private static Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
	private static MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
	private static MapleData commodities = data.getData(StringUtil.getLeftPaddedStr("Commodity.img", '0', 11));
	
	public static CashItemInfo getItem(int sn) {
		CashItemInfo stats = itemStats.get(sn);
		if (stats == null) {
			int cid = getCommodityFromSN(sn);
			
			int itemId = MapleDataTool.getIntConvert(cid + "/ItemId",commodities);
			int count = MapleDataTool.getIntConvert(cid + "/Count",commodities,1);
			int price = MapleDataTool.getIntConvert(cid + "/Price",commodities,0);
			
			stats = new CashItemInfo(itemId, count, price);
			
			itemStats.put(sn, stats);
		}
		
		return stats;
	}
	
	private static int getCommodityFromSN(int sn) {
		int cid;
		
		if (snLookup.get(sn) == null) {
			int curr = snLookup.size() - 1;
			int currSN = 0;
			if (curr == -1) {
				curr = 0;
				currSN = MapleDataTool.getIntConvert("0/SN",commodities);
				snLookup.put(currSN, curr);
				
			}
			for (int i = snLookup.size() - 1; currSN != sn; i++) {
				curr = i;
				currSN = MapleDataTool.getIntConvert(curr + "/SN",commodities);
				snLookup.put(currSN, curr);
			}
			cid = curr;
		}
		else {
			cid = snLookup.get(sn);
		}
		return cid;
	}
}