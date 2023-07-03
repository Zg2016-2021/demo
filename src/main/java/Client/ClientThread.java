package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread{
    private Socket socket;
    private Client client;
    private BufferedReader br;
    private PrintWriter pw;

    public ClientThread(Socket socket, Client client){
        this.client = client;
        this.socket = socket;
        try {
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.out.println("cannot get inputstream from socket.");
        }
    }

    public void run() {
        try{
            br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(true){
                String msg = br.readLine();
                parseMessage(msg);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseMessage(String message){
        String code = null;
        String msg=null;

        if(message.length()>0){
            Pattern pattern = Pattern.compile("<code>(.*)</code>");
            Matcher matcher = pattern.matcher(message);
            if(matcher.find()){
                code = matcher.group(1);
            }
            pattern = Pattern.compile("<msg>(.*)</msg>");
            matcher = pattern.matcher(message);
            if(matcher.find()){
                msg = matcher.group(1);
            }
            System.out.println(code+":"+msg);
            switch(code){
                case "1":
                    client.updateTextArea(msg);
                    break;
                case "2":
                    client.showEscDialog(msg);
                    break;
                case "3":
                    client.listRooms(msg);
                    break;
                case "4":
                    client.updateTextAreaFromUser(msg);
                    break;
                case "5":
                    client.updateTextArea(msg);
                    break;
                case "11":
                    client.addUser(msg);
                    break;
                case "12":
                    client.delUser(msg);
                    break;
                case "13":
                    client.delRoom(msg);
                    break;
                case "15":
                    client.addRoom(msg);
                    break;
                case "16":
                    client.updateUser(msg);
                    break;
                case "21":
                    client.listUsers(msg);
                    break;
            }
        }

    }
}
