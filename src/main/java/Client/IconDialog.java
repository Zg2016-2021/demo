package Client;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class IconDialog implements ActionListener {

    private JDialog dialog;
    private Client client;

    public IconDialog(JFrame frame, Client client) {
        this.client = client;
        dialog = new JDialog(frame, "请选择表情", true);
        /*16个表情*/
        JButton[] icon_button = new JButton[16];
        ImageIcon[] icons = new ImageIcon[16];

        Container dialogPane = dialog.getContentPane();
        dialogPane.setLayout(new GridLayout(0, 4));

        for(int i=1; i<=15; i++){
            icons[i] = new ImageIcon("icon/"+i+".png");
            icons[i].setImage(icons[i].getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            icon_button[i] = new JButton(""+i, icons[i]);
            icon_button[i].addActionListener(this);
            dialogPane.add(icon_button[i]);
        }
        dialog.setBounds(200,266,266,280);
        dialog.show();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();
        System.out.println(cmd);
        dialog.dispose();
        client.sendMsg("<emoji>"+cmd+"</emoji>", "message");
    }

}