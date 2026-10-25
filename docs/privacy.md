---
layout: page
title: Privacy Policy
---

Just Chatting is an open-source Twitch chat client for Android, iOS and desktop. It has no backend
server of its own, no analytics, no advertising and no crash reporting. Everything the app knows
about you stays on your device, except what it needs to send to Twitch and to the optional
third-party services listed below.

## What data does the app collect?

### Twitch account and token

To log you into Twitch, Just Chatting uses Twitch's OAuth system. The app never sees your Twitch
password. Before you approve the login, Twitch shows you the permissions (scopes) the app asks for:

- `chat:read` and `user:read:chat`, to read chat messages;
- `chat:edit` and `user:write:chat`, to send chat messages on your behalf;
- `user:read:follows`, to list the channels you follow.

Once you log in, Twitch returns an access token. The app stores this token and your Twitch user
information (username, display name, user ID) in the app's private storage on your device.

During login, Twitch redirects your browser to
[just-chatting.app/auth/callback.html](https://just-chatting.app/auth/callback.html), a static page
whose only job is to hand the token back to the app. The token is in the URL fragment (the part
after `#`), which browsers never send to the web server. On desktop, the page passes it to the app
through a local server running on your own computer (`localhost`).

### Chat messages

To provide its core features, Just Chatting sends the messages you write to Twitch and receives chat
messages from Twitch, over encrypted connections. Chat messages are kept in memory only while a chat
is open. They are not saved to disk and not sent to anyone other than Twitch.

### Data cached on your device

To make the app faster and work across restarts, Just Chatting keeps the following in its private
storage on your device:

- your app settings;
- the channels you follow, along with their profile information, live status and stream schedule;
- channels you recently opened;
- emotes you recently used;
- pronouns fetched for other users, if that feature is turned on.

This data is never uploaded anywhere by the app.

### Notifications

On Android, you can turn on notifications for open chats (they are off by default). These
notifications are created locally by the app on your device. No push notification service is
involved.

### Demo mode

In demo mode, the app uses built-in sample data. It does not connect to Twitch or any other service,
and it stores nothing about you.

## Communication with third parties

Just Chatting connects directly from your device to the services below. There are no intermediate
servers. Like any internet connection, these services can see your device's IP address.

### Twitch

Just Chatting regularly communicates with Twitch (its API, chat and event servers, and content
delivery network for images) to provide its core features. Communication with Twitch is required to
use the app. Twitch's privacy policy is available at
[twitch.tv/p/legal/privacy-notice](https://www.twitch.tv/p/legal/privacy-notice/).

### recent-messages.robotty.de

Just Chatting uses recent-messages.robotty.de to load messages that were sent in a chat before you
opened it. The app sends it the name of the channel you open. This service is optional and can be
turned off in the app settings. Its privacy policy is available at
[recent-messages.robotty.de/privacy](https://recent-messages.robotty.de/privacy).

### pronouns.alejo.io

Just Chatting uses pronouns.alejo.io to show the pronouns that other users have chosen to share. To
do this, the app sends it the usernames of people who write in the chats you open. This service is
optional and can be turned off in the app settings.

### BetterTTV, FrankerFaceZ and 7TV

Just Chatting uses BetterTTV, FrankerFaceZ and 7TV to show extra emotes in chat. The app sends them
the ID of the channel you open, and downloads emote images from their servers. Each of these
services is optional and can be turned off separately in the app settings.

## How can I delete my data?

Logging out from the app settings removes your Twitch token from the device. Uninstalling the app
deletes everything it stored. You can also revoke the app's access to your account at any time from
[Twitch's Connections settings](https://www.twitch.tv/settings/connections).

## How will I be informed of privacy policy changes?

If the app's handling of data changes, this page will be updated and the version date below will
change. Changes are also visible in the app's
[source code history](https://github.com/outadoc/just-chatting/commits/main/docs/privacy.md).

## Contact

If you have any questions about this policy, please
[open an issue on GitHub](https://github.com/outadoc/just-chatting/issues).

---

Version 2026-10-25
