# Lamespy

<p align="center">
  <img src="https://raw.githubusercontent.com/simao/lamespy/master/lamespy_icon_180.png" alt="Lamespy icon"/>
</p>

Lamespy keeps a history of where you have been without depending on an external api.

# Purpose

Lamespy keeps a history of your location without using any external
location API besides your android Wifi antenna.

This means that you do not depend on some big company to keep a
history of the places you have been.

You don't need to do anything, lamespy works in the background and
checks your Wifi networks, you don't need to "check-in" or manually
update your current location. This makes it really flawless and easy
to use, you don't need to remember to do anything.

Lamespy app allows you to track how much time you spend in each place you
go in your life. For example, how many hours did you spend working
last week? How many times did you go to the supermarket?

Not using any location API means that you need to setup the app with the
locations you want to track. If you don't add these locations,
`lamespy` will not track your location on that place.

## Features

- Privacy - Since you don't depend on a external API, your history is
  kept on your android.

- Easy Export and analyze - You can easily export your location
  history in JSON format and analyze it with other tools that you use
  every day.

- Automatically saves your location, no need to "check-in" anywhere to
  get badges.

- Easily check where you have been using your android

Nothing else.

# It's probably not for you

This app is probably not for you. It is a very bare bones app to track
your history so you can analyze it later, it does not do anything
else.

# What this app does

Tracks your location history. That's it.

# What this app does not

Everything not listed above.

In particular, it only deals with data collection. Your data will be
available as a JSON file and you can analyze it any way you want, but
this app won't do that for you.

# Permissions needed

- `RECEIVE_BOOT_COMPLETED` It needs this permission so it can listen to Wifi
  events after the phone is booted.

- `CHANGE_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE`
  Required to get information about Wifi networks around you. Lamespy
  will also periodically trigger a Wifi scan so it can update your
  location.

# How to build

Lamespy is built using gradle, so you can run:

` ./gradlew assemble`

After this command runs successfully a new apk will be available in
`./lamespy/build/apk/lamespy-release-unsigned.apk`.

# How to install

After building the apk with gradle you can upload it to your android.

# Missing features

- Analyzing data. You will need to write a tool that can read JSON so
  you can analyze your data.

# Install

Currently Lamespy is not on android market, so you need to build it
and install it yourself.

# Contribute

There is no minimum pull request size, all PR will be considered
regardless of size. If you want to help but don't know where to start,
here are some ideas:

- Write tests and setup a framework

- Implement manually deleting events from the current history

- Stop tracking for X of hours

- UI can easily be improved
