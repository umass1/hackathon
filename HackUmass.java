import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	private int paintX;
	private int paintY;
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        paintX = 0;
        paintY = 0;
        
        jframe = new JFrame() {
        	public void paint(Graphics g) {
        		g.setColor(Color.BLUE);
        		g.fillOval(paintX, paintY, 25, 25);
        		
        	}
        };
        jframe.setSize(1500, 1000);
		jframe.setLayout(new BorderLayout());
		jframe.add(new JPanel());
		jframe.setVisible(true);
//        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
//        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
//        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
//        Frame frame = controller.frame();
//        for(Hand h : frame.hands()) {
//        	int count = 0;
//        	double palmPosition = (h.palmPosition().getY()-65)*.80;
//        	if(palmPosition >= 0.0 ) {
////        		System.out.println((int)palmPosition);
//        	}
//        	if(frame.gestures().count() != 0) {
//        		System.out.println(frame.gestures().get(0).type().toString());
//        		switch(frame.gestures().get(0).type().toString()) {
//        			case "TYPE_SWIPE":
//        				count++;
//        				if(count == 1) {
//            				System.out.println("Swipe: Switch Color");
//        				}
//        		}
//        	}
//        }
    	Frame frame = controller.frame();
    	for(Hand h : frame.hands()) {
    		final Vector v = h.palmPosition();
//    		System.out.println(h.fingers().frontmost().id());
//    		System.out.println("(" + v.getX() + ", " + v.getY() + ")");
    		if(h.fingers().frontmost().id()%10 == 1) {
    			paintX = (int)((v.getX()*4)+jframe.getWidth()/2);
    			paintY = (int)(jframe.getHeight() - (v.getY()*4));
    			jframe.repaint();
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
