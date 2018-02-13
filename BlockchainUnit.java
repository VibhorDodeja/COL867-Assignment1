import java.util.*;

public class BlockchainUnit {
	private Block block;
	private ArrayList<BlockchainUnit> children;
      private int depth;
      private int timeStamp;

	public BlockchainUnit(Block block, int time) {
            timeStamp = time;
		this.block = block;
		children = new ArrayList<>();
            depth = -1;
	}

	public void addChild(BlockchainUnit child) {
		children.add(child);
	}

      public void setDepth(int d) {
            depth = d;
      }

      public int getParentID() {
            return block.prevBlock.ID;
      }

	public ArrayList<BlockchainUnit> getChildren() {
		return children;
	}

	public Block getBlock() {
		return block;
	}

      public int getDepth() {
            return depth;
      }

      public int getTime() {
            return timeStamp;
      }
}
