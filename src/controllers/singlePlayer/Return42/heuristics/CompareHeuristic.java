package controllers.singlePlayer.Return42.heuristics;

import controllers.singlePlayer.Return42.GameStateCache;
import controllers.singlePlayer.Return42.heuristics.features.CompareFeature;
import controllers.singlePlayer.Return42.heuristics.features.controller.FeatureController;

import java.util.List;

public interface CompareHeuristic {
    public double evaluate(GameStateCache newState, GameStateCache oldState);

    public List<CompareFeature> getFeatures();

    public List<FeatureController> getController();
}
