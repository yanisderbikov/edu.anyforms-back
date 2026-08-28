package ru.anyforms.edu.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Тело таски на письмо «модуль открыт». Намеренно НЕ содержит готового письма —
 * только адресата и модуль. Тему, ссылку и HTML рендерит раннер в момент исполнения
 * таски: если модуль к тому времени переименовали, в письмо попадёт свежее название.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleOpenedEmailPayload {
    private String to;
    private String moduleId;
}
