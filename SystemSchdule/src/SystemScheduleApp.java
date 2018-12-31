import javafx.application.Application;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class SystemScheduleApp extends Application {

    private ArrayList<Process> processesList = new ArrayList<>();
    private final NumberAxis xAxis = new NumberAxis();
    private final CategoryAxis yAxis = new CategoryAxis();
    private final GanttChart<Number, String> chart = new GanttChart<Number, String>(xAxis, yAxis);

    @Override
    public void start(Stage stage) throws IOException {

        File file = new File("job1.txt");
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
        BufferedReader br = new BufferedReader(reader);
        String line;
        line = br.readLine();
        while (line != null) {
            processesList.add(new Process(line.split(",")));
            line = br.readLine();
        }
        br.close();
        reader.close();

        stage.setTitle("系统进程调度");
        VBox vBox = new VBox();

        String[] pLabels = new String[processesList.size()];
        for (int i = 0; i < processesList.size(); i++) {
            pLabels[i] = "进程" + processesList.get(i).getPid();
        }

        xAxis.setLabel("CPU时间");
        xAxis.setTickLabelFont(Font.font(14));
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(5);

        yAxis.setLabel("");
        yAxis.setTickLabelFont(Font.font(14));
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(8);
        yAxis.setCategories(FXCollections.observableArrayList(Arrays.asList(pLabels)));

        chart.setLegendVisible(false);
        chart.setBlockHeight(48);

        chart.getStylesheets().add(getClass().getResource("ganttchart.css").toExternalForm());

        ChoiceBox choiceBox = new ChoiceBox();
        HBox hBox = new HBox();
        Label lenLebel = new Label("时间片大小：");
        ChoiceBox lenChoiceBox = new ChoiceBox();
        hBox.getChildren().add(lenLebel);
        hBox.getChildren().add(lenChoiceBox);
        hBox.setAlignment(Pos.CENTER);
        choiceBox.setItems(FXCollections.observableArrayList("短作业优先算法", "时间片轮转算法", "优先数算法"));
        choiceBox.setValue("短作业优先算法");
        choiceBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                switch (newValue.toString()) {
                    case "短作业优先算法":
                        hBox.setVisible(false);
                        lenChoiceBox.setValue(1);
                        sjf();
                        break;
                    case "时间片轮转算法":
                        hBox.setVisible(true);
                        roundRobin(1);
                        break;
                    case "优先数算法":
                        hBox.setVisible(false);
                        lenChoiceBox.setValue(1);
                        priority();
                        break;
                }
            }
        });

        lenChoiceBox.setItems(FXCollections.observableArrayList(
                1, 2, 3)
        );
        lenChoiceBox.setValue(1);
        lenChoiceBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                switch ((Integer) newValue) {
                    case 1:
                        roundRobin(1);
                        break;
                    case 2:
                        roundRobin(2);
                        break;
                    case 3:
                        roundRobin(3);
                        break;
                }
            }
        });
        hBox.setVisible(false);

        sjf();

        vBox.setPadding(new Insets(16, 32, 16, 32));
        vBox.setSpacing(8);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.getChildren().add(choiceBox);
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(chart);

        Scene scene = new Scene(vBox, 620, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void sjf() {
        chart.getData().clear();
        XYChart.Series series = new XYChart.Series();
        chart.getData().addAll(series);
        HashSet<Integer> idSet = new HashSet<>();
        for (Process p : processesList) {
            idSet.add(p.getPid());
        }
        int currentTime = -1;
        int pId = -1;
        int cpuEndTime = -1;
        HashSet<Integer> arriveSet = new HashSet<>();
        while (idSet.size() != 0) {
            currentTime++;
            for (Process p : processesList) {
                if (p.getReachTime() <= currentTime
                        && idSet.contains(p.getPid())
                        && p.getPid() != pId) {
                    arriveSet.add(p.getPid());
                }
            }
            if (currentTime >= cpuEndTime) {
                cpuEndTime = -1;
                idSet.remove(pId);
            }
            if (cpuEndTime >= 0) continue;
            int inf = 999;
            int tmpId = 0;
            for (int i : arriveSet) {
                if (inf > processesList.get(i - 1).getDurTime()) {
                    inf = processesList.get(i - 1).getDurTime();
                    tmpId = i;
                }
            }
            if (tmpId == 0) continue;
            arriveSet.remove(tmpId);
            pId = tmpId;
            series.getData().add(new XYChart.Data(currentTime, "进程" + pId, new GanttChart.ExtraData(processesList.get(tmpId - 1).getDurTime(), "status-blue")));
            // System.out.println("进程" + pId + "," + currentTime + "," + (currentTime + processesList.get(tmpId - 1).getDurTime()));
            cpuEndTime = currentTime + processesList.get(tmpId - 1).getDurTime();
        }
    }

    private void priority() {
        chart.getData().clear();
        XYChart.Series series = new XYChart.Series();
        chart.getData().addAll(series);
        HashSet<Integer> idSet = new HashSet<>();
        HashSet<Integer> arriveSet = new HashSet<>();
        for (Process p : processesList) {
            p.setVisit(false);
            idSet.add(p.getPid());
        }
        int currentTime = 0;
        while (idSet.size() != 0 || arriveSet.size() != 0) {
            for (Process p : processesList) {
                if (!p.isVisit() && p.getReachTime() <= currentTime) {
                    arriveSet.add(p.getPid());
                    idSet.remove(p.getPid());
                    p.setVisit(true);
                }
            }
            int inf = 999;
            int tmpId = 0;
            for (int i : arriveSet) {
                if (inf > processesList.get(i - 1).getLevel()) {
                    tmpId = i;
                    inf = processesList.get(i - 1).getLevel();
                }
            }
            series.getData().add(new XYChart.Data(currentTime, "进程" + tmpId, new GanttChart.ExtraData(processesList.get(tmpId - 1).getDurTime(), "status-blue")));
            // System.out.println("进程" + tmpId + "," + currentTime + "," + (currentTime + processesList.get(tmpId - 1).getDurTime()));
            currentTime += processesList.get(tmpId - 1).getDurTime();
            arriveSet.remove(tmpId);
        }
    }

    private void roundRobin(int tLen) {
        chart.getData().clear();
        XYChart.Series series = new XYChart.Series();
        chart.getData().addAll(series);
        ArrayList<Process> pList = new ArrayList<>();
        HashSet<Integer> idSet = new HashSet<>();
        Queue<Integer> queue = new LinkedList<Integer>();
        for (Process p : processesList) {
            Process temP = new Process(p);
            pList.add(temP);
            idSet.add(temP.getPid());
        }
        int currentTime = 0;
        int pId = -1;
        int cpuTime = -1;
        while (idSet.size() != 0) {
            for (int i : idSet) {
                if (currentTime == pList.get(i - 1).getReachTime()) {
                    queue.offer(i);
                }
            }
            if (pId != -1 && (cpuTime == tLen || cpuTime == pList.get(pId - 1).getDurTime())) {
                int temT = pList.get(pId - 1).getDurTime();
                pList.get(pId - 1).setDurTime(temT - cpuTime);
                if (pList.get(pId - 1).getDurTime() == 0) {
                    idSet.remove(pId);
                } else {
                    queue.offer(pId);
                }
                pId = -1;
            }
            if (idSet.size() == 0) break;
            if (pId == -1) {
                pId = queue.poll();
                cpuTime = 1;
                series.getData().add(new XYChart.Data(currentTime, "进程" + pId, new GanttChart.ExtraData(1, "status-blue")));
                // System.out.println("进程" + pId + "," + currentTime + "," + (currentTime + 1));
            } else {
                cpuTime++;
                series.getData().add(new XYChart.Data(currentTime, "进程" + pId, new GanttChart.ExtraData(1, "status-blue")));
                // System.out.println("进程" + pId + "," + currentTime + "," + (currentTime + 1));
            }
            currentTime++;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

class Process {
    private int pid;
    private int reachTime;
    private int durTime;
    private int level;
    private boolean visit = false;

    Process(String[] strList) {
        pid = Integer.valueOf(strList[0]);
        reachTime = Integer.valueOf(strList[1]);
        durTime = Integer.valueOf(strList[2]);
        level = Integer.valueOf(strList[3]);
    }

    Process(Process process) {
        pid = process.pid;
        reachTime = process.reachTime;
        durTime = process.durTime;
        level = process.level;
        visit = process.visit;
    }

    int getPid() {
        return pid;
    }

    int getReachTime() {
        return reachTime;
    }

    int getDurTime() {
        return durTime;
    }

    int getLevel() {
        return level;
    }

    boolean isVisit() {
        return visit;
    }

    void setVisit(boolean visit) {
        this.visit = visit;
    }

    void setDurTime(int durTime) {
        this.durTime = durTime;
    }
}

class GanttChart<X, Y> extends XYChart<X, Y> {

    public static class ExtraData {

        private long length;
        private String styleClass;


        public ExtraData(long lengthMs, String styleClass) {
            super();
            this.length = lengthMs;
            this.styleClass = styleClass;
        }

        long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        String getStyleClass() {
            return styleClass;
        }

        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }


    }

    private double blockHeight = 10;

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.observableArrayList());
    }

    public GanttChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis);
        if (!(xAxis instanceof ValueAxis && yAxis instanceof CategoryAxis)) {
            throw new IllegalArgumentException("Axis type incorrect, X and Y should both be NumberAxis");
        }
        setData(data);
    }

    private static String getStyleClass(Object obj) {
        return ((ExtraData) obj).getStyleClass();
    }

    private static double getLength(Object obj) {
        return ((ExtraData) obj).getLength();
    }

    @Override
    protected void layoutPlotChildren() {

        for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

            Series<X, Y> series = getData().get(seriesIndex);

            Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
            while (iter.hasNext()) {
                Data<X, Y> item = iter.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                Node block = item.getNode();
                Rectangle ellipse;
                if (block != null) {
                    if (block instanceof StackPane) {
                        StackPane region = (StackPane) item.getNode();
                        if (region.getShape() == null) {
                            ellipse = new Rectangle(getLength(item.getExtraValue()), getBlockHeight());
                        } else if (region.getShape() instanceof Rectangle) {
                            ellipse = (Rectangle) region.getShape();
                        } else {
                            return;
                        }
                        ellipse.setWidth(getLength(item.getExtraValue()) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getXAxis()).getScale()) : 1));
                        ellipse.setHeight(getBlockHeight() * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis) getYAxis()).getScale()) : 1));
                        y -= getBlockHeight() / 2.0;

                        // Note: workaround for RT-7689 - saw this in ProgressControlSkin
                        // The region doesn't update itself when the shape is mutated in place, so we
                        // null out and then restore the shape in order to force invalidation.
                        region.setShape(null);
                        region.setShape(ellipse);
                        region.setScaleShape(false);
                        region.setCenterShape(false);
                        region.setCacheShape(false);

                        block.setLayoutX(x);
                        block.setLayoutY(y);
                    }
                }
            }
        }
    }

    public double getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(double blockHeight) {
        this.blockHeight = blockHeight;
    }

    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        Node block = createContainer(series, getData().indexOf(series), item, itemIndex);
        getPlotChildren().add(block);
    }

    @Override
    protected void dataItemRemoved(final Data<X, Y> item, final Series<X, Y> series) {
        final Node block = item.getNode();
        getPlotChildren().remove(block);
        removeDataItemFromDisplay(series, item);
    }

    @Override
    protected void dataItemChanged(Data<X, Y> item) {
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
        for (int j = 0; j < series.getData().size(); j++) {
            Data<X, Y> item = series.getData().get(j);
            Node container = createContainer(series, seriesIndex, item, j);
            getPlotChildren().add(container);
        }
    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {
        for (XYChart.Data<X, Y> d : series.getData()) {
            final Node container = d.getNode();
            getPlotChildren().remove(container);
        }
        removeSeriesFromDisplay(series);

    }


    private Node createContainer(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {

        Node container = item.getNode();

        if (container == null) {
            container = new StackPane();
            item.setNode(container);
        }

        container.getStyleClass().add(getStyleClass(item.getExtraValue()));

        return container;
    }

    @Override
    protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if (xa.isAutoRanging()) xData = new ArrayList<X>();
        if (ya.isAutoRanging()) yData = new ArrayList<Y>();
        if (xData != null || yData != null) {
            for (Series<X, Y> series : getData()) {
                for (Data<X, Y> data : series.getData()) {
                    if (xData != null) {
                        xData.add(data.getXValue());
                        xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }
                    if (yData != null) {
                        yData.add(data.getYValue());
                    }
                }
            }
            if (xData != null) xa.invalidateRange(xData);
            if (yData != null) ya.invalidateRange(yData);
        }
    }

}
