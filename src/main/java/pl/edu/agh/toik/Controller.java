package pl.edu.agh.toik;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import pl.edu.agh.piechart.PieChartPanel;
import pl.edu.agh.student.mkasprz.tk.chart3.SimpleTable;
import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;
import pl.edu.agh.student.smialek.tk.communications.server.SensorReading;
import pl.edu.agh.toik.historychart.HistoryChart;
import pl.edu.agh.toik.historychart.HistoryChartFactory;
import pl.edu.agh.toik.historychart.TimeUnit;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Controller {

    @FXML
    private SimpleTable simpleTable;

    @FXML
    private SwingNode pieChartPanelSwingNode;

    private PieChartPanel pieChartPanel;

    @FXML
    private SwingNode historyChartSwingNode;

    private HistoryChart historyChart;

    @FXML
    void initialize() {
        pieChartPanel = new PieChartPanel("TITLE");
        pieChartPanelSwingNode.setContent(pieChartPanel);
        historyChart = HistoryChartFactory.createNew("NAME", "X", TimeUnit.Second, "Y", "y");
        historyChartSwingNode.setContent(historyChart);

        CommunicationsServer.registerCallback(this::printReading);
        System.out.println("Callback registered.");

        simpleTable.put("READY", "true");
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    private void printReading(SensorReading reading) {
        Platform.runLater(() -> {
            String sensorName = reading.getSensorName();
            String value = reading.getValue();
            simpleTable.put(sensorName, value);
            System.out.format("[%s] %s: <%s> %s\n",
                    dateFormatter.format(reading.getTimestamp()), sensorName,
                    reading.getColor(), value
            );

            simpleTable.put("laser", "..."); // [TODO] Data from 'laser' should be somehow formatted before displaying in 'SimpleTable'.

            if (sensorName.startsWith("roboclaw") && Double.parseDouble(value) > 0) {
                pieChartPanel.setChartValue(sensorName, Double.parseDouble(value));
            }

        });
    }


}
