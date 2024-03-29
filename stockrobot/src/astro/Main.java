package astro;

import model.robot.StartModel;
import controller.IController;
import controller.StartController;

/**
 * This is where we start ASTRo. 
 * @author Daniel
 */
public class Main {
	public static void main(final String args[]) {
		final IController startController = new StartController();
		startController.display(new StartModel());
	}
}
