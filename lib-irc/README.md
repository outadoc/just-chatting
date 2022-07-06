# Kale
Kotlin/JVM IRC message parsing, serialising and notifying. Provides useful abstractions with the intention of splitting message parsing and IRC state management. Useful for building bots, clients and servers.

[Warren](https://github.com/WillowChat/Warren) is the state tracking counterpart.

[Burrow](https://github.com/WillowChat/Burrow) is a new IRC v3.2 server daemon, which uses the parsing and serialising bits of Kale.

[Thump](https://github.com/WillowChat/Thump) is a bridge that lets people chat between Minecraft and IRC whilst they play.

[![codecov](https://codecov.io/gh/WillowChat/Kale/branch/develop/graph/badge.svg)](https://codecov.io/gh/WillowChat/Kale)

There are basic examples of usage in [KaleRunner.kt](src/main/kotlin/chat/willow/kale/KaleRunner.kt)

## Goals

* Own the parsing & serialising bit of IRC clients and servers
* Let users feed raw lines in, and be notified with strongly typed output messages, covering RFC1459 and IRCv3
* Verify the above with an extensive suite of unit tests

If this sounds good to you, you can support development through [Patreon](https://crrt.io/patreon) 🎉!

## Code License
The source code of this project is licensed under the terms of the ISC license, listed in the [LICENSE](LICENSE.md) file. A concise summary of the ISC license is available at [choosealicense.org](http://choosealicense.com/licenses/isc/).

## Building
This project uses Gradle for pretty easy setup and building.

The general idea:
* **Setup**: `./gradlew clean`
* **Building**: `./gradlew build`
* **Testing**: `./gradlew test`

If you run in to odd Gradle issues, doing `./gradlew clean` usually fixes it.
