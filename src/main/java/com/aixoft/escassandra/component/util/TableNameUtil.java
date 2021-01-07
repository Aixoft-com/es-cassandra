package com.aixoft.escassandra.component.util;

import com.aixoft.escassandra.annotation.AggregateData;
import com.aixoft.escassandra.config.constants.RegexPattern;
import com.aixoft.escassandra.exception.runtime.AggregateAnnotationInvalidFormatException;
import com.aixoft.escassandra.exception.runtime.AggregateAnnotationMissingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The type Table name util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableNameUtil {
    /**
     * Gets tableName from aggregate data class using reflection.
     * See {@link AggregateData#tableName()}.
     *
     * @param aggregateDataClass Aggregate data class.
     *
     * @return Cassandra table name for the aggregate.
     */
    public static String fromAggregateDataClass(Class<?> aggregateDataClass) {
        AggregateData annotation = aggregateDataClass.getAnnotation(AggregateData.class);

        String tableName;
        if (annotation != null) {
            tableName = annotation.tableName();
        } else {
            throw new AggregateAnnotationMissingException(String.format("%s not annotated with %s.", aggregateDataClass.getName(), AggregateData.class.getName()));
        }

        if(!tableName.matches(RegexPattern.IS_ALPHANUMERIC)) {
            throw new AggregateAnnotationInvalidFormatException(
                String.format("%s: tableName can only contain alphanumerical characters including '_'.", aggregateDataClass.getName()));
        }

        return tableName;
    }
}
