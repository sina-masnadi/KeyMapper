### Android Key Mapper with Serial Support 

This is a fork of [Key Mapper](https://github.com/sina-masnadi/KeyMapper) with the ability of sending commands to serial devices connected over USB. It uses [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) as the serial communication library.

I made this to control my old Pioneer home theater receiver using my (Sony) Android TV remote control. For example, the volume keys on the TV remote control can be assigned to send VU or VD commands to the receiver to adjust the volume.
The receiver does not support HDMI CEC and only has an RS-232 port for remote control purpose and I used a USB to RS-232 adapter which uses Ch34x Serial Driver to connect the TV to the receiver. 

You can find a list of RS-232 commands for Pioneer Receivers [here](https://www.pioneerelectronics.com/StaticFiles/Custom%20Install/RS-232%20Codes/Av%20Receivers/Elite%20&%20Pioneer%20FY13AVR%20IP%20&%20RS-232%205-8-12.xls).

![](screenshots/serial_small.png?raw=true)

Things to notice:
1. It only supports one USB serial device at this time.
2. It uses <CR> for the end of command
3. There is no UI right now to show/use the received data on serial port
4. The connection opens before sending each command and will be closed after the command has been sent
