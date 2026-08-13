package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.user.Student;

public interface SaverStudent {

    Student save(Student student);

    void delete(Student student);
}
