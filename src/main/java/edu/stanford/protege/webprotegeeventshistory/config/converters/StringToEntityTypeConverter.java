package edu.stanford.protege.webprotegeeventshistory.config.converters;

import jakarta.annotation.Nullable;
import org.semanticweb.owlapi.model.EntityType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import javax.annotation.Nonnull;

@ReadingConverter
public class StringToEntityTypeConverter implements Converter<String,EntityType> {
    @Override
    public EntityType convert(@Nullable String source) {
        if (source == null) {
            return null;
        }
        return EntityType.values()
                .stream()
                .filter(et -> et.getName().equals(source))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown EntityType: " + source));
    }
}

