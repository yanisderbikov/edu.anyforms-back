package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.course.LessonFile;

import java.util.List;

public interface SaverCourse {

    Course saveCourse(Course course);

    CourseModule saveModule(CourseModule module);

    Lesson saveLesson(Lesson lesson);

    LessonFile saveFile(LessonFile file);

    List<CourseModule> saveModules(List<CourseModule> modules);

    List<Lesson> saveLessons(List<Lesson> lessons);

    void deleteModule(CourseModule module);

    void deleteLesson(Lesson lesson);

    void deleteFile(LessonFile file);
}
