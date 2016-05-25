package pl.edu.agh.toik;

import pl.edu.agh.student.smialek.tk.communications.server.CommunicationsServer;
import pl.edu.agh.student.smialek.tk.communications.server.SensorReading;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ServerApp {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        CommunicationsServer.registerCallback(ServerApp::printReading);
        CommunicationsServer.start(3737);
        System.out.println("Server started");
    }

    private static PrintStream printReading(SensorReading reading) {
        return System.out.format("[%s] %s: <%s> %s\n",
                dateFormatter.format(reading.getTimestamp()), reading.getSensorName(),
                reading.getColor(), reading.getValue()
        );
    }
}
