package fr.therence.geotraceur;

import java.awt.Color;

/**
 * Enum�ration des diff�rentes plages d'altitude que le dr�ne peut atteindre.
 * 
 * @author Therence F.
 */
public enum EnumAltitude {

	VERY_LOW("Tr�s bas", 0, 10, Color.RED),
	LOW("Bas", 10, 20, Color.ORANGE),
	CRUISE("Croisi�re", 20, 30, Color.YELLOW),
	CRUISE_UP("Croisi�re haut", 30, 40, Color.GREEN),
	HIGH("Haut", 40, 50, Color.CYAN),
	VERY_HIGH("Tr�s haut", 50, 60, Color.BLUE),
	TOP("Maximale", 60, 100, Color.MAGENTA);
	
	private String name;
	private int lowHeight;
	private int highHeight;
	private Color color;
	
	private EnumAltitude(String name, int lowHeight, int highHeight, Color color) {
		this.name = name;
		this.lowHeight = lowHeight;
		this.highHeight = highHeight;
		this.color = color;
	}
	
	/**
	 * Permet de conna�tre le nom de d�signation de l'altitude.
	 * 
	 * @return nom de l'altitude.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Permet de conna�tre l'altitude minimale pour �tre consid�r� dans cette plage.
	 * 
	 * @return valeur de la hauteur minimale de la plage.
	 */
	public int getLowHeight() {
		return this.lowHeight;
	}
	
	/**
	 * Permet de conna�tre l'altitude maximale pour �tre consid�r� dans cette plage.
	 * 
	 * @return valeur de la hauteur maximale de la plage.
	 */
	public int getHighHeight() {
		return this.highHeight;
	}
	
	/**
	 * Permet de conna�tre la couleur repr�sentative de la plage.
	 * 
	 * @return couleur associ�e � la plage.
	 */
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Permet de conna�tre l'altitude qui se trouve au dessus de celle instanci�e.
	 * 
	 * @return altitude sup�rieure.
	 */
	public EnumAltitude up() {
		return getAltitude(this.highHeight + 10);
	}
	
	/**
	 * Permet de conna�tre l'altitude qui se trouve en dessous de celle instanci�e.
	 * 
	 * @return altitude inf�rieure.
	 */
	public EnumAltitude down() {
		return getAltitude(this.highHeight - 10);
	}
	
	/**
	 * Permet de conna�tre dans quelle plage d'altitude on se situe selon l'altitude � laquelle nous sommes.
	 * 
	 * @param height altitude � laquelle on se situe.
	 * @return .
	 */
	public static EnumAltitude getAltitude(int height) {
		for (EnumAltitude alt : values())
			if (height > alt.getLowHeight() && height <= alt.getHighHeight())
				return alt;
		return null;
	}
}
