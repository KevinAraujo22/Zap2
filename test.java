package zap2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
public class test extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField messageField;
   private JTextArea chatArea;
   private JButton connectButton;
   private JPanel connectionPanel;
   private JPanel chatPanel;
   private CardLayout cardLayout;
   private String userName;
   private ServerSocket serverSocket;
   private Socket socket;
   private BufferedReader reader;
   private PrintWriter writer;
   public test(boolean isServer) {
       super("Zap2");
       messageField = new JTextField(30);
       chatArea = new JTextArea();
       chatArea.setEditable(false);
       chatArea.setBackground(Color.BLACK);
       chatArea.setForeground(Color.WHITE);
       chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
       JScrollPane scrollPane = new JScrollPane(chatArea);
       connectButton = new JButton("Conectar");
       messageField.addActionListener(new SendMessage());
       connectButton.addActionListener(new ActionListener() {
           @Override
          
           public void actionPerformed(ActionEvent e) {
               if (isServer) {
                   userName = JOptionPane.showInputDialog("Qual é o seu nome?");
                   startServer();
               } else {
                   String serverIP = JOptionPane.showInputDialog("Insira o IP do Server:");
                   int serverPort = Integer.parseInt(JOptionPane.showInputDialog("Insira a porta do Server:"));
                   userName = JOptionPane.showInputDialog("Qual é o seu nome?");
                   connectToServer(serverIP, serverPort);
               }
           }
       });
       connectionPanel = new JPanel();
       connectionPanel.setLayout(new BorderLayout());
       connectionPanel.add(connectButton, BorderLayout.NORTH);
       chatPanel = new JPanel();
       chatPanel.setLayout(new BorderLayout());
       chatPanel.add(scrollPane, BorderLayout.CENTER);
       chatPanel.add(messageField, BorderLayout.SOUTH);
       cardLayout = new CardLayout();
       setLayout(cardLayout);
       add(connectionPanel, "connection");
       add(chatPanel, "Zap2");
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setSize(400, 600);
       setLocationRelativeTo(null);
       setResizable(false);
       setVisible(true);
   }
   private void startServer() {
       try {
           serverSocket = new ServerSocket(5000);
           appendMessage("Server inicializado!");
           socket = serverSocket.accept();
           reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           writer = new PrintWriter(socket.getOutputStream(), true);
           appendMessage("Client conectado!");
           String clientName = receiveMessage();
           appendMessage(clientName + " entrou na conversa!");
           Thread receiveThread = new Thread(new ReceiveMessages());
           receiveThread.start();
          
           cardLayout.show(getContentPane(), "Zap2");
       } catch (IOException e) {
           e.printStackTrace();
           JOptionPane.showMessageDialog(this, "Erro ao inicializar o Server!");
       }
   }
   private void connectToServer(String serverIP, int serverPort) {
       try {
           socket = new Socket(serverIP, serverPort);
           reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           writer = new PrintWriter(socket.getOutputStream(), true);
           appendMessage("Conectado ao Servidor!");
           sendMessage(userName);
           appendMessage("Você entrou na conversa!");
           Thread receiveThread = new Thread(new ReceiveMessages());
           receiveThread.start();
          
           cardLayout.show(getContentPane(), "Zap2");
       } catch (IOException e) {
           e.printStackTrace();
           JOptionPane.showMessageDialog(this, "Erro ao se conectar com o Server!");
       }
   }
   private void sendMessage(String message) {
       writer.println(message);
   }
   private String receiveMessage() {
       try {
           return reader.readLine();
       } catch (IOException e) {
           e.printStackTrace();
           JOptionPane.showMessageDialog(this, "Erro ao receber mensagem!");
           return null;
       }
   }
   private void appendMessage(String message) {
       chatArea.append(message + "\n");
       chatArea.setCaretPosition(chatArea.getDocument().getLength());
   }
   private void appendSentMessage(String message) {
       String formattedMessage = "EU: " + message + "\n";
       chatArea.append(formattedMessage);
       chatArea.setCaretPosition(chatArea.getDocument().getLength());
   }
   private class ReceiveMessages implements Runnable {
       @Override
       public void run() {
           try {
               String message;
               while ((message = reader.readLine()) != null) {
                   if (message.equals("quit")) {
                       appendMessage("Client disconectado!");
                       break;
                   }
                   appendMessage(message);
               }
           } catch (IOException e) {
               e.printStackTrace();
               JOptionPane.showMessageDialog(test.this, "Erro ao receber mensagem!");
           }
       }
   }
   private class SendMessage implements ActionListener {
       @Override
       public void actionPerformed(ActionEvent e) {
           String message = messageField.getText();
           sendMessage(userName + ": " + message);
           appendSentMessage(message);
           messageField.setText("");
       }
   }
   public static void main(String[] args) {
       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               String[] options = { "Server", "Client" };
               int choice = JOptionPane.showOptionDialog(
                       null,
                       "Escolha um modo:",
                       "Zap2",
                       JOptionPane.DEFAULT_OPTION,
                       JOptionPane.QUESTION_MESSAGE,
                       null,
                       options,
                       options[0]
               );
               boolean isServer = (choice == 0);
               new test(isServer);
           }
       });
   }
}
