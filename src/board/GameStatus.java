package board;
import java.util.Set;
import player.*;


/**
 * @author CanhGosuu
 * @done
 */
public class GameStatus {

	protected Status status;
	protected Player winner;
	protected Set<Pos> winningSet;

	/**
	 * @param status trạng thái hiện tại của game
	 * @param winner
	 * @param winningSet
	 */
	public GameStatus(Status status, Player winner, Set<Pos> winningSet) {

		this.status = status;
		this.winner = winner;
		this.winningSet = winningSet;
	}

	public Status getStatus() {
		return status;
	}

	public Player getWinner() {
		return winner;
	}

	public Set<Pos> getWinningSet() {
		return winningSet;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}

	public void setWinningSet(Set<Pos> winningSet) {
		this.winningSet = winningSet;
	}

	@Override
	public String toString() {
		return "GameStatus [status=" + status + ", winner=" + winner + ", winningSet=" + winningSet + "]";
	}

	public boolean isGameOver() {
		return this.status != Status.ONGOING;
	}

	public boolean isDraw() {
		return this.status == Status.DRAW;
	}

	public boolean isWinning() {
		return this.status == Status.P1_WIN || this.status == Status.P2_WIN;
	}
}