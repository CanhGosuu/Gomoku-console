package player;

import java.util.List;
import board.*;

public class AlphaBetaPlayer extends MinimaxPlayer {

	public AlphaBetaPlayer(char marker, int depth) {
		super(marker, depth);
	}

	/*
	 * @see player.Player#decide(board.Board)
	 * 
	 * @return next move depend alphaBeta pruining
	 */
	@Override
	protected Move decide(Board board) {
		if (this.step() <= 0 && board.getEnemy(this).step() <= 0) {
			return first();
		} else {
			alphaBeta(board, this.depth, Integer.MIN_VALUE, Integer.MAX_VALUE, this);
			return this.best;
		}
	}

	private int alphaBeta(Board board, int depth, int alpha, int beta, Player player) {
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
			int w = alphaBeta(bd, depth - 1, alpha, beta, bd.getEnemy(player));
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

			if (beta <= alpha) {
				this.history[pos.getRow()][pos.getCol()] += 2 << depth;
				break;
			}
		}
		if (bestPos != null) {
			this.history[bestPos.getRow()][bestPos.getCol()] += 2 << depth;
		}
		return v;
	}
}
