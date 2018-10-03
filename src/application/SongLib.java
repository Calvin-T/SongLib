//Created by: Calvin Tang and Joseph Morales


package application;
	
import application.view.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SongLib extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(SongLib.class.getResource("view/MainView.fxml"));
			Scene scene = new Scene(loader.load());
			
			MainController mainController = loader.getController();
			mainController.start(primaryStage);
		
			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
			primaryStage.setTitle("Song Library");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
