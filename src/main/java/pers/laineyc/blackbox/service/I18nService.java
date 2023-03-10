package pers.laineyc.blackbox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class I18nService {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String code, Object... args) {
        return getMessage(code, "", args);
    }

    public String getMessage(String code, String defaultMessage, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

}
