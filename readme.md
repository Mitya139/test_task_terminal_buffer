# Terminal Buffer

This project implements a terminal text buffer in Kotlin.

A terminal buffer stores the visible screen content and the scrollback history. It also tracks the cursor position and current text attributes used for future edits.

## Implemented functionality

The `TerminalBuffer` supports:

- configurable screen width and height
- configurable maximum scrollback size
- current text attributes:
  - foreground color
  - background color
  - bold / italic / underline flags
- cursor operations:
  - get current position
  - set exact position
  - move up / down / left / right with bounds checking
- editing operations:
  - write text
  - insert text
  - fill current line
  - insert empty line at the bottom
  - clear screen
  - clear screen and scrollback
- content access:
  - get character at position
  - get attributes at position
  - get line as string
  - get full screen as string
  - get screen + scrollback as string

## Solution overview

The implementation is split into several small classes:

- `TerminalColor` — enum of supported terminal colors
- `TextAttributes` — immutable text style description
- `Cell` — one screen cell: character + attributes
- `Line` — one fixed-width row of cells
- `TerminalBuffer` — main class that manages:
  - visible screen
  - scrollback history
  - cursor state
  - current attributes
  - editing behavior


## Main design decisions and trade-offs

### 1. Fixed-size lines and cells

Each screen line has a fixed width, and each cell stores:

- a character, or `null` for an empty cell
- text attributes

Empty cells are rendered as spaces when converting content to strings.

This keeps the internal model simple and predictable: the screen is always a fixed rectangular grid, and all editing operations work within that grid.

### 2. Separate screen and scrollback

The buffer is split into two logical parts:

- **screen** — the visible editable area
- **scrollback** — history of lines that scrolled off the top

Scrollback is stored separately and is not modified by normal editing operations on the visible screen.

This matches the conceptual model of a terminal and also makes content access clearer.

### 3. Wrapping and pending wrap behavior

Writing a character into the last column does **not** immediately move the cursor to the next line.

Instead, the buffer sets an internal `pendingWrap` flag. The actual wrap happens only before the next printable character is written.

This means that writing into the bottom-right cell does not instantly scroll the screen. Scrolling happens only when the next printable character is processed.

This behavior was chosen because it is closer to how terminal autowrap typically works and avoids premature scrolling.

### 4. Insert overflow policy: clipping

`insertText()` shifts existing cells to the right and may continue shifting text to the following lines.

If this shifting reaches the bottom-right corner of the screen and still has overflowing content, the overflowing tail is **clipped**.

This means that insertion may cause information loss in this edge case. This is not ideal, but it was a deliberate trade-off.

I considered several alternatives:

- **Scroll the screen up during insert overflow**  
  I decided against this because insertion near the top of the visible screen would then unexpectedly modify unrelated content below and even push lines into scrollback. That behavior felt difficult to reason about and not very intuitive.

- **Reject insert when overflow happens**  
  I also considered stopping the operation entirely if the inserted content does not fit. I rejected this because it makes insertion more abrupt and less practical, especially when part of the text could still be inserted successfully.

- **Clip overflowing content**  
  This was the simplest and most predictable option. It is easy to explain, deterministic in behavior, and straightforward to test, even though it may lose data in extreme cases.

So clipping was chosen as the explicit overflow policy for insert operations.

### 5. Supported control characters

Due to time constraints, I implemented support only for the following control characters:

- `\n`
- `\r`
- `\t`

All other control characters are ignored.

Their behavior in this implementation is:

- `\n` — moves to the next line **and** sets `cursorColumn = 0`
- `\r` — sets `cursorColumn = 0` on the current line
- `\t` — inserts spaces up to the next tab stop, using tab width `8`

A notable detail is that `\n` here does **not** mean “move one row down while keeping the same column”.  
Instead, it behaves as moving to the next line and returning to the beginning of that line.

This was chosen because it makes text-writing behavior simpler and more intuitive for this task.

Tabs are implemented as repeated insertion/writing of spaces until the next tab stop. This keeps the implementation simple, though it is still a simplified model of terminal tab handling.

### 6. Content access indexing

Methods such as `getCharAt`, `getAttributesAt`, and `getLineAsString` use a single **global row index** over the whole buffer.

Rows are indexed in this order:

1. scrollback rows first
2. visible screen rows after them

So the mapping is:

- `globalRow = 0 .. scrollbackSize - 1` — scrollback history
- `globalRow = scrollbackSize .. scrollbackSize + screenHeight - 1` — visible screen

This gives one consistent coordinate system for reading both history and current screen contents.

### 7. Performance considerations

The visible screen and scrollback are stored in `ArrayDeque` instead of `MutableList`.

This avoids inefficient front removals such as `removeAt(0)` when:

- scrolling the screen upward
- trimming old scrollback history

For queue-like behavior at both ends, `ArrayDeque` is a better fit.

## Testing

The project contains unit tests that cover:

- initialization and validation
- cursor movement and bounds
- attribute updates
- writing text
- insertion behavior
- wrapping
- scrollback behavior
- screen clearing
- edge cases such as:
  - minimal buffer sizes
  - zero scrollback
  - pending wrap reset

The tests are intended both to validate correctness and to document the expected behavior.


## Build and test

Run tests with:

```bash
./gradlew test