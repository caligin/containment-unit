package org.anima.ptsd;

import net.emaze.dysfunctional.contracts.dbc;
import net.emaze.dysfunctional.options.Maybe;

public class Fulfillable<T> {

    private Maybe<T> maybe = Maybe.nothing();

    public void fulfill(T value) {
        dbc.precondition(!maybe.hasValue(), "already fulfilled");
        this.maybe = Maybe.just(value);
    }

    public boolean isFulfilled() {
        return maybe.hasValue();
    }

    public T value() {
        return maybe.value();
    }

    public Maybe<T> asMaybe() {
        return maybe;
    }

}
