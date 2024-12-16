package edu.stanford.protege.webprotegeeventshistory.config.converters;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ProjectIdToStringConverter implements Converter<ProjectId, String> {
    @Override
    public String convert(ProjectId projectId) {
        return projectId.id();
    }

}