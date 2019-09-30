import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class Cliente extends JFrame implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private JTextArea texto;
    private JTextField txtMsg;

    private JButton btnSend;
    private JButton btnDirectSend;
    private JButton btnSair;
    private JButton btnAtualizarContatos;

    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JPanel pnlContent;

    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private JList contactList;
    private DefaultListModel contatos;

    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;



    public Cliente() throws IOException {
        JLabel lblMessage = new JLabel("Verificar!");
        txtIP = new JTextField("127.0.0.1");
        txtPorta = new JTextField("12345");
        txtNome = new JTextField("Cliente");
        Object[] texts = {lblMessage, txtIP, txtPorta, txtNome};
        JOptionPane.showMessageDialog(null, texts);

        pnlContent = new JPanel();

        texto = new JTextArea(10, 20);
        texto.setEditable(false);
        texto.setBackground(new Color(252, 243, 249, 135));
        txtMsg = new JTextField(20);
        lblHistorico = new JLabel("Histórico");
        lblMsg = new JLabel("Mensagem");
        btnSend = new JButton("Enviar");
        btnDirectSend = new JButton("Direct Msg");

        btnSend.setToolTipText("Enviar Mensagem");
        btnSair = new JButton("Sair");
        btnAtualizarContatos = new JButton("Atualizar");
        btnSair.setToolTipText("Sair do Chat");

        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnDirectSend.addActionListener(this);

        btnAtualizarContatos.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(texto);
        texto.setLineWrap(true);


        //test Jlist

        contatos = new DefaultListModel();
        contactList = new JList();

        pnlContent.add(contactList);
        pnlContent.add(btnAtualizarContatos);

        pnlContent.add(lblHistorico);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnSair);
        pnlContent.add(btnSend);
        pnlContent.add(btnAtualizarContatos);
        pnlContent.add(contactList);
        pnlContent.add(btnDirectSend);

        pnlContent.setBackground(Color.LIGHT_GRAY);
        texto.setBorder(BorderFactory.createEtchedBorder(Color.LIGHT_GRAY, Color.LIGHT_GRAY));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.LIGHT_GRAY, Color.LIGHT_GRAY));
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(500, 400);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void conectar() throws IOException {
        socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText() + "\r\n");
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException {
        String payload = "type:msg;from:"+txtNome.getText()+";to:all;body:"+msg+"\r\n";

        bfw.write(payload);
        texto.append(txtNome.getText() + " diz -> " + txtMsg.getText() + "\r\n");

        bfw.flush();
        txtMsg.setText("");
    }

    public void enviarMensagemDireta(String from, String to, String msg) throws IOException {
        String payload = "type:msg;from:"+from+";to:"+to+";body:"+msg+"\r\n";
        bfw.write(payload);
        texto.append(from + " to "+to+" diz -> " + msg + "\r\n");
        bfw.flush();
        txtMsg.setText("");
    }

    public void enviarSair() throws IOException {
        String payload = "type:exit;from:"+txtNome.getText()+";to:all;body: \r\n";
        bfw.write(payload);
        texto.append("Desconectado \r\n");
        bfw.flush();
        txtMsg.setText("");
    }

    public void buscarContatos() throws IOException {
        String payload = "type:contacts;from:"+txtNome.getText()+";to:all;body: \r\n";
        bfw.write(payload);
        System.out.println("Buscando contatos!");
        bfw.flush();
    }

    public void escutar () throws IOException {

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        System.out.println(msg);

        while (!"Sair".equalsIgnoreCase(msg)) {
            if (bfr.ready()) {
                msg = bfr.readLine();
                System.out.println(msg);

                String[] msgArray = msg.split(";");
                String type = msgArray[0].split(":")[1];
                String from = msgArray[1].split(":")[1];
                String to = msgArray[2].split(":")[1];
                String body = msgArray[3].split(":")[1];

                if (msg.equals("Sair")) {
                    texto.append("Servidor caiu! \r\n");
                } else if (type.equalsIgnoreCase("contacts")) {

                    System.out.println("Contatos buscados: " + body);
                    contatos = new DefaultListModel();
                    String[] contatosArray = body.split(",");

                    for (String contato :contatosArray) {
                        contatos.addElement(contato);
                    }
                    contactList.setModel(contatos);

                } else if(type.equalsIgnoreCase("msg")){
                    if(to.equalsIgnoreCase("all"))
                        texto.append(from + " diz -> "+body+"\r\n");
                    else
                        texto.append(from + " to "+to+" diz -> " + body + "\r\n");
                }
            }
        }
    }

    public void sair() throws IOException{
        enviarSair();
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                enviarMensagem(txtMsg.getText());
            else if(e.getActionCommand().equals(btnSair.getActionCommand()))
                sair();
            else if(e.getActionCommand().equals(btnAtualizarContatos.getActionCommand()))
                buscarContatos();
            else if(e.getActionCommand().equals(btnDirectSend.getActionCommand())) {
                System.out.println((String) contactList.getSelectedValue());
                enviarMensagemDireta(txtNome.getText(), (String) contactList.getSelectedValue(), txtMsg.getText());
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                enviarMensagem(txtMsg.getText());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }
    public static void main(String []args) throws IOException{
        Cliente app = new Cliente();
        app.conectar();
        app.escutar();
    }



}