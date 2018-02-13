import java.util.*;

public class BlockchainUnit {
	private Block block;
	private ArrayList<BlockchainUnit> children;
	private int depth;
	private int timeStamp;
	private BlockchainUnit parent;

	public BlockchainUnit(Block block, int time) {
		timeStamp = time;
		this.block = block;
		children = new ArrayList<>();
		depth = -1;
		parent = null;
	}

	public void addChild(BlockchainUnit child) {
		children.add(child);
		child.setParent(this);
	}

	public BlockchainUnit getParent() {
		return parent;
	}

	public void setParent(BlockchainUnit parent) {
		this.parent = parent;
	}

	public void setDepth(int d) {
		depth = d;
	}

	public void updateTime (int t) {
		timeStamp = t;
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
