package ru.anyforms.edu.repository.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.course.ModuleFile;

import java.util.UUID;

@Repository
interface ModuleFileRepo extends JpaRepository<ModuleFile, UUID> {

    /** Материалы модулей с этим ключом. Модули удаляются жёстко — «живость» проверять не нужно */
    long countByFileUrl(String fileUrl);
}
