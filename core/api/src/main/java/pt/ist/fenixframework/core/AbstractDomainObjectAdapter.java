package pt.ist.fenixframework.core;

import static pt.ist.fenixframework.FenixFramework.getDomainModel;

import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;
import pt.ist.fenixframework.dml.DeletionListener.DeletionAdapter;

/**
 * This class contains useful code, required by concrete {@link DomainObject}s. Backend
 * implementations may benefit from the code in this class when providing their own implementations
 * of DomainObject.
 */
public class AbstractDomainObjectAdapter extends AbstractDomainObject {

    protected AbstractDomainObjectAdapter() {
    }

    protected AbstractDomainObjectAdapter(DomainObjectAllocator.OID oid) {
        super(oid);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * By default, collects all blockers from registered {@link DeletionAdapter}s.
     * </p>
     */
    @Override
    protected List<String> getDeletionBlockers() {
        List<String> result = new ArrayList<>();
        for (DeletionListener<DomainObject> listener : getDomainModel().getDeletionListenersForType(getClass())) {
            if (listener instanceof DeletionAdapter) {
                result.addAll(((DeletionAdapter<DomainObject>) listener).getDeletionBlockers(this));
            }
        }
        return result;
    }

    /**
     * Invokes all the registered {@link DeletionListener}s for this type.
     * 
     * <p>
     * This method (or an equivalent one) <strong>MUST</strong> be invoked by backends that support object deletion.
     * </p>
     */
    protected final void invokeDeletionListeners() {
        for (DeletionListener<DomainObject> listener : getDomainModel().getDeletionListenersForType(getClass())) {
            listener.deleting(this);
        }
    }

    // serialization code

    @Override
    protected SerializedForm makeSerializedForm() {
        return new SerializedForm(this);
    }

    protected static class SerializedForm extends AbstractDomainObject.SerializedForm {
        private static final long serialVersionUID = 1L;

        private SerializedForm(AbstractDomainObject obj) {
            super(obj);
        }

        @Override
        protected DomainObject fromExternalId(String externalId) {
            return FenixFramework.getDomainObject(externalId);
        }
    }

}
