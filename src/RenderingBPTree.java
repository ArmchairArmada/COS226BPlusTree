import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class RenderingBPTree<K extends Comparable<K>, V> extends BPTree<K, V> {
	private abstract class NodePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public NodePanel(BPTree<K,V>.Node node) {
		}
		
		public abstract int getConnectionX(int index);
		
		public int getCenterX() {
			return getX() + getWidth() / 2;
		}
		
		public int getConnectionY() {
			return getY() + getHeight();
		}
	}
	
	
	private class LeafPanel extends NodePanel {
		private static final long serialVersionUID = 1L;

		public LeafPanel(BPTree<K,V>.Node node) {
			super(node);
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));
			
			@SuppressWarnings("unchecked")
			BPTree<K,V>.LeafNode leafNode = (BPTree<K,V>.LeafNode)node;
			
			for (int i=0; i<leafSize; i++) {
				JLabel lbl;
				
				if (i < leafNode.size()) {
					KeyVal<K, V> entry = leafNode.entries.get(i);
					lbl = new JLabel(" " + entry.getKey() + " : " + entry.getValue() + " ");
				}
				else {
					lbl = new JLabel("     ");
				}
				
				add(lbl);
			}
			
			setPreferredSize(getPreferredSize());
			validate();
		}
		
		@Override
		public int getConnectionX(int index) {
			return getCenterX();
		}
	}
	
	
	private class InnerPanel extends NodePanel {
		private static final long serialVersionUID = 1L;
		private ArrayList<JPanel> childPoints;
		
		public InnerPanel(BPTree<K,V>.Node node) {
			super(node);
			
			childPoints = new ArrayList<JPanel>();
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));
			
			@SuppressWarnings("unchecked")
			BPTree<K,V>.InnerNode innerNode = (BPTree<K,V>.InnerNode)node;
			
			for (int i=0; i<innerSize-1; i++) {
				JLabel lbl;
				
				if (i < innerNode.size()) {
					KeyVal<K, BPTree<K, V>.Node> kv = innerNode.children.get(i);
					
					if (kv.hasInfKey()) {
						lbl = new JLabel(" \u221E ");
					}
					else {
						lbl = new JLabel(" " + kv.getKey().toString() + " ");
					}
				}
				else {
					lbl = new JLabel("     ");
				}
				
				JPanel childPoint = new JPanel();
				childPoint.setBackground(Color.blue);
				childPoints.add(childPoint);
				add(childPoint);
				add(lbl);
			}
			
			JPanel childPoint = new JPanel();
			childPoint.setBackground(Color.blue);
			childPoints.add(childPoint);
			add(childPoint);
			
			setPreferredSize(getPreferredSize());
			validate();
		}
		
		@Override
		public int getConnectionX(int index) {
			JPanel childPoint = childPoints.get(index);
			return getX() + childPoint.getX() + childPoint.getWidth() / 2;
		}
	}
	
	
	private class Connector {
		public NodePanel parent;
		public NodePanel child;
		public int index;
		
		public Connector(NodePanel aParent, NodePanel aChild, int aIndex) {
			parent = aParent;
			child = aChild;
			index = aIndex;
		}
		
		public void draw(Graphics g) {
			g.drawLine(parent.getConnectionX(index), parent.getConnectionY(), child.getCenterX(), child.getY());
		}
	}
	
	
	private class ViewPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public ViewPanel() {
			setLayout(null);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(Color.black);
			for (Connector c : connectors) {
				c.draw(g);
			}
		} 
	}


	private static final int VERTICAL_SPACE = 100;
	
	
	private JPanel outerPanel;
	private JPanel panel;
	private ArrayList<NodePanel> panels;
	private ArrayList<Connector> connectors;
	
	
	public RenderingBPTree(JPanel jPanel, int innerSize, int leafSize) {
		super(innerSize, leafSize);
		
		outerPanel = jPanel;
		outerPanel.removeAll();
		
		panel = new ViewPanel();
		
		outerPanel.add(panel);
		
		panels = new ArrayList<NodePanel>();
		connectors = new ArrayList<Connector>();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	/**
	 * Creates a JPanel with all of the nodes rendered inside of it.
	 */
	public void makePanel() {
		panel.removeAll();
		panels.clear();
		connectors.clear();
		
		Rectangle rect = makePanel(root, null, new Rectangle(0,0,0,0), 0);
		
		panel.setPreferredSize(new Dimension(rect.width, rect.height));
		panel.revalidate();
		outerPanel.repaint();
	}
	
	private Rectangle makePanel(Node node, NodePanel parent, Rectangle outerRect, int index) {
		Rectangle rect;
		
		if (node instanceof BPTree.LeafNode) {
			@SuppressWarnings("unchecked")
			LeafNode leafNode = (LeafNode)node;
			
			LeafPanel pnl = new LeafPanel(leafNode);
			panel.add(pnl);
			panels.add(pnl);
			
			// Set the size of this panel
			Dimension s = pnl.getPreferredSize();
			pnl.setBounds(outerRect.x, outerRect.y, s.width, s.height);
			
			if (parent != null) {
				Connector c = new Connector(parent, pnl, index);
				connectors.add(c);
			}
			
			rect = new Rectangle(outerRect.x, outerRect.y, pnl.getWidth(), pnl.getHeight());
		}
		else {
			@SuppressWarnings("unchecked")
			InnerNode innerNode = (InnerNode)node;
			
			InnerPanel pnl = new InnerPanel(innerNode);
			panel.add(pnl);
			panels.add(pnl);

			// Set the size of this panel
			Dimension s = pnl.getPreferredSize();
			pnl.setBounds(outerRect.x, outerRect.y, s.width, s.height);

			if (parent != null) {
				Connector c = new Connector(parent, pnl, index);
				connectors.add(c);
			}
			
			int h = 0;
			int newX = 0;
			Rectangle current;
			for (int i=0; i<innerNode.size(); i++) {
				Node childNode = innerNode.children.get(i).getValue();
				Rectangle newRect = (Rectangle) pnl.getBounds().clone();
				newRect.x += newX;
				newRect.y += VERTICAL_SPACE;
				current = makePanel(childNode, pnl, newRect, i);
				newX += current.width + 5;
				h = Math.max(h, current.height);
			}
			newX -= 5;
			
			// Rectangle size needs to contain child nodes and this node
			if (newX > pnl.getWidth())
				rect = new Rectangle(outerRect.x, outerRect.y, newX, h + VERTICAL_SPACE);
			else
				rect = new Rectangle(outerRect.x, outerRect.y, pnl.getWidth(), h + VERTICAL_SPACE);
			
			// Center this node in the rectangle
			int bx = outerRect.x + (rect.width - pnl.getWidth()) / 2;
			pnl.setBounds(bx, outerRect.y, s.width, s.height);
			
			// Make the rectangle big enough to fit the node
			if (pnl.getWidth() > rect.width) {
				rect.width = pnl.getWidth();
				pnl.setBounds(outerRect.x, outerRect.y, pnl.getWidth(), pnl.getHeight());
			}
		}
		
		return rect;
	}

}
