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


### Chapter 11

Chapter 11 is arguably the most chaotic one to follow. Primarily because you're
getting glimpses into codebase, but they're, time wise, out of order.

The chapter can logically be read in two parts. First part (start-p.95) build up
test infrastructure. Second part (p.95-end) is using this test infrastructure to
build the app.

Of course, you cannot write test infrastructure without constants defined in the
app part. So they're developed in tandem, but you're clueless about small decisions
authors made during this time, thus you'll see, for example, constants moved in
various code listings.

First issue is that book starts with working on class `ApplicationRunner`, yet
acceptance test we're trying to pass, needs `FakeAuctionServer` to pass it's
step 1, `auction.startSellingItem()`. So, we're doing 'The Fake Auction' on p.92 first,
and then returning to 'The Application Runner' on p.90.

Also, code listings show later stages of the codebase then we're at. Those can
be discerned because they have implemented methods which are needed for later
_steps_ of acceptance test. We ignore those methods in favor of mimicking
progressive development between commits.


#### Step 01 - Implementing auction.startSellingItem()

Openfire should be installed on local machine and configured as explained on p.89.

Though they write that they have ant script which starts this server, create users,
and so on in the text, they don't! They've set up the Openfire manually and start it
manually as well.

You can use current Openfire version (v4.6.7 at the time of writing).

We also switched from v3 of the smack library used in the book to the latest v4.
This is more modularized version of the library and we need to use its API a bit
differently.

Changes of note:

1) Dependencies are more explicit in what we need given that v4 of smack library is modular:
 - `org.igniterealtime.smack:smack-java8` - core smack library
 - `org.igniterealtime.smack:smack-tcp` - TCP transport layer for connecting to server
 - `org.igniterealtime.smack:smack-extensions` - for `org.jivesoftware.smack.chat2.*` package
 
2) Alluded by `smack-tcp` dependency, is that we need to use TCP variant of its
   connection class: `XMPPTCPConnection`, since `XMPPConnection` is now abstract in v4 of the
   library.

3) We don't want to use SSL protocol on local installation of Openfire, so we need to use
   `XMPPTCPConnectionConfiguration.builder()` when creating the connection, passing in
   `setSecurityMode(SecurityMode.disabled)`.

4) `startSellingItem()` is implemented more succinctly, using lambda.

5) We also implement `stop()` method which is called from `@After` method in an
   end-to-end test. You may easily overlook it, and if you don't disconnect from Openfire,
   on subsequent test runs you'll get:
   `org.jivesoftware.smack.XMPPException$XMPPErrorException: XMPPError: conflict - cancel`
   exception, which is explained later in the book (in next chapter, p.110 - A Surprise
   Failure), because of how it was explained to configure Openfire.

6) We also added `throws Exception` to constructor of `AuctionSniperEndToEndTest` and
   current end-to-end test to avoid try/catch statements in code and resemble more the code
   in the book.


#### Step 02 - Implementing auction.startSellingItem()

##### 1st Commit

We're starting to build the UI and test it with AssertJ Swing testing library.

This commit builds major part of _The Application Runner_ from p.90 and resolves
p.96 _First User Interface_ of the app codebase.

Given that we use AssertJ Swing testing library and not WindowLicker, there are
notable differences in the code:

1) We don't need to start the app in our own thread. AssertJ Swing has different way
   of starting the app under test. See:
   [GUI testing Java Swing Application [closed]](https://stackoverflow.com/a/40305597)

2) We don't use `AuctionSniperDriver` since it's WindowLicker specific. What we do need
   is a custom AssertJ matcher `getMainFrameByName()` (also from above link), so it is put
   in its own class `CustomMatchers`, very similar to how custom matchers in Hamcrest are
   explained in _Appendix B_.

   Implementation of custom matcher was also updated to test that JFrame is active, since
   we don't want to pick inactive/closed window instances when we (later) run more than
   one end-to-end test in sequence. Moreover, toString() method of it is overridden to
   provide more descriptive message in stack traces.

4) It seems they have had a test statement to look for `JFrame` named
   `MainWindow.MAIN_WINDOW_NAME` before the code listing on p.90. We don't, since we
   need to get FrameFixture before any UI lookups with AssertJ's Swing testing
   library so there is no point. Anyway, imagine we had something similar and you'd
   get similar error as they have on p.96. Value for that string constant
   (`Auction Sniper Main`) was taken from their error logs.


##### 2nd Commit

This commit resolves p.97 _Showing the Sniper State_ of the app codebase.


#### Step 03 - Implementing auction.hasReceivedJoinRequestFromSniper()

Updates `FakeAuctionServer` with code listing on p.94 with
`hasReceivedJoinRequestFromSniper()` implementation.

Since we're using newer version of smack library, the implementation is a bit simpler
because we're not creating inner class `SingleMessageListener`.

By looking at _Connecting to the Auction_ on p.99 we see that `Main.STATUS_JOINING` is
moved, and we know that it is moved to `MainWindow` class because of listing on p.101.

Likewise, constants `AUCTION_RESOURCE` and `ITEM_ID_AS_LOGIN` are moved from
`FakeAuctionServer` to `Main`.

In `Main` the difference from book comes from the fact that `smack` library moved on and
deprecated methods used in the book or changed the API.

This includes:
 
1) We create empty `Message` with `createStanzaFactory()` builder. We need to provide
   `FullJID` for the `to()` setter, not a String. We also must provide empty String to
   `setBody()` setter, because otherwise, auction server won't detect it as a `Message`.

2) We also close the XMPP connection after sending the `Message`, though not in the
   book. If we didn't we'd have the same problem with `XMPPError: conflict - cancel` as
   explained on p.110 - _A Surprise Failure_.