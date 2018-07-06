package io.thorntail.config.impl;

import io.thorntail.config.impl.interpolation.Interpolator;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * Created by bob on 6/26/18.
 */
public class InterpolatingConverter implements Converter {

    public InterpolatingConverter(Interpolator interpolator, Converter delegate) {
        this.interpolator = interpolator;
        this.delegate = delegate;
    }

    @Override
    public Object convert(String value) {
        String interpolated = this.interpolator.interpolate(value);
        return this.delegate.convert(interpolated);
    }

    private final Interpolator interpolator;

    private final Converter delegate;
}
