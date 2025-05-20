package org.cardanofoundation.metabus.mapper;

import org.mapstruct.*;
import org.mapstruct.MappingConstants.ComponentModel;

@MapperConfig(
    componentModel = ComponentModel.SPRING,
    builder = @Builder(disableBuilder = true),
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface BaseMapper {}
