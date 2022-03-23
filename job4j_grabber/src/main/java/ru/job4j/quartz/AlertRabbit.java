package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        try {
            Properties properties = loadProperties("rabbit.properties");
            Class.forName(properties.getProperty("db.driver-class-name"));
            try (Connection connection = DriverManager.getConnection(
                    properties.getProperty("db.url"),
                    properties.getProperty("db.username"),
                    properties.getProperty("db.password"))) {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("connection", connection);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(getInterval(properties))
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            }
        } catch (SchedulerException | InterruptedException | SQLException | ClassNotFoundException | IOException se) {
            se.printStackTrace();
        }
    }

    public static Properties loadProperties(String fileName) throws IOException {
        InputStream fis;
        Properties property = new Properties();
        fis = AlertRabbit.class.getClassLoader().getResourceAsStream(fileName);
        property.load(fis);
        return property;
    }

    public static int getInterval(Properties properties) {
        int interval;
        String intervalStr = properties.getProperty("rabbit.interval");
        interval = Integer.parseInt(intervalStr);
        return interval;
    }

    public static class Rabbit implements Job {
        public  Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail()
                    .getJobDataMap().get("connection");
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "insert into rabbit(created_date) values (?)",
                                 Statement.RETURN_GENERATED_KEYS)) {
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                statement.setTimestamp(1, ts);
                statement.execute();
                int id = 0;
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1);
                    }
                }
                System.out.println("id = " + id + "; create_date = " + ts.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}