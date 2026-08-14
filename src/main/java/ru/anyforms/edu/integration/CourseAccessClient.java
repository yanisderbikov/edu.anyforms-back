package ru.anyforms.edu.integration;

/** Проверка в anyforms-5: покупал ли этот email курс и по какому тарифу. */
public interface CourseAccessClient {

    /**
     * @param hasAccess есть ли оплаченная покупка курса
     * @param plan      SELF / PERSONAL, null — доступа нет
     */
    record CourseAccess(boolean hasAccess, String plan) {

        public static final String PLAN_SELF = "SELF";
        public static final String PLAN_PERSONAL = "PERSONAL";

        public static CourseAccess denied() {
            return new CourseAccess(false, null);
        }
    }

    /**
     * @throws CourseAccessUnavailableException если anyforms-5 недоступен —
     *                                          отличаем «нет доступа» от «не смогли спросить»
     */
    CourseAccess check(String email);

    class CourseAccessUnavailableException extends RuntimeException {
        public CourseAccessUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
