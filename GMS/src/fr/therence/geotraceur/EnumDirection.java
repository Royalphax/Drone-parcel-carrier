package fr.therence.geotraceur;

import java.util.Random;

/**
 * Enum�ration des diff�rentes directions que le dr�ne peut emprunter.
 * La direction NONE indique que le dr�ne est arriv� � destination.
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
	 * Permet de connaitre l'identifiant de la direction instanci�e.
	 * Non utilis� dans notre cas, mais g�n�ralement pr�sent dans un enum.
	 * 
	 * @return l'identifiant de la direction instanci�e.
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Permet de connaitre l'inverse de la direction instanci�e.
	 * 
	 * @return l'inverse de la direction instanci�e.
	 */
	public EnumDirection getReverse() {
		return valueOf(this.reverse);
	}
	
	/**
	 * Permet d'avoir une direction al�atoire parmi celles mentionn�es. 
	 * M�me si, dans le r�el, un choix de direction ne pourrait pas s'effectuer al�atoirement.
	 * 
	 * @param directions directions mentionn�es.
	 * @return une direction al�atoire parmi celles mentionn�es.
	 */
	public static EnumDirection getRandomDirectionBetween(EnumDirection... directions) {
		int rdm = new Random().nextInt(directions.length);
		return directions[rdm];
	}
}
