package org.vaadin.viritin.v7.fields;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vaadin.viritin.fields.CaptionGenerator;
import org.vaadin.viritin.v7.ListContainer;
import org.vaadin.viritin.v7.fields.config.ComboBoxConfig;
import org.vaadin.viritin.v7.fields.config.ListSelectConfig;
import org.vaadin.viritin.v7.fields.config.OptionGroupConfig;

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Resource;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.ListSelect;
import com.vaadin.v7.ui.NativeSelect;
import com.vaadin.v7.ui.OptionGroup;

/**
 * A select implementation with better typed API than in core Vaadin.
 *
 * By default the options toString is used to generate the caption for option.
 * To override this behavior, use setCaptionGenerator or override getCaption(T)
 * to provide your own strategy.
 * <p>
 * Behind the scenes uses Vaadin cores NativeSelect (default) or other cores
 * AbstractSelect implementation, type provided in constructor. Tree and Table
 * are not supported, see MTable.
 * <p>
 * Note, that this select is always in single select mode. See MultiSelectTable
 * for a proper "multiselect".
 * <p>
 * NOTE, that if your options might be empty OR you might use multiple different
 * types of options in your select, you have to specify the common superclass
 * for the options either using constructor with the class parameter or using
 * setFieldType method. Due to Java type erasure, we cannot properly detect it
 * with generics (type parameters) only.
 *
 * @author mstahv
 * @param <T> the type of selects value
 */
public class TypedSelect<T> extends CustomField<T> {

    private static final long serialVersionUID = -8585094121129546069L;

    private CaptionGenerator<T> captionGenerator;
    private IconGenerator<T> iconGenerator;

    private AbstractSelect select;

    private ListContainer<T> bic;

    private Class<T> fieldType;

    /**
     * The type of element options in the select
     *
     * @param type the type of options in the list
     */
    public TypedSelect(Class<T> type) {
        this.fieldType = type;
        bic = new ListContainer<>(type);
    }

    /**
     * Note, that with this constructor, you cannot override the select type.
     *
     * @param options options to select from
     */
    public TypedSelect(T... options) {
        setOptions(options);
    }

    public TypedSelect(String caption) {
        setCaption(caption);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the width of the wrapped select component.
     *
     * @param width the new width for this select
     * @param unit the unit of the new width
     */
    @Override
    public void setWidth(float width, Unit unit) {
        if (select != null) {
            select.setWidth(width, unit);
        }
        super.setWidth(width, unit);
    }

    @Override
    public void addStyleName(String style) {
        getSelect().addStyleName(style);
        super.addStyleName(style);
    }

    @Override
    public void removeStyleName(String style) {
        getSelect().removeStyleName(style);
        super.removeStyleName(style);
    }

    @Override
    public void setStyleName(String style) {
        getSelect().setStyleName(style);
        super.setStyleName(style);
    }

    /**
     * Note, that with this constructor, you cannot override the select type.
     *
     * @param caption the caption for the select
     * @param options available options for the select
     */
    public TypedSelect(String caption, Collection<T> options) {
        this(caption);
        setOptions(options);
    }

    public TypedSelect<T> withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public TypedSelect<T> withCaption(String caption, boolean captionAsHtml) {
        setCaption(caption);
        setCaptionAsHtml(captionAsHtml);
        return this;
    }

    public TypedSelect<T> asListSelectType() {
        return asListSelectType(null);
    }

    public TypedSelect<T> asListSelectType(ListSelectConfig config) {
        ListSelect listSelect = new ListSelect() {

            private static final long serialVersionUID = -6410506555879530635L;

            @SuppressWarnings("unchecked")
            @Override
            public String getItemCaption(Object itemId) {
                return TypedSelect.this.getCaption((T) itemId);
            }

            @Override
            public Resource getItemIcon(Object itemId) {
                if (iconGenerator != null) {
                    return iconGenerator.getIcon((T) itemId);
                }
                return super.getItemIcon(itemId);
            }

        };
        if (config != null) {
            config.configurateListSelect(listSelect);
        }
        setSelectInstance(listSelect);
        return this;
    }

    public TypedSelect<T> asOptionGroupType() {
        return asOptionGroupType(null);
    }

    public TypedSelect<T> asOptionGroupType(OptionGroupConfig config) {
        OptionGroup optionGroup = new OptionGroup() {

            private static final long serialVersionUID = 6181647051422960470L;

            @SuppressWarnings("unchecked")
            @Override
            public String getItemCaption(Object itemId) {
                return TypedSelect.this.getCaption((T) itemId);
            }

            @Override
            public Resource getItemIcon(Object itemId) {
                if (iconGenerator != null) {
                    return iconGenerator.getIcon((T) itemId);
                }
                return super.getItemIcon(itemId);
            }

        };
        if (config != null) {
            config.configurateOptionGroup(optionGroup);
        }
        setSelectInstance(optionGroup);
        return this;
    }

    public TypedSelect<T> asComboBoxType() {
        return asComboBoxType(null);
    }

    public TypedSelect<T> asComboBoxType(ComboBoxConfig config) {
        ComboBox comboBox = new ComboBox() {

            private static final long serialVersionUID = -468737106391765750L;

            @SuppressWarnings("unchecked")
            @Override
            public String getItemCaption(Object itemId) {
                return TypedSelect.this.getCaption((T) itemId);
            }

            @Override
            public Resource getItemIcon(Object itemId) {
                if (iconGenerator != null) {
                    return iconGenerator.getIcon((T) itemId);
                }
                return super.getItemIcon(itemId);
            }
        };
        if (config != null) {
            config.configurateComboBox(comboBox);
        }
        setSelectInstance(comboBox);
        LazyComboBox.fixComboBoxVaadinIssue16647(comboBox);
        return this;
    }

    public TypedSelect<T> asNativeSelectType() {
        setSelectInstance(new NativeSelect() {

            private static final long serialVersionUID = 7903027904040681357L;

            @SuppressWarnings("unchecked")
            @Override
            public String getItemCaption(Object itemId) {
                return TypedSelect.this.getCaption((T) itemId);
            }

            @Override
            public Resource getItemIcon(Object itemId) {
                if (iconGenerator != null) {
                    return iconGenerator.getIcon((T) itemId);
                }
                return super.getItemIcon(itemId);
            }
        });
        return this;
    }

    public TypedSelect<T> withSelectType(
            Class<? extends AbstractSelect> selectType) {
        if (selectType == ListSelect.class) {
            asListSelectType();
        } else if (selectType == OptionGroup.class) {
            asOptionGroupType();
        } else if (selectType == ComboBox.class) {
            asComboBoxType();
        } else {
            asNativeSelectType();
        }
        return this;
    }

    protected void setSelectInstance(AbstractSelect select) {
        if (this.select != null) {
            piggyBackListener = null;
        }
        this.select = select;
    }

    /**
     *
     * @return the backing select instance, overriding this method may be
     * hazardous
     */
    protected AbstractSelect getSelect() {
        if (select == null) {
            withSelectType(null);
            if (bic != null) {
                select.setContainerDataSource(bic);
            }
        }
        ensurePiggybackListener();
        return select;
    }

    /**
     * Sets the input prompt used by the TypedSelect, in case the backing
     * instance supports inputprompt (read: is ComboBox).
     *
     * @param inputPrompt the input prompt
     * @return this TypedSelect instance
     */
    public TypedSelect<T> setInputPrompt(String inputPrompt) {
        if (getSelect() instanceof ComboBox) {
            ((ComboBox) getSelect()).setInputPrompt(inputPrompt);
        }
        return this;
    }

    protected String getCaption(T option) {
        if (captionGenerator != null) {
            return captionGenerator.getCaption(option);
        }
        return option == null ? "" : option.toString();
    }

    protected Resource getIcon(T entity) {
        if (iconGenerator != null) {
            return iconGenerator.getIcon(entity);
        }
        return null;
    }

    @Override
    public void focus() {
        getSelect().focus();
    }

    public final TypedSelect<T> setOptions(T... values) {
        return setOptions(Arrays.asList(values));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getType() {

        if (fieldType == null) {
            try {
                fieldType = (Class<T>) ((Container.Ordered) select
                        .getContainerDataSource()).firstItemId().getClass();
            } catch (Exception e) {
                // If field type isn't set or can't be detected just report
                // Object, should be fine in most cases (vaadin will just
                // assign value without conversion.
                return Object.class;
            }
        }
        return fieldType;
    }

    /**
     * Explicitly sets the element type of the select.
     *
     * @param type the type of options in the select
     * @return this typed select instance
     */
    public TypedSelect<T> setType(Class<T> type) {
        this.fieldType = type;
        return this;
    }

    /**
     * Explicitly sets the element type of the select.
     *
     * @param type the type of options in the select
     * @return this typed select instance
     */
    public TypedSelect<T> setFieldType(Class<T> type) {
        this.fieldType = type;
        return this;
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        super.setInvalidCommitted(isCommitted);
        getSelect().setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        getSelect().commit();
        super.commit();
    }

    @Override
    public void discard() throws SourceException {
        getSelect().discard();
        super.discard();
    }

    /**
     * Buffering probably doesn't work correctly for this field, but in general
     * you should never use buffering either.
     * https://vaadin.com/web/matti/blog/-/blogs/3-pro-tips-for-vaadin-developers
     *
     * @param buffered true if buffering should be on
     */
    @Override
    public void setBuffered(boolean buffered) {
        getSelect().setBuffered(buffered);
        super.setBuffered(buffered);
    }

    @Override
    public boolean isBuffered() {
        return getSelect().isBuffered();
    }

    @Override
    protected void setInternalValue(T newValue) {
        super.setInternalValue(newValue);
        getSelect().setValue(newValue);
    }

    public TypedSelect<T> addMValueChangeListener(
            MValueChangeListener<T> listener) {
        addListener(MValueChangeEvent.class, listener,
                MValueChangeEventImpl.VALUE_CHANGE_METHOD);
        return this;
    }

    public TypedSelect<T> removeMValueChangeListener(
            MValueChangeListener<T> listener) {
        removeListener(MValueChangeEvent.class, listener,
                MValueChangeEventImpl.VALUE_CHANGE_METHOD);
        return this;
    }

    @Override
    public int getTabIndex() {
        return getSelect().getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        getSelect().setTabIndex(tabIndex);
    }

    public CaptionGenerator<T> getCaptionGenerator() {
        return captionGenerator;
    }

    public TypedSelect<T> setCaptionGenerator(
            CaptionGenerator<T> captionGenerator) {
        this.captionGenerator = captionGenerator;
        return this;
    }

    public TypedSelect<T> setIconGenerator(IconGenerator<T> generator) {
        this.iconGenerator = generator;
        return this;
    }

    public IconGenerator<T> getIconGenerator() {
        return iconGenerator;
    }

    public final TypedSelect<T> setOptions(Collection<T> options) {
        if (bic != null) {
            bic.setCollection(options);
        } else {
            bic = new ListContainer<>(options);
        }
        getSelect().setContainerDataSource(bic);
        return this;
    }

    public final List<T> getOptions() {
        if (bic == null) {
            return Collections.EMPTY_LIST;
        } else {
            return (List<T>) bic.getItemIds();
        }
    }

    public TypedSelect<T> setNullSelectionAllowed(boolean nullAllowed) {
        getSelect().setNullSelectionAllowed(nullAllowed);
        return this;
    }

    public TypedSelect<T> setBeans(Collection<T> options) {
        return setOptions(options);
    }

    public Collection<T> getBeans() {
        return (Collection) bic.getItemIds();
    }

    @Override
    public void attach() {
        ensureContainerSet();
        super.attach();
    }

	private void ensureContainerSet() {
		if (bic != null && getSelect().getContainerDataSource() != bic) {
            getSelect().setContainerDataSource(bic);
        }
	}
	
	@Override
	public void setValue(T newFieldValue) throws com.vaadin.v7.data.Property.ReadOnlyException, ConversionException {
		ensureContainerSet();
		super.setValue(newFieldValue);
	}

    private ValueChangeListener piggyBackListener;

    private void ensurePiggybackListener() {
        if (piggyBackListener == null) {
            piggyBackListener = new ValueChangeListener() {

                private static final long serialVersionUID = 8457613845310646169L;

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    setValue((T) event.getProperty().getValue());
                    fireEvent(new MValueChangeEventImpl<T>(TypedSelect.this));
                }
            };
            getSelect().addValueChangeListener(piggyBackListener);
        }
    }

    public TypedSelect<T> withFullWidth() {
        setWidth("100%");
        return this;
    }

    public TypedSelect<T> withReadOnly(boolean readOnly) {
        setReadOnly(readOnly);
        return this;
    }

    public TypedSelect<T> withValidator(Validator validator) {
        setImmediate(true);
        addValidator(validator);
        return this;
    }

    public TypedSelect<T> withWidth(float width, Unit unit) {
        setWidth(width, unit);
        return this;
    }

    public TypedSelect<T> withWidth(String width) {
        setWidth(width);
        return this;
    }

    public TypedSelect<T> withWidthUndefined() {
        setWidthUndefined();
        return this;
    }

    public TypedSelect<T> withId(String id) {
        setId(id);
        return this;
    }

    public TypedSelect<T> withStyleName(String... styleNames) {
        for (String styleName : styleNames) {
            addStyleName(styleName);
        }
        return this;
    }

    public TypedSelect<T> withIcon(Resource icon) {
        setIcon(icon);
        return this;
    }

    public void selectFirst() {
        if (bic != null && bic.size() > 0) {
            getSelect().setValue(bic.getIdByIndex(0));
        }
    }

    public TypedSelect<T> withNullSelectionAllowed(boolean nullAllowed) {
        setNullSelectionAllowed(nullAllowed);
        return this;
    }

    public TypedSelect<T> withVisible(boolean visible) {
        setVisible(visible);
        return this;
    }

    public TypedSelect<T> withDescription(String description) {
        setDescription(description);
        return this;
    }

    public TypedSelect<T> withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    /**
     *
     * @return gets the ListContainer used by this component
     */
    protected ListContainer<T> getBic() {
        return bic;
    }

    /**
     *
     * @param listContainer sets the ListContainer used by this select. For
     * extensions only, should be set early or will fail.
     */
    protected void setBic(ListContainer<T> listContainer) {
        bic = listContainer;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        getSelect().setReadOnly(readOnly);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getSelect().setEnabled(enabled);
    }

    @Override
    protected Component initContent() {
        return getSelect();
    }

    public void addOption(T option) {
        getBic().addItem(option);
    }

    @Override
    public ErrorMessage getErrorMessage() {
        final ErrorMessage errorMessage = super.getErrorMessage();
        getSelect().setStyleName("error", errorMessage != null);
        return errorMessage;
    }

}
