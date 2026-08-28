package ru.anyforms.edu.service.task.runner;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.dto.email.ModuleOpenedEmailPayload;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.task.Task;
import ru.anyforms.edu.model.task.TaskStatus;
import ru.anyforms.edu.model.task.TaskType;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterTaskByStatus;
import ru.anyforms.edu.repository.SaverTask;
import ru.anyforms.edu.service.email.EmailService;
import ru.anyforms.edu.service.email.EmailTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Шлёт письма «модуль открыт». Тему, название и ссылку собирает в момент отправки
 * по живому модулю: переименовали модуль — в письме будет свежее название,
 * удалили или снова закрыли — письмо не уйдёт (таска ляжет в FAILED с причиной).
 */
@Slf4j
@Component
class ModuleOpenedEmailTaskRunner extends AbstractRunnableTask {

    private final GetterTaskByStatus getterTaskByStatus;
    private final GetterCourse getterCourse;
    private final EmailService emailService;
    private final Gson gson = new Gson();

    @Value("${url.front}")
    private String urlFront;

    ModuleOpenedEmailTaskRunner(GetterTaskByStatus getterTaskByStatus,
                                GetterCourse getterCourse,
                                EmailService emailService,
                                SaverTask saverTask) {
        super(saverTask);
        this.getterTaskByStatus = getterTaskByStatus;
        this.getterCourse = getterCourse;
        this.emailService = emailService;
    }

    @Override
    protected List<Task> fetchBatch(int batchSize) {
        return getterTaskByStatus.getByTaskTypeAndStatus(TaskType.MODULE_OPENED_EMAIL, TaskStatus.NEW, batchSize);
    }

    @Override
    protected void process(Task task) {
        ModuleOpenedEmailPayload payload = gson.fromJson(task.getPayload(), ModuleOpenedEmailPayload.class);
        CourseModule module = getterCourse.getModuleById(UUID.fromString(payload.getModuleId()))
                .orElseThrow(() -> new IllegalStateException(
                        "Модуль " + payload.getModuleId() + " удалён — письмо не отправлено"));
        if (!module.isOpen()) {
            throw new IllegalStateException(
                    "Модуль «" + module.getTitle() + "» снова закрыт — письмо не отправлено");
        }
        String moduleUrl = moduleUrl(module.getId());
        emailService.sendEmail(
                payload.getTo(),
                "Открыт новый модуль: " + module.getTitle(),
                EmailTemplate.getModuleOpenedEmail(module.getTitle(), moduleUrl));
    }

    private String moduleUrl(UUID moduleId) {
        String base = urlFront.endsWith("/") ? urlFront.substring(0, urlFront.length() - 1) : urlFront;
        return base + "/module/" + moduleId;
    }
}
