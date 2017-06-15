package org.vaadin.viritin.v7.grid;

import com.vaadin.v7.data.Item;
import org.vaadin.viritin.v7.ListContainer;

/**
 *
 * @author datenhahn (http://datenhahn.de)
 * @since 23.04.2016
 * @param <M> the entity type listed in the consumer of the generator's container, Vaadin Grid
 */
public class StringPropertyValueGenerator<M> extends TypedPropertyValueGenerator<M, String> {

    public StringPropertyValueGenerator(Class<M> modelType, ValueGenerator<M> valueGenerator) {
        super(modelType, String.class, valueGenerator);
    }

    @Override
    public String getValue(Item item, Object itemId, Object propertyId) {
        return valueGenerator.getValue((M) ((ListContainer.DynaBeanItem) item).getBean());
    }

    public interface ValueGenerator<M> extends TypedPropertyValueGenerator.ValueGenerator<M, String> {
        @Override
        String getValue(M bean);
    }
}
