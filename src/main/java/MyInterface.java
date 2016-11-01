import com.ericsson.otp.erlang.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by oabdelhafith on 01/11/2016.
 */
public class MyInterface extends JFrame {

    JLabel label;
    JPanel mainPanel;
    JButton button;

    OtpErlangPid lastPid = null;
    OtpMbox myOtpMbox = null;

    public static void main(String[] args) {

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        MyInterface i = new MyInterface();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public MyInterface() {
        createAndShowGUI();

        Thread t = new Thread(new Runnable() {
            public void run() {
                setupMBox();
            }
        });
        t.start();
    }

    private void setupMBox() {
        try {
            OtpNode myOtpNode = new OtpNode("server");
            myOtpNode.setCookie("secret");

            myOtpMbox = myOtpNode.createMbox("java-server");

            while (true) {
                OtpErlangTuple tuple = (OtpErlangTuple) myOtpMbox.receive();

                lastPid = (OtpErlangPid) tuple.elementAt(0);
                OtpErlangAtom dispatch = (OtpErlangAtom) tuple.elementAt(1);

                if (dispatch.toString().equals("settext")) {

                    final OtpErlangBinary message = (OtpErlangBinary) tuple.elementAt(2);

                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            label.setText(new String(message.binaryValue()));
                        }
                    });
                } else if (dispatch.toString().equals("greet")) {
                    final OtpErlangBinary message = (OtpErlangBinary) tuple.elementAt(2);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            label.setText("Bye " + new String(message.binaryValue()));
                        }
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createAndShowGUI() {
        JFrame frame = this;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        mainPanel = panel;
        frame.add(panel);

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        frame.setLocationRelativeTo(null);
        frame.setSize(400, 150);

        frame.setVisible(true);

        addButton();
    }

    private void addButton() {
        label = new JLabel(" - ");
        mainPanel.add(label);

        mainPanel.add(Box.createVerticalGlue());

        button = new JButton("Send Message");
        mainPanel.add(button);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                myOtpMbox.send(lastPid, new OtpErlangString("Hello from java"));
            }
        });
    }
}
