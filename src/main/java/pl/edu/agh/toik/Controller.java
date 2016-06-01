package pl.edu.agh.toik;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import pl.edu.agh.piechart.PieChartPanel;
import pl.edu.agh.student.mkasprz.tk.chart3.SimpleTable;
import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;
import pl.edu.agh.student.smialek.tk.communications.server.SensorReading;
import pl.edu.agh.toik.historychart.HistoryChartFactory;
import pl.edu.agh.toik.historychart.TimeUnit;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Controller {

    @FXML
    private SimpleTable simpleTable;

    @FXML
    private SwingNode pieChartPanel;

    @FXML
    private SwingNode historyChart;

    @FXML
    void initialize() {
        pieChartPanel.setContent(new PieChartPanel("TITLE"));
        historyChart.setContent(HistoryChartFactory.createNew("NAME", "X", TimeUnit.Second, "Y", "y"));

        CommunicationsServer.registerCallback(this::printReading);
        System.out.println("Callback registered.");

        simpleTable.put("READY", "true");
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    private PrintStream printReading(SensorReading reading) {
        simpleTable.put(reading.getSensorName(), reading.getValue());
        return System.out.format("[%s] %s: <%s> %s\n",
                dateFormatter.format(reading.getTimestamp()), reading.getSensorName(),
                reading.getColor(), reading.getValue()
        );
    }

}
