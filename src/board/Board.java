package board;

import java.util.*;
import java.util.stream.Collectors;
import player.*;

/**
 * @author CanhGosuu
 * @done? quite good but not evaluate
 */
public class Board {
	private static final int N_ROW = 15;
	private static final int N_COL = 15;
	private static final char EMPTY_CHAR = '-';
	private static final int AVAILABLE_DISTANCE = 2;
	private static final List<Pos> ALL_POS = buildAllPos();//
	private static final List<List<Pos>> BANDS = buildBands();
	private static final Map<Player, Set<Set<Pos>>> GROUPS_CACHE = new HashMap<>();
	private static final int[][] SCORE_TABLE = { { 1, 1, 1 }, { 5, 10, 20 }, { 10, 500, 1000 }, { 25, 5000, 10000 },
			{ 1000000, 1000000, 1000000 } };

	public static int getnRow() {
		return N_ROW;
	}

	public static int getnCol() {
		return N_COL;
	}

	private final GameStatus status;
	private final char[][] grid;
	public final Player player1;
	public final Player player2;

	public Board(Player player1, Player player2) {
		this.grid = buildGrid();
		this.player1 = player1;
		this.player2 = player2;
		this.status = new GameStatus(Status.ONGOING, null, Collections.emptySet());
	}

	public Board(Board other) {
		this.player1 = other.player1;
		this.player2 = other.player2;
		this.grid = copyOf(other.grid);
		this.status = new GameStatus(other.status.getStatus(), other.status.getWinner(), other.status.getWinningSet());
	}

	/**
	 * @param src
	 * @return một ma trận copy từ src
	 */
	private static char[][] copyOf(char[][] src) {
		int length = src.length;
		char[][] target = new char[length][src[0].length];
		for (int i = 0; i < length; i++) {
			System.arraycopy(src[i], 0, target[i], 0, src[i].length);
		}
		return target;
	}

	/**
	 * @return List lưu trữ khởi tạo vị trí
	 */
	private static List<Pos> buildAllPos() {
		List<Pos> poses = new ArrayList<>();
		for (int i = 0; i < N_ROW; i++) {
			for (int j = 0; j < N_COL; j++) {
				poses.add(new Pos(i, j));
			}
		}
		return poses;
	}

	/**
	 * @return List pos theo 4 hướng ( lưu vào BANDS)
	 */
	private static List<List<Pos>> buildBands() {
		Map<Integer, List<Pos>> _hashMap = new HashMap<>();
		int offset = 2 * (N_ROW + N_COL); // 15 row, 15col, 15 topleft-botright,15 topright-botleft =60
		for (int i = 0; i < N_ROW; i++) {
			for (int j = 0; j < N_COL; j++) {
				// row
				load(_hashMap, i, new Pos(i, j));// key=1->14 --

				// col
				load(_hashMap, j + offset, new Pos(i, j));// key=60->74 |

				// diagonal
				load(_hashMap, i + j + 2 * offset, new Pos(i, j)); // key=120->148 \
				load(_hashMap, i - j + 3 * offset, new Pos(i, j));// key= 166->194 /
			}
		}
		return _hashMap.values().stream().filter(val -> val.size() > 1).collect(Collectors.toList());
	}

	/**
	 * @param _map lưu trữ giá trị <key, pos>
	 * @param pos  không thể ghi đè lên các vị trí => Collections.singletonList(pos)
	 * @param key
	 */
	private static void load(Map<Integer, List<Pos>> _map, int key, Pos pos) {
		List<Pos> _pos = _map.get(key);
		if (_pos == null) { //
			_map.put(key, new ArrayList<>(Collections.singletonList(pos)));
		} else {
			_pos.add(pos);
		}
	}

	/**
	 * @return khởi tạo ma trận. Start game
	 */
	private char[][] buildGrid() {
		char[][] grid = new char[N_ROW][N_COL];
		for (int i = 0; i < N_ROW; i++) {
			for (int j = 0; j < N_COL; j++) {
				grid[i][j] = EMPTY_CHAR;
			}
		}
		return grid;
	}

	/**
	 * @param pos
	 * @param player
	 * @return đánh dấu những vị trí đã được đánh và pust vào GROUP_CACHE
	 */
	public boolean mark(Pos pos, Player player) {
		if ((pos.getRow() < 0 || pos.getRow() > N_ROW - 1) || (pos.getCol() < 0 || pos.getCol() > N_COL - 1)) {
			System.out.println("Row must between 1 and " + N_ROW + ", Col must between 1 and " + N_COL);
			return false;
		}
		if (this.grid[pos.getRow()][pos.getCol()] != EMPTY_CHAR) {
			System.out.println(pos + "=" + this.grid[pos.getRow()][pos.getCol()] + " is not empty");
			return false;
		}
		this.grid[pos.getRow()][pos.getCol()] = player.marker;
		scanGroups();
		selfCheck();
		return true;
	}

	/**
	 * check trạng trái sau mỗi lần đánh
	 */
	private void selfCheck() {
		Set<Set<Pos>> groupsOfP1 = GROUPS_CACHE.get(this.player1);
		Set<Set<Pos>> groupsOfP2 = GROUPS_CACHE.get(this.player2);
		if (groupsOfP1.stream().anyMatch(g -> g.size() >= 5)) {
			this.status.status = Status.P1_WIN;
			this.status.winner = this.player1;
			this.status.winningSet = groupsOfP1.stream().filter(g -> g.size() >= 5).findFirst()
					.orElse(Collections.emptySet());
		} else if (groupsOfP2.stream().anyMatch(g -> g.size() >= 5)) {
			this.status.status = Status.P2_WIN;
			this.status.winner = this.player2;
			this.status.winningSet = groupsOfP2.stream().filter(g -> g.size() >= 5).findFirst()
					.orElse(Collections.emptySet());
		} else if (isDraw()) {
			this.status.status = Status.DRAW;
		}
	}

	/**
	 * scan board và put vào 2 GROUPS_CACHE chứa các group con của 2 người chơi
	 */
	private void scanGroups() {
		Set<Set<Pos>> groupsOfP1 = new HashSet<>();
		Set<Set<Pos>> groupsOfP2 = new HashSet<>();
		for (List<Pos> band : BANDS) {
			Set<Pos> group1 = new HashSet<>();
			Set<Pos> group2 = new HashSet<>();
			for (int i = 0; i < band.size(); i++) {
				Pos pos = band.get(i);
				if (this.grid[pos.getRow()][pos.getCol()] == this.player1.marker) {
					group1.add(pos);
					// last one trigger
					if (i == band.size() - 1) {
						groupsOfP1.add(group1);
					}
				} else {
					if (!group1.isEmpty()) {
						groupsOfP1.add(group1);
						group1 = new HashSet<>();
					}
				}
				if (this.grid[pos.getRow()][pos.getCol()] == this.player2.marker) {
					group2.add(pos);
					// last one trigger
					if (i == band.size() - 1) {
						groupsOfP2.add(group2);
					}
				} else {
					if (!group2.isEmpty()) {
						groupsOfP2.add(group2);
						group2 = new HashSet<>();
					}
				}
			}
		}
		GROUPS_CACHE.put(this.player1, groupsOfP1);
		GROUPS_CACHE.put(this.player2, groupsOfP2);
	}

	/**
	 * @return đã hết đất cho 2 thằng múa?
	 */
	private boolean isDraw() {
		return ALL_POS.stream().noneMatch(p -> this.grid[p.getRow()][p.getCol()] == EMPTY_CHAR);
	}

	public GameStatus status() {
		return this.status;
	}

	/**
	 * @return list các ứng cử viên cho quân cờ tiếp theo
	 */
	public Set<Pos> getChildPos() {
		return ALL_POS.stream().filter(p -> this.grid[p.getRow()][p.getCol()] == EMPTY_CHAR && hasPlayerAdjacent(p))
				.collect(Collectors.toSet());
	}

	/**
	 * @param pos là một vị trí trống đang cần xét
	 * @return kiểm tra xem có quân nào liền kề không
	 */
	private boolean hasPlayerAdjacent(Pos pos) {
		int rowL = pos.getRow() - AVAILABLE_DISTANCE < 0 ? 0 : pos.getRow() - AVAILABLE_DISTANCE;
		int colL = pos.getCol() - AVAILABLE_DISTANCE < 0 ? 0 : pos.getCol() - AVAILABLE_DISTANCE;
		int rowH = pos.getRow() + AVAILABLE_DISTANCE > N_ROW ? N_ROW : pos.getRow() + AVAILABLE_DISTANCE;
		int colH = pos.getCol() + AVAILABLE_DISTANCE > N_COL ? N_COL : pos.getCol() + AVAILABLE_DISTANCE;

		for (int i = rowL; i < rowH; i++) {
			for (int j = colL; j < colH; j++) {
				if (this.grid[i][j] != EMPTY_CHAR) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param player
	 * @param _depth: độ sâu cao nhất trừ độ sâu của điểm đang xét=> c
	 * @return	điểm đánh giá của các ứng cử viên 
	 */
	public int evaluate(Player player, int _depth) {
		if (this.status.isWinning()) {
			return (player == this.status.getWinner()) ? (Integer.MAX_VALUE - 1 - _depth)
					: (Integer.MIN_VALUE + 1 + _depth);
		} else if (this.status.isDraw()) {
			return 0;
		} else {
			int selfScore = GROUPS_CACHE.get(player).stream().mapToInt(g -> score(g, false)).sum();
			int enemyScore = GROUPS_CACHE.get(getEnemy(player)).stream().mapToInt(g -> score(g, true)).sum();
			return selfScore - enemyScore;
		}
	}

	/**
	 * @param group: tập các xxx liền kề
	 * @param isEnemy: true/false
	 * @return số điểm của group hiện tại
	 */
	private int score(Set<Pos> group, boolean isEnemy) {
		int size = group.size(); // kích thước hiện tại
		int open = countOfOpen(group);// số đầu tự do
		int ratio = isEnemy && size > 2 ? 2 : 1;
		return ratio * SCORE_TABLE[size - 1][open];
	}

	/**
	 * @param player
	 * @return chuyển người chơi tiếp theo
	 */
	public Player getEnemy(Player player) {
		return player == this.player1 ? this.player2 : this.player1;
	}

	/**
	 * @param group
	 * @return số khả năng mở rộng về 2 phía của 1 group: chặn 2 đầu:0, chặn 1 đầu:
	 *         1, không chặn: 2
	 */
	private int countOfOpen(Set<Pos> group) {
		List<Pos> poses = new ArrayList<>(group);
		poses.sort(Comparator.comparing(Pos::getIndex));
		Pos min = poses.get(0);
		Pos max = poses.get(poses.size() - 1);
		if (min.getRow() == max.getRow()) { // cùng hàng: check 2 đầu cột để mở rộng
			return (min.getCol() > 0 && this.grid[min.getRow()][min.getCol() - 1] == EMPTY_CHAR ? 1 : 0)
					+ (max.getCol() < N_COL - 1 && this.grid[min.getRow()][max.getCol() + 1] == EMPTY_CHAR ? 1 : 0);
		} else if (min.getCol() == max.getCol()) {// cùng cột: check 2 đầu cột để mở rộng
			return (min.getRow() > 0 && this.grid[min.getRow() - 1][min.getCol()] == EMPTY_CHAR ? 1 : 0)
					+ (max.getRow() < N_ROW - 1 && this.grid[max.getRow() + 1][min.getCol()] == EMPTY_CHAR ? 1 : 0);
		} else {
			if (min.getCol() < max.getCol()) { // đường chéo 1: check 2 đầu cột để mở rộng
				return (min.getRow() > 0 && min.getCol() > 0
						&& this.grid[min.getRow() - 1][min.getCol() - 1] == EMPTY_CHAR ? 1 : 0)
						+ (max.getRow() < N_ROW - 1 && max.getCol() < N_COL - 1
								&& this.grid[max.getRow() + 1][max.getCol() + 1] == EMPTY_CHAR ? 1 : 0);
			} else {// đường chéo 2: check 2 đầu cột để mở rộng
				return (min.getRow() > 0 && min.getCol() < N_COL - 1
						&& this.grid[min.getRow() - 1][min.getCol() + 1] == EMPTY_CHAR ? 1 : 0)
						+ (max.getRow() < N_ROW - 1 && max.getCol() > 0
								&& this.grid[max.getRow() + 1][max.getCol() - 1] == EMPTY_CHAR ? 1 : 0);
			}
		}
	}

	/**
	 * @done
	 */
	public void start() {
		print();
		while (!this.status.isGameOver()) {
			this.player1.next(this);
			print();
			if (this.status.isGameOver()) {
				break;
			}
			if (this.player1 instanceof MinimaxPlayer && this.player2 instanceof MinimaxPlayer) {
				pause();
			}
			this.player2.next(this);
			print();
			if (this.player1 instanceof MinimaxPlayer && this.player2 instanceof MinimaxPlayer) {
				pause();
			}
		}
	}

	/**
	 * @done
	 */
	public void print() {
		System.out.println();
		System.out.println();
		System.out.println("#" + (this.player1.step() + this.player2.step()));
		System.out.println((this.player1.step() == this.player2.step() ? "*" : " ") + buildPlayerInfo(this.player1));
		System.out.println((this.player1.step() == this.player2.step() ? " " : "*") + buildPlayerInfo(this.player2));
		System.out.println();
		System.out.print("    ");
		for (int i = 0; i < 9; i++) {
			System.out.print((i + 1) + "   ");
		}
		for (int i = 9; i < N_COL; i++) {
			System.out.print((i + 1) + "  ");
		}
		System.out.println();
		for (int i = 0; i < N_ROW; i++) {
			System.out.print((i + 1) + (i >= 9 ? "  " : "   "));
			for (int j = 0; j < N_COL; j++) {
				System.out.print(this.grid[i][j] + "   ");
			}
			System.out.println();
			System.out.println();
		}
		System.out.println();
		if (this.status.isGameOver()) {
			if (this.status.isWinning()) {
				System.out.println(this.status.getWinner() + " is the WINNER(" + this.status.getWinningSet()
						+ "), congratulations!");
			} else {
				System.out.println("You both are so good, but game is draw!");
			}
			System.out.println("Summary:");
			double timesOfP1 = this.player1.time() * 1.0 / 1E9;
			double timesOfP2 = this.player2.time() * 1.0 / 1E9;
			System.out.printf("%s   Step: %d   Total Time: %3.1fs   Avg Time: %.1fs\n", "*" + this.player1,
					this.player1.step(), timesOfP1, timesOfP1 / this.player1.step());
			System.out.printf("%s   Step: %d   Total Time: %3.1fs   Avg Time: %.1fs\n", " " + this.player2,
					this.player2.step(), timesOfP2, timesOfP2 / this.player2.step());
		}
	}

	/**
	 * @param player
	 * @return string: vị trí đánh
	 */
	private String buildPlayerInfo(Player player) {
		return player + "  Step: " + player.step() + "  Last Pos: " + player.getLastPos();
	}

	/**
	 * @done
	 */
	private void pause() {
		System.out.println("[Print any key to continue]");
		final Scanner cin = new Scanner(System.in);
		cin.useDelimiter("\n");
		cin.nextLine();
		// cin.close();
	}
}