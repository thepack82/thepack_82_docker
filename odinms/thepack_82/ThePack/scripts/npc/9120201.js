/* Konpei (9120201)
* Bain Armory Version
*/

function start() {
    if (cm.getPlayerCount(801040100) == 0) {
        cm.resetMap(801040100);
    }
    cm.warp(801040100);
    cm.dispose();
} 
