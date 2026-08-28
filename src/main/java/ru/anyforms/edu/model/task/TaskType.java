package ru.anyforms.edu.model.task;

import ru.anyforms.edu.dto.email.ModuleOpenedEmailPayload;

public enum TaskType {
    /** Письмо студенту об открытии модуля курса. */
    MODULE_OPENED_EMAIL(ModuleOpenedEmailPayload.class);

    private final Class<?> payloadClass;

    TaskType(Class<?> payloadClass) {
        this.payloadClass = payloadClass;
    }

    public Class<?> getPayloadClass() {
        return payloadClass;
    }

    public static TaskType fromObject(Object request) {
        if (request == null) {
            throw new IllegalArgumentException("Request не может быть null");
        }
        Class<?> requestClass = request.getClass();
        for (TaskType taskType : values()) {
            if (taskType.payloadClass.isAssignableFrom(requestClass)) {
                return taskType;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип таски для класса: " + requestClass.getName());
    }
}
