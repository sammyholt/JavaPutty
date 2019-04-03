import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

/**
 * A Java SSH client for ECE 4303 (TCP/IP). A simple implementation of an application like PuTTY.
 *
 */
public class JavaPutty {

	private JFrame frame;
	private JTextField hostTextField;
	private JLabel lblUsername;
	private JTextField usernameTextField;
	private JLabel lblPassword;
	private JPasswordField passwordTextField;
	private JTextField sendTextField;
	private JTextArea textArea;
	private JButton btnSend;
	private String hostName;
	private String userName;
	private String password;
	private JSch jsch;
	private PrintStream print;
	private Session session;
	private Channel channel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaPutty window = new JavaPutty();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JavaPutty() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 679, 619);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Java SSH Client");
		frame.getContentPane().setLayout(null);
		
		hostTextField = new JTextField();
		hostTextField.setToolTipText("");
		hostTextField.setBounds(83, 11, 568, 30);
		frame.getContentPane().add(hostTextField);
		hostTextField.setColumns(10);
		hostTextField.setText("login.cpp.edu");
		
		JLabel lblHostIp = new JLabel("Host IP:");
		lblHostIp.setBounds(7, 15, 71, 22);
		frame.getContentPane().add(lblHostIp);
		
		lblUsername = new JLabel("Username:");
		lblUsername.setBounds(7, 65, 71, 14);
		frame.getContentPane().add(lblUsername);
		
		usernameTextField = new JTextField();
		usernameTextField.setBounds(83, 57, 568, 30);
		frame.getContentPane().add(usernameTextField);
		usernameTextField.setColumns(10);
		
		lblPassword = new JLabel("Password:");
		lblPassword.setBounds(7, 109, 63, 14);
		frame.getContentPane().add(lblPassword);
		
		passwordTextField = new JPasswordField();
		passwordTextField.setBounds(83, 101, 568, 30);
		frame.getContentPane().add(passwordTextField);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation") // ignore deprecated getText method from the password field
			public void actionPerformed(ActionEvent arg0) {
				// Connect button pressed, create new SSH session
				hostName = hostTextField.getText();
				userName = usernameTextField.getText();
				password = passwordTextField.getText();
				
				if(!hostName.isEmpty() && !userName.isEmpty() && !password.isEmpty()) {
					// required fields are supplied
					jsch = new JSch();
					
					// use piped streams to redirect output to text area and input to text field
					PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
					PipedInputStream pip = new PipedInputStream(40);
					try {
						PipedOutputStream pop = new PipedOutputStream(pip);
						print = new PrintStream(pop);
					} catch (IOException e1) {
						// issue with piped streams
						e1.printStackTrace();
					}
					
					try {
						session = jsch.getSession(userName, hostName);
						session.setPassword(password);
						
						// stop strict host checking to avoid unknown host error
						java.util.Properties config = new java.util.Properties(); 
						config.put("StrictHostKeyChecking", "no");
						session.setConfig(config);
						
						session.connect(30000); // timeout of 30000
						
						channel = session.openChannel("shell");
						
						channel.setInputStream(pip);
						
						channel.setOutputStream(printStream);
						
						// set channel pty type to dumb to exclude color coded characters
						((ChannelShell) channel).setPtyType("dumb");
						
						
						channel.connect(3000); // timeout of 3000
						
						btnConnect.setEnabled(false);
						btnSend.setEnabled(true);
						sendTextField.setEnabled(true);
						hostTextField.setEnabled(false);
						usernameTextField.setEnabled(false);
						passwordTextField.setEnabled(false);
					} catch (JSchException e) {
						// issue with JSch
						e.printStackTrace();
					} catch (Exception e) {
						// issue setting pty type
						e.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(null, "You need to supply a host, username, and password!");
				}
				
				
				
			}
		});
		btnConnect.setBounds(7, 148, 644, 40);
		frame.getContentPane().add(btnConnect);
		
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(7, 199, 644, 329);
		
		frame.getContentPane().add(scrollPane);
		
		
		sendTextField = new JTextField();
		sendTextField.setBounds(7, 539, 521, 30);
		frame.getContentPane().add(sendTextField);
		sendTextField.setColumns(10);
		sendTextField.setEnabled(false);
		
		btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// send command button clicked
				String send = sendTextField.getText();
				print.println(send);
				sendTextField.setText("");
			}
		});
		
		sendTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// user pressed enter key, send command
				String send = sendTextField.getText();
				print.println(send);
				sendTextField.setText("");
			}
		});
		
		btnSend.setBounds(538, 539, 113, 30);
		btnSend.setEnabled(false);
		frame.getContentPane().add(btnSend);
		
	}
}
