package board;

/**
 * @author CanhGosuu
 * @done
 */
public class Pos {
	private final int row;
	private final int col;
	private final int index;

	/**
	 * @param row
	 * @param col
	 */
	public Pos(int row, int col) {

		this.row = row;
		this.col = col;
		this.index = row * Board.getnRow() + col;
	}

	/**
	 * @param index :tham chiếu trong board
	 */
	Pos(int index) {
		super();
		this.index = index;
		this.row = index / Board.getnRow();
		this.col = index - this.row * Board.getnRow();
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "(" + (this.row + 1) + ", " + (this.col + 1) + ")";
	}
}
