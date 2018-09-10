# Contributing to Skript
Skript is an open source project. That basically means that you are
encouraged to contribute by reporting issues and writing code. Basic
guidelines are posted below to save time from everyone.

## Issues
Issues are usually used to report bugs and request improvements or new features.

Script writers should **not** use issue tracker to ask why their code is broken,
*unless* theyt think it might be a bug. Correct places for getting scripting advise
are SkUnity forums and Discord (see README again). That being said, this is
mostly to save your time - if you're not sure if something is a bug, report it
anyway.

### Reporting Bugs
So, you have found out a potential Skript bug. By reporting it correctly, you
can ensure that it will be correctly categorized as a bug and hopefully, fixed.

First, please make sure you have **latest** Skript version available. If there
are no stable (no pre-release tag) versions, this means latest dev build.
If you can find non-prerelease in downloads page of this repository, you may
also use that one. Do **not** use 2.2, 2.1 or older, since while they are
technically stable, they tend to not work reliably with Minecraft 1.9+.

If you are running latest Skript and the error still persists, search the
issue tracker for similar errors. Check if your issue might be already reported.
If it has been reported, leave a comment, otherwise open a new issue.

Now, what would you fill to the issue report?
* Description of the issue, aka what does *not* work
* Any error messages in the console (you can also use a paste service)
* Any addons that you have - this is **very** important
* Any additional information you think would be helpful

Do not use offensive language or insult *anyone*. Distruptive behaviour in issue
tracker will be dealt with harshly and in the end, will leave your issue unresolved.

## Pull Requests
Pull requests are a great way to contribute code, but there are still a few
guidelines on how to use them.

Note that these guidelines do not apply to pull requesting changes to
documentation. For that kind of pull requests, just use common sense.

### What to Contribute?
You can find issues tagged with "help wanted" on tracker to see if there is
something for you. If you want to take over one of these, just leave a comment
so other contributors don't accidentally pick same issue to work on. You can also
offer your help to any other issue, but for "help wanted" tasks, help is really
*needed*.

### Before Programming...
If you did not pick an existing issue to work on, you should perhaps ask if your
change is wanted. This can be done by opening an issue or contacting developer
directly via Discord.

Then, a few words of warning: Skript codebase will not be pleasant, easy or
that sane to work around with. You will need some Java skills to create anything
useful in sane amount of time. Skript is not a good programming/Java learning
project!

Still here? Good luck. If you did not learn how to use Git, now might be a good
time to [learn](https://help.github.com/categories/bootcamp/).

### When Programming
Use a sane development environment. README has some instructions about working
with Skript's code, please follow them. As said there, you really should follow
them to get your pull request merged.

If you cannot or do not wish to follow some of the guidelines, it does not mean
that all is lost. You can change your code even after submitting a pull request,
so you can later improve it. Also, if your feature is valuable enough, it might
be merged and then fixed for you. It does not happen very often, though.

### After Programming
Test your changes. Actually, test more than your changes: if you think that you
might have broken something unrelated, better to test that too. Nothing is more
annoying than breaking existing features.

When you are ready to submit a pull request, please follow the template. Don't
be scared, usually everything goes well and your pull request will be present
in next Skript release.

Good luck!

### Insight: Pull Request Review
Pull requests will be reviewed by one of following persons before they will be
merged. This includes testing the code, but *no* debugging if it doesn't work.
So please test your code beforehand, the review is last safeguard.

Reviewers:

* @bensku - Skript developer, repo owner
* @Snow-Pyon Skript developer
* @xXAndrew28Xx - addon developer

Review also includes checking code style. That is usually something that @bensku
does, since it requires relatively little time.

Note that reviewers may naturally not review their own pull requests, but must
get someone else do it. If no one else can, @bensku will do it.

## Code Bounties
Just to let you know, [Bountysource](https://www.bountysource.com/) is a thing.
While putting a bounty on issue *probably* won't make me do it any faster,
someone else might get interested.

Other donation methods are possible in the future, but right now it is not
possible to just donate money for me (bensku). Putting bounties on things
I'm already doing is always possible, though :)
