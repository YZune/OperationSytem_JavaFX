import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ShareMemoryReaderApp extends Application {

    private StringBuilder msgBuilder = new StringBuilder();
    private boolean quit = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("share.txt", "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 10240);

        primaryStage.setTitle("读取共享内存");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(24, 24, 24, 24));

        Label label = new Label("接收数据：");
        grid.add(label, 0, 0);

        Label msgLabel = new Label("");
        grid.add(msgLabel, 0, 1);

        Task<String> progressTask = new Task<String>() {

            @Override
            protected String call() {
                while (!quit) {
                    System.out.println("loop");
                    if (mbb.get(0) == 2) {
                        byte[] msgByte = new byte[mbb.get(1)];
                        for (int i = 0; i < mbb.get(1); i++) {
                            msgByte[i] = mbb.get(i + 2);
                        }
                        mbb.put(0, (byte) 0);
                        mbb.force();
                        updateValue(new String(msgByte, StandardCharsets.UTF_8));
                    }
                }
                return null;
            }
        };

        progressTask.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                msgLabel.setVisible(true);
                msgBuilder.append("收到数据“");
                msgBuilder.append(newValue);
                msgBuilder.append("”。\n");
                msgLabel.setText(msgBuilder.toString());
            }
        });

        new Thread(progressTask).start();

        Scene scene = new Scene(grid, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                quit = true;
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
