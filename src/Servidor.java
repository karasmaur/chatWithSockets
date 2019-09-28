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
import java.io.BufferedWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Servidor extends Thread {

    private static ArrayList<BufferedWriter> clientes;
    private static ArrayList<String> nomesClientes;
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
            OutputStream ou =  this.con.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clientes.add(bfw);
            nome = msg = bfr.readLine();
            nomesClientes.add(nome);

            while(!"Sair".equalsIgnoreCase(msg))
            {
                if (msg == null){
                    //faz nada
                } else if (msg.equalsIgnoreCase("getcontato")){
                    // Isso vai enviar os contatos pra todos os clientes, tem que  modificar pra enviar somenta para um.

                    String contatos = "!contatos;";

                    for(String cliente: nomesClientes){
                        contatos += cliente + ";";
                    }

                    sendContatos(bfw, contatos);
                    msg = null;

                } else {
                    msg = bfr.readLine();
                    sendToAll(bfw, msg);
                    System.out.println(msg);
                }

            }

        }catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException
    {
        BufferedWriter bwS;

        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){ // if usado para nao mandar para ele mesmo
                bw.write(nome + " -> " + msg+"\r\n");
                bw.flush();
            }
        }
    }

    public void sendContatos(BufferedWriter bwSaida, String msg) throws  IOException
    {
        BufferedWriter bwS;

        for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            bw.write(msg+"\r\n");
            bw.flush();

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
            clientes = new ArrayList<BufferedWriter>();
            nomesClientes = new ArrayList<String>();
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



