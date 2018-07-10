package net.sf.odinms.server;

/**
 *
 * @author Patrick/PurpleMadness
 */

public enum MapleDueyActions {

	C_SEND_ITEM(0x02),
	C_CLOSE_DUEY(0x07),
	S_RECEIVED_PACKAGE_MSG(0x1B),
	C_CLAIM_RECEIVED_PACKAGE(0x04),
	S_SUCCESSFULLY_RECEIVED(0x17),
	S_SUCCESSFULLY_SENT(0x18),
	S_ERROR_SENDING(0x12),
	S_OPEN_DUEY(0x08);

	final byte code;

	private MapleDueyActions(int code) {
		this.code = (byte) code;
	}

	public byte getCode() {
		return code;
	}
}
