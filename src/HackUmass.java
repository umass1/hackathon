import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Math;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.Sides;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

import org.opencv.*;

class HackUmass extends Listener {
	private JFrame drawingFrame;
	private JFrame cursorFrame;
	private int paintX;
	private int paintY;
	private int radius;
	private Color color;
	private boolean drawing;
	private String handedness;

	public void onInit(Controller controller) {
		System.out.println("Initialized");
	}

	public void onConnect(Controller controller) {
		System.out.println("Connected");
		paintX = 0;
		paintY = 0;
		radius = 25;
		color = Color.BLACK;
		drawing = true;

		// Prompt for handedness
		String[] options = { "Left", "Right" };
		int result = JOptionPane.showOptionDialog(null,
				"Which hand will you draw with?", "Handedness",
				JOptionPane.DEFAULT_OPTION, JOptionPane.DEFAULT_OPTION, null,
				options, options[1]);
		handedness = result == 1 ? "right" : "left";

		// Prompt for frame name
		String drawingName = JOptionPane
				.showInputDialog("Enter Drawing Name: ");

		drawingFrame = new JFrame(drawingName) {
			public void paint(Graphics g) {
				if (drawing) {
					g.setColor(color);
					g.fillOval(paintX, paintY, radius, radius);
				}
			}
		};
		drawingFrame.setBounds(0, 100, 1350, 850);
		drawingFrame.setLayout(new BorderLayout());
		drawingFrame.setBackground(new Color(130, 134, 135));
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setVisible(true);
		drawingFrame.add(panel);

		cursorFrame = new JFrame("Size/Color") {
			public void paint(Graphics g) {
				g.setColor(color);
				g.fillOval((this.getWidth() / 2) - (radius / 2),
						(this.getHeight() / 2) - 25, radius, radius);
			}
		};
		cursorFrame.setBounds(1350, 100, 200, 200);
		cursorFrame.setLayout(new BorderLayout());
		JPanel cursorPanel = new JPanel();
		cursorPanel.setBackground(Color.WHITE);
		panel.setVisible(true);
		cursorFrame.add(cursorPanel);
		
		// Add Gestures to controller
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				drawingFrame.setVisible(true);
				cursorFrame.setVisible(true);
			}
		});
	}

	public void onDisconnect(Controller controller) {
		// Note: not dispatched when running in a debugger.
		System.out.println("Disconnected");
	}

	public void onExit(Controller controller) {
		System.out.println("Exited");
	}

	public void onFrame(Controller controller) {
		Frame frame = controller.frame();
		for (Hand h : frame.hands()) {
			final Vector v = h.palmPosition();
			
			// Draw if the user is pinching with their drawing hand
			if (h.pinchStrength() == 1.0
					&& (handedness.equals("left") ? h.isLeft() : h.isRight())) {
				paintX = (int) ((v.getX() * 4) + drawingFrame.getWidth() / 2);
				paintY = (int) (drawingFrame.getHeight() - ((v.getY() * 4) - 50));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						drawingFrame.repaint();
					}
				});
			}
			
			// Clear if a fist with non- hand
			if (h.grabStrength() == 1.0) {
				if (handedness.equals("right") ? h.isLeft() : h.isRight()) {
					SwingUtilities.updateComponentTreeUI(drawingFrame);					
				}
			}

			// If the frame detects a gesture
			if (frame.gestures().count() > 0) {
				String gestureType = frame.gestures().get(0).type().toString();
				switch (gestureType) {
				case "TYPE_KEY_TAP":
					
					//Prevents thread exception
					Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

						@Override
						public void uncaughtException(Thread t, Throwable e) {
							// TODO Auto-generated method stub

						}
					});
					
					// Change the radius
					if (h.isRight()) {
						if (radius <= 90) {
							radius += 5;
							SwingUtilities.updateComponentTreeUI(cursorFrame);
						}
					} else if (h.isLeft()) {
						if (radius >= 30) {
							radius -= 5;
							SwingUtilities.updateComponentTreeUI(cursorFrame);
						}
					}
					break;
				case "TYPE_CIRCLE":
					
					// Changes color based on extent of circle motion with non-drawing hand
					if (handedness.equals("right") ? h.isLeft() : h.isRight()) {
						controller.config().setFloat(
								"Gesture.Circle.MinRadius", 25);
						controller.config().save();
						CircleGesture circle = new CircleGesture(frame
								.gestures().get(0));
						float turns = circle.progress();
						int red = 0;
						int green = 0;
						int blue = 0;
						if (turns >= 7.0) {
							turns = 0;
						}
						if (turns < 1.0) {
							red = 255;
							green = 0;
							blue = 0;
							color = new Color(red,
									(int) ((green + turns) * 255), blue);
						}
						if (turns >= 1.0 && turns < 2.0) {
							red = 255;
							green = 255;
							blue = 0;
							color = new Color(red - (int) (turns * 127.5),
									green, blue);
						}
						if (turns >= 2.0 && turns < 3.0) {
							red = 0;
							green = 255;
							blue = 0;
							color = new Color(red, green, blue
									+ (int) (turns * 85));
						}
						if (turns >= 3.0 && turns < 4.0) {
							red = 0;
							green = 255;
							blue = 0;
							color = new Color(red,
									(int) (green - (turns * 63.75)), blue);
						}
						if (turns >= 4.0 && turns < 5.0) {
							red = 0;
							green = 0;
							blue = 255;
							color = new Color(red + (int) (turns * 51), green,
									blue);
						}
						if (turns >= 6.0 && turns < 7.0) {
							red = 255;
							green = 0;
							blue = 255;
							color = new Color(red, green, blue
									- (int) (turns * 42.5));
						}
						cursorFrame.repaint();
					}
					break;
				default:
					break;
				}
			}
		}
	}

	public void loadFile(File file) throws Exception {
		drawingFrame.toFront();
		cursorFrame.toFront();
		drawingFrame.setContentPane(new JLabel(
				new ImageIcon(ImageIO.read(file))));
		drawingFrame.repaint();
		cursorFrame.repaint();
	}
}

/**
 * Netbeans Generated
 * 
 * @author Matt
 *
 */
class MenuFrame extends javax.swing.JFrame {

	/**
	 * Creates new form MenuFrame
	 */
	public MenuFrame() {
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		Font f = new Font("sans-serif", Font.PLAIN, 20);
		UIManager.put("Menu.font", f);
		UIManager.put("MenuItem.font", f);
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		panel = new javax.swing.JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				int w = getWidth();
				int h = getHeight();
				Color color1 = new Color(102, 204, 255);
				Color color2 = new Color(178, 178, 204);
				GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, w, h);
			}
		};
		jMenuBar1 = new javax.swing.JMenuBar();
		Help = new JMenu();
		helpMenuItem = new JMenuItem();
		fileMenu = new javax.swing.JMenu();
		newDrawingMenuItem = new javax.swing.JMenuItem();
		Image = new javax.swing.JMenu();
		loadImageMenuItem = new javax.swing.JMenuItem();
		takePictureMenuItem = new javax.swing.JMenuItem();
		controller = new Controller();
		listener = new HackUmass();
		windowOpen = false;

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Get The Hands");

		panel.setBackground(new java.awt.Color(255, 255, 255));

		javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
		panel.setLayout(panelLayout);
		panelLayout.setHorizontalGroup(panelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 1334,
				Short.MAX_VALUE));
		panelLayout.setVerticalGroup(panelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 768,
				Short.MAX_VALUE));

		fileMenu.setText("File");

		newDrawingMenuItem.setText("New Drawing");
		newDrawingMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						newDrawing(evt);
					}
				});
		fileMenu.add(newDrawingMenuItem);
		jMenuBar1.add(fileMenu);

		Image.setText("Image");
		Image.setActionCommand("Image");

		loadImageMenuItem.setText("Load Image");
		loadImageMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				// TODO Auto-generated method stub
				try {
					loadImage(evt);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		Image.add(loadImageMenuItem);
		takePictureMenuItem.setText("Take Picture");
		takePictureMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				takePicture(e);
			}
		});
		Image.add(takePictureMenuItem);

		jMenuBar1.add(Image);
		
		Help.setText("Help");
		Help.setActionCommand("Help");
		
		helpMenuItem.setText("Show Help");
		helpMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					Desktop d = Desktop.getDesktop();
					d.open(new File("helpdoc.docx"));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		Help.add(helpMenuItem);
		jMenuBar1.add(Help);
		setJMenuBar(jMenuBar1);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel,
				javax.swing.GroupLayout.DEFAULT_SIZE,
				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(panel,
				javax.swing.GroupLayout.DEFAULT_SIZE,
				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		pack();
	}

	private void newDrawing(java.awt.event.ActionEvent evt) {
		// Create a sample listener and controller
		windowOpen = true;
		controller.removeListener(listener);
		controller.addListener(listener);
	}

	private void loadImage(ActionEvent evt) throws Exception {
		if (!windowOpen) {
			JOptionPane.showMessageDialog(this,
					"Must have a drawing open first", "Error",
					JOptionPane.ERROR_MESSAGE);
		} else {
			JFileChooser fChooser = new JFileChooser();
			int val = fChooser.showDialog(this, "Load Image");
			if (val == JFileChooser.APPROVE_OPTION) {
				listener.loadFile(fChooser.getSelectedFile());
			}
		}
	}

	private void takePicture(ActionEvent e) {

	}

	public static void main(String args[]) {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(MenuFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(MenuFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(MenuFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MenuFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MenuFrame().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JMenu Image;
	private javax.swing.JMenu fileMenu;
	private JMenu Help;
	private JMenuItem helpMenuItem;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenuItem newDrawingMenuItem;
	private javax.swing.JMenuItem loadImageMenuItem;
	private javax.swing.JPanel panel;
	private javax.swing.JMenuItem takePictureMenuItem;
	private Controller controller;
	private HackUmass listener;
	private boolean windowOpen;
	// End of variables declaration//GEN-END:variables
}
