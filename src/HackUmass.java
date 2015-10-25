import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

class HackUmass extends Listener {
	private JFrame jframe;
	private JFrame frame2;
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
		// controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		paintX = 0;
		paintY = 0;
		radius = 25;
		color = Color.BLACK;
		drawing = true;
		String[] options = {"Left", "Right"};
		int result = JOptionPane.showOptionDialog(null, "Choose your dominant hand", "Handedness", JOptionPane.DEFAULT_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[1]);
		handedness = result == 1 ? "right" : "left";
		String drawingName = JOptionPane.showInputDialog("Enter Drawing Name: ");

		jframe = new JFrame(drawingName) {
			public void paint(Graphics g) {
				if (drawing) {
					g.setColor(color);
					g.fillOval(paintX, paintY, radius, radius);
				}
			}
		};
		jframe.setBounds(0, 100, 1350, 850);
		jframe.setLayout(new BorderLayout());
		jframe.setBackground(Color.WHITE);
		JPanel panel = new JPanel();
		panel.setVisible(true);
		jframe.add(panel);
//		jframe.setAlwaysOnTop(true);
		jframe.setVisible(true);

		frame2 = new JFrame("Size/Color") {
			public void paint(Graphics g) {
				g.setColor(color);
				g.fillOval((this.getWidth() / 2) - (radius / 2),
						(this.getHeight() / 2) - 25, radius, radius);
			}
		};
		frame2.setBounds(1350,100,200, 200);
		frame2.setLayout(new BorderLayout());
		frame2.setAlwaysOnTop(true);
		frame2.setVisible(true);
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		// controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
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
			if (h.pinchStrength() == 1.0 && (handedness.equals("left") ? h.isLeft():h.isRight())) {
				paintX = (int) ((v.getX() * 4) + jframe.getWidth() / 2);
				paintY = (int) (jframe.getHeight() - ((v.getY() * 4) - 50));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						jframe.repaint();
					}
				});
			}
			if (h.grabStrength() == 1.0) {
				if (handedness.equals("right") ? h.isLeft():h.isRight()) {
					SwingUtilities.updateComponentTreeUI(jframe);
					jframe.getContentPane().setBackground(Color.WHITE);
				}
			}

			if (frame.gestures().count() > 0) {
				String gestureType = frame.gestures().get(0).type().toString();
				switch (gestureType) {
				case "TYPE_KEY_TAP":
					if (h.isRight()) {
						if (radius <= 90) {
							radius += 5;
						}
					} else if (handedness.equals("right") ? h.isLeft():h.isRight()) {
						if (radius >= 30) {
							radius -= 5;
						}
					}
					SwingUtilities.updateComponentTreeUI(frame2);
					frame2.getContentPane().setBackground(Color.WHITE);
//					frame2.getContentPane().removeAll();
//					frame2.repaint();

					break;
				case "TYPE_CIRCLE":
					if (handedness.equals("right") ? h.isLeft():h.isRight()) {
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
						frame2.repaint();
					}
					break;
				default:
					break;
				}
			}
		}
	}
	public void loadFile(File file) throws Exception {
		jframe.toFront();
		jframe.setContentPane(new JLabel(new ImageIcon(ImageIO.read(file))));
		jframe.repaint();
	}
}

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
		        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        int w = getWidth();
		        int h = getHeight();
		        Color color1 = new Color(102,204,255);
		        Color color2 = new Color(178,178,204);
		        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
		        g2d.setPaint(gp);
		        g2d.fillRect(0, 0, w, h);
		    }
		};
		jMenuBar1 = new javax.swing.JMenuBar();
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
		Image.add(takePictureMenuItem);

		jMenuBar1.add(Image);

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
		if(!windowOpen) {
			JOptionPane.showMessageDialog(this, "Must have a drawing open first", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			JFileChooser fChooser = new JFileChooser();
			int val = fChooser.showDialog(this, "Load Image");
			if(val == JFileChooser.APPROVE_OPTION) {
				listener.loadFile(fChooser.getSelectedFile());
			}
		}
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
