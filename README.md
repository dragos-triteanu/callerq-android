# CallerQ

#### The site for the app: http://www.callerq.com/

#### Repo for the old app:  

## General use case:

The CallerQ app is used in a business context, where sales reprezentatives have to undergo many phone calls with various clients, and thus need help in scheduling reminders for recontacting popetial clients.

After downloading and installing the CallerQ app (old version in the store) the user's phone calls will be intercepted by the app, which will fire an Android button notification after each call in order to let the user chose how he wishes to handle the scheduling of an event.

## The options are the following:

Swiping the notification away, would cause it to dissapear without having to handle a specific event. Clicking the 'Snooze' button, will delay the notification for 30 minutes. Clicking on the 'Schedule' button will open an activity, where the user can set up when exactly he wants to be notified by the reminder. If the notification has already been rescheduled, then clicking on it, would fire a call to the person defined in the scheduling activity. The app store version of the app can be checked out here: https://play.google.com/store/apps/details?id=com.callerq

The app is mostly functional, but requires migration to gradle (already done), restructuring and minor usability improvements.

The use of notifications as opposed to how the app already works is meant to reduce the amount of 'invasivity' that the app has towards the user (the app store version opens up an activity after each call).

A .NET backend service is responsible for storing the calendar and also receives usage statistics from the app (via Google Analythics)

## What was attempted:

Instead of using the backend service, an attept was made to have the events be tracked using the android local calendar. When a user would schedule an event, a reminder would be creating using the android calendar. When the calendar's alarm would trigger a notification, then the CallerQ app would close it, and fire it's own notification. This however has the limitation, that an app cannot close another app's notification before it got a change to be posted. This approach would have solved a lot of synch-ing issues between the server and the app.
