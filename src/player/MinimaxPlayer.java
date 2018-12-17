package player;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import board.*;

/**
 * @author CanhGosuu
 *
 */
public class MinimaxPlayer extends Player {
	private static final Random RANDOM = new Random();
	protected final int depth;
	protected final int[][] history;
	protected Move best;

	public MinimaxPlayer(char marker, int depth) {
		super(marker);
		this.depth = depth;
		this.history = buildHistory();
		this.setBestDepth(0);
	}

	/**
	 * @return ma trận 2 chiều lưu trữ độ sâu của nút tại thời điểm đang xét
	 */
	private int[][] buildHistory() {
		int[][] history = new int[Board.getnRow()][Board.getnCol()];
		for (int i = 0; i < Board.getnRow(); i++) {
			for (int j = 0; j < Board.getnCol(); j++) {
				history[i][j] = 0;
			}
		}
		return history;
	}

	/**
	 * @return first Move is a random move from 4->11
	 */
	protected Move first() {
		return new Move(0, new Pos(Board.getnRow() / 4 + RANDOM.nextInt(Board.getnRow()) / 2,
				Board.getnCol() / 4 + RANDOM.nextInt(Board.getnCol()) / 2));
	}

	/*
	 * @see player.Player#decide(board.Board)
	 * 
	 * @return next move depend Minimax pruining
	 */
	@Override
	protected Move decide(Board board) {
		if (this.step() <= 0 && board.getEnemy(this).step() <= 0) {
			return first();
		} else {
			Minimax(board, this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE, this);
			return this.best;
		}
	}

	/**
	 * @param board
	 * @return List pos sắp xếp theo độ sâu tăng dần
	 */
	protected List<Pos> sortChildPos(Board board) {
		return board.getChildPos().stream().sorted(new Comparator<Pos>() {
			@Override
			public int compare(Pos o1, Pos o2) {
				return Integer.compare(MinimaxPlayer.this.history[o2.getRow()][o2.getCol()],
						MinimaxPlayer.this.history[o1.getRow()][o1.getCol()]);
			}
		}).collect(Collectors.toList());
	}

	private int Minimax(Board board, int depth, int alpha, int beta, Player player) {
		if (board.status().isGameOver() || depth <= 0) {
			this.setBestDepth(this.depth - depth);
			return board.evaluate(this, this.depth - depth);
		}

		Pos bestPos = null;
		int v = (this == player) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		List<Pos> childPos = sortChildPos(board);
		for (Pos pos : childPos) {
			Board bd = new Board(board);
			bd.mark(pos, player);
			int w = Minimax(bd, depth - 1, alpha, beta, bd.getEnemy(player));
			if (this == player) {
				if (v < w) {
					v = w;
					bestPos = pos;
					if (depth == this.depth) {
						this.best = new Move(v, pos);
					}
				}
				alpha = Integer.max(alpha, w);
			} else {
				if (v > w) {
					v = w;
					bestPos = pos;
				}
				beta = Integer.min(beta, w);
			}

			// if (beta <= alpha) {
			// this.history[pos.getRow()][pos.getCol()] += 2 << depth;
			// break;
			// }
		}
		if (bestPos != null) {
			this.history[bestPos.getRow()][bestPos.getCol()] += 2 << depth;
		}
		return v;
	}

}
