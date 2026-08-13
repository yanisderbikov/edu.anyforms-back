package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;

public interface SaverCourse {

    Course saveCourse(Course course);

    CourseModule saveModule(CourseModule module);

    Lesson saveLesson(Lesson lesson);

    void deleteModule(CourseModule module);

    void deleteLesson(Lesson lesson);
}
