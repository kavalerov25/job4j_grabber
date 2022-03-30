package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String PAGE_FIVE = "%s/vacancies/java_developer?PAGE=%s";

    public static void main(String[] args) throws Exception {
        for (int page = 1; page <= 5; page++) {
            String pageLink = String.format(PAGE_FIVE, SOURCE_LINK, page);
            Connection connection = Jsoup.connect(pageLink);
            Document document = connection.get();
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
            });
        }
    }
}