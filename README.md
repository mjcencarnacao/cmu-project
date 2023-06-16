---
class: Mobile and Ubiquitous Computing
term: 2022/23
skill: Mobile Application Development for Android
title: "LibrarIST"
author:
  - Prof. Luis D. Pedrosa
  - Prof. João C. Garcia
assignment: Project
issued: 2023-05-01
checkpoint: 2023-05-26
due: 2023-06-16
version: 1.0
---


# Overview

In this project you will learn how to develop a non-trivial mobile application for the Android Operating System.
This application will explore several key aspects of mobile development, including location awareness, judicious use of limited resources, and social behavior.
Towards this end, you will be developing a simple but powerful library management app: LibrarIST.

This app gives mobile support to free libraries much like the ones promoted by the [Little Free Library initiative](https://littlefreelibrary.org/).
LibrarIST will help users find free libraries in their surroundings and discover the books they contain.
The system will also help keep track of book inventories, by managing donations, checkouts, and returns.

The project will be built as the sum of a set of [mandatory](#mandatory-features) and [additional](#additional-components) components and features that will be evaluated during both a [checkpoint and the final submission](#grading-process-and-milestones).
The idea is that everyone implements the mandatory features (worth up to 75% of the grade), and then selects a combination of additional features that add up to complete the grade at 100%.
Grades from the optional features are allowed to accumulate beyond 25% (and compensate for any limitations in the mandatory part of the submission), though the final grade is capped at 100%.
At the checkpoint we will evaluate the [mandatory features](#mandatory-features), whereas the final submission will contemplate all functionality.


# Mandatory Features
The mandatory features include the core library management functionalities.
Though the exact UI design is up to you, the app should support the features outlined in this section.
At a top level, the app should have two screens: a map of available free libraries and a book search screen.
The map should support the following features:

* The map can be dragged around to show more libraries.
* There should be a search bar to lookup and center the map on a given address.
* The user should be able to center the map on their current location at the press of a button.
* Free libraries should show up on the map with markers.
* The user's favorite libraries should be highlighted with a different marker.
* Tapping a marker goes to the respective library's information panel, which should include the following:
  * The library's name, location (shown on a map), and photo.
  * A button to help the user navigate to the library.
  * Button to add/remove the library from the user's favorites.
  * A button to check in / donate a book (scan barcode).
    * If the code is unknown, create a new book with a title and cover photo (taken from the camera).
  * A button to check out a book (scan barcode).
  * The list of books currently available at the library.
    * Tapping a book opens a panel with more information about the book (see below).
* Allow the user to add a new library, with the following information:
  * A name for the library.
  * A location (either picked from a map, searched by address, or using current location)
  * A photo (taken from the phone camera)

The book search screen allows users to see the full list of books ever donated to any library managed from within the App and to filter it down with a text search.
Tapping a book takes the user to a more detailed information panel with more information about the book, including:

* The book's title and cover picture.
* A button to enable/disable notifications of when the book becomes available in one of the user's favorite libraries.
* A list of libraries where the book is available indicating how far away they are and sorted by this distance.
  * Tapping a library brings up its information panel (see above).


## Back-end
LibrarIST supports a number of features that build on explicit data sharing and crowd-sourcing among multiple devices.
To enable such functionality you will need some sort of back-end service that holds and processes shared data (e.g. library and book information) and that each device communicates with to synchronize its state.

How you implement this back-end service is not the focus of this class but is nonetheless necessary for the project to work.
Feel free to implement your own server as you see fit (e.g. using Java RMI, gRPC, or a RESTful service), or otherwise use an existing server software or database.
The server can be kept simple for debug and development purposes and can hold data in memory alone (no need for persistence).
In any case, be prepared to justify your choices.

  
## Resource Frugality
When developing data-intensive mobile applications, developers often have to contend with trade-offs between timeliness, data size, and efficiency.
It's nice if the user gets notified the moment a book they wanted becomes available, but constantly polling the server can drain the battery and use up the user's metered data.
In LibrarIST, library and book information should sync across devices in a timely manner while also using the network efficiently.
When the user is actively viewing a library or book, ensure that any new content shows up quickly.
If the user disengages from the application, use more efficient messaging to save network resources, even if at the expense of increased latency.

It's also important to avoid using resources unnecessarily.
Avoid wasting resources by only downloading data related to UI elements as they become visible to the user. For example, when a user searches for books using a search filter, search results can be downloaded only as scrolling requires. Furthermore, search filters should be applied on the server side to reduce data transmission.

Particularly large content can be further optimized to avoid costly metered data.
Photos, e.g. book covers, can represent a hefty data cost so, to optimize network usage, show a placeholder for them when the user is on a metered connection, retrieving the image only when the user taps it.
If, on the other hand, the user is on WiFi, automatically retrieve photos when visible.


## Context Awareness

The LibrarIST application should be aware of its location and should automatically open free library information panels when close to the library (e.g. within 100m).


## Caching
Often users have only spotty data-connections with metered data.
As such, communication between the LibrarIST application and its back-end server should be optimized to use the network judiciously and to compensate for short term outages.
On the one hand you should avoid downloading the same content multiple times when it could reasonably be avoided, on the other you want to minimize disruption during a momentary outage.

To address this challenge, use a cache to store content as you retrieve it from the server.
With this in place, repeated downloads of images are minimized and any content recently viewed will be available offline if needed.

Further optimize your cache through careful pre-loading when the user connects to WiFi.
As WiFi data is virtually free, use the opportunity to load the most relevant content (e.g. the library data for libraries within a 10km radius and their respective books).
This way, later when the user no longer has WiFi, they can still browse a large set of nearby libraries with minimal data usage. 


# Additional Components
In this section we list a series of features that can be combined to add value to LibrarIST.
Each feature lists the grading percentage it is worth, reflecting the relative difficulty in implementing it.

The project core (described [above](#mandatory-features)) is worth 75%, which leaves at least 25% for these additional components.
Select a combination of these features that adds up to or exceeds that threshold.

Many of these features can be naturally integrated with each other and gain from being implemented in such a manner.
Implementing your selection of additional features in such a cohesive manner is encouraged.


## Securing Communication [5%]
Sending data across a network in the clear poses a significant risk to users.
A malicious party can eavesdrop the data being transmitted and use it to infer all sorts of private information, including e.g. a user's location.
Unprotected transmissions can also be modified in transit, feeding users fake data that can influence their decisions (e.g. hiding or faking the availability of books) or even put them in danger (give them directions to the middle of a construction site).

To avoid such scenarios, upgrade the LibrarIST infrastructure to secure all communications between the mobile application and the back-end server to use SSL.
Also, do be careful with how you manage your certificates to be sure the client only communicates with your authorized LibrarIST server, preventing an untrusted party from conducting a man-in-the-middle attack.


## Meta Moderation [10%]
Another way to attack the integrity of LibrarIST data is for the attacker to simulate one or more valid users and to then simply feed bad data into the system.
There is no easy way to completely solve this problem but a meta-moderation system raises the bar.

Create a mechanism whereby users can flag fake libraries or books.
As users flag entities, keep track of how many times each entity has been flagged by distinct users.
When a library or book is flagged, immediately hide it from the flagger's view, providing immediate feedback that their concern has been registered.
As more users flag the library or book, hide it from everyone once a given threshold (e.g. 2 for debug purposes) has been surpassed.


## User Ratings [10%] 
 
A good way to guide readers to interesting books is to allow for user-submitted book ratings.
Allow users to rate books with 1-5 stars.
If a user tries to submit a new rating for the same book, it should overwrite the previous one.
If ratings are supported, the book information panel should include a histogram of all ratings and all lists of books (including in search and library panels) should be sorted by their average rating.
 

## User Accounts [10%]
The project [core](#mandatory-features) does not include a login/logout procedure so users can be tracked via simple GUIDs.
This simplifies the user experience, as users need not setup an account before using LibrarIST but it also makes it more difficult to keep multiple devices consistent.

Allow users to create accounts so that they can login from multiple devices and seamlessly keep their list of favorite libraries in sync.
Include Login/Logout buttons in the context menu.
Multiple devices logged in for the same user should automatically sync account data, providing a seamless cross-device experience.
User accounts should be optional, so users can start using the app without an explicit account and later upgrade to using an account if they want.


## Social Sharing To Other Apps [5%]
Another important aspect in mobile development is social sharing.
It's often convenient to share information with friends in context and directly from within an application, without needing to open a separate communication app to do so.

Add the option to share content from LibrarIST with other apps, e.g. common social media (e.g. Facebook, Twitter) and communication applications (E-Mail).
Allow sharing of books (with name and cover photo), and libraries (with name, photo, and location).


## Localization [5%]
Users are diverse and multi-lingual and LibrarIST should reflect that.
Localization---also called L10n---is a collection of tools and techniques that allow different users that speak different languages to use the application and share data without barriers.

Translate all static strings presented to the user into at least two languages (e.g. English and your own native language) and store these strings in such a way that adding new translations is easy and does not require refactoring the application.
Do not translate user provided strings, such as library and book names.


## UI Adaptability: Rotation [5%]
Users don't always use their phones with the same orientation and can rotate to view content either vertically or horizontally, as they see fit.
Allow LibrarIST to adapt to these circumstances and make sure all screens show correctly given the current orientation.
Make sure the user can experience the application equally well with the phone oriented vertically or horizontally.


## UI Adaptability: Light/Dark Theme [5%]
Though many applications default to a light theme with dark text on a light background, many users prefer a dark theme which is less harsh on the eyes, especially at night, and mitigates burn-in on OLED screens.
The Android Operating System has native support for dark themes and lets the user pick between a light and dark mode within the phone settings.
Applications can then adapt accordingly, using an appropriate theme to ensure a consistent look-and-feel with the rest of the phone UI.

Implement both a Light and a Dark theme in LibrarIST.
Make sure the user can experience the application equally well in either mode.


## Recommendations [10%]


Finding relevant books that match users' interests is often a challenge but users typically share common interest profiles which can be crowd-sourced and used to suggest new books that might interest them.
The family of algorithms that power these suggestions is known as *recommender systems* and they power all kinds of recommendations, from e-commerce ("people who bought X also bought Y."), to dating apps, and much more.

Build a simple recommender system for books and add a new panel to LibrarIST that suggests new books to the user that may interest them.
This isn't an ML class so we can use a simple crowd-sourcing algorithm on the back-end: for every pair of books, track how many users have checked out both of them.
Based on books checked out by the user, recommend others from within their favorite libraries.
Sort the books by this metric and present to the user the top few (enough to fit the screen), which are most likely to be the ones they will be interested in.


# Alternative Projects
Groups have the option to develop a different project if they so wish and with faculty approval.
Consider this option if you already had a project in mind, whether for some collaboration with industry, to explore an idea for a start-up, or as a passion project.
We might need to make adjustments to manage complexity, given the time-frame and effort allocated to the course.
There are also some features that we consider essential to mobile development and we may need to add or adjust functionality in your idea for the sake of the class project.
You can also propose additional features to this project, if you so wish.
Start a discussion with the faculty and seek approval as early as possible if you wish to try this option.


# Grading Process and Milestones
Projects will be evaluated on a variety of dimensions.
The most important factors are: the degree to which the specification is implemented, the technical quality of algorithms and protocols, and resource efficiency decisions.
We will also assess the responsiveness and intuitiveness of the application interface.
This is not a course in graphic design so, beyond basic utility and effectiveness, GUI aesthetics will not be graded.
Do not invest too much time in creating pretty icons or other superficial assets and focus on making information accessible to the user.
Consider using Unicode characters in place of hand drawn icons, when appropriate.

Students are encouraged to start project development early and to demo features and receive feedback in an ongoing manner throughout the entire quarter.
That said, there are two important project milestones where grading will occur:

May 26^th^: Project Checkpoint
: Students should demo their current prototype with a key focus on [mandatory functionality](#mandatory-features).
  These features will be evaluated and a partial grade computed to incentivize an early start on development.
  This partial grade---contemplating *just* the [mandatory functionality](#mandatory-features)---will count towards 15% of the final grade if it improves on the final submission.

June 16^th^: Project Submission
: Students should submit a fully functional prototype.
  All source code must be submitted through the course Fénix website.
  At this point, *all* functionality will be evaluated.
  Should this grade---which contemplates the [mandatory features](#mandatory-features) as well as any implemented [additional components](#additional-components)---be greater than checkpoint grade, then the checkpoint is ignored.
  Otherwise the final grade is calculated as 85% the submission grade and 15% the checkpoint grade.

  
# Collaboration, Fraud, and Group Grading
Student groups are allowed and encouraged to discuss their project's technical solutions without showing, sharing, or copying code with other groups or any other person, whether attending the course or otherwise.
Fraud detection tools will be used to detect code similarities.
Instances of fraud will disqualify the groups involved and will be reported to the relevant authorities.
Furthermore, within each group all students are expected to have a working knowledge of the entire project's code.
