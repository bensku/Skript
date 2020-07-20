# Skript Development Model
I.e. how we organize development of Skript to avoid new releases surprisingly
breaking every once a while.

## Branches
We use multiple branches within this repository to organize the development.
Essentially, there are three kinds of branches.

### Feature branches
Large, breaking and/or experimental changes are done in their own feature
branches. They will be merged to master branch after they are ready and
have been tested.

### Master
Active development for next release happens here. New features can be added,
in addition to fixing bugs. Large changes should not appear in this branch
until at least some testing on them has been performed.

### Freeze
Only bug fixes will be done in this branch to avoid introducing *more* bugs.
It will also be regularly merged back to master to avoid fixing same bugs twice.
All stable releases - major or minor - will be made from this freeze branch.

Master will be merged to freeze a while before a stable release is going to be
published. That marks the end of bug fix releases for the previous stable
release.
