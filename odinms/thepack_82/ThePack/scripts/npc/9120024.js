function start() {
    cm.sendSimple("Hello, I sell pirate weapons and acessories. What do you want?\r\n#L0#Knuckles#l\r\n#L1#Guns#l\r\n#L2#Bullets#l\r\n#L3#Hats#l\r\n#L4#Overalls#l\r\n#L5#Gloves#l\r\n#L6#Shoes#l\r\n#L7#Scrolls#l");
}

function action(mode, type, selection) {
    if (mode>0)
        cm.openShop(5000+selection);//test
    cm.dispose();
}