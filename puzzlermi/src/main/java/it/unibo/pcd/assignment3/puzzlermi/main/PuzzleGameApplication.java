package it.unibo.pcd.assignment3.puzzlermi.main;

import it.unibo.pcd.assignment3.puzzlermi.controller.impl.ExtraPeerControllerBuilder;
import it.unibo.pcd.assignment3.puzzlermi.controller.impl.FirstPeerControllerBuilder;
import it.unibo.pcd.assignment3.puzzlermi.view.impl.ViewImpl;
import javafx.application.Application;
import javafx.stage.Stage;

/** The main class of the application. */
public class PuzzleGameApplication extends Application {

    /**
     * The main method of the application, the one to be used for launching it.
     * @param args unused
     */
	public static void main(final String[] args) {
        Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
        final var rows = 3;
        final var columns = 5;
        if (this.getParameters().getNamed().containsKey("h")) {
            final var port = Integer.parseInt(this.getParameters().getNamed().getOrDefault("p", "1099"));
            if (this.getParameters().getNamed().containsKey("H")) {
                final var remotePort = Integer.parseInt(
                    this.getParameters().getNamed().getOrDefault("P", "1099")
                );
                new ViewImpl(
                    primaryStage,
                    rows,
                    columns,
                    ClassLoader.getSystemResource("bletchley-park-mansion.jpg").toExternalForm(),
                    v -> new ExtraPeerControllerBuilder(this.getParameters().getNamed().get("h"),
                                                        this.getParameters().getNamed().get("H"),
                                                        v)
                             .setLocalPort(port)
                             .setRemotePort(remotePort)
                             .build()
                );
            } else {
                new ViewImpl(
                    primaryStage,
                    rows,
                    columns,
                    ClassLoader.getSystemResource("bletchley-park-mansion.jpg").toExternalForm(),
                    v -> new FirstPeerControllerBuilder(this.getParameters().getNamed().get("h"), rows, columns, v)
                             .setLocalPort(port)
                             .build()
                );
            }
        } else {
            System.out.println("Missing local host parameter...");
            System.exit(1);
        }
	}
}
