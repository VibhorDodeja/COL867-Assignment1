import java.util.*;

public class Block {
      public int ID;
      public Block prevBlock;
      public LinkedList<Txn> txnList;
      public Map<Node, Integer> balance;
}
