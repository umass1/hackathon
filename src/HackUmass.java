import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.io.IOException;
import java.lang.Math;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

class SampleListener extends Listener {
	private JFrame jframe;
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
//		controller.enableGesture(Gesture.Type.TYPE_SWIPE);
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
		// controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
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
				paintY = (int) (jframe.getHeight() - (v.getY() * 4));
				// cursorPanel.repaint();
				jframe.repaint();
			}
			if(h.grabStrength() == 1.0) {
				if(h.isLeft()) {
					SwingUtilities.updateComponentTreeUI(jframe);
				}
			}

			if (frame.gestures().count() > 0) {
				switch (frame.gestures().get(0).type().toString()) {
				case "TYPE_SCREEN_TAP":
//					SwingUtilities.updateComponentTreeUI(jframe);
					break;
				case "TYPE_KEY_TAP":
					if(h.isRight()) {
						radius+=5;
					} else if(h.isLeft()) {
						if(radius >= 30) {
							radius -=5;
						}
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
