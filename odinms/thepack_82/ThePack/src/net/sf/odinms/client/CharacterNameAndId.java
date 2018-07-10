package net.sf.odinms.client;

public class CharacterNameAndId {
	private int id;
	private String name;

	public CharacterNameAndId(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}