import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A mess of tests.  Nothing special.
 * 
 * @author Nathan
 *
 */
public class Tests {
	public static void test() {
		BPTree<Integer,Integer> bpTree = new BPTree<Integer,Integer>(3,3);
		
		for (int i=0; i<20; i++) {
			int k = 20-i; //(int)(Math.random() * 50);
			System.out.println("Adding: " + k);
			bpTree.put(k, i);
			bpTree.printFullTree();
			System.out.println("---------------------------------------");
		}
		
		IntegerStringParse isp = new IntegerStringParse();
		try {
			bpTree.save("test.txt", isp, isp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bpTree.load("test.txt", isp, isp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Node Count: " + bpTree.countNodes());
		System.out.println("Values: " + bpTree.values());
		System.out.println("Keys: " + bpTree.keySet());
		System.out.println("Key in Keyset: " + bpTree.keySet().contains(5));
		System.out.println("Entries: " + bpTree.entrySet());
		System.out.println("Contains Val: " + bpTree.containsValue(10));
		
		System.out.println("To Key: " + bpTree.getFirstToKey(10));
		System.out.println("From Key: " + bpTree.getKeyToLast(10));
		System.out.println("From To Key: " + bpTree.getKeyToKey(5, 15));
		
		System.out.println(bpTree.containsKey(10));
		System.out.println(bpTree.containsKey(20));
		System.out.println(bpTree.containsKey(30));
		System.out.println(bpTree.containsKey(40));
		
		for (int i=0; i<21; i++) {
			int k = i;  //(int)(Math.random() * 50);
			System.out.println("Removing: " + k);
			bpTree.remove(k);
			bpTree.printFullTree();
			System.out.println("---------------------------------------");
		}
		bpTree.printFullTree();
		
		System.out.println(bpTree.isEmpty());
	}
	
	public static void elements() {
		try {
			List<String> lines = Files.readAllLines(Paths.get("example_data" + File.separator + "text_elements.txt"), Charset.defaultCharset());
			
			BPTree<String,Integer> bptree = new BPTree<String,Integer>(4,12);
			
			for (int i=0; i<lines.size(); i++) {
				bptree.put(lines.get(i).trim(), i+1);
			}
			
			StringStringParse ssp = new StringStringParse();
			IntegerStringParse isp = new IntegerStringParse();
			bptree.save("example_data" + File.separator + "bptree_elements.txt", ssp, isp);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void alphabet() {
		BPTree<String,Integer> bpTree = new BPTree<String,Integer>(3,3);
		
		for (int i=0; i<26; i++) {
			String s = "" + (char)(65 + i);
			bpTree.put(s, i+1);
		}
		
		StringStringParse ssp = new StringStringParse();
		IntegerStringParse isp = new IntegerStringParse();
		try {
			bpTree.save("example_data" + File.separator + "bptree_alphabet.txt", ssp, isp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//test();
		//elements();
		alphabet();
	}
}
