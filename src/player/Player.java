package player;

import java.util.ArrayList;
import java.util.List;

import board.*;

/**
 * @author CanhGosuu
 * @done
 */
public abstract class Player {
	public final char marker;
	private final List<Long> times;
	private final List<Pos> path;
	private int score;

	public Player(char marker) {
		this.marker = marker;
		this.times = new ArrayList<>();
		this.path = new ArrayList<>();
		this.setScore(0);
	}

	public long time() {
		return this.times.stream().mapToLong(t -> t).sum();
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public long getLastTime() {
		if (this.times.size() <= 0) {
			return 0;
		}
		return this.times.get(this.times.size() - 1);
	}

	public int step() {
		return this.path.size();
	}

	public Pos getLastPos() {
		if (this.path.size() <= 0) {
			return null;
		}
		return this.path.get(this.path.size() - 1);
	}

	/**
	 * @param board
	 * @return next Move
	 */
	public Pos next(Board board) {
		long start = System.nanoTime();
		Move move = decide(board);
		while (!board.mark(move.getNext(), this)) {
			move = decide(board);
		}
		long end = System.nanoTime();
		this.times.add(end - start);
		this.path.add(move.getNext());
		this.score=move.getScore();
		return move.getNext();
	}

	protected abstract Move decide(Board board);

	@Override
	public String toString() {
		return "Player " + this.marker;
	}

}
