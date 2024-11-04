# DMS Explorer
[![license](https://img.shields.io/github/license/ohmae/dms-explorer.svg)](./LICENSE)
[![GitHub release](https://img.shields.io/github/release/ohmae/dms-explorer.svg)](https://github.com/ohmae/dms-explorer/releases)
[![GitHub issues](https://img.shields.io/github/issues/ohmae/dms-explorer.svg)](https://github.com/ohmae/dms-explorer/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/ohmae/dms-explorer.svg)](https://github.com/ohmae/dms-explorer/issues?q=is%3Aissue+is%3Aclosed)
[![Build Status](https://travis-ci.org/ohmae/dms-explorer.svg?branch=develop)](https://travis-ci.org/ohmae/dms-explorer)
[![codecov](https://codecov.io/gh/ohmae/dms-explorer/branch/develop/graph/badge.svg)](https://codecov.io/gh/ohmae/dms-explorer)

This is a DLNA player featuring displaying server and content metadata.
DMC function is also implemented and can be make play back to DMR.
Source code is published under the Open source license (MIT license).

This app's feature is to information display about DLNA server (DMS) and its contents.
Since the playback function uses the codec of the device, which file can be played depends on the device.
For example, in the case of movies,
if it is a format compliant with the Android standard such as H.264 / VP8 / VP9, it seems playable on almost all devices.
Some devices may be able to play MPEG 1 / MPEG 2 / WMV / DivX, etc.
If you can not play it, you can launch the external application in settings so please try it.

As one of the playback methods, the DMC function is implemented.
If you have a TV with DMR function on the same network, you can make DMS contents play back to DMR.
If the DMR supports it, DTCP-IP content playback is also possible.
Also, if you have a SONY recorder such as nasne, or Panasonic recorder, you can use chapter jump function.

Although it carries the minimum playback function of movies, still images and music,
it features a metadata display function of server and contents rather than usability as a player.
Since information on ARIB extension tag (arib:longDescription, etc) is also displayed,
detailed program information can be seen if recorder etc is compatible.
Also, if the program information contains a URL, it will automatically act as a link.

## Screenshots

|![](docs/img/1.png)|![](docs/img/2.png)|![](docs/img/3.png)|
|-|-|-|
|![](docs/img/4.png)|![](docs/img/5.png)|![](docs/img/6.png)|

|![](docs/img/7.png)|![](docs/img/8.png)|
|-|-|

## Install

Google has pointed out a violation of the Incomplete Features Policy (“Apps that install, but don’t load”) and has
rejected this app.
This is likely due to this app not working in environments without a DLNA server.
Due to the nature of the app, it is difficult to resolve this issue, so unfortunately, I have decided to make DMS
Explorer unpublished on Google Play.
You can continue to use this app if you have already installed it, but you will no longer be able to install this app.

~~[https://play.google.com/store/apps/details?id=net.mm2d.dmsexplorer](https://play.google.com/store/apps/details?id=net.mm2d.dmsexplorer)~~

### Repository by contributors
[IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/net.mm2d.dmsexplorer)

## Build
```
gradlew assembleRelease
```
And this project can open as Android Studio project.

## Remarks
This is an application example of [mmupnp](https://github.com/ohmae/mmupnp).

## Author
大前 良介 (OHMAE Ryosuke)
http://www.mm2d.net/

## License
[MIT License](./LICENSE)
