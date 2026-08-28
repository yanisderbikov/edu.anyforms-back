package ru.anyforms.edu.service.email;

/** Загрузка HTML-шаблонов писем из resources/templates (паттерн anyforms-back). */
public final class EmailTemplate {

    private EmailTemplate() {
    }

    /** Письмо с кодом входа на платформу. */
    public static String getLoginCodeEmail(String code) {
        return load("templates/email-login-code.html").replace("%CODE%", esc(code));
    }

    /** Письмо «модуль открыт» со ссылкой на страницу модуля. */
    public static String getModuleOpenedEmail(String moduleTitle, String moduleUrl) {
        return load("templates/email-module-opened.html")
                .replace("%MODULE_TITLE%", esc(moduleTitle))
                .replace("%MODULE_URL%", esc(moduleUrl));
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String load(String templatePath) {
        try (var stream = EmailTemplate.class.getClassLoader().getResourceAsStream(templatePath)) {
            if (stream == null) {
                throw new IllegalStateException("Шаблон письма не найден: " + templatePath);
            }
            return new String(stream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Не получилось использовать шаблон письма: " + templatePath, e);
        }
    }
}
