package net.sf.odinms.client;

/**
 * @author Matze
 */

public class InventoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InventoryException() {
		super();
	}

	public InventoryException(String msg) {
		super(msg);
	}
}