package pl.edu.agh.toik;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import pl.edu.agh.piechart.PieChartPanel;
import pl.edu.agh.student.mkasprz.tk.chart3.SimpleTable;
import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;
import pl.edu.agh.student.smialek.tk.communications.server.SensorReading;
import pl.edu.agh.toik.historychart.DataLineDoesNotExistException;
import pl.edu.agh.toik.historychart.HistoryChart;
import pl.edu.agh.toik.historychart.HistoryChartFactory;
import pl.edu.agh.toik.historychart.TimeUnit;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;

public class Controller {

    @FXML
    private SimpleTable simpleTable;

    @FXML
    private SwingNode pieChartPanelSwingNode;

    private PieChartPanel pieChartPanel;

    @FXML
    private SwingNode historyChartSwingNode;

    private HistoryChart historyChart;
    private int RR;
    private int RL;
    private int FR;
    private int FL;

    @FXML
    void initialize() {
        pieChartPanel = new PieChartPanel("TITLE");
        pieChartPanelSwingNode.setContent(pieChartPanel);
        historyChart = HistoryChartFactory.createNew("Roboclow motors", "X", TimeUnit.Second, "Y", "y");
        historyChartSwingNode.setContent(historyChart);

        // register lines, one for each motor
        RL =  historyChart.registerNewLine("Roboclow Rear Left");
        RR = historyChart.registerNewLine("Roboclow Rear Right");
        FL = historyChart.registerNewLine("Roboclow Front Left");
        FR = historyChart.registerNewLine("Roboclow Front Right");

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
                    dateFormatter.format((TemporalAccessor) reading.getTimestamp()), sensorName,
                    reading.getColor(), value
            );

            // LASER
            if (sensorName.startsWith("laser")) {
                // try to split it somehow
                if (value.contains(";")) {
                    String[] rawLaser = value.split(";");
                    simpleTable.put("laser dist", rawLaser[0].split(":")[1]);
                }
            }

            // ROBOCLOW
            try {
                if (sensorName.startsWith("roboclawRR") && Double.parseDouble(value) > 0) {
                    historyChart.addNewEntry(RR, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawRL") && Double.parseDouble(value) > 0) {
                    historyChart.addNewEntry(RL, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawFR") && Double.parseDouble(value) > 0) {
                    historyChart.addNewEntry(FR, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawFL") && Double.parseDouble(value) > 0) {
                    historyChart.addNewEntry(FL, Double.parseDouble(value), reading.getTimestamp());
                }
            } catch (DataLineDoesNotExistException ignore) {}
        });
    }


}
