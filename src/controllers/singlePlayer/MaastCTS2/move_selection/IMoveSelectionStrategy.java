package controllers.singlePlayer.MaastCTS2.move_selection;

import ontology.Types.ACTIONS;
import controllers.singlePlayer.MaastCTS2.model.MctNode;
import controllers.singlePlayer.MaastCTS2.test.IPrintableConfig;

public interface IMoveSelectionStrategy extends IPrintableConfig {
	
	/** Should be implemented to select a move to play in the real game for the position in the given root */
	public ACTIONS selectMove(MctNode root);

}
