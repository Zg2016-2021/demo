package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



public class Client implements ActionListener{
    private JFrame frame;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private String name;
    private HashMap<String, Integer> rooms_map;
    private HashMap<String, Integer> users_map;
    private JTextField host_textfield;
    private JTextField port_textfield;
    private JTextField text_field;
    private JTextField name_textfiled;
    private JLabel rooms_label;
    private JLabel users_label;
    private JList<String> roomlist;
    private JList<String> userlist;
    private JTextPane msgArea;
    private JScrollPane textScrollPane;
    private JScrollBar vertical;
    DefaultListModel<String> rooms_model;
    DefaultListModel<String> users_model;


    public Client(){
        rooms_map = new HashMap<>();
        users_map = new HashMap<>();
        initialize();
    }


    public boolean connect(String host, int port){
        try {
            socket = new Socket(host, port);
            System.out.println("Connected to server!"+socket.getRemoteSocketAddress());
            br=new BufferedReader(new InputStreamReader(System.in));
            pw=new PrintWriter(socket.getOutputStream());

            ClientThread thread = new ClientThread(socket, this);
            thread.start();

            return true;

        } catch (IOException e) {
            System.out.println("Server error");
            JOptionPane.showMessageDialog(frame, "服务器无法连接！");
            return false;
        }
    }


    public void sendMsg(String msg, String code){
        try {
            pw.println("<code>"+code+"</code><msg>"+msg+"</msg>");
            pw.flush();
        } catch (Exception e) {
            //一般是没有连接的问题
            System.out.println("error in sendMsg()");
            JOptionPane.showMessageDialog(frame, "请先连接服务器！");
        }
    }

    /**
     * 窗口初始化
     */
    private void initialize() {
        /*设置窗口的UI风格和字体*/
        setUIStyle();
        setUIFont();

        frame = new JFrame("ChatOnline");
        JPanel panel = new JPanel();  /*主要的panel，上层放置连接区，下层放置消息区，
             中间是消息面板，左边是room列表，右边是当前room的用户列表*/
        JPanel headpanel = new JPanel(); /*上层panel，用于放置连接区域相关的组件*/
        JPanel footpanel = new JPanel(); /*下层panel，用于放置发送信息区域的组件*/
        JPanel leftpanel = new JPanel(); /*左边panel，用于放置房间列表和加入按钮*/
        JPanel rightpanel = new JPanel(); /*右边panel，用于放置房间内人的列表*/

        /*最上层的布局，分中间，东南西北五个部分*/
        BorderLayout layout = new BorderLayout();
        /*格子布局，主要用来设置西、东、南三个部分的布局*/
        GridBagLayout gridBagLayout = new GridBagLayout();
        /*主要设置北部的布局*/
        FlowLayout flowLayout = new FlowLayout();
        /*设置初始窗口的一些性质*/
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setLayout(layout);
        /*设置各个部分的panel的布局和大小*/
        headpanel.setLayout(flowLayout);
        footpanel.setLayout(gridBagLayout);
        leftpanel.setLayout(gridBagLayout);
        rightpanel.setLayout(gridBagLayout);
        leftpanel.setPreferredSize(new Dimension(130, 0));
        rightpanel.setPreferredSize(new Dimension(130, 0));


        /*以下均是headpanel中的组件*/
        host_textfield = new JTextField("127.0.0.1");
        port_textfield = new JTextField("9996");
        name_textfiled = new JTextField("匿名");
        host_textfield.setPreferredSize(new Dimension(100, 25));
        port_textfield.setPreferredSize(new Dimension(70, 25));
        name_textfiled.setPreferredSize(new Dimension(150, 25));

        JLabel host_label = new JLabel("服务器IP");
        JLabel port_label = new JLabel("端口");
        JLabel name_label = new JLabel("昵称");

        JButton head_connect = new JButton("连接");
//  JButton head_change = new JButton("确认更改");
        JButton head_create = new JButton("创建房间");

        headpanel.add(host_label);
        headpanel.add(host_textfield);
        headpanel.add(port_label);
        headpanel.add(port_textfield);
        headpanel.add(head_connect);
        headpanel.add(name_label);
        headpanel.add(name_textfiled);
//  headpanel.add(head_change);
        headpanel.add(head_create);

        /*以下均是footpanel中的组件*/
        JButton foot_emoji = new JButton("表情");
        JButton foot_send = new JButton("发送");
        text_field = new JTextField();
        footpanel.add(text_field, new GridBagConstraints(0, 0, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        footpanel.add(foot_emoji, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        footpanel.add(foot_send, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /*两边的格子中的组件*/
        rooms_label = new JLabel("当前房间数：0");
        users_label = new JLabel("房间内人数：0");
        JButton join_button = new JButton("加入房间");
        JButton esc_button = new JButton("退出房间");

        rooms_model = new DefaultListModel<>();
        users_model = new DefaultListModel<>();
//  rooms_model.addElement("房间1");
//  rooms_model.addElement("房间2");
//  rooms_model.addElement("房间3");
//  String fangjian = "房间1";
//  rooms_map.put(fangjian, 1);

        roomlist = new JList<>(rooms_model);
        userlist = new JList<>(users_model);

        JScrollPane roomListPane = new JScrollPane(roomlist);
        JScrollPane userListPane = new JScrollPane(userlist);

        leftpanel.add(rooms_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        leftpanel.add(join_button, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        leftpanel.add(esc_button, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        leftpanel.add(roomListPane, new GridBagConstraints(0, 3, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(users_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(userListPane,new GridBagConstraints(0, 1, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /*中间的文本区组件*/
        msgArea = new JTextPane();
        msgArea.setEditable(false);
        textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(msgArea);
        vertical = new JScrollBar(JScrollBar.VERTICAL);
        vertical.setAutoscrolls(true);
        textScrollPane.setVerticalScrollBar(vertical);

        /*设置顶层布局*/
        panel.add(headpanel, "North");
        panel.add(footpanel, "South");
        panel.add(leftpanel, "West");
        panel.add(rightpanel, "East");
        panel.add(textScrollPane, "Center");

        /*注册各种事件*/
        /*连接服务器*/
        head_connect.addActionListener(this);
        /*发送消息，如果没有连接则会弹窗提示*/
        foot_send.addActionListener(this);
        /*改名字*/
//  head_change.addActionListener(this);
        /*创建房间*/
        head_create.addActionListener(this);
        /*发送表情*/
        foot_emoji.addActionListener(this);
        /*加入room*/
        join_button.addActionListener(this);
        /*退出房间*/
        esc_button.addActionListener(this);

        /*最终显示*/
        frame.setVisible(true);
    }

    /**
     * 事件监听处理
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "连接": /*点击连接按钮*/
                String strhost = host_textfield.getText();
                String strport = port_textfield.getText();
                connect(strhost, Integer.parseInt(strport));
                String nameSeted = JOptionPane.showInputDialog("请输入你的昵称："); /*提示输入昵称*/
                name_textfiled.setText(nameSeted);
                name_textfiled.setEditable(false);
                port_textfield.setEditable(false);
                host_textfield.setEditable(false);
                /*发送设置姓名的消息和列出用户列表的消息*/
                sendMsg(nameSeted, "setname");
                sendMsg("", "list");
                break;
//  case "确认更改":
//   String strname = name_textfiled.getText();
//   name = strname;
//   sendMsg(strname, "setname");
//   break;
            case "加入房间": /*选择房间后，点击加入房间按钮*/
                int selectedIndex = roomlist.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selected = rooms_model.getElementAt(selectedIndex);
                    if (rooms_map.containsKey(selected)) {
                        sendMsg("" + rooms_map.get(selected), "join");
                    }
                }
                break;
            case "退出房间": /*点击退出房间的按钮*/
                sendMsg("", "esc");
                break;
            case "发送":  /*点击发送消息的按钮*/
                String text = text_field.getText();
                text_field.setText("");
                sendMsg(text, "message");
                break;
            case "表情":  /*发送表情，新建一个表情窗口，并直接在表情窗口中处理消息发送*/
                IconDialog dialog = new IconDialog(frame, this);
                break;
            case "创建房间": /*点击创建房间的按钮，弹出提示框数据房间名称*/
                String string = JOptionPane.showInputDialog("请输入你的房间名称");
                if(string==null || string.equals("")){
                    string = name+(int)(Math.random()*10000)+"的房间";
                }
                sendMsg(string, "create");
                break;
            default:
                break;
        }

    }


    /*很多辅助和clientThread互动的*/

    /**
     * 加入用户，通过正则表达式，匹配消息内容中的用户信息
     * @param content
     */
    public void addUser(String content){
        if(content.length()>0){
            Pattern pattern = Pattern.compile("<name>(.*)</name><id>(.*)</id>");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()){
                /*
                 * 获得用户的name和id
                 * 加入用户列表
                 * 在消息区显示系统提示
                 */
                String name = matcher.group(1);
                String id = matcher.group(2);
                insertUser(Integer.parseInt(id), name);
                insertMessage(textScrollPane, msgArea, null, "系统：", name+"加入了聊天室");
            }
        }
        users_label.setText("房间内人数："+users_map.size()); /*更新房间内的人数*/
    }

    /**
     * 删除用户
     * @param content
     */
    public void delUser(String content){
        if(content.length()>0){
            int id = Integer.parseInt(content);
            /*
             * 从维护的用户map中取得所有的用户名字，然后去遍历匹配的用户
             * 匹配到的用户名字从相应的数据模型中移除
             * 并从map中移除，并在消息框中提示系统消息
             */
            Set<String> set = users_map.keySet();
            Iterator<String> iter = set.iterator();
            String name=null;
            while(iter.hasNext()){
                name = iter.next();
                if(users_map.get(name)==id){
                    users_model.removeElement(name);
                    break;
                }
            }
            users_map.remove(name);
            insertMessage(textScrollPane, msgArea, null, "系统：", name+"退出了聊天室");
        }
        users_label.setText("房间内人数："+users_map.size());
    }


    public void updateUser(String content){
        if(content.length()>0){
            Pattern pattern = Pattern.compile("<id>(.*)</id><name>(.*)</name>");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()){
                String id = matcher.group(1);
                String name = matcher.group(2);
                insertUser(Integer.parseInt(id), name);
            }
        }
    }

    public void listUsers(String content){
        String name = null;
        String id=null;
        Pattern rough_pattern=null;
        Matcher rough_matcher=null;
        Pattern detail_pattern=null;

        if(content.length()>0){
            rough_pattern = Pattern.compile("<member>(.*?)</member>");
            rough_matcher = rough_pattern.matcher(content);
            while(rough_matcher.find()){
                String detail = rough_matcher.group(1);
                detail_pattern = Pattern.compile("<name>(.*)</name><id>(.*)</id>");
                Matcher detail_matcher = detail_pattern.matcher(detail);
                if(detail_matcher.find()){
                    name = detail_matcher.group(1);
                    id = detail_matcher.group(2);
                    insertUser(Integer.parseInt(id), name);
                }
            }
        }
        users_label.setText("房间内人数："+users_map.size());
    }

    public void updateTextArea(String content){
        insertMessage(textScrollPane, msgArea, null, "系统：", content);
    }

    public void updateTextAreaFromUser(String content){
        if(content.length()>0){
            Pattern pattern = Pattern.compile("<from>(.*)</from><smsg>(.*)</smsg>");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()){
                String from = matcher.group(1);
                String smsg = matcher.group(2);
                String fromName = getUserName(from);
                if(fromName.equals(name))
                    fromName = "你";
                if(smsg.startsWith("<emoji>")){
                    String emojiCode = smsg.substring(7, smsg.length()-8);
                    insertMessage(textScrollPane, msgArea, emojiCode, fromName+"说：", null);
                    return ;
                }
                insertMessage(textScrollPane, msgArea, null, fromName+"说：", smsg);
            }
        }
    }

    public void showEscDialog(String content){
        JOptionPane.showMessageDialog(frame, content);
        /*清除消息区内容，清除用户数据模型内容和用户map内容，更新房间内人数*/
        msgArea.setText("");
        users_model.clear();
        users_map.clear();
        users_label.setText("房间内人数：0");

    }


    public void addRoom(String content){
        if(content.length()>0){
            Pattern pattern = Pattern.compile("<rid>(.*)</rid><rname>(.*)</rname>");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()){
                String rid = matcher.group(1);
                String rname = matcher.group(2);
                insertRoom(Integer.parseInt(rid), rname);
            }
        }
        rooms_label.setText("当前房间数："+rooms_map.size());
    }

    public void delRoom(String content){
        if(content.length()>0){
            int delRoomId = Integer.parseInt(content);

            Set<String> set = rooms_map.keySet();
            Iterator<String> iter = set.iterator();
            String rname=null;
            while(iter.hasNext()){
                rname = iter.next();
                if(rooms_map.get(rname)==delRoomId){
                    rooms_model.removeElement(rname);
                    break;
                }
            }
            rooms_map.remove(rname);
        }
        rooms_label.setText("当前房间数："+rooms_map.size());
    }

    public void listRooms(String content){
        String rname = null;
        String rid=null;
        Pattern rough_pattern=null;
        Matcher rough_matcher=null;
        Pattern detail_pattern=null;
        if(content.length()>0){
            rough_pattern = Pattern.compile("<room>(.*?)</room>");
            rough_matcher = rough_pattern.matcher(content);
            while(rough_matcher.find()){
                String detail = rough_matcher.group(1);
                detail_pattern = Pattern.compile("<rname>(.*)</rname><rid>(.*)</rid>");
                Matcher detail_matcher = detail_pattern.matcher(detail);
                if(detail_matcher.find()){
                    rname = detail_matcher.group(1);
                    rid = detail_matcher.group(2);
                    insertRoom(Integer.parseInt(rid), rname);
                }
            }
        }
        rooms_label.setText("当前房间数："+rooms_map.size());
    }

    private void insertRoom(Integer rid, String rname){
        if(!rooms_map.containsKey(rname)){
            rooms_map.put(rname, rid);
            rooms_model.addElement(rname);
        }else{
            rooms_map.remove(rname);
            rooms_model.removeElement(rname);
            rooms_map.put(rname, rid);
            rooms_model.addElement(rname);
        }
        rooms_label.setText("当前房间数："+rooms_map.size());
    }

    private void insertUser(Integer id, String name){
        if(!users_map.containsKey(name)){
            users_map.put(name, id);
            users_model.addElement(name);
        }else{
            users_map.remove(name);
            users_model.removeElement(name);
            users_map.put(name, id);
            users_model.addElement(name);
        }
        users_label.setText("房间内人数："+users_map.size());
    }

    private String getUserName(String strId){
        int uid = Integer.parseInt(strId);
        Set<String> set = users_map.keySet();
        Iterator<String> iterator = set.iterator();
        String cur=null;
        while(iterator.hasNext()){
            cur = iterator.next();
            if(users_map.get(cur)==uid){
                return cur;
            }
        }
        return "";
    }

    private String getRoomName(String strId){
        int rid = Integer.parseInt(strId);
        Set<String> set = rooms_map.keySet();
        Iterator<String> iterator = set.iterator();
        String cur = null;
        while(iterator.hasNext()){
            cur = iterator.next();
            if(rooms_map.get(cur)==rid){
                return cur;
            }
        }
        return "";
    }

    private void insertMessage(JScrollPane scrollPane, JTextPane textPane,
                               String icon_code, String title, String content){
        StyledDocument document = textPane.getStyledDocument();  /*获取textpane中的文本*/
        /*设置标题的属性*/
        SimpleAttributeSet title_attr = new SimpleAttributeSet();
        StyleConstants.setBold(title_attr, true);
        StyleConstants.setForeground(title_attr, Color.BLUE);
        /*设置正文的属性*/
        SimpleAttributeSet content_attr = new SimpleAttributeSet();
        StyleConstants.setBold(content_attr, false);
        StyleConstants.setForeground(content_attr, Color.BLACK);
        Style style = null;
        if(icon_code!=null){
            Icon icon = new ImageIcon("icon/"+icon_code+".png");
            style = document.addStyle("icon", null);
            StyleConstants.setIcon(style, icon);
        }

        try {
            document.insertString(document.getLength(), title+"\n", title_attr);
            if(style!=null)
                document.insertString(document.getLength(), "\n", style);
            else
                document.insertString(document.getLength(), " "+content+"\n", content_attr);

        } catch (BadLocationException ex) {
            System.out.println("Bad location exception");
        }
        vertical.setValue(vertical.getMaximum());
    }

    public static void setUIFont()
    {
        Font f = new Font("微软雅黑", Font.PLAIN, 14);
        String names[]={ "Label", "CheckBox", "PopupMenu","MenuItem", "CheckBoxMenuItem",
                "JRadioButtonMenuItem","ComboBox", "Button", "Tree", "ScrollPane",
                "TabbedPane", "EditorPane", "TitledBorder", "Menu", "TextArea","TextPane",
                "OptionPane", "MenuBar", "ToolBar", "ToggleButton", "ToolTip",
                "ProgressBar", "TableHeader", "Panel", "List", "ColorChooser",
                "PasswordField","TextField", "Table", "Label", "Viewport",
                "RadioButtonMenuItem","RadioButton", "DesktopPane", "InternalFrame"
        };
        for (String item : names) {
            UIManager.put(item+ ".font",f);
        }
    }

    public static void setUIStyle(){
        String lookAndFeel =UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
    }
}
