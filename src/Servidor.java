import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javafx.util.Pair;

public class Servidor extends Thread {

    private static ArrayList<Pair<String, BufferedWriter>> clientes;
    private static ServerSocket server;
    private String nome;
    private Socket con;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;


    public Servidor(Socket con){
        this.con = con;
        try {
            in  = con.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){

        try{
            String msg;
            String[] msgArray;
            String type = "";
            String from = "";
            String to = "";
            String body = "";

            OutputStream ou =  this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            nome = msg = bfr.readLine();

            clientes.add(new Pair <String, BufferedWriter> (nome, bfw));

            //Servidor escutando mensagens dos clientes:
            while(!"exit".equalsIgnoreCase(type)){

                msg = bfr.readLine();

                if (msg == null){
                    continue;
                } else {
                    System.out.println(msg);

                    msgArray = msg.split(";");
                    type = msgArray[0].split(":")[1];
                    from = msgArray[1].split(":")[1];
                    to = msgArray[2].split(":")[1];
                    body = msgArray[3].split(":")[1];

                    if (type.equalsIgnoreCase("contacts")){
                        // Isso vai enviar os contatos pra todos os clientes,
                        // TODO: tem que  modificar pra enviar somenta para um.

                        String contatos = "";

                        for(Pair <String, BufferedWriter> cliente: clientes){
                            contatos += cliente.getKey() + ",";
                        }
                        sendContatos(from, contatos);

                    } else if(type.equalsIgnoreCase("msg")){
                        if(to.equalsIgnoreCase("all"))
                            sendToAll(bfw, body);
                        else
                            sendDirect(to, body);

                    }
                }

            }

        }catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException
    {
        BufferedWriter bwS;

        String payload = "type:msg;from:"+nome+";to:all;body:"+msg+"\r\n";

        for(Pair <String, BufferedWriter> cliente : clientes){
            bwS = (BufferedWriter)cliente.getValue();
            if(!(bwSaida == bwS)){ // if usado para nao mandar para ele mesmo
                cliente.getValue().write(payload);
                cliente.getValue().flush();
            }
        }
    }

    public void sendDirect(String to, String msg) throws  IOException
    {
        String payload = "type:msg;from:"+nome+";to:"+to+";body:"+msg+"\r\n";
        //String[] toArray = to.split();

        for(Pair <String, BufferedWriter> cliente : clientes){
            if(cliente.getKey().equalsIgnoreCase(to)) {
                cliente.getValue().write(payload);
                cliente.getValue().flush();
            }
        }
    }

    public void sendContatos(String to, String msg) throws  IOException
    {
        //BufferedWriter bwS;
        String payload = "type:contacts;from:server;to:"+to+";body:"+msg+"\r\n";
        System.out.println(payload);
        for(Pair <String, BufferedWriter> cliente : clientes){
            if(cliente.getKey().equalsIgnoreCase(to)){
                cliente.getValue().write(payload);
                cliente.getValue().flush();
            }
            //bwS = (BufferedWriter)bw;
        }
    }

    public static void main(String []args) {

        try{
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("12345");
            Object[] texts = {lblMessage, txtPorta };
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clientes = new ArrayList<Pair<String, BufferedWriter>>();
            JOptionPane.showMessageDialog(null,"Servidor ativo na porta: "+
                    txtPorta.getText());

            while(true){
                System.out.println("Aguardando conexão...");
                Socket con = server.accept();
                System.out.println("Cliente conectado...");
                Thread t = new Servidor(con);
                t.start();
            }

        }catch (Exception e) {

            e.printStackTrace();
        }
    }
}



