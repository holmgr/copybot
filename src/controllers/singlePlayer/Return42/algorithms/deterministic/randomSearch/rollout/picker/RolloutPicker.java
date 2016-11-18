package controllers.singlePlayer.Return42.algorithms.deterministic.randomSearch.rollout.picker;

import controllers.singlePlayer.Return42.algorithms.deterministic.randomSearch.rollout.strategy.RollOutStrategy;

public interface RolloutPicker {

	public void iterationFinished( boolean didFindPlan );
	public RollOutStrategy getCurrentRolloutStrategy();
	
}
