<!--
SPDX-FileCopyrightText: 2023 Deutsche Telekom AG

SPDX-License-Identifier: CC0-1.0    
-->

![Citykey App's Overview](./images/cover.png)

# Android App of CityKey [![Apache-2.0](https://img.shields.io/badge/license-Apache%202.0-blue?style=flat-square)](https://opensource.org/license/apache-2-0) [![OpenSSF Scorecard Score](https://api.scorecard.dev/projects/github.com/telekom/citykey-android/badge)](https://scorecard.dev/viewer/?uri=github.com/telekom/citykey-android/badge)

## Overview

Citykey is an urban living companion developed by [Deutsche Telekom AG](https://www.telekom.com/de),
designed to enhance the experience of residents and visitors in German cities. The app provides easy
access to local services, events, and transportation information, making it a must-have tool for
navigating urban environments. It is the digital way to access the citizen services.

Please [visit the website](https://citykey.app) for more information!

## Features

- **Garbage collection calendar:🚛**:
    - Distinguish between different types of garbage, such as residual waste, waste paper and
      organic waste
    - Always up-to-date and street-accurate
    - The smart garbage collection calendar helps you plan weeks in advance
- **Find events / activities:🎭**:
  Always stay in the loop about interesting events in your city!
    - Find festivals, events and cultural activities in your area
    - Use built-in filters for your interests
    - Citykey shows you an overview of upcoming events
    - Share exciting events with your friends over Citykey
    - Add activities and events to your calendar
- **News:🛰**:
    - Keep track of the latest city-related news
    - Get daily updates about important topics, such as culture, community, citizens, nature and
      helpful news
- **Book appointments with offices:👨‍💼**:
    - Book appointments with your local office and minimize waiting times when dealing with
      authorities
    - Ensure you have all documents required for your appointment and get the location to easily
      find the office
- **Digital administration with your eID:📱**:
    - Fill out forms for various administrative purposes
    - Citykey supports the Online ID (eID) of the ID card for identification on the Internet, so you
      can use even more citizen services digitally and mobile-friendly. The electronic residence
      permit is also supported
    - Take care of common applications in no time at all, such as applying for a resident parking
      permit or changing your residency
- **Citizen participation:📝**:
  Shaping the city together is now made even easier.
    - Take part in surveys on urban development and all projects that affect you
    - View all ongoing surveys in an overview
- **Interesting places:🌃**:
  New to your city? Get the best tips and first-hand information.
    - Find out what characterizes the city and which places are worth a visit
    - The app helps you find your way around and get familiar with the city faster
- **Defect reporter:🤳🚧**:
  A deep pothole in the road, a crooked guard rail or a defective street light caught your eye?
    - Report damaged or defective infrastructure to the city
    - Detail your request, simply by sending a photo with a location marker

## Want to try out?

Play Store deployed: https://play.google.com/store/apps/details?id=com.telekom.citykey

## Building From Source

If you want to start working on Citykey and if you haven't done already, you
should [familiarize yourself with Android development](https://developer.android.com/training/basics/firstapp/index.html)
and [set up a development environment](https://developer.android.com/sdk/index.html).

The next step is to clone the source code repository.

    $ git clone https://github.com/telekom/CityKey-Android.git

Then, in the file `secrets.properties` present in the root folder, put your own Google Maps API key.
This file is not checked into Git as it contains the sensitive credentials.

Use `debug` build variant to run the application for debugging purpose.

If you don't want to use an IDE like Android Studio, you can build Citykey on the command line as
follows.

    $ ./gradlew assembleRelease

## Contribute

Code contributions are welcome!

You should fork the repo as described
here: https://help.github.com/en/github/getting-started-with-github/fork-a-repo

See the Issues list for bug
reports: https://github.com/telekom/CityKey-Android/issues

Before adding new features, please create an issue, or contact
us: https://public.telekom.de/digitalisierungsloesungen/smart-city#Kontaktaufnahme

See the [CONTRIBUTING](CONTRIBUTING.md) file for more details.

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.1 as our code of conduct.
By participating in this project, you agree to abide by its [Code of Conduct](CODE_OF_CONDUCT.md) at all times.

## Licensing

This project follows the [REUSE standard for software licensing](https://reuse.software/).    
Each file contains copyright and license information, and license texts can be found in the [./LICENSES](./LICENSES) folder. For more information visit https://reuse.software/.    
You can find a guide for developers at https://telekom.github.io/reuse-template/.
