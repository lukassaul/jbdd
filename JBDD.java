import java.util.Hashtable;
import java.util.Enumeration;
/**
* Implement BDDs as per Anderson tutorial (U of Austin Texas)
*    to increase our knowledge of fundamental logic towards tauchain development
*/

public class JBDD {

	public Hashtable<Node,Node> T;
	
	interface BooleanFunction {
		public int getNumVariables();
		public boolean f(boolean[] args);
	}
	
	public JBDD() {
		T = new Hashtable<Node,Node>();
		
		termZero.setValue(false);
		termOne.setValue(true);
		
		
		
		
	}
	
	public Node getHighestNode() {
		int currentHeight = 0;
		Node tbr = new Node(0,null,null);
		Enumeration e = T.elements();
		while (e.hasMoreElements()) {
			Node test = (Node)e.nextElement();
			if (test.i > currentHeight) {
				currentHeight = test.i;
				tbr = test;
			}
		}
		System.out.println("found highest: " + currentHeight);
		return tbr;
	}
	 
	/**
	* Doing some testing here
	*/
	public static final void main(String[] args) {
		JBDD test = new JBDD();
		JBDD test2 = new JBDD();
		
		
		test.Build(1
		,new BooleanFunction() {
			public int getNumVariables() {return 5;};
			public boolean f(boolean[] args) {
				return BoolFunc1(args);
			}
		});
		System.out.println("final table size: " + test.T.size());
		
		test2.Build(1
		,new BooleanFunction() {
			public int getNumVariables() {return 5;};
			public boolean f(boolean[] args) {
				return BoolFunc2(args);
			}
		});
		System.out.println("final table size: " + test2.T.size());
		
		
		
		Node topNode1 = test.getHighestNode();
		Node topNode2 = test2.getHighestNode();
		Node answer = test.ApplyConjunction(topNode1,topNode2);
			
	}
	
	// we need some nodes to build this bdd
	public class Node {
		public int i;  //index
		public boolean value; // used only for terminal nodes
		public Node l;  // pointer to low node
		public Node h; // pointer to high node
		public Node(int ii, Node ll, Node hh) {
			i=ii; l=ll; h=hh;
		}
		public void setValue(boolean b) {
			value=b;
		}
	}
	
	// terminal nodes have no children
	// they need to have a boolean value somehow...   
	// we recognize them by having null children
	Node termZero = new Node(0,null,null); // false
	Node termOne = new Node(0,null,null);  // true  
	
	
	public Node Mk(int i, Node l, Node h) {
		System.out.println("Calaled MK");
		Node u = new Node(i,l,h);
		Node temp;
		// if (l.i==h.i) return l;  // skip this test for now
		 if ((temp = (Node)T.get(u))!=null) {
			System.out.println("apparently already found");
			return temp;
		}
		try {
			T.put(u,u);
			//System.out.println("Called put on T : " + T.isEmpty());
			return u;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
		
	}
	
	// test with a boolean expressio
	public static boolean BoolFunc1(boolean[] args) {
		if ((args[0] && args[1]) | (args[2] && args[3])) return false; 
		else return args[4];
	}
	
	public static boolean BoolFunc2(boolean[] args) {
		if ((args[0] | args[1]) && (args[2] | args[3])) return false; 
		else return args[4];
	}
	
	
	// lets just build with our BoolFunc1 defined above rather than pass it in 
	public Node Build(int i, BooleanFunction t) {
		System.out.println("Called Build on BoolFunc w/ " + t.getNumVariables() + " variables");
		if (i>t.getNumVariables()) {
			return termOne;
			//if (t==0) return termZero;
			//else return termOne;
		}
		else {
			class TempFunc implements BooleanFunction {
				public int getNumVariables() {
					return t.getNumVariables()-1;
				}
				public boolean f(boolean[] args) {
					//if (getNumVariables()!=args.length) System.out.println("ouch");
					// replacing index i of parent function t with false
					boolean[] oArgs = new boolean[t.getNumVariables()];
					for (int j=0; j<t.getNumVariables(); j++) {
						oArgs[j]=args[j];
					}
					oArgs[i]=false;
					return t.f(oArgs);
				}
			}
			BooleanFunction temp = new TempFunc();
			Node v0 = this.Build(i+1,temp);
			
			class TempFunc2 implements BooleanFunction {
				public int getNumVariables() {
					return t.getNumVariables()-1;
				}
				public boolean f(boolean[] args) {
					//if (numVariables!=args.length) System.out.println("ouch");
					// replacing index i of parent function t with true
					boolean[] oArgs = new boolean[t.getNumVariables()];
					for (int j=0; j<t.getNumVariables(); j++) {
						oArgs[j]=args[j];
					}
					oArgs[i]=true;
					return t.f(oArgs);
				}
			}
			BooleanFunction temp2 = new TempFunc2();
			Node v1 = Build(i+1,temp2);
			
			return Mk(i, v0, v1);
		}
	}// end build
	
	
	// table for doing Apply operation
	public Hashtable<Node[],Node> G = new Hashtable();
	
	public Node ApplyConjunction (Node u1, Node u2) {
		System.out.println("Called Apply with table G size: " + G.size());
		Node u; 
		Node[] tempArry = new Node[2];
		tempArry[0]=u1; tempArry[1]=u2;
		if ((u=(Node)G.get(tempArry))!=null) {
			return u;
		}
		else if (u1.l==null && u1.h == null && u2.l == null && u2.h==null) {
			// we have terminal nodes here
			// this is where we finally do the && operation :)  
			boolean result = u1.value && u2.value;
			if (result) u=termOne;
			else u = termZero;
		}	
		else if (u1.i==u2.i) {
			u = Mk(u1.i, ApplyConjunction(u1.l, u2.l), ApplyConjunction(u1.h, u2.h));
		}
		else if (u1.i<u2.i) {
			u = Mk(u1.i, ApplyConjunction(u1.l, u2), ApplyConjunction(u1.h, u2));
		}
		else /*if (u1.i>u2.i)*/ {
			u = Mk(u2.i, ApplyConjunction(u1, u2.l), ApplyConjunction(u1, u2.h));
		}
		// add our answer to the new table 
		G.put(tempArry,u);
		return u;
	}
}	

		
			
			
	
			
