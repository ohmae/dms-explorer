# DMS Explorer
This is a kind of DLNA player for Android.
That can display the information of the DLNA server (DMS),
and can playback the contents.

This app has an ability of playback contents. But it isn't powerful.
In the settings, you can choose to play with this app or to throw
Intent to play in another app.

App playback ability simply uses the codec of the device, so what kind of file
that can be played depends on the device.
For example, in the case of video, almost device can play the
Android-standard formats such as H.264 / VP8 / VP9,
and some device seem to be able to play MPEG1 / MPEG2 / WMV / DivX etc..

This can playback, movie, photo and music. But it is minimum function.
Feature of this app is to display the metadata.
You can display of metadata of the recorded content, such as in the recorder.
This can recognize the ARIB extension tags, and can display it.
If the program information is included URL string, it act as a link.

## Install
[Google Play](https://play.google.com/store/apps/details?id=net.mm2d.dmsexplorer)

## Build
This is Android Studio project.
So, you can select this repository from "Open an existing Android Studio project".

## Remarks
This is an application example of [mmupnp](https://github.com/ohmae/mmupnp).

## Author
大前 良介 (OHMAE Ryosuke)
http://www.mm2d.net/

## License
[MIT License](./LICENSE)
