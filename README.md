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


#### Step 04 - Losing an Auction

Implements final two steps of acceptance test: `auction.announceClosed()` and
`application.showsSniperHasLostAuction()`.

Of note is that we needed to modify `ApplicationRunner.showsSniperHasLostAuction()` to
wait for the sniper status label to change with AssertJ Swing `pause()` with `Condition()`.
Otherwise, test would be flaky and often fail while expecting the `STATUS_LOST` to be
shown there...

Also, note _Why the Chat Field?_ on p.101 still holds in the latest `smack` library
we use.


### Chapter 12

Chapter 11 was chaotic for various reasons. You'll be glad to know that the following
chapters are much easier to follow.


#### Step 01 - New Acceptance Test

Created new acceptance test from p.106 _Starting with a Test_.

Since we use stubbed functions in `FakeAuctionServer` and `ApplicationRunner`, at this
point you'll have trouble running them multiple times because of
`XMPPError: conflict - cancel` exception until we reach p.110 - _A Surprise Failure_. We'll resolve
this in the next step/commit.


#### Step 02 - A Surprise Failure

We're just skipping a few pages to fix this annoying issue of not closing the
connection properly. This is basically just implementing p.110 - _A Surprise
Failure_.

Now we can continually run this failing acceptance test and not be bothered
with Openfire refusing to connect.


#### Step 03 - Implementing auction.reportPrice()

Though the book implements methods further down the acceptance test, we only
implement this simple method to get to the next test failure.


#### Step 04 - The First Unit Test

When we implemented next test method, `application.hasShownSniperIsBidding()`,
we need to skip to p.114 - _The First Unit Test_, and start creating first
unit test class.

We added test Mockito testing library to easily create mocks and test their
behavior. It is another deviation from book's choice of jMock, but it is choice
made on account of Mockito being more modern, under active development and with
a much nicer API for verification of mocks.

Another small deviation is that we don't bind `AuctionMessageTranslator` as an
implementation of `Smack`'s `MessageListener` because there is no point with
v4 of `Smack`, and also we're able to call its single method `translateMessage()` 
and not `processMessage()`.

At the end we took the opportunity to clean up `showsSniperHasLostAuction()` and
`hasShownSniperIsBidding()` in `ApplicationRunner` by creating private utility
function `showsSniperStatus()` containing common code. Additional benefit is that
now they more resemble original code by the authors.


#### Step 05 - Unpacking a Price Message

Now that application has place of translating messages, we need to differentiate
between PRICE and CLOSE messages.


#### Step 06 - Implementing auction.hasReceivedBid()

We're now backtracking to p.107 and implementing `auction.hasReceivedBid()`.

Again, some differences because we use AssertJ, but the test now fails in
expected way.

We follow a couple of refactorings, including updating
`auction.hasReceivedJoinRequestFromSniper()`, and extracting message constants
`JOIN_COMMAND_FORMAT` and `BID_COMMAND_FORMAT` to `Main`.

To end the chapter we fix the `auction.announceClosed()` as described on p.120.

With that, we've improved first end-to-end test, and we have two Unit tests
passing.

Next chapter is continuing working on testing `auction.hasReceivedBid()` from
the second end-to-end test.


### Chapter 13

#### Step 01 - Introducing AuctionSniper

We're just following creation of `SniperListener` interface and retrofitting it
into `Main`.


#### Step 02 - Sending a Bid

This step finalizes code required to pass the second end-to-end test by sending
back a bid.


#### Step 03 - Tidying Up the Implementation

We're finishing the chapter with three little steps:


##### Commit 1 - Extracting XMPPAuction

Cleaning up `joinAuction()` method with small changes in implementation of
XMPPAuction because of Smack API, and thus our, differences.


##### Commit 2 - Extracting the User Interface

The only difference is that we don't implement `snipperWinning()`, since we
haven't started working on the next end-to-end test (which is the next chapter).


##### Commit 3 - Tidying Up the Translator

Simple follow through.


### Chapter 14

#### Step 01 - First, a Failing Test

We create a new failing end-to-end test `sniperWinsAnAuctionByBiddingHigher()`.


#### Step 02 - Who Knows About Bidders?

Simple follow through.


#### Step 03 - The Sniper Has More To Say

Apart from adding `snipperWinning()` in `SniperStateDisplayer` which was hinted
on p.134, but we skipped in previous chapter's step 03, commit 2 (since it wasn't
relevant at a time), we simply follow through the text and now have an additional
passing unit test + we're one step closer to passing end-to-end test!


#### Step 04 - The Sniper Acquires Some State

We diverge here since we're not using jMock2 but Mockito. Though authors want strict
testing, and use jMock's `States` to track the state of `AuctionSniper` by listening
to `SniperListener`, we do it with Mockito's `InOrder` verification.

This is probably not great substitute, but good enough...


#### Step 05 - The Sniper Wins

Finalizing the chapter with another unit test.

Then we implement `showsSniperHasWonAuction()` in `ApplicationRunner` and we have a
passing end-to-end test.


### Chapter 15

Chapter which substitutes JLabel with JTable for showing more information about auction.


#### Step 01 - Replacing JLabel

Simple change where we replace JLabel with single cell JTable and update the logic of
tests to look for that cell. Of course it's a bit different in _AssertJ Swing_ then in
_WindowLicker_, but nothing too confusing.


#### Step 02 - Displaying Price Details

Concentrating on testing bidding event in end-to-end tests, we move through this step
with a couple of commits to catch all the changes in the codebase.


##### Commit 1 - First, a Failing Test

Changes in this commit will void end-to-end tests but they'll be fixed in the following steps
of the chapter.

Most notably, text doesn't mention how to update `showsSniperHasLostAuction()`. Because
of that, we'll replace its implementation with: `fail("fix me");` to know we need to
resolve it. We'll return and fix it in Step 04 - Follow Through.

Next, we update the `auctionSniper.sniperWinsAnAuctionByBiddingHigher()` and more
importantly, `ApplicationRunner` to now search inside JTable, reaching understandable
fail message.

Again we need to tweak out approach to AssertJ Swing but that's easy enough.


##### Commit 2 - Sending the State out of the Sniper

Main point here is that we're creating `SniperState` class. Text mentions they're using
Apache's Common Lang helpers, though they don't show how. Thus, we add this dependency in
gradle, and implement simplest `equals()`, `hashCode()` and `toString()` with it.

After we have `SniperState` value object, we modify
`AuctionSniperTest.bidsHigherAndReportsBiddingWhenNewPriceArrives()`


##### Commit 3 - Showing a bidding sniper

Now we have auction information in four cells of JTable. Couple of notes about
this commit:

- In `SnipersTableModelTest.setsSniperValuesInColumns()` we just test that
  `tableChanged()` was called on `listener` ignoring `TableModelEvent` arguments.

  In theory Hamcrest's `samePropertyValuesAs()` could've been replaced with
  Mockito's `refEq()` as described in [Mockito equivalent to this Hamcrest "samePropertyValuesAs"/jMock "with" idiom?](https://stackoverflow.com/a/39930882),
  yet, in practice, it didn't work for unknown reason and fallback is good enough.
- We also needed to update  `ApplicationRunner.startBiddingIn()` to look in
  `Column.SNIPER_STATUS` cell for status value. This change was necessary
  because of differences in testing libraries.

At this point we've reached the same error stage in the book with `sniperWinsAnAuctionByBiddingHigher()`
end-to-end test failing as expected.

The other two end-to-end tests are failing with `"fix me"` error, and that will be
resolved soon as already noted.


#### Step 03 - Simplifying Sniper Events

Text rushes through changes, often completely skipping changes by merely mentioning them.


##### Commit 1 - Listening to the Mood Music

Just finding better names. Since it touches a bunch of files, we'll put it in a separate
commit.


##### Commit 2 - Repurposing sniperBidding()

First, we introduce `SniperState` enum and related refactorings. Then we modify
`sniperBidding()` method and given that it's more generic it's renamed to
`sniperStateChanged()`.

We also created another custom matcher similar to book's `aSniperThatIs()` on p.161. It's
an ArgumentMatcher implementation for testing `SniperState` inside `SniperSnapshot` and
we created a helper static method called `isStateOf()` so that the `AuctionSniperTest`
reads nicely with our testing framework.

Of note is that `MainWindow.sniperStatusChanged()` changed name to `MainWindow.sniperStateChanged()`
somewhere between pages p.156 and p.167 code listings. So we assume it happened here.


##### Commit 3 - Filling In the Numbers

This commit is basically converting `sniperWinning()` to the new `sniperStateChanged()` we
did in previous one.

We need to make changes to `ActionSniperTest`, the `ActionSniper` itself, `SniperSnapshoot`
and delete references to `sniperWinning()` with further refactorings of existing unit tests.

We also added `MainWindow.STATUS_WINNING` to `STATUS_TEXT` in `SnipersTableModel`.

With that, our target end-to-end test now passes. Two more to go...


#### Step 04 - Follow Through

This is a step where we'll fix two remaining end-to-end tests by further refactoring of the app.


##### Commit 1 - Converting Won and Lost

Now that we've converted `sniperBidding()` and `sniperWinning()` to `sniperStateChanged()`, two
remaining methods remain: `sniperWon()` and `sniperLost()`. When we do that, we're able to simplify
the code.

Text also mentions that they've used `SniperStateTests` and `Defect` classes, but they're not shown
in the book. We found them in the books repository. Though that codebase contains only final code
of the book, we're able to augment missing information from the text that way.


##### Commit 2 - Fixing showsSniperHasLostAuction()

Deviating from the book with just a simple commit, to revisit the broken `fix me` end-to-end tests.

Now that we completely converted `SniperListener` and we're updating the table cells, we simply
reuse the logic we had when we converted JLabel to JTable at the beginning of the chapter,
and just look at the last column of the single row, expecting it to contain `MainWindow.STATUS_LOST`.

With that, now all tests are green!


##### Commit 3 - Trimming the Table Model

We've already cleaned up the code base related to `setStatusText()` in Commit 1, so we're just
continuing to remove description strings from MainWindow into SnipersTableModel and update the
tests to reflect that.


##### Commit 4 - Object-Oriented Column

Though the text mentions updating test code, it doesn't show what they did. So we change
`startBiddingIn()` method to expect empty string for `itemId` and `0` for `lastPrice` and
`lastBid` when checking for joining status. As for `showsSniperHasLostAuction()`, since it's
used by `sniperMakesAHigherBidButLoses()` *and* `sniperJoinsAuctionUntilAuctionCloses()`, yet
those two have different expectations for `lastPrice` and `lastBid`, so we've added those as
parameters to reflect that. Now all test code is checking for equality in all columns of
the JTable.

Then we've updated `Column` class to avoid switch statement... and pull mentioned unit test
from final code of the book, like we did in Commit 1.
