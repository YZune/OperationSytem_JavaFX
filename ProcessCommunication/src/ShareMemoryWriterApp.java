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

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ShareMemoryWriterApp extends Application {

    private String msg;
    private StringBuilder msgBuilder = new StringBuilder();

    @Override
    public void start(Stage primaryStage) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("share.txt", "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 10240);
        mbb.put(0, (byte) 0);
        for (int i = 0; i < 10240; i++) {
            mbb.put(i, (byte) 0);
        }
        mbb.force();

        primaryStage.setTitle("写入共享内存");
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

        Label msgLabel = new Label("数据已发送，等待接收……");
        grid.add(msgLabel, 0, 2, 3, 1);
        msgLabel.setVisible(false);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                btn.setDisable(true);
                msg = dataField.getText();
                while (mbb.get(0) == 1) {
                }
                mbb.put(0, (byte) 1);
                byte[] msgByte = msg.getBytes(StandardCharsets.UTF_8);
                for (int i = 0; i < msgByte.length; i++) {
                    mbb.put(i + 2, msgByte[i]);
                }
                msgLabel.setVisible(true);
                mbb.put(0, (byte) 2);
                mbb.put(1, (byte) msgByte.length);
                System.out.println((byte)msgByte.length);
                mbb.force();
                while (mbb.get(0) == 2) {
                }
                msgLabel.setVisible(true);
                msgBuilder.append("对方已收到数据“");
                msgBuilder.append(msg);
                msgBuilder.append("”。\n");
                msgLabel.setText(msgBuilder.toString());
                btn.setDisable(false);
                dataField.setText("");
            }
        });

        Scene scene = new Scene(grid, 500, 275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
