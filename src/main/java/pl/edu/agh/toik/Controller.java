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

import static java.lang.Math.abs;

@SuppressWarnings("Duplicates")
public class Controller {

    @FXML
    private SimpleTable simpleTable;

    @FXML
    private SwingNode pieChartPanelSwingNode;

    private PieChartPanel pieChartPanel;

    @FXML
    private SwingNode historyChartSwingNode;

    private HistoryChart historyChart;
    // line ids for roboclow
    private int roboclowRR;
    private int roboclowRL;
    private int roboclowFR;
    private int roboclowFL;

    @FXML
    void initialize() {
        pieChartPanel = new PieChartPanel("TITLE");
        pieChartPanelSwingNode.setContent(pieChartPanel);
        historyChart = HistoryChartFactory.createNew("Roboclow motors", "X", TimeUnit.Second, "Y", "y");
        historyChartSwingNode.setContent(historyChart);

        // register lines, one for each motor
        roboclowRL =  historyChart.registerNewLine("Roboclow Rear Left");
        roboclowRR = historyChart.registerNewLine("Roboclow Rear Right");
        roboclowFL = historyChart.registerNewLine("Roboclow Front Left");
        roboclowFR = historyChart.registerNewLine("Roboclow Front Right");

        CommunicationsServer.registerCallback(this::printReading);
        System.out.println("Callback registered.");

        simpleTable.put("READY", "true");
        pieChartPanel.setChartValue("laser-dist", 1);
        pieChartPanel.setChartValue("laser-angle", 1);
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    private void printReading(SensorReading reading) {
        Platform.runLater(() -> {
            final String sensorName = reading.getSensorName();
            final String value = reading.getValue();
            simpleTable.put(sensorName, value);
            System.out.format("[%s] %s: <%s> %s\n",
                    dateFormatter.format((TemporalAccessor) reading.getTimestamp()), sensorName,
                    reading.getColor(), value
            );

            // LASER
            if(sensorName.startsWith("laser")) {
                if (value.contains(";")) {
                    final String[] rawLaser = value.split(";");
                    String splitVal = rawLaser[0].split(":")[1];
                    simpleTable.put("laser-dist", splitVal);
                    pieChartPanel.setChartValue("laser-dist", abs(Double.parseDouble(splitVal)));
                    if (value.contains("|")) {
                        splitVal = rawLaser[1].split("\\|")[0];
                        simpleTable.put("laser-angle", splitVal);
                        pieChartPanel.setChartValue("laser-angle", abs(Double.parseDouble(splitVal)));
                    }
                }
            }

            // ROBOCLOW
            try {
                if (sensorName.startsWith("roboclawRR")) {
                    historyChart.addNewEntry(roboclowRR, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawRL")) {
                    historyChart.addNewEntry(roboclowRL, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawFR")) {
                    historyChart.addNewEntry(roboclowFR, Double.parseDouble(value), reading.getTimestamp());
                } else if (sensorName.startsWith("roboclawFL")) {
                    historyChart.addNewEntry(roboclowFL, Double.parseDouble(value), reading.getTimestamp());
                }
            } catch (DataLineDoesNotExistException ignore) {}
        });
    }
}
