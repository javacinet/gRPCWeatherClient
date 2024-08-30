package net.javaci;

import net.javaci.grpc.*;
import io.grpc.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Sample client code that makes gRPC calls to the server.
 */
public class WeatherClient {

    private static final Logger logger = Logger.getLogger(WeatherClient.class.getName());

    private final WeatherReporterGrpc.WeatherReporterBlockingStub blockingStub;

    /**
     * Construct client for accessing WeatherReporter server using the existing channel.
     */
    public WeatherClient(Channel channel) {
        blockingStub = WeatherReporterGrpc.newBlockingStub(channel);
    }

    /**
     * Blocking unary call example. Calls getCityWeatherSingleDay at the server and prints the response via the Logger.
     */
    public void getCityWeatherSingleDay(int day, int month, int year, String city, String country) {
        Date date = Date.newBuilder().setDay(day).setMonth(month).setYear(year).build();
        Location location = Location.newBuilder().setCity(city).setCountry(country).build();
        LocationDate locationDate = LocationDate.newBuilder().setDate(date).setLocation(location).build(); CityWeatherData response = null;
        try {
            response = blockingStub.getCityWeatherSingleDay(locationDate);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus()); return;
        }
        if (response.getWeather() == null) {
            logger.log(Level.INFO, "Found no weather data at {0}", locationDate);
        } else {
            logger.log(Level.INFO, "Found the following weather data: {0}", response);
        }
    }

    /**
     * Blocking server-streaming example. Calls getCityWeatherMultipleDays with a locationDatePeriod. Prints each
     * response CityWeatherData as it arrives via the Logger.
     */
    public void getCityWeatherMultipleDays(int startDay, int startMonth, int startYear, int endDay, int endMonth, int endYear, String city, String country) {
        Date startDate = Date.newBuilder().setDay(startDay).setMonth(startMonth).setYear(startYear).build(); Date endDate = Date.newBuilder().setDay(endDay).setMonth(endMonth).setYear(endYear).build(); Location location = Location.newBuilder().setCity(city).setCountry(country).build();
        LocationDatePeriod request = LocationDatePeriod.newBuilder().setStartDate(startDate).setEndDate(endDate).setLocation(location).build();
        Iterator<CityWeatherData> responses;
        try {
            responses = blockingStub.getCityWeatherMultipleDays(request);
            for (int i = 1; responses.hasNext(); i++) {
                CityWeatherData response = responses.next();
                logger.log(Level.INFO, "Response No. " + i + ": {0}", response); }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
    }}

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:8980";
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [target]");
                System.err.println("");
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            target = args[0];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            WeatherClient weatherClient = new WeatherClient(channel);

            // Here you can test some calls the WeatherReporterServer
            // For example:
            weatherClient.getCityWeatherSingleDay(30, 8, 2024, "Ankara", "Turkiye");
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}