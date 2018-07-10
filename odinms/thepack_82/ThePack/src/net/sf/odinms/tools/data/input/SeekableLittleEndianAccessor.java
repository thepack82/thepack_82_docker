package net.sf.odinms.tools.data.input;

public interface SeekableLittleEndianAccessor extends LittleEndianAccessor {

    void seek(long offset);

    long getPosition();
}
