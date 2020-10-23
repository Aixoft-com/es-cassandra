package com.aixoft.escassandra.component.util;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Aggregate;
import com.aixoft.escassandra.exception.runtime.AggregateAnnotationInvalidFormatException;
import com.aixoft.escassandra.exception.runtime.AggregateAnnotationMissingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableNameUtil {
    public static String fromAggregateClass(Class<? extends AggregateRoot> aggregateClass) {
        Aggregate annotation = aggregateClass.getAnnotation(Aggregate.class);

        String tableName;
        if (annotation != null) {
            tableName = annotation.tableName();
        } else {
            throw new AggregateAnnotationMissingException(String.format("%s not annotated with %s", aggregateClass.getName(), Aggregate.class.getName()));
        }

        if(!tableName.matches("^\\w+$")) {
            throw new AggregateAnnotationInvalidFormatException(
                String.format("%s: tableName can only contain alphanumerical characters including '_'", aggregateClass.getName()));
        }

        return tableName;
    }
}
