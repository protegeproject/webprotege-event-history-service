package edu.stanford.protege.webprotegeeventshistory.config.converters;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import javax.annotation.Nonnull;

@ReadingConverter
public class StringToProjectIdConverter implements Converter<String, ProjectId> {
    @Override
    public ProjectId convert(@Nonnull String source) {
        return ProjectId.valueOf(source);
    }
}