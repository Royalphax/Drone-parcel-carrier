package fr.therence.geotraceur;

import java.util.Random;

/**
 * Enumération des différentes directions que le drône peut emprunter.
 * La direction NONE indique que le drône est arrivé à destination.
 * 
 * @author Therence F.
 */
public enum EnumDirection {

	NONE(-1, "NONE"),
	UP(0, "DOWN"),
	DOWN(1, "UP"),
	RIGHT(2, "LEFT"),
	LEFT(3, "RIGHT");
	
	private int id;
	private String reverse;
	
	private EnumDirection(int id, String reverse) {
		this.id = id;
		this.reverse = reverse;
	}
	
	/**
	 * Permet de connaitre l'identifiant de la direction instanciée.
	 * Non utilisé dans notre cas, mais généralement présent dans un enum.
	 * 
	 * @return l'identifiant de la direction instanciée.
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Permet de connaitre l'inverse de la direction instanciée.
	 * 
	 * @return l'inverse de la direction instanciée.
	 */
	public EnumDirection getReverse() {
		return valueOf(this.reverse);
	}
	
	/**
	 * Permet d'avoir une direction aléatoire parmi celles mentionnées. 
	 * Même si, dans le réel, un choix de direction ne pourrait pas s'effectuer aléatoirement.
	 * 
	 * @param directions directions mentionnées.
	 * @return une direction aléatoire parmi celles mentionnées.
	 */
	public static EnumDirection getRandomDirectionBetween(EnumDirection... directions) {
		int rdm = new Random().nextInt(directions.length);
		return directions[rdm];
	}
}
