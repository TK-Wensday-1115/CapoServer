package pl.edu.agh.toik;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;

import java.util.Optional;

public class ServerApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextInputDialog textInputDialog = new TextInputDialog("3737");
        Optional<String> integerInputDialogResult = textInputDialog.showAndWait();
        if (integerInputDialogResult.isPresent()) {
            CommunicationsServer.start(Integer.valueOf(integerInputDialogResult.get()));
            System.out.println("Server started.");

            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ServerApplication.fxml"));
            primaryStage.setTitle("CapoMonitoring");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
