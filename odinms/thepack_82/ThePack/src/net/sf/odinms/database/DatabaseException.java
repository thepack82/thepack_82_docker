package net.sf.odinms.database;

/**
 *
 * @author Matze
 */
public class DatabaseException extends RuntimeException {
	private static final long serialVersionUID = -420103154764822555L;

	public DatabaseException() {
	}
	
	public DatabaseException(String msg) {
		super(msg);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
