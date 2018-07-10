importPackage(net.sf.odinms.client);

var setupTask;

function init() {
    hpInterval();
}

function hpInterval() {
    var cal = java.util.Calendar.getInstance();
    cal.set(java.util.Calendar.SECOND, 5);
    var nextTime = cal.getTimeInMillis();
    while (nextTime <= java.lang.System.currentTimeMillis()) {
        nextTime += 5000;
    }
    setupTask = em.scheduleAtTimestamp("decrease", nextTime);
}

function cancelSchedule() {
    setupTask.cancel(true);
}

function decrease() {
    hpInterval();
    var iter = em.getChannelServer().getPlayerStorage().getAllCharacters().iterator();

    while (iter.hasNext()) {
            var pl = iter.next();
            if (pl.getHp() > 0 && !pl.inCS() && pl.getMap().getHPdecrease() > 0)
            {
                pl.setHp(pl.getHp()-pl.getMap().getHPdecrease());
                pl.updateSingleStat(MapleStat.HP, pl.getHp());
            }
    }
}