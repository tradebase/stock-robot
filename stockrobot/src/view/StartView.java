package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class StartView extends JFrame {

	private JPanel contentPane;
	private JTextField txtLocalhost;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartView frame = new StartView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StartView() {
		setTitle("Stock Robot");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 378, 209);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JCheckBox chckbxSimulateStocks = new JCheckBox("Simulate Stocks");
		
		JButton btnStartParser = new JButton("Start Parser");
		
		txtLocalhost = new JTextField();
		txtLocalhost.setText("localhost:54551");
		txtLocalhost.setColumns(10);
		
		JButton btnStartAstro = new JButton("Start ASTRo");
		
		textField = new JTextField();
		textField.setText("54551");
		textField.setColumns(10);
		
		JLabel lblPort = new JLabel("Port:");
		
		JLabel lblParserServer = new JLabel("Parser server:");
		
		JButton btnStopParser = new JButton("Stop Parser");
		
		JButton btnStopAstro = new JButton("Stop ASTRo");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(chckbxSimulateStocks)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblPort)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnStartParser)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnStopParser))
						.addComponent(txtLocalhost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnStartAstro)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnStopAstro))
						.addComponent(lblParserServer))
					.addContainerGap(113, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxSimulateStocks)
						.addComponent(lblPort)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStartParser)
						.addComponent(btnStopParser))
					.addGap(23)
					.addComponent(lblParserServer)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtLocalhost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnStartAstro)
						.addComponent(btnStopAstro))
					.addContainerGap(116, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
}