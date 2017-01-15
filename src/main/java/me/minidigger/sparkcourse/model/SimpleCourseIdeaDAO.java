package me.minidigger.sparkcourse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Martin on 15.01.2017.
 */
public class SimpleCourseIdeaDAO implements CourseIdeaDAO {

    private List<CourseIdea> ideas = new ArrayList<>();

    @Override
    public boolean add(CourseIdea idea) {
        return ideas.add(idea);
    }

    @Override
    public List<CourseIdea> findAll() {
        return Collections.unmodifiableList(ideas);
    }

    @Override
    public CourseIdea findBySlug(String slug) {
        return ideas.stream().filter(idea -> idea.getSlug().equals(slug)).findFirst().orElseThrow(NotFoundExection::new);
    }
}
