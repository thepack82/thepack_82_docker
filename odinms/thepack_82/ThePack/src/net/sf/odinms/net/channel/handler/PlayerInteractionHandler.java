package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleMiniGame;
import net.sf.odinms.server.MaplePlayerShop;
import net.sf.odinms.server.MaplePlayerShopItem;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class PlayerInteractionHandler extends AbstractMaplePacketHandler {

    private enum Action {

        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        CHAT(6),
        EXIT(0xA),
        OPEN(0xB),
        SET_ITEMS(0xE),
        SET_MESO(0xF),
        CONFIRM(0x10),
        ADD_ITEM(0x13),
        BUY(0x14),
        REMOVE_ITEM(0x18), //slot(byte) bundlecount(short)
        REQUEST_TIE(44),
        ANSWER_TIE(45),
        GIVE_UP(46),
        EXIT_AFTER_GAME(50),
        CANCEL_EXIT(51),
        READY(52),
        UN_READY(53),
        MOVE_OMOK(58),
        START(55),
        SKIP(57),
        SELECT_CARD(62);
        final byte code;

        private Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == Action.CREATE.getCode()) {
            byte createType = slea.readByte();
            if (createType == 3) { // trade
                MapleTrade.startTrade(c.getPlayer());
            } else if (createType == 4) { // shop
                String desc = slea.readMapleAsciiString();
                @SuppressWarnings("unused")
                byte uk1 = slea.readByte(); // maybe public/private 00
                @SuppressWarnings("unused")
                short uk2 = slea.readShort(); // 01 00
                @SuppressWarnings("unused")
                int uk3 = slea.readInt(); // 20 6E 4E
                MaplePlayerShop shop = new MaplePlayerShop(c.getPlayer(), desc);
                c.getPlayer().setPlayerShop(shop);
                c.getPlayer().getMap().addMapObject(shop);
                shop.sendShop(c);
            } else if (createType == 1) { // omok mini game
                String desc = slea.readMapleAsciiString();
                @SuppressWarnings("unused")
                int uk = slea.readByte(); // 20 6E 4E
                int type = slea.readByte(); // 20 6E 4E
                MapleMiniGame game = new MapleMiniGame(c.getPlayer(), desc);
                c.getPlayer().setMiniGame(game);
                game.setPieceType(type);
                game.setGameType("omok");
                c.getPlayer().getMap().addMapObject(game);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(c.getPlayer(), 1, 0));
                game.sendOmok(c, type);

            } else if (createType == 2) { // matchcard
                String desc = slea.readMapleAsciiString();
                @SuppressWarnings("unused")
                int uk = slea.readByte(); // 20 6E 4E
                int type = slea.readByte(); // 20 6E 4E
                MapleMiniGame game = new MapleMiniGame(c.getPlayer(), desc);
                game.setPieceType(type);
                if (type == 0) {
                    game.setMatchesToWin(6);
                }
                if (type == 1) {
                    game.setMatchesToWin(10);
                }
                if (type == 2) {
                    game.setMatchesToWin(15);
                }
                game.setGameType("matchcard");
                c.getPlayer().setMiniGame(game);
                c.getPlayer().getMap().addMapObject(game);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(c.getPlayer(), 1, 0));
                game.sendMatchCard(c, type);

            }
        } else if (mode == Action.INVITE.getCode()) {
            int otherPlayer = slea.readInt();
            MapleCharacter otherChar = c.getPlayer().getMap().getCharacterById(otherPlayer);
            MapleTrade.inviteTrade(c.getPlayer(), otherChar);
        } else if (mode == Action.DECLINE.getCode()) {
            MapleTrade.declineTrade(c.getPlayer());
        } else if (mode == Action.VISIT.getCode()) {
            if (c.getPlayer().getTrade() != null && c.getPlayer().getTrade().getPartner() != null) {
                MapleTrade.visitTrade(c.getPlayer(), c.getPlayer().getTrade().getPartner().getChr());
            } else {
                int oid = slea.readInt();
                MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
                if (ob instanceof MaplePlayerShop) {
                    MaplePlayerShop shop = (MaplePlayerShop) ob;
                    if (shop.hasFreeSlot() && !shop.isVisitor(c.getPlayer())) {
                        shop.addVisitor(c.getPlayer());
                        c.getPlayer().setPlayerShop(shop);
                        shop.sendShop(c);
                    }
                } else if (ob instanceof MapleMiniGame) {
                    MapleMiniGame game = (MapleMiniGame) ob;
                    if (game.hasFreeSlot() && !game.isVisitor(c.getPlayer())) {
                        if (game.getGameType().equals("omok")) {
                            game.addVisitor(c.getPlayer());
                            c.getPlayer().setMiniGame(game);
                            game.sendOmok(c, game.getPieceType());
                        }
                        if (game.getGameType().equals("matchcard")) {
                            game.addVisitor(c.getPlayer());
                            c.getPlayer().setMiniGame(game);
                            game.sendMatchCard(c, game.getPieceType());
                        }
                    } else {
                        c.getPlayer().getClient().getSession().write(MaplePacketCreator.getMiniGameFull());
                    }
                }
            }
        } else if (mode == Action.CHAT.getCode()) { // chat lol
            if (c.getPlayer().getTrade() != null) {
                c.getPlayer().getTrade().chat(slea.readMapleAsciiString());
            } else if (c.getPlayer().getPlayerShop() != null) { //mini game
                MaplePlayerShop shop = c.getPlayer().getPlayerShop();
                if (shop != null) {
                    shop.chat(c, slea.readMapleAsciiString());
                }
            } else if (c.getPlayer().getMiniGame() != null) {
                MapleMiniGame game = c.getPlayer().getMiniGame();
                if (game != null) {
                    game.chat(c, slea.readMapleAsciiString());
                }
            }
        } else if (mode == Action.EXIT.getCode()) {
            if (c.getPlayer().getTrade() != null) {
                MapleTrade.cancelTrade(c.getPlayer());
            } else {
                MaplePlayerShop shop = c.getPlayer().getPlayerShop();
                MapleMiniGame game = c.getPlayer().getMiniGame();
                if (shop != null) {
                    c.getPlayer().setPlayerShop(null);
                    if (shop.isOwner(c.getPlayer())) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeCharBox(c.getPlayer()));
                        for (MaplePlayerShopItem item : shop.getItems()) {
                            IItem iItem = item.getItem().copy();
                            iItem.setQuantity((short) (item.getBundles() * iItem.getQuantity()));
                            StringBuilder logInfo = new StringBuilder("Returning items not sold in ");
                            logInfo.append(c.getPlayer().getName());
                            logInfo.append("'s shop");
                            MapleInventoryManipulator.addFromDrop(c, iItem, logInfo.toString(), false);
                        }
                    } else {
                        shop.removeVisitor(c.getPlayer());
                    }
                } else if (game != null) {
                    c.getPlayer().setMiniGame(null);
                    if (game.isOwner(c.getPlayer())) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeCharBox(c.getPlayer()));
                        game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose((byte) 0));
                    } else {
                        game.removeVisitor(c.getPlayer());
                    }
                }
            }
        } else if (mode == Action.OPEN.getCode()) {
            MaplePlayerShop shop = c.getPlayer().getPlayerShop();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.addCharBox(c.getPlayer(), 4));
                MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(5140000), 5140000, 1, true, false);
                @SuppressWarnings("unused")
                byte uk1 = slea.readByte(); // 01
            }
        } else if (mode == Action.READY.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            game.broadcast(MaplePacketCreator.getMiniGameReady(game));
        } else if (mode == Action.UN_READY.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            game.broadcast(MaplePacketCreator.getMiniGameUnReady(game));
        } else if (mode == Action.START.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            if (game.getGameType().equals("omok")) {
                game.broadcast(MaplePacketCreator.getMiniGameStart(game, game.getLoser()));
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(game.getOwner(), 2, 1));
                game.setStarted(1);
            }
            if (game.getGameType().equals("matchcard")) {
                game.shuffleList();
                game.broadcast(MaplePacketCreator.getMatchCardStart(game, game.getLoser()));
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(game.getOwner(), 2, 1));
                game.setStarted(1);
            }
        } else if (mode == Action.GIVE_UP.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            if (game.getGameType().equals("omok")) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMiniGameOwnerForfeit(game));
                } else {
                    game.broadcast(MaplePacketCreator.getMiniGameVisitorForfeit(game));
                }
            }
            if (game.getGameType().equals("matchcard")) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMatchCardVisitorWin(game));
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardOwnerWin(game));
                }
            }
        } else if (mode == Action.REQUEST_TIE.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcastToVisitor(MaplePacketCreator.getMiniGameRequestTie(game));
            } else {
                game.getOwner().getClient().getSession().write(MaplePacketCreator.getMiniGameRequestTie(game));
            }
        } else if (mode == Action.ANSWER_TIE.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            int type = slea.readByte();
            if (game.getGameType().equals("omok")) {
                game.broadcast(MaplePacketCreator.getMiniGameTie(game));
            }
            if (game.getGameType().equals("matchcard")) {
                game.broadcast(MaplePacketCreator.getMatchCardTie(game));
            }
        } else if (mode == Action.SKIP.getCode()) {
            MapleMiniGame game = c.getPlayer().getMiniGame();
            if (game.isOwner(c.getPlayer())) {
                game.broadcast(MaplePacketCreator.getMiniGameSkipOwner(game));
            } else {
                game.broadcast(MaplePacketCreator.getMiniGameSkipVisitor(game));
            }
        } else if (mode == Action.MOVE_OMOK.getCode()) {
            int x = slea.readInt(); // x point
            int y = slea.readInt(); // y point
            int type = slea.readByte(); // piece ( 1 or 2; Owner has one piece, visitor has another, it switches every game.)
            c.getPlayer().getMiniGame().setPiece(x, y, type, c.getPlayer());
        } else if (mode == Action.SELECT_CARD.getCode()) {
            int turn = slea.readByte(); // 1st turn = 1; 2nd turn = 0
            int slot = slea.readByte(); // slot
            MapleMiniGame game = c.getPlayer().getMiniGame();
            int firstslot = game.getFirstSlot();
            if (turn == 1) {
                game.setFirstSlot(slot);
                if (game.isOwner(c.getPlayer())) {
                    game.broadcastToVisitor(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                } else {
                    game.getOwner().getClient().getSession().write(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, turn));
                }
            } else if ((game.getCardId(firstslot + 1)) == (game.getCardId(slot + 1))) {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 2));
                    game.setOwnerPoints();
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 3));
                    game.setVisitorPoints();
                }
            } else {
                if (game.isOwner(c.getPlayer())) {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 0));
                } else {
                    game.broadcast(MaplePacketCreator.getMatchCardSelect(game, turn, slot, firstslot, 1));
                }
            }
        } else if (mode == Action.SET_MESO.getCode()) {
            c.getPlayer().getTrade().setMeso(slea.readInt());
        } else if (mode == Action.SET_ITEMS.getCode()) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
            IItem item = c.getPlayer().getInventory(ivType).getItem((byte) slea.readShort());
            short quantity = slea.readShort();
            byte targetSlot = slea.readByte();
            if (c.getPlayer().getTrade() != null) {
                if ((quantity <= item.getQuantity() && quantity >= 0) || ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                    if (!c.getChannelServer().allowUndroppablesDrop() && ii.isDropRestricted(item.getItemId())) { // ensure that undroppable items do not make it to the trade window
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                    IItem tradeItem = item.copy();
                    if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                        tradeItem.setQuantity(item.getQuantity());
                        MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), item.getQuantity(), true);
                    } else {
                        tradeItem.setQuantity(quantity);
                        MapleInventoryManipulator.removeFromSlot(c, ivType, item.getPosition(), quantity, true);
                    }
                    tradeItem.setPosition(targetSlot);
                    c.getPlayer().getTrade().addItem(tradeItem);
                    return;
                }
            }
        } else if (mode == Action.CONFIRM.getCode()) {
            MapleTrade.completeTrade(c.getPlayer());
        } else if (mode == Action.ADD_ITEM.getCode()) {
            MaplePlayerShop shop = c.getPlayer().getPlayerShop();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                byte slot = (byte) slea.readShort();
                short bundles = slea.readShort();
                short perBundle = slea.readShort();
                int price = slea.readInt();
                IItem ivItem = c.getPlayer().getInventory(type).getItem(slot);
                if (ivItem != null && ivItem.getQuantity() >= bundles * perBundle) {
                    IItem sellItem = ivItem.copy();
                    sellItem.setQuantity(perBundle);
                    MaplePlayerShopItem item = new MaplePlayerShopItem(shop, sellItem, bundles, price);
                    shop.addItem(item);
                    MapleInventoryManipulator.removeFromSlot(c, type, slot, (short) (bundles * perBundle), true);
                    c.getSession().write(MaplePacketCreator.getPlayerShopItemUpdate(shop));
                }
            }
        } else if (mode == Action.REMOVE_ITEM.getCode()) {
            MaplePlayerShop shop = c.getPlayer().getPlayerShop();
            if (shop != null && shop.isOwner(c.getPlayer())) {
                int slot = slea.readShort();
                MaplePlayerShopItem item = shop.getItems().get(slot);
                IItem ivItem = item.getItem().copy();
                shop.removeItem(slot);
                ivItem.setQuantity((short) (item.getBundles() * ivItem.getQuantity()));
                StringBuilder logInfo = new StringBuilder("Taken out from player shop by ");
                logInfo.append(c.getPlayer().getName());
                MapleInventoryManipulator.addFromDrop(c, ivItem, logInfo.toString(), false);
                c.getSession().write(MaplePacketCreator.getPlayerShopItemUpdate(shop));
            }
        } else if (mode == Action.BUY.getCode()) {
            MaplePlayerShop shop = c.getPlayer().getPlayerShop();
            if (shop != null && shop.isVisitor(c.getPlayer())) {
                int item = slea.readByte();
                short quantity = slea.readShort();
                shop.buy(c, item, quantity);
                StringBuilder logInfo = new StringBuilder("Taken out from player shop by ");
                logInfo.append(c.getPlayer().getName());
                shop.broadcast(MaplePacketCreator.getPlayerShopItemUpdate(shop));
            }
        }
    }
}