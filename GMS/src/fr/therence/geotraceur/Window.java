package fr.therence.geotraceur;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Deuxième class la plus importante de notre application. Elle va générer une
 * interface et s'occuper de gérer les intéractions avec l'utilisateur.
 * 
 * @author Therence F.
 */
public class Window extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private PathRunnable pathRunnable;

	private JPanel container = new JPanel();
	private JTextField xField = new JTextField("");
	private JTextField zField = new JTextField("");
	private JTextField resField = new JTextField("1");
	private JButton drawButton = new JButton("GO");
	private JButton eraseButton = new JButton("Effacer");
	private JButton pauseButton = new JButton("");
	private JButton playButton = new JButton("");
	private JButton stopButton = new JButton("");
	private JButton obstacleButton = new JButton("Obstacle");
	private JLabel altitude = new JLabel("NA");
	private JLabel result = new JLabel("");

	public Window() {

		this.setTitle("Fonctionnement de l'algorithme de déplacement du drône (par Thérence F.)");
		this.setIconImage(new ImageIcon(this.getClass().getResource("icon.png")).getImage());
		this.setSize(800, 800);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);

		drawButton.addActionListener(this);
		drawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		eraseButton.addActionListener(this);
		eraseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		obstacleButton.addActionListener(this);
		obstacleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		pauseButton.addActionListener(this);
		pauseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		pauseButton.setIcon(new ImageIcon(this.getClass().getResource("pause_button.png")));
		pauseButton.setPreferredSize(new Dimension(30, 30));
		pauseButton.setFocusPainted(false);

		playButton.addActionListener(this);
		playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		playButton.setIcon(new ImageIcon(this.getClass().getResource("play_button.png")));
		playButton.setPreferredSize(new Dimension(30, 30));
		playButton.setFocusPainted(false);
		playButton.setEnabled(false);

		stopButton.addActionListener(this);
		stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		stopButton.setIcon(new ImageIcon(this.getClass().getResource("stop_button.png")));
		stopButton.setPreferredSize(new Dimension(30, 30));
		stopButton.setFocusPainted(false);

		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());
		xField.setPreferredSize(new Dimension(30, 30));
		zField.setPreferredSize(new Dimension(30, 30));
		resField.setPreferredSize(new Dimension(30, 30));
		JPanel top = new JPanel();
		top.add(result);
		top.add(new JLabel("X="));
		top.add(xField);
		top.add(new JLabel("Z="));
		top.add(zField);
		top.add(new JLabel("Résolution="));
		top.add(resField);
		top.add(drawButton);
		top.add(eraseButton);
		top.add(new JLabel("|"));
		top.add(pauseButton);
		top.add(playButton);
		top.add(stopButton);
		top.add(new JLabel("|"));
		top.add(obstacleButton);
		top.add(new JLabel("Altitude="));
		top.add(altitude);
		container.add(top, BorderLayout.NORTH);
		container.add(new Panel());
		this.setContentPane(container);
		this.setVisible(true);

		validate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == drawButton) {
			try {
				// On récupère les nombres mis dans les champs.
				float inputX = Float.parseFloat(xField.getText());
				float inputZ = Float.parseFloat(zField.getText());
				float inputRes = Float.parseFloat(resField.getText());
				if (inputRes <= 0) {
					result.setText("La résolution doit être strictement supérieure à 0 !");
					return;
				}
				// On désactive les boutons.
				drawButton.setEnabled(false);
				eraseButton.setEnabled(false);
				playButton.setEnabled(false);
				// On dessine le point à atteindre.
				getGraphics().fillOval(Math.round((this.getWidth() / 2 - 5) + inputX * 30), Math.round((this.getHeight() / 2 + 30) - inputZ * 30), 10, 10);
				// On prépare et on lance la tâche de déplacement séparemment de
				// la tâche de fonctionnement de l'application principale.
				pathRunnable = new PathRunnable(inputX, inputZ, inputRes, this);
				new Thread(pathRunnable).start();

				result.setText("");
			} catch (NumberFormatException | NullPointerException ex) {
				// On empêche le cas ou l'utilisateur aurait mis une lettre dans
				// un des champs.
				result.setText("X ou Z ne sont pas des nombres !");
				return;
			}
		} else if (src == eraseButton) {
			this.repaint();
		} else {
			if (pathRunnable != null) {
				if (src == pauseButton) {
					pauseButton.setEnabled(false);
					playButton.setEnabled(true);
					pathRunnable.pauseRunnable();
				} else if (src == playButton) {
					pauseButton.setEnabled(true);
					playButton.setEnabled(false);
					pathRunnable.playRunnable();
				} else if (src == stopButton) {
					pauseButton.setEnabled(true);
					playButton.setEnabled(false);
					pathRunnable.stopRunnable();
				} else if (src == obstacleButton) {
					obstacleButton.setEnabled(false);
					pathRunnable.makeObstacle();
				}
			}
		}
	}

	/**
	 * Permet de déclarer que la réalisation du traçé s'est terminé avec succès.
	 */
	public void finishedDrawing() {
		drawButton.setEnabled(true);
		eraseButton.setEnabled(true);
		obstacleButton.setEnabled(true);
		pathRunnable = null;
	}

	/**
	 * Permet de mettre à jour l'affichage de l'altitude du drône.
	 * 
	 * @param alt altitude actuelle du drône.
	 */
	public void setAltitude(EnumAltitude alt) {
		altitude.setText(alt.getLowHeight() + "-" + alt.getHighHeight() + "m (" + alt.getName() + ")");
	}
	
	/**
	 * Permet de mettre à jour l'état du bouton permettant d'ajouter un obstacle.
	 */
	public void updateObstacleButton() {
		obstacleButton.setEnabled(true);
	}
}