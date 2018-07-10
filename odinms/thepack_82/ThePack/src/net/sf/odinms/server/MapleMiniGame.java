package net.sf.odinms.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.server.maps.AbstractMapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class MapleMiniGame extends AbstractMapleMapObject {

    private MapleCharacter owner;
    private MapleCharacter visitor;
    private String GameType = null;
    private int[] piece = new int[225];
    private List<Integer> list4x3 = new ArrayList<Integer>();
    private List<Integer> list5x4 = new ArrayList<Integer>();
    private List<Integer> list6x5 = new ArrayList<Integer>();
    private List<MaplePlayerShopItem> items = new ArrayList<MaplePlayerShopItem>();
    private MapleCharacter slot1 = null, slot2 = null, slot3 = null;
    private String description;
    boolean ready = false;
    int loser = 1;
    int piecetype;
    int started = 0; // 0 = waiting, 1 = in progress
    int firstslot = 0;
    int visitorpoints = 0;
    int ownerpoints = 0;
    int matchestowin = 0;

    public MapleMiniGame(MapleCharacter owner, String description) {
        this.owner = owner;
        this.description = description;
    }

    public boolean hasFreeSlot() {
        return visitor == null;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner == c;
    }

    public void addVisitor(MapleCharacter challenger) {
        visitor = challenger;
        if (GameType == "omok") {
            this.getOwner().getClient().getSession().write(MaplePacketCreator.getMiniGameNewVisitor(challenger, 1));
            this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(owner, 2, 0));
        }
        if (GameType == "matchcard") {
            this.getOwner().getClient().getSession().write(MaplePacketCreator.getMatchCardNewVisitor(challenger, 1));
            this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(owner, 2, 0));
        }
    }

    public void removeVisitor(MapleCharacter challenger) {
        if (visitor == challenger) {
            visitor = null;
            this.getOwner().getClient().getSession().write(MaplePacketCreator.getMiniGameRemoveVisitor());
            if (GameType == "omok") {
                this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(owner, 1, 0));
            }
            if (GameType == "matchcard") {
                this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(owner, 1, 0));
            }
        }
    }

    public boolean isVisitor(MapleCharacter challenger) {
        return visitor == challenger;
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeItem(int item) {
        items.remove(item);
    }

    public void buy(MapleClient c, int item, short quantity) {
        if (isVisitor(c.getPlayer())) {
            MaplePlayerShopItem pItem = items.get(item);
            owner = this.getOwner();
            synchronized (c.getPlayer()) {
                IItem newItem = pItem.getItem().copy();
                newItem.setQuantity((short) (newItem.getQuantity() * quantity));
                c.getPlayer().gainMeso(-pItem.getPrice() * quantity, true);
                owner.gainMeso(pItem.getPrice() * quantity, true);
                MapleInventoryManipulator.addFromDrop(c, newItem, "");
                pItem.setBundles((short) (pItem.getBundles() - quantity));
            }
        }
    }

    public void broadcastToVisitor(MaplePacket packet) {
        if (visitor != null) {
            visitor.getClient().getSession().write(packet);
        }
    }

    public void removeVisitors() {
        if (visitor != null) {
            visitor.changeMap(visitor.getMap(), visitor.getPosition());
        }
    }

    public void setStarted(int type) {
        started = type;
    }

    public int getStarted() {
        return started;
    }

    public void setFirstSlot(int type) {
        firstslot = type;
    }

    public int getFirstSlot() {
        return firstslot;
    }

    public void setOwnerPoints() {
        ownerpoints++;
        if ((ownerpoints + visitorpoints) == matchestowin) {
            if (ownerpoints == visitorpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMatchCardTie(this));
            }
            if (ownerpoints > visitorpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMatchCardOwnerWin(this));
            }
            if (visitorpoints > ownerpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMatchCardVisitorWin(this));
            }
        }
    }

    public int getOwnerPoints() {
        return ownerpoints;
    }

    public void setVisitorPoints() {
        visitorpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints > visitorpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMiniGameOwnerWin(this));
            }
            if (visitorpoints > ownerpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMiniGameVisitorWin(this));
            }
            if (ownerpoints == visitorpoints) {
                ownerpoints = 0;
                visitorpoints = 0;
                this.broadcast(MaplePacketCreator.getMiniGameTie(this));
            }
        }
    }

    public int getVisitorPoints() {
        return ownerpoints;
    }

    public void setMatchesToWin(int type) {
        matchestowin = type;
    }

    public void setPieceType(int type) {
        piecetype = type;
    }

    public int getPieceType() {
        return piecetype;
    }

    public void setGameType(String game) {
        if (GameType.equals("matchcard")) {
            if (matchestowin == 6) {
                for (int i = 0; i < 6; i++) {
                    list4x3.add(i);
                    list4x3.add(i);
                }
            }
            if (matchestowin == 10) {
                for (int i = 0; i < 10; i++) {
                    list5x4.add(i);
                    list5x4.add(i);
                }
            }
            if (matchestowin == 15) {
                for (int i = 0; i < 15; i++) {
                    list6x5.add(i);
                    list6x5.add(i);
                }
            }
        }
    }

    public String getGameType() {
        return GameType;
    }

    public void shuffleList() {
        if (matchestowin == 6) {
            Collections.shuffle(list4x3);
        }
        if (matchestowin == 10) {
            Collections.shuffle(list5x4);
        }
        if (matchestowin == 15) {
            Collections.shuffle(list6x5);
        }
    }

    public int getCardId(int slot) {
        int cardid = 0;
        if (matchestowin == 6) {
            cardid = list4x3.get(slot - 1);
        }
        if (matchestowin == 10) {
            cardid = list5x4.get(slot - 1);
        }
        if (matchestowin == 15) {
            cardid = list6x5.get(slot - 1);
        }
        return cardid;
    }

    public int getMatchesToWin() {
        return matchestowin;
    }

    public void setLoser(int type) {
        loser = type;
    }

    public int getLoser() {
        return loser;
    }

    public void broadcast(MaplePacket packet) {
        if (owner.getClient() != null && owner.getClient().getSession() != null) {
            owner.getClient().getSession().write(packet);
        }
        broadcastToVisitor(packet);
    }

    public void chat(MapleClient c, String chat) {
        broadcast(MaplePacketCreator.getPlayerShopChat(c.getPlayer(), chat, isOwner(c.getPlayer())));
    }

    public void sendOmok(MapleClient c, int type) {
        c.getSession().write(MaplePacketCreator.getMiniGame(c, this, isOwner(c.getPlayer()), type));
    }

    public void sendMatchCard(MapleClient c, int type) {
        c.getSession().write(MaplePacketCreator.getMatchCard(c, this, isOwner(c.getPlayer()), type));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter getVisitor() {
        return visitor;
    }

    public MapleCharacter getSlot1() {
        return slot1;
    }

    public MapleCharacter getSlot2() {
        return slot2;
    }

    public MapleCharacter getSlot3() {
        return slot3;
    }

    public void setReady() {
        if (ready = false) {
            ready = true;
        } else {
            ready = false;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void setPiece(int move1, int move2, int type, MapleCharacter chr) {
        int slot = ((move2 * 15) + (move1 + 1));
        if (piece[slot] == 0) {
            piece[slot] = type;
            this.broadcast(MaplePacketCreator.getMiniGameMoveOmok(this, move1, move2, type));
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 11; x++) {
                    if (searchCombo(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.broadcast(MaplePacketCreator.getMiniGameOwnerWin(this));
                            this.setStarted(0);
                            this.setLoser(0);
                        } else {
                            this.broadcast(MaplePacketCreator.getMiniGameVisitorWin(this));
                            this.setStarted(0);
                            this.setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = ((y2 * 15) + (x2 + 1));
                                piece[slot2] = 0;

                            }
                        }
                    }
                }
            }
            for (int y = 0; y < 15; y++) {
                for (int x = 4; x < 15; x++) {
                    if (searchCombo2(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.broadcast(MaplePacketCreator.getMiniGameOwnerWin(this));
                            this.setStarted(0);
                            this.setLoser(0);
                        } else {
                            this.broadcast(MaplePacketCreator.getMiniGameVisitorWin(this));
                            this.setStarted(0);
                            this.setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = ((y2 * 15) + (x2 + 1));
                                piece[slot2] = 0;

                            }
                        }
                    }
                }
            }
        }

    }

    public boolean searchCombo(int x, int y, int type) {
        boolean winner = false;
        int slot = ((y * 15) + (x + 1));
        if (piece[slot] == type) {
            if (piece[slot + 1] == type) {
                if (piece[slot + 2] == type) {
                    if (piece[slot + 3] == type) {
                        if (piece[slot + 4] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 16] == type) {
                if (piece[slot + 32] == type) {
                    if (piece[slot + 48] == type) {
                        if (piece[slot + 64] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 15] == type) {
                if (piece[slot + 30] == type) {
                    if (piece[slot + 45] == type) {
                        if (piece[slot + 60] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        return winner;
    }

    public boolean searchCombo2(int x, int y, int type) {
        boolean winner = false;
        int slot = ((y * 15) + (x + 1));
        if (piece[slot] == type) {
            if (piece[slot + 15] == type) {
                if (piece[slot + 30] == type) {
                    if (piece[slot + 45] == type) {
                        if (piece[slot + 60] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 14] == type) {
                if (piece[slot + 28] == type) {
                    if (piece[slot + 42] == type) {
                        if (piece[slot + 56] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        return winner;
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
}

