# Branches
Skript utilizes Git branches to keep development more manageable.

## Feature branches
Anyone with push access can create these. Core developers may use them to work
on code changes that other people would fork Skript for. Pull requests are used
to merge code from feature branches to anywhere else.

## Stable
A branch that contains the last revision current stable release or current
beta release (e.g. 2.4). Only bug fixes should be merged here. This branch is
merged to master every time bugs are fixed.

## Master
A branch that contains the next Skript release, or current alpha release.
Development on new features happens here, but bug fixes should target the
stable branch instead, unless the bugs do not exist there.