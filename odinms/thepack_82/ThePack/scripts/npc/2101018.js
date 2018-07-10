/** 
    Cesar 
    Ariant Coliseum 
**/ 

importPackage(net.sf.odinms.server.maps); 

var status = 0; 

function start() { 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (status == 0) { 
        cm.sendYesNo("Would you like to go to #bAriant Coliseum#k? You must be level 20 to 30 to participate."); 
        status++; 
    } else { 
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) { 
            cm.dispose(); 
        } else { 
            if (status == 1) { 
                if(cm.getChar().getLevel() >= 20 && cm.getChar().getLevel() < 31 || cm.getChar().isGM()) { 
                    cm.getPlayer().saveLocation(SavedLocationType.FREE_MARKET); 
                    cm.warp(980010000, 3); 
                    cm.dispose(); 
                } else { 
                    cm.sendOk("You're not between level 20 and 30. Sorry, you may not participate."); 
                    cm.dispose(); 
                } 
            } 
        } 
    } 
}  