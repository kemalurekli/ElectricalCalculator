# VoltageBoard — Privacy Policy

_Last updated: 11 September 2026_

VoltageBoard is an offline toolkit for electrical work. The calculators, the
reference texts, your projects and your calculation history stay on your
device. They are never uploaded, and they are not covered by anything below.

One part of the app is different: the **forum**. Using it means creating an
account and posting to a server. This policy is about that part.

## If you never sign in

Nothing about you is collected. You can read the forum without an account, and
reading does not create one. Every other feature works with no account at all
and no network connection.

The one exception is the report button, and only when you press it — see
*Reporting a mistake* below.

## Reporting a mistake

Calculators, theory topics and reference tables carry a "report a mistake"
button. Nothing is sent unless you write something and press send. What is sent
is:

- the message you typed,
- which screen you were on, as an internal key such as
  `calculator:voltage_drop`,
- the app version, the platform (Android or iOS) and the language the app was
  being read in,
- a random identifier created once for this installation. It is not a device
  identifier and is not tied to your name or account; it exists so that the
  server can refuse a flood of reports from one source. Reinstalling the app
  replaces it,
- your account identifier, **only if you are signed in to the forum**. It lets
  the operator see which account sent the report, and therefore the display
  name and the email address on it. Reports sent without signing in are not
  tied to anyone.

**Nothing you typed into a calculator is sent.** The values, results and
projects on your device stay on your device.

Reports are visible only to the operator of the service. They are kept until the
problem they describe has been dealt with.

## What is collected when you sign in

There are three ways in: **Google** on Android, **Apple** on iOS, and a
**six-digit code sent to your email address** on either. Whichever you use, the
server stores the same three things and nothing else:

| What | Why |
|---|---|
| A user identifier | Ties your posts to you, so you can edit and delete them |
| Your email address | Identifies the account. **It is never shown to other users** and is never sent to any app screen |
| A display name | Shown next to what you post. Taken from your Google or Apple name at first, and you can change it |

The app never sees a password. Google and Apple return a token, which the server
checks; the code sign-in sends a one-time code to an address you type and no
password exists at all.

If you use **Sign in with Apple** and choose *Hide My Email*, what is stored is
Apple's relay address. We cannot see your real one, and we do not ask for it.

Your profile picture is not used. The forum shows no avatars.

## What is collected when you post

- The text of your threads and messages, and when they were written
- Which messages you have thanked
- Which people you have blocked
- Reports you file: what you reported, the reason, and that it was you

Your post count and the number of thanks you have received are shown publicly on
your profile.

## Who can see it

- **Your display name, your posts, your post count and your thanks** are public
  to anyone using the forum.
- **Your email address** is visible to nobody except the operator of the
  service.
- **Reports** are visible only to the operator. The person you reported is not
  told who reported them.
- **Blocks** are visible only to you. The person you blocked is not told.

## Deleted content

Deleting a message removes it from the forum. A copy — the text, who wrote it,
who deleted it and when — is kept in a moderation record that no app user can
read. This exists so that content someone reports does not disappear before it
has been looked at.

## Deleting your account

**Settings → Forum account → Delete forum account.**

This removes your profile, your threads, your messages, your thanks, your
blocks, and your sign-in record. It cannot be undone.

Reports you filed are kept with your identity removed from them. A moderation
queue that empties itself whenever a reporter leaves would not be a moderation
queue.

Deleted content already in the moderation record stays there.

## Where the data is

The forum and the error reports run on [Supabase](https://supabase.com). Data is
stored on their infrastructure and subject to their security practices.

Nothing else leaves your device. Your projects, your calculation history, your
field notes, your favourites and every figure you have typed into a calculator
are stored on the device only, and there is no account that syncs them.

## Purchases

VoltageBoard Pro is a one-off purchase. The payment itself is handled entirely
by Google Play or the App Store; the app never sees a card number, a billing
address or a name.

To know whether this copy has been paid for, the app uses
[RevenueCat](https://www.revenuecat.com), a service that records purchases and
answers that one question. It receives:

- the purchase and its receipt, as the store reports it,
- an identifier RevenueCat generates for this installation,
- the store, the country the store reports, and the app version.

It does not receive your name, your email address, or anything else in this
app. RevenueCat is based in the United States, so this information is processed
there.

Restoring a purchase on a new device asks the store, not us, which is why it
works without an account.

## Children

The forum is not intended for children. Do not sign in if you are under the age
at which you can consent to data processing where you live.

## Analytics and advertising

There are none. The app contains no analytics SDK, no advertising and no
tracking: nothing measures what you open, how long you stay, or what you
calculate.

Three third-party services are used, each for one job and nothing else: Google
and Apple for signing in to the forum, Supabase for the forum and error reports
(*Where the data is*), and RevenueCat for purchases (*Purchases*). There are no
others.

## Changes

Material changes to this policy will be reflected in the "last updated" date
above.

## Contact

For questions about this policy, or about data held about you:
**devforandr@gmail.com**

Requests to see or delete your data are answered from this address. You can also
delete everything yourself, at any time, without asking: **Settings → Forum
account → Delete forum account**.
