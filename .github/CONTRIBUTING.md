# Contributing to Skript
Skript is an open source project. That basically means that you are
encouraged to contribute by reporting issues and writing code. Basic
guidelines are posted below to save time from everyone.

## Issues
Issues are usually used to report bugs and request improvements or new features.

Script writers should **not** use issue tracker to ask why their code is broken,
*unless* they think it might be a bug. Correct places for getting scripting advise
are SkUnity forums and Discord (see README again).

Don't be scared to report real bugs, though. We won't be angry if we receive
invalid reports; it is just that you're unlikely to get help with those here.

Oh, and one more thing: please avoid being offensive in Skript's issue tracker.
It doesn't help anyone and may also get you banned.

### Reporting Bugs
So, you have found out a potential Skript bug. By reporting it correctly, you
can ensure that it will be correctly categorized as a bug and hopefully, fixed.

First, please make sure you have **latest** Skript version available. If there
are no stable (no pre-release tag) versions, this means latest dev build.
If you can find non-prerelease in downloads page of this repository, you may
also use that one. Do **not** use 2.2, 2.1 or older, since while they are
technically stable, they tend to not work reliably with Minecraft 1.9+.

Second, test without addons. No, seriously; unless you're an addon developer,
test without plugins that hook to Skript before reporting anything. We can't
help you with addon issues here, unless we get a lot of technical information
about the addon in question. Usually only developers of them know addons'
Java code well enough.

If the issue still persists, search the issue tracker for similar
errors and check if your issue might have been already reported.
Only if you can't find anything, open a new issue.

Now, what would you fill to the issue report?
* A useful title (tl;dr for us busy and/or lazy developers)
* Description of the issue, aka what does *not* work
* Any error messages in the console (please use a paste service for lenghty errors)
* Whether you tested without addons or not
  - *Sometimes* we do not ignore people who are testing with addons
  - Testing without is still better, unless you're an addon developer
* Any additional information you think would be helpful

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
We recommend using an IDE; you can find some set up instructions in README.
Also, please follow our CODING_CONVENTIONS.

### After Programming
Test your changes. Actually, test more than your changes: if you think that you
might have broken something unrelated, better to test that too. Nothing is more
annoying than breaking existing features.

When you are ready to submit a pull request, please follow the template. Don't
be scared, usually everything goes well and your pull request will be present
in next Skript release.

Good luck!

### Insight: Pull Request Review
Pull requests will be reviewed before they are merged.
This includes testing the code, but *no* debugging if it doesn't work;
please test your code before submitting a pull request.

The reviews are done by Skript developers with push access, but you're
likely to get others to give feedback too. If you're asked about something,
please answer - even if it is not us asking.

## Code Bounties
Just to let you know, [Bountysource](https://www.bountysource.com/) is a thing.
While it has not ever happened, you could post a bounty on an issue to make
someone potentially do it faster.
