package sec02.exam01;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ChanKaoTalk_UDP extends Frame {
    Panel panel;
    Label isReady;
    Button Connect;
    Button Send;
    Button exit;
    TextArea talk_list;
    TextArea send_message;

    InetAddress group;
    String multicastAddress = "230.0.0.1";
    int port = 9000;
    MulticastSocket socket;

    String username = "User" + (int)(Math.random() * 1000);

    public ChanKaoTalk_UDP(){
        setTitle("Multicast chat program");
        setSize(800,800);
        setLayout(null);

        panel = new Panel();
        panel.setBackground(Color.YELLOW);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));
        panel.setBounds(0,0,800,70);

        isReady = new Label("Disconnected...");

        Connect = new Button("Connect with Multicast group");
        Send = new Button("Send");
        exit = new Button("Exit");
        Connect.addActionListener((ae) -> connectToGroup());
        Send.addActionListener((ae) -> sendMessage());
        exit.addActionListener((ae) -> leaveGroup());

        panel.add(isReady);
        panel.add(Connect);
        panel.add(Send);
        panel.add(exit);

        talk_list = new TextArea(790, 500);
        talk_list.setBackground(Color.LIGHT_GRAY);
        talk_list.setEditable(false);
        talk_list.setBounds(50,90,700,450);

        send_message = new TextArea(790,300);
        send_message.setBackground(Color.WHITE);
        send_message.setBounds(50,550,700,200);

        add(panel);
        add(talk_list);
        add(send_message);

        send_message.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_ENTER){
                    sendMessage();
                    ke.consume();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {}
            @Override
            public void keyTyped(KeyEvent ke) {}
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leaveGroup();
                dispose();
                System.exit(0);
            }
        });
        setVisible(true);
    }

    public void connectToGroup(){
        try{
            group = InetAddress.getByName(multicastAddress);
            socket = new MulticastSocket(port);
            socket.joinGroup(group);
            isReady.setText("Status : Connected successfully");
            new Thread(this::receiveMessages).start();
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }

    public void receiveMessages(){
        while(socket != null && !socket.isClosed()){
            try{
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String (packet.getData(), 0, packet.getLength());
                if (!message.startsWith(username + " : ")) {
                    talk_list.append(message + "\n");
                }
            }catch(IOException ie){
                ie.printStackTrace();
            }
        }
    }

    public void sendMessage(){
        String message = send_message.getText().trim();
        if(!message.isEmpty() && socket != null && !socket.isClosed()){
            try{
                String formattedMessage = username + " : " + message;
                byte[] bytes = formattedMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, port);
                socket.send(packet);
                talk_list.append("Me : " + message + "\n");
                send_message.setText("");
            }catch(IOException ie){
                ie.printStackTrace();
            }
        }
    }

    public void leaveGroup(){
        if(socket != null && !socket.isClosed()){
            try{
                socket.leaveGroup(group);
                socket.close();
                isReady.setText("Disconnected...");
            }catch(IOException ie){
                ie.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        Frame frame = new ChanKaoTalk_UDP();
    }
}
