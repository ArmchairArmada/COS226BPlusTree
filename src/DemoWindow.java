import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class DemoWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private RenderingBPTree<String,String> bpTree;
	private StringStringParse strParse;
	
	private JFileChooser fileChooser;
	private JPanel pnlTreeView;
	private JEditorPane editOutput;
	
	private JFormattedTextField txtInnerSize;
	private JFormattedTextField txtLeafSize;
	private JTextField txtModKey;
	private JTextField txtModValue;
	private JTextField txtQueryKey;
	private JTextField txtQueryValue;
	
	private JPanel pnlCmdFile;
	private JPanel pnlCmdModify;
	private JPanel pnlCmdQuery;
	private JPanel pnlCmdInfo;
	
	public DemoWindow() {
		super();
		
		fileChooser = new JFileChooser();
		strParse = new StringStringParse();
		
		setTitle("B+ Tree Demonstration");
		setLayout(new BorderLayout());
		
		add(Box.createRigidArea(new Dimension(2,2)), BorderLayout.PAGE_START);
		add(Box.createRigidArea(new Dimension(2,2)), BorderLayout.PAGE_END);
		add(Box.createRigidArea(new Dimension(2,2)), BorderLayout.LINE_START);
		add(Box.createRigidArea(new Dimension(2,2)), BorderLayout.LINE_END);
		
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		
		pnlTreeView = new JPanel();
		JScrollPane treePane = new JScrollPane(pnlTreeView,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		treePane.setPreferredSize(new Dimension(900, 500));
		
		pnl.add(treePane);
		
		pnl.add(Box.createRigidArea(new Dimension(2,2)));
		
		JPanel pnl2 = new JPanel();
		pnl2.setLayout(new BoxLayout(pnl2, BoxLayout.X_AXIS));
		
		// --- File Tab ---
		
		pnlCmdFile = new JPanel();
		pnlCmdFile.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JButton btnLoad = new JButton("Load From File");
		btnLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int retVal = fileChooser.showOpenDialog(DemoWindow.this);
				
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					
					try {
						bpTree.load(file.getAbsolutePath(), strParse, strParse);
						txtInnerSize.setValue(bpTree.getInnerSize());
						txtLeafSize.setValue(bpTree.getLeafSize());
						
						editOutput.setText("bpTree.load(\""+file.getAbsolutePath()+"\", stringStringParse, stringStringParse);");
					} catch (IOException e) {
						JOptionPane.showMessageDialog(DemoWindow.this, "Error loading file.");
					}
					bpTree.makePanel();
				}
			}
		});
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx=1.0;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=2;
		gbc.insets = new Insets(5,5,5,5);
		pnlCmdFile.add(btnLoad, gbc);
		
		JButton btnSave = new JButton("Save To File");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int retVal = fileChooser.showSaveDialog(DemoWindow.this);
				
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						bpTree.save(file.getAbsolutePath(), strParse, strParse);
						editOutput.setText("bpTree.save(\""+file.getAbsolutePath()+"\", stringStringParse, stringStringParse);");
					} catch (IOException e) {
						JOptionPane.showMessageDialog(DemoWindow.this, "Error loading file.");
					}
				}
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.weightx=0.5;
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=2;
		pnlCmdFile.add(btnSave, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.weightx=0.0;
		gbc.weightx=0.0;
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.gridwidth=1;
		pnlCmdFile.add(new JLabel("Inner Node Size:"), gbc);
		
		txtInnerSize = new JFormattedTextField(new Integer(5));
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx=1.0;
		gbc.gridx=1;
		gbc.gridy=2;
		gbc.gridwidth=1;
		pnlCmdFile.add(txtInnerSize, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx=0.0;
		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdFile.add(new JLabel("Leaf Node Size:"), gbc);
		
		txtLeafSize = new JFormattedTextField(new Integer(3));
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx=1.0;
		gbc.gridx=1;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdFile.add(txtLeafSize, gbc);
		
		JButton btnCreate = new JButton("Create New Tree");
		btnCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int i = (int) txtInnerSize.getValue();
				int l = (int) txtLeafSize.getValue();
				bpTree = new RenderingBPTree<String,String>(pnlTreeView, i, l);
				bpTree.makePanel();
				
				editOutput.setText("new BPTree&lt;String,String&gt;(" + i + ", " + l + ");");
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx=0;
		gbc.gridy=4;
		gbc.gridwidth=2;
		pnlCmdFile.add(btnCreate, gbc);
		
		// --- Modify Tab ---
		
		pnlCmdModify = new JPanel();
		pnlCmdModify.setLayout(new GridBagLayout());
		
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=1;
		pnlCmdModify.add(new JLabel("Key:"), gbc);
		
		gbc.weightx=1.0f;
		gbc.gridx=1;
		txtModKey = new JTextField();
		pnlCmdModify.add(txtModKey, gbc);
		
		JButton btnRndKey = new JButton("Rnd");
		btnRndKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtModKey.setText(rndString(5));
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=2;
		gbc.gridy=0;
		gbc.gridwidth=1;
		pnlCmdModify.add(btnRndKey, gbc);
		
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=1;
		pnlCmdModify.add(new JLabel("Value:"), gbc);
		
		gbc.weightx=1.0f;
		gbc.gridx=1;
		txtModValue = new JTextField();
		pnlCmdModify.add(txtModValue, gbc);
		
		JButton btnRndVal = new JButton("Rnd");
		btnRndVal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtModValue.setText(rndString(5).toLowerCase());
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=2;
		gbc.gridy=1;
		gbc.gridwidth=1;
		pnlCmdModify.add(btnRndVal, gbc);
		
		JButton btnPut = new JButton("Put Entry");
		btnPut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String oldVal = bpTree.put(txtModKey.getText(), txtModValue.getText());
				bpTree.makePanel();
				
				editOutput.setText("bpTree.put(\"" + txtModKey.getText() + "\", \"" + txtModValue.getText() + "\");<br><br><font style='color:blue'>" + oldVal + "</font>");
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=2;
		pnlCmdModify.add(btnPut, gbc);
		
		JButton btnPutRnd = new JButton("Put Rnd");
		btnPutRnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String k = rndString(5);
				String v = rndString(5).toLowerCase();
				
				txtModKey.setText(k);
				txtModValue.setText(v);
				
				String oldVal = bpTree.put(k, v);
				bpTree.makePanel();
				
				editOutput.setText("bpTree.put(\"" + k + "\", \"" + v + "\");<br><br><font style='color:blue'>" + oldVal + "</font>");
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=2;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdModify.add(btnPutRnd, gbc);
		
		JButton btnRemove = new JButton("Remove Entry");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String oldVal = bpTree.remove(txtModKey.getText());
				bpTree.makePanel();
				
				editOutput.setText("bpTree.remove(\"" + txtModKey.getText() + "\");<br><br><font style='color:blue'>" + oldVal + "</font>");
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=4;
		gbc.gridwidth=2;
		pnlCmdModify.add(btnRemove, gbc);
		
		JButton btnRemoveRnd = new JButton("Rem Rnd");
		btnRemoveRnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (bpTree.size() > 0) {
					ArrayList<String> keys = new ArrayList<String>(bpTree.keySet());
					String k = keys.get((int)(Math.random() * keys.size()));
					
					txtModKey.setText(k);
					
					String oldVal = bpTree.remove(k);
					bpTree.makePanel();
					
					editOutput.setText("bpTree.remove(\"" + k + "\");<br><br><font style='color:blue'>" + oldVal + "</font>");
				}
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=2;
		gbc.gridy=4;
		gbc.gridwidth=1;
		pnlCmdModify.add(btnRemoveRnd, gbc);
		
		// --- Query Tab ---
		
		pnlCmdQuery = new JPanel();
		pnlCmdQuery.setLayout(new GridBagLayout());
		
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=1;
		pnlCmdQuery.add(new JLabel("Key:"), gbc);
		
		gbc.weightx=1.0f;
		gbc.gridx=1;
		txtQueryKey = new JTextField();
		pnlCmdQuery.add(txtQueryKey, gbc);
		
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=1;
		pnlCmdQuery.add(new JLabel("Value/Key:"), gbc);
		
		gbc.weightx=1.0f;
		gbc.gridx=1;
		txtQueryValue = new JTextField();
		pnlCmdQuery.add(txtQueryValue, gbc);
		
		JButton btnGet = new JButton("Get Value");
		btnGet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.get(\"" + txtQueryKey.getText();
				s += "\");<br><br><font style='color:blue'>";
				s += bpTree.get(txtQueryKey.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnGet, gbc);
		
		JButton btnGetToKey = new JButton("Get First To Key");
		btnGetToKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.getFirstToKey(\"" + txtQueryKey.getText();
				s += "\");<br><br><font style='color:blue'>";
				s += bpTree.getFirstToKey(txtQueryKey.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=1;
		gbc.gridy=2;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnGetToKey, gbc);
		
		
		JButton btnGetToLast = new JButton("Get Key To Last");
		btnGetToLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.getKeyToLast(\"" + txtQueryKey.getText();
				s += "\");<br><br><font style='color:blue'>";
				s += bpTree.getKeyToLast(txtQueryKey.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnGetToLast, gbc);
		
		JButton btnGetKeyToKey = new JButton("Get Key To Key");
		btnGetKeyToKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.getKeyToKey(\"" + txtQueryKey.getText();
				s += "\", \"" + txtQueryValue.getText() + "\");<br><br><font style='color:blue'>";
				s += bpTree.getKeyToKey(txtQueryKey.getText(), txtQueryValue.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=1;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnGetKeyToKey, gbc);
		
		
		JButton btnContainsKey = new JButton("Contains Key");
		btnContainsKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.containsKey(\"" + txtQueryKey.getText();
				s += "\");<br><br><font style='color:blue'>";
				s += bpTree.containsKey(txtQueryKey.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=0;
		gbc.gridy=4;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnContainsKey, gbc);
		
		JButton btnContainsValue = new JButton("Contains Value");
		btnContainsValue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.containsValue(\"" + txtQueryValue.getText();
				s += "\");<br><br><font style='color:blue'>";
				s += bpTree.containsValue(txtQueryValue.getText()) + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=0.0f;
		gbc.gridx=1;
		gbc.gridy=4;
		gbc.gridwidth=1;
		pnlCmdQuery.add(btnContainsValue, gbc);
		
		// --- Info Tab ---
		
		pnlCmdInfo = new JPanel();
		pnlCmdInfo.setLayout(new GridBagLayout());
		
		JButton btnSize = new JButton("Size");
		btnSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.size();<br><br><font style='color:blue'>";
				s += bpTree.size() + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=1.0f;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=1;
		pnlCmdInfo.add(btnSize, gbc);
		
		JButton btnCountNodes = new JButton("Count Nodes");
		btnCountNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.countNodes();<br><br><font style='color:blue'>";
				s += bpTree.countNodes() + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=1.0f;
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=1;
		pnlCmdInfo.add(btnCountNodes, gbc);
		
		JButton btnValues = new JButton("Values");
		btnValues.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.values();<br><br><font style='color:blue'>";
				s += bpTree.values() + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=1.0f;
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.gridwidth=1;
		pnlCmdInfo.add(btnValues, gbc);
		
		JButton btnKeySet = new JButton("Key Set");
		btnKeySet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = "";
				s += "bpTree.keySet();<br><br><font style='color:blue'>";
				s += bpTree.keySet() + "</font>";
				
				editOutput.setText(s);
			}
		});
		gbc.weightx=1.0f;
		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=1;
		pnlCmdInfo.add(btnKeySet, gbc);
		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("File", null, pnlCmdFile, "New, Load, and Save B+ Trees");
		tabbedPane.addTab("Modify", null, pnlCmdModify, "Add & Remove Entries");
		tabbedPane.addTab("Query", null, pnlCmdQuery, "Query Tree For Info");
		tabbedPane.addTab("Info", null, pnlCmdInfo, "Get B+ Tree Information");
		
		tabbedPane.setMaximumSize(tabbedPane.getPreferredSize());
		
		pnl2.add(tabbedPane);
		
		pnl2.add(Box.createRigidArea(new Dimension(2,2)));
		
		// Text editor area for displaying the command output
		editOutput = new JEditorPane();
		editOutput.setEditable(false);
		editOutput.setContentType("text/html");
		
		String intro = "";
		intro += "<h1>B+ Tree Demonstration Java Appliction</h1>";
		intro += "<p>The demo has been written for a tree that uses Strings ";
		intro += "for both the keys and the values stored.  This means that, ";
		intro += "even though you can load a tree that has been saved with ";
		intro += "a tree configured to use different types, it's behavior may ";
		intro += "not work as expected.  For example, inserting String keys ";
		intro += "into a tree that was originally saved using Integer keys ";
		intro += "can result in keys being place where they 'don't belong' or ";
		intro += "not being able to find entries because they are not where ";
		intro += "they are 'suppose' to be.  If all you are doing is viewing ";
		intro += "the structure of a previously saved file, without modifying ";
		intro += "it -- or if you know the keys are suppose to be Strings -- ";
		intro += "everything will be fine.</p>";
		
		editOutput.setText(intro);
		
		// Scroll pane for scrolling text area
		JScrollPane scrlOutput = new JScrollPane(editOutput);
		scrlOutput.setPreferredSize(new Dimension(650, 0));
		
		pnl2.add(scrlOutput);
		
		pnl2.setMaximumSize(new Dimension(999999, 300));
		
		pnl.add(pnl2);
		
		add(pnl);
		pack();
		
		bpTree = new RenderingBPTree<String,String>(pnlTreeView, 5, 3);
		bpTree.makePanel();
	}
	
	public String rndString(int size) {
		char c[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<size; i++) {
			sb.append(c[(int)(Math.random() * c.length)]);
		}
		
		return sb.toString();
	}
	
	/**
	 * Main method for launching the application
	 * 
	 * @param args  Arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DemoWindow win = new DemoWindow();
				win.setVisible(true);
				win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
	}

}
