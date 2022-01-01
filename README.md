# Growing Object-Oriented Software, Guided by Tests


## Description

This is updated version of "Worked example" from the book "Growing
Object-Oriented Software, Guided by Tests" by
[Steve Freeman](http://www.higherorderlogic.com/) and
[Nat Pryce](http://www.natpryce.com/).

The contents of the book are still valuable as on the day this IT classic was
published, but main issue most readers have is tracking code changes, because
only code snippets are shown. To a lesser extent, some libraries used are
either hard to find and/or not relevant or there are nicer alternatives.

Noted differences are:
- Gradle build system
- Testing libraries have been replaced with _AssertJ ecosystem_:
    - Instead of WindowLicker (which is deprecated and/or hard to find) we're
      using [AssertJ's Swing](https://joel-costigliola.github.io/assertj/assertj-swing.html)
    - Since we're already using _AssertJ_ as replacement for WindowLicker, we
      might as well use [AssertJ's core](https://assertj.github.io/doc/). This
      gives use consistent testing API for matchers (instead of
      [Hamcrest](http://hamcrest.org/)) or for verification of mocks (instead
      of [jMock2](http://jmock.org/))

Obviously this makes the code a bit different from the book, but hopefully,
benefits of working with simpler/modern testing library you'll probably find in
contemporary work environments, outweigh the "cost" of enforcing one-to-one
port of the book's code or of using 3 testing libraries when one will do.

This decision can be compounded with the argument that the main issue I'm trying
to alleviate with this repository is tracking how the code base evolves. Thus,
exact implementation details are secondary to tracking what changed when and
where.

## Repository organization

To make tracking even easier, though you can walk through commits, I'm also
using Git branches to have better navigation for tracking of each logical step.
This allows following the progress of changes in the codebase even throughout
individual (complex) chapters.

Branch names have format of `chapter-<number>/step-<number>-description`

Most GUI Git clients treat '/' character as "sub-branches" marker. Thus you may
see something like:

- chapter-10
    - step-01-failing-acceptance-test
- chapter-11
    - step-01-implementing-auction-startSellingItem
    - step-02-implementing-application-startBiddingIn-auction
    - step-03-implementing-auction-hasReceivedJoinRequestFromSniper
    - ...

## Following along

On each commit, this README.md will be updated elaborating changes and
decisions during development recreation. Given that we have library changes
mentioned above, and since some changes are chaotic, especially in Chapter 11,
this is the only way to help you understand behind the scenes thought process.

### Chapter 10

There is just one code sample of an acceptance end-to-end test. To make project
compilable, the only addition I've added are stubbed classes this test uses where
each method is just failing with "not implemented" message.