package it.unibo.pcd.assignment3.main;

import it.unibo.pcd.assignment3.controller.impl.ControllerFactory;
import it.unibo.pcd.assignment3.view.impl.ViewImpl;
import javafx.application.Application;
import javafx.stage.Stage;

public class PuzzleGameApplication extends Application {

	public static void main(final String[] args) {
        Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
        if (this.getParameters().getNamed().containsKey("h")) {
            if (this.getParameters().getNamed().containsKey("p")) {
                new ViewImpl(
                primaryStage,
                ClassLoader.getSystemResource("bletchley-park-mansion.jpg").toExternalForm(),
                v -> ControllerFactory.createNewPeer(this.getParameters().getNamed().get("h"),
                                                        Integer.parseInt(this.getParameters().getNamed().get("p")))
                );
            }
            final var host = commandLine.getOptionValue("host", "localhost");
            final var port = Integer.parseInt(commandLine.getOptionValue("port", "1099"));
            // Start peer search
        } else {

        }
	}
}
