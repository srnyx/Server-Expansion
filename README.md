# Server-Expansion

Adds server-related placeholders.  
Read the [placeholder page](https://helpch.at/placeholders#server) for a list of placeholders.

- Adds MSPT placeholders (I just copied the changes from [ChimneySwift/Server-Expansion](https://github.com/ChimneySwift/Server-Expansion)):
```
%server_mspt%
%server_mspt_10s%
%server_mspt_1m%
%server_mspt_colored%
%server_mspt_10s_colored%
%server_mspt_1m_colored%
```
- Removes the `*` when TPS goes above 20
- Uses Gradle
- Has GitHub workflows/actions

This fork requires [Paper](https://papermc.io).
It will NOT work with spigot servers.
*But you should be using Paper anyway*

## Build Instructions

You can get the latest version from the [Actions page](https://github.com/srnyx/Server-Expansion/actions).

Or, you can download the source code and then run `gradle build`.
