import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class TaskManagerApp extends Application {

    private final TableView<ProcessInfoBean> table = new TableView<>();
    private final ObservableList<ProcessInfoBean> data = FXCollections.observableArrayList();


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("任务管理器");
        VBox root = new VBox();
        HBox hBox = new HBox();
        Scene scene = new Scene(root);

        MenuBar menuBar = new MenuBar();

        Menu menu = new Menu("菜单");
        MenuItem newTaskBtn = new MenuItem("新建任务");
        newTaskBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("新建任务");
                dialog.setHeaderText("");
                dialog.setContentText("");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && result.get().isEmpty()) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "输入不能为空！");
                    error.showAndWait();
                }
                if (result.isPresent() && !result.get().isEmpty()) {
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                ManagerUtils.readConsole(result.get(), true);
                            } catch (IOException e1) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert error = new Alert(Alert.AlertType.ERROR, "异常：" + e1.getMessage());
                                        error.showAndWait();
                                    }
                                });
                            }
                        }
                    };
                    t.start();
                }
            }
        });
        menu.getItems().add(newTaskBtn);
        MenuItem refreshBtn = new MenuItem("刷新");
        menu.getItems().add(refreshBtn);
        MenuItem startManagerBtn = new MenuItem("启动本地任务管理器");
        menu.getItems().add(startManagerBtn);
        MenuItem aboutBtn = new MenuItem("关于任务管理器");
        menu.getItems().add(aboutBtn);
        menuBar.getMenus().add(menu);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        TableColumn nameCol = new TableColumn("映像名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn pidCol = new TableColumn("PID");
        pidCol.setCellValueFactory(new PropertyValueFactory<>("pid"));

        TableColumn sessionNameCol = new TableColumn("会话名");
        sessionNameCol.setCellValueFactory(new PropertyValueFactory<>("sessionName"));

        TableColumn sessionNumCol = new TableColumn("会话");
        sessionNumCol.setCellValueFactory(new PropertyValueFactory<>("sessionNum"));

        TableColumn memCol = new TableColumn("内存");
        memCol.setCellValueFactory(new PropertyValueFactory<>("memory"));

        Map map = ManagerUtils.getProcessList();

        for (int i = 0; i < map.size(); i++) {
            ProcessInfoBean pInfo = (ProcessInfoBean) map.get(i + 1);
            if (pInfo == null)
                continue;
            data.add(pInfo);
        }

        table.setItems(data);
        table.getColumns().addAll(nameCol, pidCol, sessionNameCol, sessionNumCol, memCol);

        Button killBtn = new Button("结束进程");
        Button newBtn = new Button("新建进程");
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(killBtn, newBtn);
        hBox.setSpacing(8);
        hBox.setPadding(new Insets(8, 8, 8, 0));

        root.getChildren().addAll(menuBar, table, hBox);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });
        primaryStage.show();
    }
}
