package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("db.driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            cnn = DriverManager.getConnection(
                    cfg.getProperty("db.url"),
                    cfg.getProperty("db.username"),
                    cfg.getProperty("db.password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties initProperties() {
        Properties config = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        Post post1 = new Post("name", "link", "description", LocalDateTime.now());
        Post post2 = new Post("name2", "link2", "description2", LocalDateTime.now());
        Post post3 = new Post("name3", "link3", "description3", LocalDateTime.now());
        Post post4 = new Post("name4", "link4", "description4", LocalDateTime.now());
        try (PsqlStore psqlStore = new PsqlStore(initProperties())) {
            psqlStore.save(post1);
            psqlStore.save(post2);
            psqlStore.save(post3);
            psqlStore.save(post4);
            System.out.println("All");
            psqlStore.getAll().forEach(System.out::println);
            System.out.println("By ID");
            System.out.println(psqlStore.findById(3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement prepare = cnn.prepareStatement(
                "insert into post (name,link,text,created) values (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            prepare.setString(1, post.getTitle());
            prepare.setString(2, post.getLink());
            prepare.setString(3, post.getDescription());
            prepare.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            prepare.executeUpdate();
            try (ResultSet resultSet = prepare.getGeneratedKeys()) {
                if (resultSet.next()) {
                    post.setId(resultSet.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement prepare = cnn.prepareStatement("select * from post")) {
            prepare.executeQuery();
            try (ResultSet resultSet = prepare.getResultSet()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getTimestamp(5).toLocalDateTime()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement prepare = cnn.prepareStatement("select * from post where id=?")) {
            prepare.setInt(1, id);
            prepare.executeQuery();
            try (ResultSet resultSet = prepare.getResultSet()) {
                if (resultSet.next()) {
                    post = new Post(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getTimestamp(5).toLocalDateTime()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

}