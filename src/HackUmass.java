import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.lang.Math;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

class SampleListener extends Listener {
	private JFrame jframe;
	private JFrame frame2;
	private JPanel cursorPanel;
	private int paintX;
	private int paintY;
	private int radius;
	private Color color;
	private boolean drawing;

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

		jframe = new JFrame() {
			public void paint(Graphics g) {
				if (drawing) {
					g.setColor(color);
					g.fillOval(paintX, paintY, radius, radius);
				}
			}
		};
		jframe.setSize(1500, 1000);
		jframe.setLayout(new BorderLayout());
		// cursorPanel = new JPanel();
		// cursorPanel.setOpaque(false);
		// cursorPanel.setVisible(true);
		jframe.add(new JPanel());
		jframe.setVisible(true);
		
		frame2 = new JFrame() {
			public void paint(Graphics g) {
				g.setColor(color);
				g.drawString("Color", 25, 100);
				g.setColor(Color.BLACK);
				g.fillOval(50, 150, radius, radius);
			}
		};
		frame2.setBackground(Color.BLACK);
		frame2.setSize(100, 1000);
		frame2.setLayout(new BorderLayout());
		frame2.setVisible(true);
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
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
			if (h.pinchStrength() == 1.0 && h.isRight()) {
				paintX = (int) ((v.getX() * 4) + jframe.getWidth() / 2);
				paintY = (int) (jframe.getHeight() - ((v.getY() * 4) - 50));
				// cursorPanel.repaint();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						jframe.repaint();
					}
				});
			}
			if (h.grabStrength() == 1.0) {
				if (h.isLeft()) {
					SwingUtilities.updateComponentTreeUI(jframe);
				}
			}

			if (frame.gestures().count() > 0) {
				String gestureType = frame.gestures().get(0).type().toString();
				switch (gestureType) {
				case "TYPE_SCREEN_TAP":
					// SwingUtilities.updateComponentTreeUI(jframe);
					break;
				case "TYPE_KEY_TAP":
					if (h.isRight()) {
						radius += 5;
					} else if (h.isLeft()) {
						if (radius >= 30) {
							radius -= 5;
						}
					}
					SwingUtilities.updateComponentTreeUI(frame2);
					frame2.repaint();
					break;
				case "TYPE_CIRCLE":
					if (h.isLeft()) {
						controller.config().setFloat(
								"Gesture.Circle.MinRadius", 50);
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
									(int)((green + turns) * 255), blue);
						}
						if (turns >= 1.0 && turns < 2.0) {
							color = new Color(red
									- (int) (turns * 127.5), green,
									blue);
						}
						if (turns >= 2.0 && turns < 3.0) {
							color = new Color(red, green, blue + (int)(turns * 85));
						}
						if (turns >= 3.0 && turns < 4.0) {
							color = new Color(red,
									(int) (green - (turns * 63.75)), blue);
						}
						if (turns >= 4.0 && turns < 5.0) {
							color = new Color(red + (int)(turns * 51), green, blue);
						}
						if (turns >= 6.0 && turns < 7.0) {
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
}

class HackUmass {
	public static void main(String[] args) {
		// Create a sample listener and controller
		SampleListener listener = new SampleListener();
		Controller controller = new Controller();

		// Have the sample listener receive events from the controller
		controller.addListener(listener);

		// Keep this process running until Enter is pressed
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(listener);
	}
}
