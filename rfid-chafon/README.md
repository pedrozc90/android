# Chafon Module

## Usage

### Required Permissions

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Issues

1. Command `startInventory` always return `FALSE`
2. Reader `trigger button` start reading, even with `startInventory` returning `FALSE`
3. Reader notification callback only works when we get the line below in logs
    >   BluetoothGatt |  setCharacteristicNotification() - uuid: 0000ffe4-0000-1000-8000-00805f9b34fb enable: true
4.  Command `setPower` always return `FALSE`

## Devices

### [H103 Bluetooth UHF RFID Sled Reader](https://www.chafontech.com/productinfo/1186158.html)

#### Lights

| Name                | Function Description                                                                                                                                                                                                                                                                                                                                                                                          |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Power Button        | Used for device power on/off operation, short press to power on, long press (>1S) to power off.                                                                                                                                                                                                                                                                                                               |
| Read button         | Used to trigger the device to perform a scan or query action, or to stop the device's current scan or query action.                                                                                                                                                                                                                                                                                           |
| Power indicator     | Red, used to indicate the power on/off status, low battery (battery level <10%) and charging status. The indicator light is always on when the device is turned on, and it goes out when the device is turned off. The indicator light flashes slowly when the device is low battery, flashes quickly when the device is charging, and the indicator light is always on when the device is finished charging. |
| Read indicator      | Green, used to indicate the status of scanning or querying. When the device performs scanning or querying, the indicator light is always on, and the device scanning or querying indicator light flashes.                                                                                                                                                                                                     |
| Bluetooth indicator | Blue, used to indicate whether Bluetooth is connected. The indicator light flashes when Bluetooth is not connected, and the indicator light is always on when Bluetooth is connected.                                                                                                                                                                                                                         |
| Buzzer              | Used to indicate the device power on/off status and standby wake-up status. The device power on buzzer will emit a 1S power on reminder tone, and the device power off buzzer will emit a 1S shutdown reminder tone. The device standby buzzer will emit a 200mS standby reminder tone, and the device wake-up buzzer will emit a 1S power on reminder tone.                                                  |
