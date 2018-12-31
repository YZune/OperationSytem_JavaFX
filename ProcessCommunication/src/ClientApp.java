import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientApp extends Application {

    private StringBuilder msgBuilder = new StringBuilder();
    //通过socket获取字符流
    private BufferedWriter bufferedWriter;
    //通过标准输入流获取字符流
    private BufferedReader bufferedReader;

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("客户端");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(24, 24, 24, 24));

        Label label = new Label("发送数据：");
        grid.add(label, 0, 0);

        TextField dataField = new TextField();
        grid.add(dataField, 1, 0);

        Button btn = new Button("发送");
        grid.add(btn, 2, 0);

        Label label1 = new Label("服务器回应：");
        grid.add(label1, 0, 1, 3, 1);

        Label msgLabel = new Label("");
        grid.add(msgLabel, 0, 2, 3, 1);

        try {//初始化一个socket
            Socket socket = new Socket("127.0.0.1", 9999);
            //通过socket获取字符流
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //通过标准输入流获取字符流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    bufferedWriter.write(dataField.getText());
                    bufferedWriter.write("\n");
                    bufferedWriter.flush();
                    dataField.setText("");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        Task<String> progressTask = new Task<String>() {
            @Override
            protected String call(){
                String str;
                while (true) {
                    try {
                        str = bufferedReader.readLine();
                        if (str != null){
                            updateValue(str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        progressTask.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                msgBuilder.append(newValue);
                msgBuilder.append("\n");
                msgLabel.setText(msgBuilder.toString());
            }
        });

        new Thread(progressTask).start();

        Scene scene = new Scene(grid, 500, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
    }
}
