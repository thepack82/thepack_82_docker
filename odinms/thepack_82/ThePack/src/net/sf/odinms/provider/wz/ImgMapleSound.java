package net.sf.odinms.provider.wz;

public class ImgMapleSound {

    private int dataLenght,  offset;

    public ImgMapleSound(int dataLength, int offset) {
        this.dataLenght = dataLength;
        this.offset = offset;
    }

    public int getDataLength() {
        return dataLenght;
    }

    public int getOffset() {
        return offset;
    }
}
