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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServiceApp extends Application {

    private StringBuilder msgBuilder = new StringBuilder();

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("服务器端");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(24, 24, 24, 24));

        Label label = new Label("收到来自客户端的消息：");
        grid.add(label, 0, 0);

        Label msgLabel = new Label("");
        msgLabel.setMaxHeight(300);
        grid.add(msgLabel, 0, 1);

        Task<String> progressTask = new Task<String>() {
            ServerSocket serverSocket = new ServerSocket(9999);

            @Override
            protected String call() throws IOException {
                while (true) {
                    Socket socket = serverSocket.accept();
                    //每当有一个客户端连接进来后，就启动一个单独的线程进行处理
                    new Thread(new Runnable() {
                        @Override
                        public void run() {//获取输入流,并且指定统一的编码格式
                            BufferedWriter bufferedWriter;
                            BufferedReader bufferedReader;
                            try {
                                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                                //读取一行数据
                                String str;
                                //通过while循环不断读取信息，
                                while ((str = bufferedReader.readLine()) != null) {
                                    updateValue(str);
                                    bufferedWriter.write("收到长度为" + str.length() + "的信息：" + str);
                                    bufferedWriter.write("\n");
                                    bufferedWriter.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
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

        Scene scene = new Scene(grid, 300, 500);
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
