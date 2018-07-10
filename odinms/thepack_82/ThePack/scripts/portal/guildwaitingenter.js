/* @Author Lerk
 * 
 * Guild Quest Waiting Room - Entry Portal (map 990000000)
 */

function enter(pi) {
        if (pi.getPlayer().getEventInstance() == null) {
                pi.warp(101030104);
                return true;
        }
        else {
                if (pi.getPlayer().getEventInstance().getProperty("canEnter").equals("false")) {
                        pi.warp(990000100);
                        return true;
                }
                else { //cannot proceed while allies can still enter
                        pi.playerMessage("The portal is not open yet.");
                        return false;
                }
        }
}