/**
  Bell (NLC Ticket Usher and Platform Usher)
  in Maps: 600010001,600010002,103000100,600010004
  Final by aexr
**/

var status = 0;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0) {
        if (cm.getChar().getMapId() == 103000100) 
            cm.sendYesNo("Do you wish to visit New Leaf City on the continent of Masteria?");
        else if (cm.getChar().getMapId() == 600010001)
            cm.sendYesNo("Do you wish to visit Kerning City on the continent of Masteria?");
        else if (cm.getChar().getMapId() == 600010004)
            cm.sendYesNo("Do you wish to go to back to Kerning City?");
        else if (cm.getChar().getMapId() == 600010002)
            cm.sendYesNo("Do you wish to go to back to New Leaf City?");
        status++;
    } else {
        if ((status == 1 && type == 1 && selection == -1 && mode == 0) || mode == -1) {
            cm.dispose();
        } else {
            if (status == 1) {
                cm.sendNext ("Alright, see you next time. Take care.");
                status++
            } else if (status == 2) {
                if (cm.getChar().getMapId() == 103000100) {
                    cm.warp(600010004, 0);
                    cm.dispose();
                } else if (cm.getChar().getMapId() == 600010001) {
                    cm.warp(600010002, 0);
                    cm.dispose();
                } else if (cm.getChar().getMapId() == 600010004) {
                    cm.warp(103000100, 0);
                    cm.dispose();
                } else if (cm.getChar().getMapId() == 600010002) {
                    cm.warp(600010001, 0);
                    cm.dispose();
                }
            }
        }
    }
}
