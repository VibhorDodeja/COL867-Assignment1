import java.util.*;

public class Network {
      private ArrayList<Node> nodeList;

      Network() {
            nodeList = new ArrayList<Node>();
      }

      public void addNode(Node node) {
            nodeList.add(node);
      }

      public Node getNode(int index) {
            return nodeList.get(index);
      }
}
