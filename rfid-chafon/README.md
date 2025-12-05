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
