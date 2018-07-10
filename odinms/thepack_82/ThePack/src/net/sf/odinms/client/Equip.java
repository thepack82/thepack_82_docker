package net.sf.odinms.client;

import java.util.LinkedList;

public class Equip extends Item implements IEquip {

    private byte upgradeSlots;
    private byte level;
    private byte locked;
    private MapleJob job;
    private short str,  dex,  _int,  luk,  hp,  mp,  watk,  matk,  wdef,  mdef,  acc,  avoid,  hands,  speed,  jump;
    private int ringid;

    public Equip(int id, byte position) {
        super(id, position, (short) 1);
        this.ringid = -1;
    }

    public Equip(int id, byte position, int ringid) {
        super(id, position, (short) 1);
        this.ringid = ringid;
    }

    @Override
    public IItem copy() {
        Equip ret = new Equip(getItemId(), getPosition(), ringid);
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.locked = locked;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.log = new LinkedList<String>(log);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        return ret;
    }

    @Override
    public byte getType() {
        return IItem.EQUIP;
    }

    @Override
    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    @Override
    public byte getLocked() {
        return locked;
    }

    @Override
    public int getRingId() {
        return ringid;
    }

    @Override
    public short getStr() {
        return str;
    }

    @Override
    public short getDex() {
        return dex;
    }

    @Override
    public short getInt() {
        return _int;
    }

    @Override
    public short getLuk() {
        return luk;
    }

    @Override
    public short getHp() {
        return hp;
    }

    @Override
    public short getMp() {
        return mp;
    }

    @Override
    public short getWatk() {
        return watk;
    }

    @Override
    public short getMatk() {
        return matk;
    }

    @Override
    public short getWdef() {
        return wdef;
    }

    @Override
    public short getMdef() {
        return mdef;
    }

    @Override
    public short getAcc() {
        return acc;
    }

    @Override
    public short getAvoid() {
        return avoid;
    }

    @Override
    public short getHands() {
        return hands;
    }

    @Override
    public short getSpeed() {
        return speed;
    }

    @Override
    public short getJump() {
        return jump;
    }

    public MapleJob getJob() {
        return job;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setLocked(byte locked) {
        this.locked = locked;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setRingId(int ringId) {
        this.ringid = ringId;
    }
}