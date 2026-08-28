package ru.anyforms.edu.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.anyforms.edu.dto.email.ModuleOpenedEmailPayload;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.SaverCourse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Объявляет об открытии модулей. Раз в несколько минут ищет модули, которые уже
 * открылись (наступила дата opensAt или админ открыл модуль руками), но о которых
 * студентам ещё не объявляли, и ставит в очередь по письму каждому активному студенту.
 * <p>
 * Сами письма шлёт {@link ru.anyforms.edu.service.task.runner.AbstractRunnableTask
 * раннер} пачками — так рассылка не упирается в лимит NotiSend. Таски всем студентам
 * и отметка «объявлено» на модуле коммитятся одной транзакцией: упало на середине —
 * всё откатилось, следующий тик начнёт с чистого листа, дублей не будет.
 */
@Slf4j
@Component
public class ModuleOpenedEmailSchedulerTask {

    private final GetterCourse getterCourse;
    private final GetterStudent getterStudent;
    private final SaverCourse saverCourse;
    private final TaskAdder taskAdder;
    private final TransactionTemplate transactionTemplate;

    public ModuleOpenedEmailSchedulerTask(GetterCourse getterCourse,
                                          GetterStudent getterStudent,
                                          SaverCourse saverCourse,
                                          TaskAdder taskAdder,
                                          TransactionTemplate transactionTemplate) {
        this.getterCourse = getterCourse;
        this.getterStudent = getterStudent;
        this.saverCourse = saverCourse;
        this.taskAdder = taskAdder;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelayString = "${tasks.module-opened.check-rate-ms}",
            initialDelayString = "${tasks.initial-delay-ms}")
    public void tick() {
        List<CourseModule> opened;
        try {
            // Та же «календарная» логика, что в CourseModule.isOpen()
            opened = getterCourse.getModulesToAnnounceOpen(LocalDate.now());
        } catch (Exception e) {
            log.error("Не удалось получить список открывшихся модулей", e);
            return;
        }
        for (CourseModule module : opened) {
            try {
                announce(module);
            } catch (Exception e) {
                log.error("Не удалось объявить об открытии модуля {}", module.getId(), e);
            }
        }
    }

    private void announce(CourseModule module) {
        List<Student> students = getterStudent.getAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .toList();
        transactionTemplate.executeWithoutResult(tx -> {
            for (Student student : students) {
                taskAdder.addTask(ModuleOpenedEmailPayload.builder()
                        .to(student.getEmail())
                        .moduleId(module.getId().toString())
                        .build());
            }
            module.setOpenEmailQueuedAt(Instant.now());
            saverCourse.saveModule(module);
        });
        log.info("Модуль «{}» открылся: в очередь поставлено {} писем", module.getTitle(), students.size());
    }
}
