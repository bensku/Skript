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
File names should contain respective issue (or PR) number and its title.
For example, <code>1234-Item comparison issues.sk</code>.

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

* Test cases are events: <code>test "test name"</code>
* Assertions are available as effects: <code>assert \<condition\> with </code>
* Take a look at existing tests for examples; in particular,
  <code>misc/dummy.sk</code> is useful for beginners

## Test Development
Use Gradle to launch a test development server:

```
./gradlew skriptTestDev
```

The server launched will be running at localhost:25555. You can use console
as normal, although it may look a bit ugly.

To run individual test files, use <code>/sk test \<file\></code>.