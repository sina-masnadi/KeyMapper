/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package io.github.sds100.keymapper.usbserial.driver;

import android.util.Pair;

import io.github.sds100.keymapper.usbserial.driver.UsbSerialDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps (vendor id, product id) pairs to the corresponding serial driver.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class ProbeTable {

    private final Map<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>> mProbeTable =
            new LinkedHashMap<Pair<Integer, Integer>, Class<? extends UsbSerialDriver>>();

    /**
     * Adds or updates a (vendor, product) pair in the table.
     *
     * @param vendorId the USB vendor id
     * @param productId the USB product id
     * @param driverClass the driver class responsible for this pair
     * @return {@code this}, for chaining
     */
    public io.github.sds100.keymapper.usbserial.driver.ProbeTable addProduct(int vendorId, int productId,
                                                                   Class<? extends UsbSerialDriver> driverClass) {
        mProbeTable.put(Pair.create(vendorId, productId), driverClass);
        return this;
    }

    /**
     * Internal method to add all supported products from
     * {@code getSupportedProducts} static method.
     *
     * @param driverClass
     * @return
     */
    @SuppressWarnings("unchecked")
    io.github.sds100.keymapper.usbserial.driver.ProbeTable addDriver(Class<? extends UsbSerialDriver> driverClass) {
        final Method method;

        try {
            method = driverClass.getMethod("getSupportedDevices");
        } catch (SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        final Map<Integer, int[]> devices;
        try {
            devices = (Map<Integer, int[]>) method.invoke(null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Integer, int[]> entry : devices.entrySet()) {
            final int vendorId = entry.getKey();
            for (int productId : entry.getValue()) {
                addProduct(vendorId, productId, driverClass);
            }
        }

        return this;
    }

    /**
     * Returns the driver for the given (vendor, product) pair, or {@code null}
     * if no match.
     *
     * @param vendorId the USB vendor id
     * @param productId the USB product id
     * @return the driver class matching this pair, or {@code null}
     */
    public Class<? extends UsbSerialDriver> findDriver(int vendorId, int productId) {
        final Pair<Integer, Integer> pair = Pair.create(vendorId, productId);
        return mProbeTable.get(pair);
    }

}
