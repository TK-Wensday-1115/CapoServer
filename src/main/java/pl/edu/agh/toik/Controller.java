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
import java.util.Arrays;
import java.util.stream.Collectors;

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
//            simpleTable.put(sensorName, value);
//            System.out.format("[%s] %s: <%s> %s\n",
//                    dateFormatter.format((TemporalAccessor) reading.getTimestamp()), sensorName,
//                    reading.getColor(), value
//            );

            // LASER
            if(sensorName.startsWith("laser")) {
                if (value.contains("|")) {
                    final String[] laserMeasurements = value.split("\\|");
                    double averageDistance = Arrays.asList(laserMeasurements).stream()
                            .map(pair -> {
                                String[] parts = pair.split(";");
                                String distPart = (parts[0].startsWith("dist")) ? parts[0] : parts[1];
                                String distFloat = distPart.split(":")[1];
                                return Float.parseFloat(distFloat);
                            })
                            .collect(Collectors.averagingDouble(v -> v));
                    simpleTable.put("laser-avg-dist", String.valueOf(averageDistance));
                    pieChartPanel.setChartValue("laser-avg-dist", abs(averageDistance));
                }
            } else {
                simpleTable.put(sensorName, value);
            }

            // ROBOCLOW
            try {
                if (sensorName.startsWith("roboclawRR")) {
                    historyChart.addNewEntry(roboclowRR, Double.parseDouble(value), new java.util.Date()); // reading.getTimestamp()
                } else if (sensorName.startsWith("roboclawRL")) {
                    historyChart.addNewEntry(roboclowRL, Double.parseDouble(value), new java.util.Date());
                } else if (sensorName.startsWith("roboclawFR")) {
                    historyChart.addNewEntry(roboclowFR, Double.parseDouble(value), new java.util.Date());
                } else if (sensorName.startsWith("roboclawFL")) {
                    historyChart.addNewEntry(roboclowFL, Double.parseDouble(value), new java.util.Date());
                }
            } catch (DataLineDoesNotExistException ignore) {}
        });
    }
}
