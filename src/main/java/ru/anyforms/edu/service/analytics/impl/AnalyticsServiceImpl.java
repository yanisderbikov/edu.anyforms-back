package ru.anyforms.edu.service.analytics.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.anyforms.edu.dto.admin.AnalyticsDTO;
import ru.anyforms.edu.dto.admin.AnalyticsDTO.ModuleProgress;
import ru.anyforms.edu.dto.admin.AnalyticsDTO.ModuleState;
import ru.anyforms.edu.dto.admin.AnalyticsDTO.Stage;
import ru.anyforms.edu.dto.admin.AnalyticsDTO.StageKind;
import ru.anyforms.edu.model.course.Course;
import ru.anyforms.edu.model.course.CourseModule;
import ru.anyforms.edu.model.course.Lesson;
import ru.anyforms.edu.model.user.LessonProgress;
import ru.anyforms.edu.model.user.ModuleVisit;
import ru.anyforms.edu.model.user.ServiceUser;
import ru.anyforms.edu.model.user.Student;
import ru.anyforms.edu.repository.ActivityStore;
import ru.anyforms.edu.repository.GetterCourse;
import ru.anyforms.edu.repository.GetterServiceUser;
import ru.anyforms.edu.repository.GetterStudent;
import ru.anyforms.edu.repository.ProgressStore;
import ru.anyforms.edu.service.analytics.AnalyticsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Собирает воронку в памяти: четыре запроса (модули с уроками, студенты,
 * все отметки уроков, все заходы) и группировка по студенту и модулю.
 * При сотнях студентов это дешевле, чем считать агрегаты в SQL и потом
 * всё равно склеивать их по строкам.
 */
@Service
@AllArgsConstructor
class AnalyticsServiceImpl implements AnalyticsService {

    private final GetterCourse getterCourse;
    private final GetterStudent getterStudent;
    private final GetterServiceUser getterServiceUser;
    private final ProgressStore progressStore;
    private final ActivityStore activityStore;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDTO getStudents() {
        Course course = getterCourse.getBySlug(Course.DEFAULT_SLUG)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));
        Instant now = Instant.now();
        List<CourseModule> modules = course.getModules();

        // Урок → модуль. Удалённые уроки в коллекцию не попадают, их отметки ниже отсеются
        Map<UUID, UUID> lessonModule = new HashMap<>();
        for (CourseModule module : modules) {
            for (Lesson lesson : module.getLessons()) {
                lessonModule.put(lesson.getId(), module.getId());
            }
        }

        // студент → модуль → отметки уроков
        Map<UUID, Map<UUID, List<LessonProgress>>> progress = new HashMap<>();
        for (LessonProgress row : progressStore.getAll()) {
            UUID moduleId = lessonModule.get(row.getLessonId());
            if (moduleId == null) {
                continue;
            }
            progress.computeIfAbsent(row.getStudentId(), k -> new HashMap<>())
                    .computeIfAbsent(moduleId, k -> new ArrayList<>())
                    .add(row);
        }

        // студент → модуль → заход
        Map<UUID, Map<UUID, ModuleVisit>> visits = new HashMap<>();
        for (ModuleVisit visit : activityStore.getAllVisits()) {
            visits.computeIfAbsent(visit.getStudentId(), k -> new HashMap<>())
                    .put(visit.getModuleId(), visit);
        }

        // Админы — не студенты: прогресс им не ведётся, в воронке они только шумят
        Set<String> adminEmails = getterServiceUser.getActive().stream()
                .map(ServiceUser::getEmail)
                .collect(Collectors.toSet());

        List<AnalyticsDTO.StudentRow> rows = getterStudent.getAll().stream()
                .filter(student -> !adminEmails.contains(student.getEmail()))
                .map(student -> toRow(student, modules, now,
                        progress.getOrDefault(student.getId(), Map.of()),
                        visits.getOrDefault(student.getId(), Map.of())))
                .toList();

        List<AnalyticsDTO.ModuleInfo> moduleInfos = modules.stream()
                .map(m -> new AnalyticsDTO.ModuleInfo(m.getId().toString(), m.getOrd(), m.getTitle(),
                        m.getLessons().size(), isOpen(m, now), m.getOpensAt()))
                .toList();

        return new AnalyticsDTO(now, moduleInfos, rows);
    }

    private AnalyticsDTO.StudentRow toRow(Student student, List<CourseModule> modules, Instant now,
                                          Map<UUID, List<LessonProgress>> progress,
                                          Map<UUID, ModuleVisit> visits) {
        List<ModuleProgress> cells = new ArrayList<>(modules.size());
        int available = 0;
        int started = 0;
        int done = 0;
        Instant lastActivity = latest(student.getLastSeenAt(), student.getOnboardingDoneAt());

        for (CourseModule module : modules) {
            boolean open = isOpen(module, now);
            ModuleProgress cell = toCell(module, open,
                    progress.getOrDefault(module.getId(), List.of()), visits.get(module.getId()));
            cells.add(cell);
            if (open) {
                available += cell.lessonsCount();
            }
            started += cell.lessonsStarted();
            done += cell.lessonsDone();
            lastActivity = latest(lastActivity, cell.lastActivityAt());
        }

        boolean noActivity = progress.isEmpty() && visits.isEmpty();
        Stage stage = stageOf(student, modules, cells, now, noActivity);

        return new AnalyticsDTO.StudentRow(
                student.getId().toString(),
                student.getEmail(),
                student.getPlan(),
                Boolean.TRUE.equals(student.getActive()),
                student.getCreatedAt(),
                student.getFirstSeenAt(),
                student.getLastSeenAt(),
                student.getOnboardingDoneAt(),
                lastActivity,
                available,
                started,
                done,
                stage,
                cells
        );
    }

    private ModuleProgress toCell(CourseModule module, boolean open,
                                  List<LessonProgress> rows, ModuleVisit visit) {
        int count = module.getLessons().size();
        int started = rows.size();
        int done = (int) rows.stream().filter(LessonProgress::isCompleted).count();

        Instant firstStarted = rows.stream().map(LessonProgress::getStartedAt)
                .filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        Instant lastStarted = rows.stream().map(LessonProgress::getStartedAt)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        Instant lastCompleted = rows.stream().map(LessonProgress::getCompletedAt)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        Instant lastVisited = visit == null ? null : visit.getLastVisitedAt();

        ModuleState state;
        if (count > 0 && done == count) {
            state = ModuleState.DONE;
        } else if (done > 0) {
            state = ModuleState.IN_PROGRESS;
        } else if (started > 0) {
            state = ModuleState.STARTED;
        } else if (visit != null) {
            state = ModuleState.OPENED;
        } else {
            state = open ? ModuleState.NOT_OPENED : ModuleState.LOCKED;
        }

        return new ModuleProgress(
                module.getId().toString(),
                state,
                count,
                started,
                done,
                visit == null ? 0 : visit.getVisits(),
                visit == null ? null : visit.getFirstVisitedAt(),
                lastVisited,
                firstStarted,
                lastCompleted,
                latest(lastVisited, lastStarted, lastCompleted)
        );
    }

    /**
     * Ступень воронки: до курса (не заходил / онбординг), внутри курса — по первому
     * открытому модулю с уроками, который ещё не пройден, после — ждёт следующий
     * модуль или прошёл всё.
     */
    private Stage stageOf(Student student, List<CourseModule> modules, List<ModuleProgress> cells,
                          Instant now, boolean noActivity) {
        if (noActivity && student.getOnboardingDoneAt() == null) {
            // Онбординг не пройден и следов в курсе нет. Отметки активности
            // могли не сохраниться, поэтому «есть прогресс» тоже считаем за вход
            return new Stage(student.getFirstSeenAt() == null ? StageKind.NEVER_SEEN : StageKind.ONBOARDING,
                    null, null);
        }
        for (int i = 0; i < modules.size(); i++) {
            CourseModule module = modules.get(i);
            ModuleProgress cell = cells.get(i);
            if (!isOpen(module, now) || cell.lessonsCount() == 0 || cell.state() == ModuleState.DONE) {
                continue;
            }
            StageKind kind = switch (cell.state()) {
                case OPENED -> StageKind.OPENED;
                case STARTED -> StageKind.STARTED;
                case IN_PROGRESS -> StageKind.IN_PROGRESS;
                default -> StageKind.NOT_OPENED;
            };
            return new Stage(kind, module.getOrd(), module.getTitle());
        }
        boolean lockedAhead = modules.stream().anyMatch(m -> !isOpen(m, now));
        return new Stage(lockedAhead ? StageKind.WAITING_NEXT : StageKind.COMPLETED, null, null);
    }

    /** Один момент «сейчас» на весь ответ, чтобы модуль не оказался открытым в одной строке и закрытым в другой */
    private static boolean isOpen(CourseModule module, Instant now) {
        return module.getOpensAt() == null || !module.getOpensAt().isAfter(now);
    }

    /** Позднейший из моментов, null пропускаются; все null → null */
    private static Instant latest(Instant... instants) {
        return Arrays.stream(instants)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
