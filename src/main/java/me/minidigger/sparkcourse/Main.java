package me.minidigger.sparkcourse;

import java.util.HashMap;
import java.util.Map;

import me.minidigger.sparkcourse.model.CourseIdea;
import me.minidigger.sparkcourse.model.CourseIdeaDAO;
import me.minidigger.sparkcourse.model.NotFoundExection;
import me.minidigger.sparkcourse.model.SimpleCourseIdeaDAO;

import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

/**
 * Created by Martin on 15.01.2017.
 */
public class Main {

    public static final String FLASH_MESSAGE_KEY = "flash_message";

    public static void main(String[] args) {
        staticFileLocation("/public");
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

        before((rq, rs) -> {
            if (rq.cookie("username") != null) {
                rq.attribute("username", rq.cookie("username"));
            }
        });

        before("/ideas", (rq, rs) -> {
            if (rq.attribute("username") == null) {
                setFlashMessage(rq, "Please sign in first!");
                rs.redirect("/");
                halt();
            }
        });

        get("/", (rq, rs) -> {
            Map<String, String> model = new HashMap<>();
            model.put("username", rq.attribute("username"));
            model.put("flashMessage", captureFlashMessage(rq));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/sign-in", (rq, rs) -> {
            String username = rq.queryParams("username");
            rs.cookie("username", username);
            rs.redirect("/");
            return null;
        });

        get("/ideas", (rq, rs) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            model.put("flashMessage", captureFlashMessage(rq));
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        get("/ideas/:slug", (rq, rs) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("idea", dao.findBySlug(rq.params("slug")));
            return new ModelAndView(model, "idea.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas", (rq, rs) -> {
            String title = rq.queryParams("title");
            CourseIdea idea = new CourseIdea(title, rq.attribute("username"));
            dao.add(idea);
            rs.redirect("/ideas");
            return null;
        });

        post("/ideas/:slug/vote", (rq, rs) -> {
            CourseIdea idea = dao.findBySlug(rq.params("slug"));
            boolean added = idea.addVoter(rq.attribute("username"));
            if (added) {
                setFlashMessage(rq, "Thanks for your vote");
            } else {
                setFlashMessage(rq, "You already voted!");
            }
            rs.redirect("/ideas");
            return null;
        });

        exception(NotFoundExection.class, (ex, rq, rs) -> {
            rs.status(404);
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null, "not-found.hbs"));
            rs.body(html);
        });
    }

    private static void setFlashMessage(Request rq, String message) {
        rq.session().attribute(FLASH_MESSAGE_KEY, message);
    }

    private static String getFlashMessage(Request rq) {
        if (rq.session(false) == null) {
            return null;
        }
        if (!rq.session().attributes().contains(FLASH_MESSAGE_KEY)) {
            return null;
        }
        return (String) rq.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static String captureFlashMessage(Request rq) {
        String message = getFlashMessage(rq);
        if (message != null) {
            rq.session().removeAttribute(FLASH_MESSAGE_KEY);
        }
        return message;
    }
}
