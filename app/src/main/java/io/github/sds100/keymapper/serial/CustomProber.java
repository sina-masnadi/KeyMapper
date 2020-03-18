package io.github.sds100.keymapper.serial;

import io.github.sds100.keymapper.usbserial.driver.CdcAcmSerialDriver;
import io.github.sds100.keymapper.usbserial.driver.ProbeTable;
import io.github.sds100.keymapper.usbserial.driver.UsbSerialProber;

/**
 * add devices here, that are not known to DefaultProber
 *
 * if the App should auto start for these devices, also
 * add IDs to app/src/main/res/xml/usb_device_filter.xml
 */
class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver.class); // e.g. Digispark CDC
        return new UsbSerialProber(customTable);
    }

}
