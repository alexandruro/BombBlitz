
package bomber.AI;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import bomber.game.GameState;
import bomber.game.Player;

// TODO: Auto-generated Javadoc
/**
 * The Class HardAI for making moves.
 *
 * @author Jokubas Liutkus
 */
public class ExtremeAI extends AITemplate {

	/**
	 * Instantiates a new hard AI.
	 *
	 * @param ai
	 *            the AI
	 * @param gameState
	 *            the game state
	 */
	public ExtremeAI(GameAI ai, GameState gameState) {
		super(ai, gameState);
	}

	/**
	 * Perform sequence of moves.
	 *
	 * @param moves
	 *            the list of moves
	 * @param inDanger
	 *            the variable determining if the escape moves are passed in
	 *            that case make moves without considering anything else
	 */
	protected void performMoves(LinkedList<AIActions> moves, boolean inDanger) {
		if (inDanger)
			while (moves != null && !moves.isEmpty() && gameAI.isAlive() && !pause) {
				makeSingleMove(moves.removeFirst());
			}
		else
			while (moves != null && !moves.isEmpty() && !safetyCh.inDanger() && safetyCh.checkMoveSafety(moves.peek())
					&& !safetyCh.isEnemyInBombRangeExludeAIs() && gameAI.isAlive()  && !pause) {
				makeSingleMove(moves.removeFirst());
			}
	}

	/**
	 * Perform planned moves. When none of the players are reachable
	 *
	 * @param moves
	 *            the moves
	 */
	protected void performPlannedMoves(LinkedList<AIActions> moves) {
		AIActions action;

		while (moves != null && !moves.isEmpty() && getMovesToEnemyExcludeAIs() == null && gameAI.isAlive()
				&& (!checkIfAIsReachable() || isPerformer())  && !pause) {
			action = moves.removeFirst();
			// if actions is bomb place it
			if (action == AIActions.BOMB) {
				gameAI.getKeyState().setBomb(true);
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				gameAI.getKeyState().setBomb(false);
			}
			// if action is none wait until the next move is safe
			else if (action == AIActions.NONE) {
				if (moves != null) {
					while (!safetyCh.checkMoveSafety(moves.peek()) && gameAI.isAlive() && !pause) {

					}
				}
			}
			// otherwise make a standard move
			else {
				makeSingleMove(action);
			}
		}

	}

	/**
	 * Checks if the AI is performer to pick the upgrade.
	 * Determines which AI will get the upgrade
	 *
	 * @return true, if is performer
	 */
	private boolean isPerformer() {
		List<Player> players = gameState.getPlayers().stream().filter(p -> (p instanceof GameAI) && p.isAlive())
				.collect(Collectors.toList());
		if (!players.isEmpty())
			return players.get(0).equals(gameAI);
		return false;
	}

	
	
	/**
	 * Check if other AIs are reachable
	 *
	 * @return true, if successful
	 */
	private boolean checkIfAIsReachable() {
		List<Player> players = gameState.getPlayers().stream().filter(p -> (p instanceof GameAI) && p.isAlive())
				.collect(Collectors.toList());
		for (Player p : players) {
			if (!p.equals(gameAI) && finder.findRoute(gameAI.getGridPos(), p.getGridPos()) != null)
				return true;
		}
		return false;
	}

	/**
	 * Main method for controlling what moves to make.
	 */
	protected void move() {
		LinkedList<AIActions> moves;
		while (gameAI.isAlive()) {
			
			while(pause)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// if AI is in danger then find the escape route
			if (safetyCh.inDanger()) {
				moves = finder.escapeFromExplotion((safetyCh.getTilesAffectedByBombs()));
				performMoves(moves, true);

			}

			// if enemy is in range and it is possible to place bomb and escape
			// then perform it
			else if ((moves = finder.canPutBombAndEscapeExcludeAIs()) != null) {
				gameAI.getKeyState().setBomb(true);
				performMoves(moves, true);
			}

			// else if there is an upgrade find the moves to it
			else if ((moves = finder.findRouteToUpgrade()) != null /*&& (!checkIfAIsReachable() || isPerformer())*/) {

				performMoves(moves, false);
			}

		
			// if enemy is accessible(no boxes are blocking the path) then
			// find a route to it and make moves
			else if ((moves = getMovesToEnemyExcludeAIs()) != null) {
				performMoves(moves, false);
			}
			// if enemy is not in the range get the plan how to reach enemy and
			// full-fill it
			else if ((moves = finder.getPlanToEnemy(gameAI.getGridPos(), finder.getNearestEnemyExcludeAIs())) != null) {
				if (!checkIfAIsReachable() || isPerformer())
					performPlannedMoves(moves);
			}

			gameAI.getKeyState().setBomb(false);
		}
	}

}	
