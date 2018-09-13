package br.com.scicrop.s3batchuploader.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import br.com.scicrop.commons.Constants;



public class MainGuiDesktop implements ActionListener, PropertyChangeListener { 
	JPanel cards; 

	final static String BUTTONPANEL = "Buttons";


	JButton executeButton;
	JFileChooser fc = new JFileChooser();
	JFileChooser dc = new JFileChooser();

	String inputButtonLabel = "Set input exec";
	String outputButtonLabel = "Set output path";

	String input = null, output = null;
	JCheckBox typeProcA = null;

	
	private Task task;
	private JProgressBar progressBar;
	boolean done = false;

	JTextArea console;
	
	JCheckBox unicodeChk = null;

	public void addComponentToPane(Container pane) {

		unicodeChk = new JCheckBox("unicode");

		progressBar = new JProgressBar(0, 100);
		progressBar.setString("ready.");
		progressBar.setStringPainted(true);

		executeButton  = new JButton("Spell!");
		executeButton.addActionListener(this);

		console = new JTextArea();
		console.setBackground(Color.BLACK);
		console.setForeground(Color.GREEN);
		

		typeProcA = new JCheckBox("LP");
		typeProcA.setSelected(true);

				
		console.setBounds(4,154+30+8,494, 160);
		console.setEditable(true);
		console.setEnabled(true);
		console.setAutoscrolls(true);
		
		unicodeChk.setSelected(true);
		unicodeChk.setBounds(125, 152, 70, 30);
		typeProcA.setBounds(195, 152, 40, 30);
		executeButton.setBounds(235, 152, 70, 30);
		progressBar.setBounds(300, 152, 197, 30);
		JScrollPane sp = new JScrollPane(console);
		sp.setBounds(5,154+30+2,494, 180);
		
		BufferedImage logoTitle = null;
		try {
			logoTitle = ImageIO.read(new File("./resources/logoTitle.png"));
			JLabel logoTitleLabel = new JLabel(new ImageIcon( logoTitle ));
			logoTitleLabel.setBounds(0, 0, 498, 149);
			pane.add(logoTitleLabel, BorderLayout.PAGE_START);
		} catch (IOException e) {
			String msg = "Wrong installation. The executable file must be parallel to resources/ directory. The application will try to run anyway.";
			JOptionPane.showMessageDialog(new JFrame(),msg,"Error",JOptionPane.ERROR_MESSAGE);
		}
		pane.setLayout(null);

		pane.add(unicodeChk);
		pane.add(typeProcA);
		pane.add(executeButton);
		pane.add(progressBar);
		pane.add(sp);
		
	}

	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, (String)evt.getItem());
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	private static void createAndShowGUI() {

		JFrame frame = new JFrame(Constants.APP_NAME+" - "+Constants.APP_VERSION);
	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MainGuiDesktop gui = new MainGuiDesktop();
		gui.addComponentToPane(frame.getContentPane());

		ImageIcon icon = new ImageIcon("./resources/Blue-Magic-Bunny-icon.png");
		frame.setIconImage(icon.getImage());

		frame.pack(); 
		frame.setSize(510, 400);
		frame.setResizable(false);
		frame.setVisible(true);

	}

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		UIManager.put("swing.boldMetal", Boolean.FALSE);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	
	public void actionPerformed(ActionEvent e) {
		console.setForeground(Color.GREEN);
		 if(e.getSource() == executeButton){

			
			executeButton.setEnabled(done);
			
			

			task = new Task();
			task.addPropertyChangeListener(this);
			task.execute();
		}
	}

	class Task extends SwingWorker<Void, Void> {

		@Override
		public Void doInBackground() {


			while (!done) {



			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			done = true;

			executeButton.setEnabled(done);
		}
	}

	
	public void propertyChange(PropertyChangeEvent arg0) {
		if (!done){
			progressBar.setString("running...");
			progressBar.setIndeterminate(true);
		} else {
			progressBar.setIndeterminate(false);
			done = false;
		}
	}

}
