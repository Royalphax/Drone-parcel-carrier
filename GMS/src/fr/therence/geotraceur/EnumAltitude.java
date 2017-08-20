package fr.therence.geotraceur;

import java.awt.Color;

/**
 * Enumération des différentes plages d'altitude que le drône peut atteindre.
 * 
 * @author Therence F.
 */
public enum EnumAltitude {

	VERY_LOW("Très bas", 0, 10, Color.RED),
	LOW("Bas", 10, 20, Color.ORANGE),
	CRUISE("Croisière", 20, 30, Color.YELLOW),
	CRUISE_UP("Croisière haut", 30, 40, Color.GREEN),
	HIGH("Haut", 40, 50, Color.CYAN),
	VERY_HIGH("Très haut", 50, 60, Color.BLUE),
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
	 * Permet de connaître le nom de désignation de l'altitude.
	 * 
	 * @return nom de l'altitude.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Permet de connaître l'altitude minimale pour être considéré dans cette plage.
	 * 
	 * @return valeur de la hauteur minimale de la plage.
	 */
	public int getLowHeight() {
		return this.lowHeight;
	}
	
	/**
	 * Permet de connaître l'altitude maximale pour être considéré dans cette plage.
	 * 
	 * @return valeur de la hauteur maximale de la plage.
	 */
	public int getHighHeight() {
		return this.highHeight;
	}
	
	/**
	 * Permet de connaître la couleur représentative de la plage.
	 * 
	 * @return couleur associée à la plage.
	 */
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Permet de connaître l'altitude qui se trouve au dessus de celle instanciée.
	 * 
	 * @return altitude supérieure.
	 */
	public EnumAltitude up() {
		return getAltitude(this.highHeight + 10);
	}
	
	/**
	 * Permet de connaître l'altitude qui se trouve en dessous de celle instanciée.
	 * 
	 * @return altitude inférieure.
	 */
	public EnumAltitude down() {
		return getAltitude(this.highHeight - 10);
	}
	
	/**
	 * Permet de connaître dans quelle plage d'altitude on se situe selon l'altitude à laquelle nous sommes.
	 * 
	 * @param height altitude à laquelle on se situe.
	 * @return .
	 */
	public static EnumAltitude getAltitude(int height) {
		for (EnumAltitude alt : values())
			if (height > alt.getLowHeight() && height <= alt.getHighHeight())
				return alt;
		return null;
	}
}
