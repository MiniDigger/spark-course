package me.minidigger.sparkcourse.model;

import java.util.List;

/**
 * Created by Martin on 15.01.2017.
 */
public interface CourseIdeaDAO {

    boolean add(CourseIdea idea);

    List<CourseIdea> findAll();

    CourseIdea findBySlug(String slug);
}
