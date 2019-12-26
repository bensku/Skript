# Skript Testing System
Configuration and actual test scripts for Skript's integration test system can
found here.

## Contributing to Tests
We're not very strict about what can be added to our test suite.
Tests don't have to be good code - often, testing edge cases pretty
much requires weird code. That being said, there are a couple of things
to keep in mind:

* Use tabs for indentation
* Write descriptive assert messages and write comments
* Ensure your tests pass with all supported Skript versions
  * Use standard condition for checking MC version

## Test Categories
Scripts under <code>tests</code> are run on environments. Most of them are
stored in subdirectories to help keep them organized.

### Syntax tests
Under <code>syntaxes</code>, there are tests for individual expressions,
effects and commands. Again, each in their own subdirectories. Names of files
should follow names of respective syntax implementations in Java
(e.g. <code>ExprTool.sk</code>).

Contributors can add tests for expressions that do not yet have them, or
improve existing tests.

### Regression tests
Under <code>regressions</code>, there are regression tests. Such tests are
created when bugs are fixed to ensure they do not come back in future.
File names should contain respective issue number and its title. If no issue
is available, PR number and title can be used in place of them.

For example, <code>2381-multiplication in list index.sk</code>. Use only
lower case letters in names, because issue titles are not consistent with
their capitalization.

Contributors should not add regression tests unless they also fix bugs in
Skript. Those who do fix bugs *should* write regression tests.

### Miscellaneous tests
All other tests go in this subdirectory. Contributions for generic tests
will need to meet a few criteria:

* They must not be duplicates of other tests
  * Similar tests are ok
* They must currently pass
* They should not rely on bugs in Skript to pass

Aside these things, pretty much anything goes.

## Testing Syntaxes
Test scripts have all normal Skript syntaxes available. In addition to that,
some syntaxes for test development are available.

* Test cases are events: <code>test "test name" [when \<condition\>]</code>
  * Contents of tests are not parsed when conditions are not met
* Assertions are available as effects: <code>assert \<condition\> with "failure message"</code>
* Take a look at existing tests for examples; in particular,
  <code>misc/dummy.sk</code> is useful for beginners

## Test Development
Use Gradle to launch a test development server:

```
TERM=dumb ./gradlew skriptTestDev
```

The server launched will be running at localhost:25565. You can use console
as normal, though there is some lag due to Gradle. If you're having trouble,
try without <code>TERM=dumb</code>.

To run individual test files, use <code>/sk test \<file\></code>. To run last
used file again, just use <code>/sk test</code>.