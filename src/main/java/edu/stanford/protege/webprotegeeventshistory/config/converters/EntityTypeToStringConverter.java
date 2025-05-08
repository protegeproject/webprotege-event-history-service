package edu.stanford.protege.webprotegeeventshistory.config.converters;

import org.semanticweb.owlapi.model.EntityType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EntityTypeToStringConverter implements Converter<EntityType, String> {
    @Override
    public String convert(EntityType entityType) {
        return entityType.getName();
    }
}
