/* Mr. Do
	Mu Lung: Mu Lung (250000000)
	
	"Refining" NPC:
 * makes special medicine including quest item 4031554 - Peach Tree Herb Pouch if player has 4161030 - Book on Herbal Medicine
 * Accepts Mu Lung etc. items for Dr. Do's Marble [4001124]
 * Accepts Dr. Do's Marble + ores for 100% weapon scrolls (alternate code provided to make 50/50 100%/60% or 3-way equal chance of 10%/60%/100% instead)
 */

importPackage(net.sf.odinms.client);

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {		
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        var selStr = "I am a man of many talents. Let me what you'd like to do.#b"
        var options = new Array("Make a medicine","Make a scroll","Donate medicine ingredients");
        for (var i = 0; i < options.length; i++){
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
			
        cm.sendSimple(selStr);
    }
    else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { 
            if(!cm.haveItem(4161030)){ //does not have the required item
                cm.sendNext("If you want to make a medicine, you'll first have to study the formulas. There's nothing more dangerous than having someone who is not knowledgeable on medicine formulas make one.")
                cm.dispose();
            }
            else { //make a medicine
                var itemSet = new Array(2022145,2022146,2022147,2022148,2022149,2022150,2050004,4031554);
                var selStr = "What kind of medicine are you interested in making?#b";
                for (var i = 0; i < itemSet.length; i++){
                    selStr += "\r\n#L" + i + "# #i" + itemSet[i] + "##z" + itemSet[i] + "##l";
                }
                cm.sendSimple(selStr);
            }
        }
        else if (selectedType == 1){ //make a scroll
            var itemSet = new Array(2043000,2043100,2043200,2043300,2043700,2043800,2044000,2044100,2044200,2044300,2044400,2044500,2044600,2044700);
            var selStr = "What kind of scrolls are you interested in making?#b";
            for (var i = 0; i < itemSet.length; i++){
                selStr += "\r\n#L" + i + "# #z" + itemSet[i] + "##l";
            }
            cm.sendSimple(selStr);
        }
        else if (selectedType == 2){ //donate etc. item
            var itemSet = new Array(4000276,4000277,4000278,4000279,4000280,4000291,4000292,4000286,4000287,4000293,4000294,4000298,4000284,4000288,4000285,4000282,4000295,4000289,4000296,4000297);
            var selStr = "So you wish to donate some medicine ingredients? This is great news! Donations will be accepted in units of #b100#k. The donator will recieve marbles that enables one to make a scroll. Which of these would you like to donate?#b";
            for (var i = 0; i < itemSet.length; i++){
                selStr += "\r\n#L" + i + "# #z" + itemSet[i] + "##l";
            }
            cm.sendSimple(selStr);
        }
    }
    else if (status == 2 && mode == 1) {
        var prompt;
		
        selectedItem = selection;
        if (selectedType == 0){ //make medicine
            var itemSet = new Array(2022145,2022146,2022147,2022148,2022149,2022150,2050004,4031554);
            var matSet = new Array(2022116,2022116,new Array(4000281,4000293),new Array(4000276,2002005),new Array(4000288,4000292),4000295,new Array(2022131,2022132),new Array(4000286,4000287,4000293));
            var matQtySet = new Array(3,3,new Array(10,10),new Array(20,2),new Array(20,20),10,new Array(1,1),new Array(20,20,20));
            var costSet = new Array(0,0,910,950,1940,600,0,0);
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
			
            prompt = "You want to make a #b#t" + item + "##k? In order to make that, you'll need..."
        }
        else if (selectedType == 1){ //make scroll
            var itemSet = new Array(2043000,2043100,2043200,2043300,2043700,2043800,2044000,2044100,2044200,2044300,2044400,2044500,2044600,2044700);
            var matSet = new Array(new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4020000),new Array(4001124,4020005),new Array(4001124,4020005),
            new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010001),new Array(4001124,4010002),new Array(4001124,4010002),new Array(4001124,4020000));
            var matQtySet = new Array(new Array(1,20,25,3,5),new Array(1,20,25,5,3));
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = new Array(100,10); //always 100 marbles and 10 ores
            cost = 0; //always 0 cost
			
            prompt = "You want to make a #b#t" + item + "##k? In order to make that, you'll need..."
        }
        else if (selectedType == 2){ //donate item
            var matSet = new Array(4000276,4000277,4000278,4000279,4000280,4000291,4000292,4000286,4000287,4000293,4000294,4000298,4000284,4000288,4000285,4000282,4000295,4000289,4000296,4000297);
            item = 4001124; //always rewards Dr. Do's Marble
            mats = matSet[selectedItem];
            matQty = 100; //always 100 of the item
            cost = 0; //always 0 cost
			
            prompt = "Are you sure you want to donate #b100 #t" + mats + "##k?"
        }

        if (selectedType != 2){
            if (mats instanceof Array){
                for(var i = 0; i < mats.length; i++){
                    prompt += "\r\n#i"+mats[i]+"# " + matQty[i] + " #t" + mats[i] + "#";
                }
            }
            else {
                prompt += "\r\n#i"+mats+"# " + matQty + " #t" + mats + "#";
            }
			
            if (cost > 0)
                prompt += "\r\n#i4031138# " + cost + " meso";
        }
		
        cm.sendYesNo(prompt);
    }
    else if (status == 3 && mode == 1) {
        var complete = true;
        if (selectedType == 2)
            qty = getNumMarbles(mats);
        else
            qty = 1;
		
        if (cm.getMeso() < cost)
        {
            cm.sendOk("Sorry, but the fee cannot be ignored in this case, despite your help.")
        }
        else
        {
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                {
                    if (!cm.haveItem(mats[i], matQty[i]))
                    {
                        complete = false;
                    }					
                }
            }
            else {
                if (!cm.haveItem(mats, matQty))
                {
                    complete = false;
                }
            }
        }
			
        if (!complete) 
            cm.sendOk("Are you sure you have the right ingredients? If not, there will be... deadly issues with the result...");
        else {
            if (!cm.canHold(item)) {
                cm.sendOk("Please make sure you have room in your inventory for the final product.")
            }
            else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++){
                        cm.gainItem(mats[i], -matQty[i]);
                    }
                }
                else
                    cm.gainItem(mats, -matQty);
		
                if (cost > 0)
                    cm.gainMeso(-cost);
                //Note: Make a Scroll only creates 100% scrolls. The following segments of code will change that result.
                //if (selectedType == 1) {
                /*// 50/50 chance of 100%/60%:
						item += Math.floor(Math.random() * 2);
                 */
                /*// 1/3 chance each of 100%/60%/10%:
						item += Math.floor(Math.random() * 3);
                 */
                //Do not go above +2 or you may end up creating GM scrolls!
                //}
                cm.gainItem(item, qty);
                cm.sendOk("I wish you the best of luck on your journey.");
            }
        }
        cm.dispose();
    }
}

function getNumMarbles(donateID){
    var offset = donateID - 4000276; //acorn = 4000276 = lowest ID
    var numMarbles; 
	
    switch (offset){
        case 0: //acorn
            numMarbles = 6;
            break;
        case 1: //thimble
            numMarbles = 6;
            break;
        case 2: //needle pouch
            numMarbles = 6;
            break;
        case 3: //necki flower
            numMarbles = 7;
            break;
        case 4: //necki swimming cap
            numMarbles = 7;
            break;
        case 15: //broken piece of pot
            numMarbles = 8;
            break;
        case 16: //ginseng-boiled water
            numMarbles = 8;
            break;
        case 10: //straw doll
            numMarbles = 9;
            break;
        case 11: //wooden doll
            numMarbles = 9;
            break;
        case 17: //bellflower
            numMarbles = 10;
            break;
        case 18: //100-year-old bellflower
            numMarbles = 10;
            break;
        case 22: //old paper
            numMarbles = 11;
            break;
        case 8: //yellow belt
            numMarbles = 11;
            break;
        case 12: //broken deer horn
            numMarbles = 12;
            break;
        case 9: //red belt
            numMarbles = 12;
            break;
        case 6: //peach seed
            numMarbles = 13;
            break;
        case 19: //mr. alli's leather
            numMarbles = 13;
            break;
        case 13: //cat doll
            numMarbles = 14;
            break;
        case 20: //mark of the pirate
            numMarbles = 15;
            break;
        case 21: //captain's hat
            numMarbles = 15;
            break;
    }
	
    return numMarbles;
}