# EC Habitats FOSS Hack Day
#### January 13th 2024 12 noon until ?? Pacific time

* Physical Location: The MADE in Oakland, CA https://www.themade.org/
* Live Stream: #hack-day on https://discord.gg/eTN9kHBYsZ
* Ongoing Community: #ec-habitats on https://neohabitat.slack.com
* Hack Day Coordinator: Randy Farmer randy@spritely.institute
* Repository: https://github.com/Museum-of-Art-and-Digital-Entertainment/Electric-Communities
  * Web readable [doc root here](https://htmlpreview.github.io/?https://github.com/Museum-of-Art-and-Digital-Entertainment/Electric-Communities/blob/main/Microcosm/Source/www-old/engineering/OldStuff/index.html)
  
## Introduction
Electric Communities was formed to develop a secure, distributed platform for online socialization and commerce. The first demonstration of this was Microcosm (aka EC Habitat). Microcosm was a 3D virtual world, in which people could meet, talk (by typing or speaking), and exchange objects - it created the E programming language and did pioneering research into many modern programming techniques, such as webapps, promises/futures. EC later grew to include the work of The Palace Inc, and Onlive Technologies.

Here's a video of it running in '97: 

[![Randy Demos ECHAbitats](https://img.youtube.com/vi/KNiePoNiyvE/0.jpg)](https://www.youtube.com/watch?v=KNiePoNiyvE)

Thanks to the leadership of The MADE (Museum of Art and Digital Entertainment) working with the [Spritely Networked Communities Institute](http://spritely.institute), we are working to make [EC IP](https://docs.google.com/document/d/19iarmahK9_-Yp9VKF_yQ4vRY78fisViAuCAqsTkUQuo/edit?usp=sharing) open source.

## This Hack Day: restoring/opening the first 3D distributed metaverse
We’ve gathered repositories from far and wide, and today we bite off the first chunk: 
Restoring the first decentralized metaverse: EC Habitats Beta (WIndows PC) full working condition (peer-to-peer virtual worlds) as well as attempting to recover the source code and documentation for that project.

Future hack days will attack other portions of the extensive EC product portfolio, including The Palace, E, Passport, and Onlive.

## SETUP TASKS 
* StuBlad has created a [Windows-11 compatible installer executable](https://github.com/Museum-of-Art-and-Digital-Entertainment/Electric-Communities/blob/main/Microcosm/ECHabitatBeta/Restoration/ECHabitatsInstaller_r167.exe) of EC Habitats r167
* For access to private repositories - email Github ID to randy@spritely.institute
  * Policy: No forks/use until released as FOSS. See [Archival Agreement]([url](https://drive.google.com/file/d/1tmPeAtsSDoDxMpKohYyCMmLOjAs0u3Wf/view?usp=sharing)).

## GOALS
* Get the first Decentralized Metaverse: EC Habitats r167 Beta disk-software for Windows PCs fully operational (permitting people to connect their locally run worlds to each other without a centralized server running game or message code.)
  * There are multiple world configuration files, get as many working as possible.
  * The application has URLs for web pages and a rendezvous server (LDAP?) that need to be changed/proxied/served.
  * Re-Package for easy use by modern users
    * (This may fork future multi-platform installer projects.)
  * Make this work available as FOSS
* Try to get EC Habitats to compile from source. We have two potential source trees (COSM and COSM1), but don’t know the status of the code.
  * Identify critical design documents (unum, dist containers, etc.)  that should be FOSS
  * Build or not, we will open the EC Habitats code and documentation as FOSS
* OPTIONAL:  Identity leaders for future EC Hack Days for other products:
  * The Original E programming language
  * Electric Communities Passport platform and many, many worlds - needs IP scrubbing
  * The Palace (including the original Mansion source provided by JBum)
  * Onlive Technologies

## TEAMS
* Web Integration Team
  * Member Required: MADE system admin for DNS changes and service installations.
  * We'll need web page services, possible redirects, and to recover the game's support pages.
  * Also, the service that turns portal URLs into IP addresses for p2p connections. Need to open some ports?
    * Are there installer mods to open ports? A web page for instructions for platorms.
* Interoperability Team aka Worlds Team
  * Works closely with Web Team to get the r167 decentralized portals working again (LDAP server?)
  * Bring up tshe other worlds (see r167 config files up) at peristent locations as a service.
  * Note: There is a "headless" mode that allows r167 to run on a non-windows box for hosted worlds.
* Compilation Team
  * Do we have the source code required to recreate r167 or a similar version?
  * Make sure to open what source we have after screening for IP issues (none expected, but maybe drivers?)
  * Compile it? This may be ongoing post hack day.


