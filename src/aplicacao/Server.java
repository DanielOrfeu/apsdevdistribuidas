package aplicacao;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Server {

    private static Socket socket;
    private static ServerSocket srvSocket;
    private static byte[] objectAsByte;

    public static void main(String args[]) {
        try {
            //1 Iniciando o server e aguardando a conexão do cliente.
            srvSocket = new ServerSocket(5566);
            System.out.println("Aguardando envio de arquivo ...");

            while (true) {
                socket = srvSocket.accept();
                new Thread(new ServerListen(socket)).start();
            }

        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, sw.toString());
        }

    }

    private static class ServerListen implements Runnable {

        public ServerListen(Socket socket) throws SocketException, IOException {
            //2 Saber o tamanho do arquivo/objeto em bytes e capturar o que foi lido do inputStream do socket  
            objectAsByte = new byte[socket.getReceiveBufferSize()];
            BufferedInputStream bf = new BufferedInputStream(
                    socket.getInputStream());
            bf.read(objectAsByte);
        }

        @Override
        public void run() {
            try {
                //3 Transforma bytes em um objeto Arquivo
                Arquivo arquivo = (Arquivo) getObjectFromByte(objectAsByte);
                
                //4 Formata o diretório de destino
                String dir = arquivo.getDiretorioDestino().endsWith("/") ? arquivo
                        .getDiretorioDestino() + arquivo.getNome() : arquivo
                        .getDiretorioDestino() + "/" + arquivo.getNome();
                System.out.println("Escrevendo arquivo " + dir);
                
                //5 Gravando arquivos no diretório de destino
                FileOutputStream fos;
                fos = new FileOutputStream(dir);
                fos.write(arquivo.getConteudo());
                fos.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static Object getObjectFromByte(byte[] objectAsByte) {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(objectAsByte);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();

            bis.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, sw.toString());
        }
        return obj;
    }
}
