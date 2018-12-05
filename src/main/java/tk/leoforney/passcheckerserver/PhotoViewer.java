package tk.leoforney.passcheckerserver;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class PhotoViewer extends Application {

    static ImageView imageView;
    static boolean closed = false;

    public static void main(String[] args) {
        if (args.length > 0) {
            if (!args[0].toLowerCase().contains("nogui")) {
                launch(args);
            }
        }
        if (args.length == 0) {
            System.out.println("No args");
            launch(args);
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("PhotoViewer started");
        primaryStage.setTitle("PassChecker");

        primaryStage.getIcons().add(new Image(PhotoViewer.class.getResourceAsStream("/launcher.png")));

        JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.setStyle("-fx-background-color: #29b6f6;");
        btn.getStyleClass().add("button-raised");
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> System.out.println("Hello World!"));

        Image placeholder = new Image(PhotoViewer.class.getResourceAsStream("/placeholder.jpg"));
        imageView = new ImageView(placeholder);

        JFXScrollPane scrollPane = new JFXScrollPane();
        scrollPane.setContent(imageView);

        HBox bottom = new HBox();
        bottom.setPadding(new Insets(15, 12, 15, 12));
        bottom.setSpacing(10);
        bottom.setStyle("-fx-background-color: #006db3;");
        bottom.getChildren().add(btn);

        HBox top = new HBox();
        bottom.setPadding(new Insets(15, 12, 15, 12));

        BorderPane root = new BorderPane();
        root.setBottom(bottom);
        root.setCenter(scrollPane);
        primaryStage.setScene(new Scene(root, 1250, 767));
        primaryStage.show();
    }

    @Override
    public void stop(){
        closed = true;
    }

}
