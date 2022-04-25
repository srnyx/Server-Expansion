# Server-Expansion

Adds server-related placeholders.  
Read the [placeholder page](https://helpch.at/placeholders#server) for a list of placeholders.

- Adds MSPT placeholders (I just copied the changes from https://github.com/ChimneySwift/Server-Expansion):
```
%server_mspt%
%server_mspt_10s%
%server_mspt_1m%
%server_mspt_colored%
%server_mspt_10s_colored%
%server_mspt_1m_colored%
```
- Removes the `*` when TPS goes above 20

This fork requires [Paper](https://papermc.io).
It will NOT work with spigot servers.
*But you should be using Paper anyway*

## Build Instructions

This is built using [Maven](https://maven.apache.org/).
You will have to install that first if you don't have it already.

Simply run `mvn install` and the jar should end up in `target/`.
