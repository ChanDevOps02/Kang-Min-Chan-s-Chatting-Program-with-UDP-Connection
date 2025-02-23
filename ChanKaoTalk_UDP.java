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

public class ChanKaoTalk_UDP extends Frame implements KeyListener {
    Panel panel; //최상단 라벨과 버튼들을 배치할 판넬
    Label isReady; //멀티캐스트 그룹과의 연결상태를 표시하는 라벨 (True -> Status : Connected successfully! / False -> Status : Disconnected..)
    Button Connect; //멀티캐스트 그룹에 해당기기를 연결시키는 기능
    Button Send; //멀티캐스트 그룹에 포함되어 있는 기기들에게 메세지를 뿌리는 기능
    Button exit; //해당 멀티캐스트 그룹에서 나가는 기능
    TextArea talk_list; //멀티캐스트 그룹에 포함되어 있는 기기들이 송수신한 메세지를 나열
    TextArea send_message;//내가 보낼 메세지를 하단에 표시

    InetAddress group;
    String multicastAddress = "230.0.0.0";
    int port = 9000; //멀티캐스트 포트
    MulticastSocket socket; //멀티캐스트 소켓

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

        this.addKeyListener(this);

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
                talk_list.append("Other : " + message + "\n");
            }catch(IOException ie){
                ie.printStackTrace();
            }
        }
    }

    public void sendMessage(){
        String message = send_message.getText();
        if(!message.isEmpty() && socket != null && !socket.isClosed()){
            try{
                byte[] bytes = message.getBytes();
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

    public void keyPressed(KeyEvent ke){
        if(ke.getKeyCode() == KeyEvent.VK_ENTER){
            sendMessage();
        }
    }
    public void keyReleased(KeyEvent ke){}
    public void keyTyped(KeyEvent ke){}
    public static void main(String[] args){
        Frame frame = new ChanKaoTalk_UDP();
    }
}
