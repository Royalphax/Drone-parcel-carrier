package fr.therence.geotraceur;

import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Class permettant de créer notre propre Panneau.
 * 
 * @author Therence F.
 */
public class Panel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Mise en place des axes.
	 */
	public void paintComponent(Graphics g) {
		g.drawLine(this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight());
		g.drawLine(0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
		g.drawString("Z", this.getWidth() / 2 - 15, 20);
		g.drawString("X", this.getWidth() - 20, this.getHeight() / 2 - 15);
		g.fillOval(this.getWidth() / 2 - 6, this.getHeight() / 2 - 6, 12, 12);
	}
}
