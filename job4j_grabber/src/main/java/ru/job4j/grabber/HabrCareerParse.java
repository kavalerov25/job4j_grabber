package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final String PAGE_FIVE = "%s/vacancies/java_developer?PAGE=%s";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }


    public static void main(String[] args) throws Exception {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HarbCareerDateTimeParser());
        habrCareerParse.list(PAGE_LINK);
    }


    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".job_show_description__body");
        Elements descriptionElement = rows.select(".job_show_description__vacancy_description");
        return descriptionElement.text();
    }

    @Override
    public List<Post> list(String address) {
        List<Post> posts = new ArrayList<>();
        Connection connection = Jsoup.connect(address);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select(".vacancy-card__date").first().child(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String vacancyDate = null;
            try {
                vacancyDate = dateFormat.format(dateFormat.parse(dateElement.attr("datetime")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s: %s %s%n", new HarbCareerDateTimeParser().parse(dateElement.attr("datetime")), vacancyName, link);

            String text = null;
            try {
                text = retrieveDescription(link);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("< " + text + " />");
        });
        return posts;
    }
}