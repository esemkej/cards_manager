# Quick-message decisions

- Removed: item-deleted success message; the item disappearing already confirms the action.
- Kept silent: selection, favorite changes, moves, reordering, card/photo creation, and cancelled pickers.
- Clipboard: show confirmation only below Android 13; newer versions already provide a system preview.
- Import/export: retain short confirmation because the user may still be in Settings and cannot see the saved file or replaced collection.
- Invalid text import/name: inline field error, preserving the text and dialog for correction.
- Keep actionable feedback for unreadable scans/photos, camera permission/opening problems,
  unavailable color data, missing code/photos, failed file imports/exports, invalid folder moves,
  and reordering in a filtered view or across sections. No exception text is shown to the user.

All remaining quick messages use AppMessages: rounded surface-var Snackbar with theme text,
accent/error icon, Close action, fade animation, and accessible timing. Messages sit above the
FAB or the active dialog footer, replace old messages, suppress duplicates, and clear on stop.
Android Material handles accessibility timeouts and swipe dismissal where a CoordinatorLayout
is available. Messages resolve the current window after dialogs have opened/closed.

Device checks pending (local emulator acceleration unavailable): verify FAB shadow at 1.4x,
centered delete action row, notices above card editor/import controls with keyboard visible,
large text/TalkBack reading time, both themes and both languages, and rapid repeated errors.
