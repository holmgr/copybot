package controllers.singlePlayer.MaastCTS2.heuristics.states;

import controllers.singlePlayer.MaastCTS2.test.IPrintableConfig;
import core.game.StateObservation;

public interface IPlayoutEvaluation extends IPrintableConfig {
	public double scorePlayout(StateObservation stateObs);
}
