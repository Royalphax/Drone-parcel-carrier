package fr.therence.gms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Cette class permet de s�parer l'execution de l'algorithme pour dessiner le
 * chemin du fonctionnement principal de l'application. C'est � dire de cr�er
 * une nouvelle t�che s�par�e de la principale.
 * 
 * @author Therence F.
 */
public class PathRunnable implements Runnable {

	private final float xMax;
	private final float zMax;
	private final float res;
	private final Window window;
	private final Graphics graphics;
	private boolean obstacle = false;
	private boolean pause = false;
	private boolean stop = false;
	private EnumAltitude altitude = EnumAltitude.VERY_LOW;

	/**
	 * @param xMax coordonn�e X du point de destination.
	 * @param zMax coordonn�e Z du point de destination.
	 * @param res r�solution durant le parcours.
	 * @param window fen�tre d'execution de l'algorithme de dessin.
	 */
	public PathRunnable(float xMax, float zMax, float res, Window window) {
		this.xMax = xMax;
		this.zMax = zMax;
		this.res = res;
		this.window = window;
		this.graphics = window.getGraphics();
	}

	/**
	 * Fonctionnement principal de l'algorithme de d�placement.
	 */
	@Override
	public void run() {

		int centerX = window.getWidth() / 2;
		int centerZ = window.getHeight() / 2 + 35;
		float x = xMax;
		float z = zMax;

		float waitingTime = res * 250;

		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setStroke(new BasicStroke(5));
		graphics.setColor(altitude.getColor());

		int resInt = Math.round(res * 30);
		long time = System.currentTimeMillis();
		while ((!plusOrLessEquals(x, 0, res, true) || !plusOrLessEquals(z, 0, res, true)) && !stop) {
			if (pause)
				continue;
			window.setAltitude(altitude);
			
			if (System.currentTimeMillis() - time >= waitingTime) {
				time = System.currentTimeMillis();

				float absX = Math.abs(x);
				float absZ = Math.abs(z);

				int deltaZ = (int) ((zMax - z) * 30);
				int deltaX = (int) ((xMax - x) * 30);

				EnumDirection dir = EnumDirection.NONE;

				if (x > 0) {
					if (z > 0) {
						if (absX >= absZ) {
							dir = EnumDirection.RIGHT;
						} else if (absX < absZ) {
							dir = EnumDirection.UP;
						}
					} else if (z < 0) {
						if (absX >= absZ) {
							dir = EnumDirection.RIGHT;
						} else if (absX < absZ) {
							dir = EnumDirection.DOWN;
						}
					} else {
						dir = EnumDirection.RIGHT;
					}
				} else if (x < 0) {
					if (z > 0) {
						if (absX >= absZ) {
							dir = EnumDirection.LEFT;
						} else if (absX < absZ) {
							dir = EnumDirection.UP;
						}
					} else if (z < 0) {
						if (absX >= absZ) {
							dir = EnumDirection.LEFT;
						} else if (absX < absZ) {
							dir = EnumDirection.DOWN;
						}
					} else {
						dir = EnumDirection.LEFT;
					}
				} else {
					if (z > 0) {
						dir = EnumDirection.UP;
					} else if (z < 0) {
						dir = EnumDirection.DOWN;
					}
				}
				if (isMovingBack(dir, x, z) && !obstacle) {
					break;
				} else if (obstacle) {
					this.obstacle = false;
					if (isOnForbiddenDiagonal(x, z)) {
						if (altitude == EnumAltitude.TOP)
							break;
						altitude = altitude.up();
						drawObstacle(dir, centerX, centerZ, deltaX, deltaZ, resInt);
					} else {
						drawObstacle(dir, centerX, centerZ, deltaX, deltaZ, resInt);
						dir = skirtObstacle(dir, x, z);
					}
					window.updateObstacleButton();
				}
				switch (dir) {
				case UP:
					z -= res;
					break;
				case DOWN:
					z += res;
					break;
				case LEFT:
					x += res;
					break;
				case RIGHT:
					x -= res;
					break;
				case NONE:
					break;
				}
				drawLine(dir, centerX, centerZ, deltaX, deltaZ, resInt);
				if (altitude == EnumAltitude.VERY_LOW || altitude == EnumAltitude.LOW) {
					if (altitude == EnumAltitude.TOP)
						break;
					altitude = altitude.up();
				}
			}
		}
		window.finishedDrawing();
	}
	
	/**
	 * Permet de pr�voir si le prochain mouvement du dr�ne sera un �loignement du point de destination.
	 * 
	 * @param dir direction pr�vu lors du prochain mouvement.
	 * @param x position du dr�ne sur l'axe x.
	 * @param z position du dr�ne sur l'axe z.
	 * @return Renvoie si oui ou non le prochain mouvement conduira le dr�ne � s'�loigner de son objectif.
	 */
	public boolean isMovingBack(EnumDirection dir, float x, float z) {
		float xCopy = x;
		float zCopy = z;
		switch (dir) {
		case UP:
			zCopy -= res;
			break;
		case DOWN:
			zCopy += res;
			break;
		case LEFT:
			xCopy += res;
			break;
		case RIGHT:
			xCopy -= res;
			break;
		case NONE:
			break;
		}
		return (Math.abs(xCopy) > Math.abs(x) || Math.abs(zCopy) > Math.abs(z));
	}
	
	/**
	 * Permet de savoir si l'�vitement par le c�t� est plus rentable que l'�vitement par les airs (en augmentant l'altitude).
	 * 
	 * @param x position du dr�ne sur l'axe x.
	 * @param z position du dr�ne sur l'axe z.
	 * @return Renvoie si le dr�ne se trouve sur la diagonale interdite appartenant � son rep�re.
	 */
	public boolean isOnForbiddenDiagonal(float x, float z) {
		if (plusOrLessEquals(xMax, 0, res, true) || plusOrLessEquals(zMax, 0, res, true))
			return true;
		if (!plusOrLessEquals(Math.abs(x), Math.abs(z), res, false))
			return false;
		if ((xMax > 0 && zMax < 0) || (xMax < 0 && zMax > 0)) {
			if ((x < 0 && z < 0) || (x > 0 && z > 0))
				return true;
		}
		if ((xMax > 0 && zMax > 0) || (xMax < 0 && zMax < 0)) {
			if ((x > 0 && z < 0) || (x < 0 && z > 0))
				return true;
		}
		return false;
	}
	
	/**
	 * Cette fonction retourne la direction qu'il faut emprunter dans le cas ou un obstacle est pr�sent sur le chemin.
	 * 
	 * @param dir direction de l'obstacles.
	 * @param x position du dr�ne sur l'axe x.
	 * @param z position du dr�ne sur l'axe z.
	 * @return Renvoie la direction qu'il faut suivre afin d'�viter l'obstacle.
	 */
	private EnumDirection skirtObstacle(EnumDirection dir, float x, float z) {
		switch (dir) {
		case UP:
			if (xMax > 0) {
				return EnumDirection.RIGHT;
			} else if (xMax < 0) {
				return EnumDirection.LEFT;
			}
			break;
		case DOWN:
			if (xMax > 0) {
				return EnumDirection.RIGHT;
			} else if (xMax < 0) {
				return EnumDirection.LEFT;
			}
			break;
		case LEFT:
			if (zMax > 0) {
				return EnumDirection.UP;
			} else if (zMax < 0) {
				return EnumDirection.DOWN;
			}
			break;
		case RIGHT:
			if (zMax > 0) {
				return EnumDirection.UP;
			} else if (zMax < 0) {
				return EnumDirection.DOWN;
			}
			break;
		case NONE:
			break;
		}
		return dir;
	}

	
	/**
	 * Permet de tracer un trait entre la position actuelle et la prochaine atteinte.
	 * 
	 * @param dir direction vers laquelle on veut se d�placer (vue du dessus).
	 * @param centerX centre de la fen�tre sur l'axe X.
	 * @param centerZ centre de la fen�tre sur l'axe Z.
	 * @param deltaX soustraction des coordonn�e sur l'axe X du point d'arriv� par le point actuel.
	 * @param deltaZ soustraction des coordonn�e sur l'axe Z du point d'arriv� par le point actuel.
	 * @param resInt r�solution sous forme d'un nombre entier.
	 */
	private void drawLine(EnumDirection dir, int centerX, int centerZ, int deltaX, int deltaZ, int resInt) {
		if (graphics.getColor() != altitude.getColor())
			graphics.setColor(altitude.getColor());
		switch (dir) {
		case UP:
			graphics.drawLine(centerX + (deltaX), centerZ - (deltaZ), centerX + (deltaX), centerZ - (deltaZ) - resInt);
			break;
		case DOWN:
			graphics.drawLine(centerX + (deltaX), centerZ - (deltaZ), centerX + (deltaX), centerZ - (deltaZ) + resInt);
			break;
		case LEFT:
			graphics.drawLine(centerX + (deltaX), centerZ - (deltaZ), centerX + (deltaX) - resInt, centerZ - (deltaZ));
			break;
		case RIGHT:
			graphics.drawLine(centerX + (deltaX), centerZ - (deltaZ), centerX + (deltaX) + resInt, centerZ - (deltaZ));
			break;
		case NONE:
			break;
		}
	}
	
	/**
	 * Permet de dessiner un obstacle.
	 * 
	 * @param dir direction vers laquelle on veut se d�placer (vue du dessus).
	 * @param centerX centre de la fen�tre sur l'axe X.
	 * @param centerZ centre de la fen�tre sur l'axe Z.
	 * @param deltaX soustraction des coordonn�es sur l'axe x du point d'arriv� par la position actuelle du dr�ne.
	 * @param deltaZ soustraction des coordonn�es sur l'axe z du point d'arriv� par la position actuelle du dr�ne.
	 * @param resInt r�solution sous forme d'un nombre entier.
	 */
	private void drawObstacle(EnumDirection dir, int centerX, int centerZ, int deltaX, int deltaZ, int resInt) {
		int size = 10;
		int demiSize = 5;
		graphics.setColor(Color.BLACK);
		switch (dir) {
		case UP:
			graphics.fillRect(centerX + (deltaX) - demiSize, centerZ - (deltaZ) - resInt - demiSize, size, size);
			break;
		case DOWN:
			graphics.fillRect(centerX + (deltaX) - demiSize, centerZ - (deltaZ) + resInt - demiSize, size, size);
			break;
		case LEFT:
			graphics.fillRect(centerX + (deltaX) - resInt - demiSize, centerZ - (deltaZ) - demiSize, size, size);
			break;
		case RIGHT:
			graphics.fillRect(centerX + (deltaX) + resInt - demiSize, centerZ - (deltaZ) - demiSize, size, size);
			break;
		case NONE:
			break;
		}
		graphics.setColor(altitude.getColor());
	}
	
	/**
	 * Permet de savoir si un nombre est plus ou moins �gal � un autre avec une certaine fluctuation.
	 * 
	 * @param value1 valeur 1
	 * @param value2 valeur 2
	 * @param fluc fluctuation admissible entre les deux valeurs.
	 * @param strictEquality Si l'�galit� est stricte et la valeur 1 est �gale � la valeur 2 moins la fluctuation, le retour sera faux.
	 * @return Renvoie si la valeur 1 et la valeur 2 sont �gale � une certaine fluctuation pr�t.
	 */
	public boolean plusOrLessEquals(float value1, float value2, double fluc, boolean strictEquality) {
		if (strictEquality) {
			if (value1 == value2)
				return true;
			if (value1  > value2 - fluc && value1 < value2)
				return true;
			if (value1  < value2 + fluc && value1 > value2)
				return true;
		} else {
			if (value1 == value2)
				return true;
			if (value1  >= value2 - fluc && value1 <= value2)
				return true;
			if (value1  <= value2 + fluc && value1 >= value2)
				return true;
		}
		return false;
	}

	/**
	 * Permet de cr�er un obstacle ponctuel.
	 */
	public void makeObstacle() {
		this.obstacle = true;
	}
	
	/**
	 * Permet de mettre en pause l'algorithme de d�placement du dr�ne.
	 */
	public void pauseRunnable() {
		this.pause = true;
	}
	
	/**
	 * Permet de remettre en cours l'algorithme de d�placement du dr�ne.
	 */
	public void playRunnable() {
		this.pause = false;
	}
	
	/**
	 * Permet de stopper l'execution de l'algorithme de d�placement du dr�ne.
	 */
	public void stopRunnable() {
		this.stop = true;
	}
}
