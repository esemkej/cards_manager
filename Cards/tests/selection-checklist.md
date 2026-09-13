# Card selection and drag regression checks

Automated tree checks (no device needed): compile CardTreeOperations.java and
CardTreeOperationsTest.java with javac, then run com.eas.cards2.CardTreeOperationsTest.
Existing SavedChoiceTest and CardIdCompactorTest should also pass.

Device checks still required (the local emulator cannot boot without its acceleration driver):

- Hold a card and release: selected, no editor or quick view opens. Tap additional cards;
  tap selected cards to deselect. Back/Done exits selection before leaving a folder.
- One selected item exposes Edit; multiple selected items hide it. Save edits and verify
  its manual position, images, code, favorites and folder children survive.
- Hold a selected card and move: visible selected cards gather under the finger, count
  includes offscreen selections, source cards dim, FAB becomes a red trash target.
- Hover trash: target enlarges and gives haptic feedback. Release: confirmation appears;
  Cancel preserves every selected card; confirm removes selected subtrees and their photos.
- Move outside targets and release, interrupt with a second finger, or background the app:
  no mutation, overlay disappears, source views and add FAB recover.
- Drag near top/bottom edges: auto-scroll continues without lifting the finger. Move away:
  scrolling stops. Confirm accurate trash hit testing with gesture and three-button navigation.
- Drop at a folder's center: group moves inside. Drop at an item's upper/lower edge:
  insertion marker indicates before/after; same-section group order persists after restart.
- Move to folder menu: existing nested folders, root, new folder, blank-name validation,
  cancel. Selected folders and their descendants must be excluded as destinations.
- Favorite/Unfavorite all, including a parent and child selected in Favorites. Select all
  excludes section headings and the virtual Favorites folder. Filters prune hidden selection.
- Filtered/search/Favorites reordering explains that the full folder must be opened first.
- Create and delete the last card/folder: animated insert/remove and empty-state fade;
  no empty-state layout gap and FAB remains reachable. Search with no matches behaves likewise.
- Repeat with 1/2/3-column grids, large font, dark theme, Slovak, TalkBack (Select action),
  animations disabled, rapid taps, and a large collection.

Floating top bar:
- From the top, scroll slowly until the first row crosses the controls' bottom edge: glass
  tint, rounded lower edge, and shadow animate in; card colors remain visible underneath.
- Scroll/fling back: the surface returns to its resting state, without collapsing controls.
- Touch Search, Filters, Settings and blank toolbar space while a card is underneath:
  the underlying card must never open or select.
- Check selection mode at the top and midway through the list, drag auto-scroll below the
  floating bar, pull-to-refresh spinner position, text-size changes, and empty/filter states.
- Verify day/night themes and disabled system animations. This effect uses translucency,
  not a costly bitmap blur or a blur applied to the controls themselves.
