package pl.edu.agh.toik;

import javafx.fxml.FXML;
import pl.edu.agh.student.mkasprz.tk.chart3.SimpleTable;
import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;
import pl.edu.agh.student.smialek.tk.communications.server.SensorReading;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Controller {

    @FXML
    private SimpleTable simpleTable;

    @FXML
    void initialize() {
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
