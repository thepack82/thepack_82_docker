/* 
 * @Author TheRamon
 * 
 * Sharen III's Soul, Sharenian: Sharen III's Grave (990000700)
 * 
 * Guild Quest - end of stage 4
 */

var status = 0;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            cm.dispose();
        if (status == 0) {
            if (cm.getPlayer().getEventInstance().getProperty("leader").equals(cm.getPlayer().getName())) {
                if (cm.getPlayer().getEventInstance().getProperty("stage4clear") != null && cm.getPlayer().getEventInstance().getProperty("stage4clear").equals("true"))
                {
                    cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
                    cm.dispose();
                }
                else {
                    var prev = cm.getPlayer().getEventInstance().setProperty("stage4clear","true",true);
                    if (prev == null) {
                        cm.sendNext("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. This old man will now pave the way for you to finish the quest." + mode);
                    }
                    else {//if not null, was set before, and Gp already gained
                        cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
                        cm.dispose();
                    }
                }
            }
            else
            {
                if (cm.getPlayer().getEventInstance().getProperty("stage4clear") != null && cm.getPlayer().getEventInstance().getProperty("stage4clear").equals("true"))
                    cm.sendOk("After what I thought would be an immortal sleep, I have finally found someone that will save Sharenian. I can truly rest in peace now.");
                else
                    cm.sendOk("I need the leader of your party to speak with me, nobody else.");
                cm.dispose();
            }
        }
        else if (status == 1) {
            cm.getGuild().gainGP(30);
            cm.getPlayer().getMap().getReactorByName("ghostgate").hitReactor(cm.getC());
            cm.showEffect("quest/party/clear");
            cm.playSound("Party1/Clear");
            cm.dispose();
        }
    }
}