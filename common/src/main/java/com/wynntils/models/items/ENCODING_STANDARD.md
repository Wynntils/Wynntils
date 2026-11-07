# Wynntils Item Encoding
This document specifies version 4 of Wynntils Encoding, whose version byte is `0x03`.

## Credits
- [kristofbolyai](https://github.com/kristofbolyai) created the original standard.
- [wenweia](https://github.com/wenweia) created the new format and updated the standard for versions 2 and 3.
- [ShadowCat117](https://github.com/ShadowCat117/) updated the standard for version 4.

# Overview
Wynntils Encoding turns a Wynncraft item into a short list of **blocks**, the blocks into **bytes**, and the bytes into **text** that can be pasted into chat or stored.

| At a glance | |
|-------------|-|
| Current version | 4 · version byte `0x03` |
| Kinds of block | 23 · ids `0x00`–`0x15` and `0xFF` ([registry](#block-registry)) |
| Item types | 6 · ids `0x00`–`0x05` ([Type block](#2-type-block)) |
| Text forms | UTF-16 [private-use characters](#private-use-areas) (chat) · base64 (storage) |
| Size | 2 bytes per character · a gear item is roughly 20–30 characters, up to about a quarter of a chat message |

## Table of Contents
- [Overview](#overview)
    - [Wire Format](#wire-format)
        - [Layers](#layers)
        - [Block Stream](#block-stream)
        - [Block Registry](#block-registry)
        - [Data Types](#data-types)
        - [Elements](#elements)
        - [Text Encoding](#text-encoding)
        - [Private Use Areas](#private-use-areas)
        - [Sharing in Chat](#sharing-in-chat)

- Blocks
    - Every item
        - [1. Start](#1-start-block)
        - [2. Type](#2-type-block)
        - [23. End](#23-end-block)
    - Gear, tomes and charms
        - [3. Name](#3-name-block)
            - [Shiny and Ascended Names](#shiny-and-ascended-names)
        - [4. Identifications](#4-identifications-block)
            - [Stat Ids](#stat-ids)
        - [5. Powder](#5-powder-block)
        - [6. Rerolls](#6-rerolls-block)
        - [7. Shiny](#7-shiny-block)
    - Crafted items
        - [8. Custom Gear Type](#8-custom-gear-type-block)
        - [9. Durability](#9-durability-block)
        - [10. Requirements](#10-requirements-block)
        - [11. Damage](#11-damage-block)
        - [12. Defense](#12-defense-block)
        - [13. Custom Identifications](#13-custom-identifications-block)
        - [14. Custom Consumable Type](#14-custom-consumable-type-block)
        - [15. Uses](#15-uses-block)
        - [16. Effects](#16-effects-block)
    - Mounts
        - [17. Mount Type](#17-mount-type-block)
        - [18. Mount Potential](#18-mount-potential-block)
        - [19. Mount Color](#19-mount-color-block)
        - [20. Mount Energy](#20-mount-energy-block)
        - [21. Mount Stats](#21-mount-stats-block)
        - [22. Mount Stats Max](#22-mount-stats-max-block)
            - [Mount Checks](#mount-checks)

## Wire Format
### Layers
Encoding transforms an item into a sequence of blocks, serializes the blocks into bytes, and converts those bytes into private-use Unicode characters for chat or Base64 for storage. Decoding reverses these transformations.

### Block Stream
An item is a sequence of blocks. Each block is a 1-byte id followed by its data.

```
┌───────┬───────┬───────────┬───────────┬───┬─────┐
│ Start │  Type │   Block   │   Block   │ … │ End │
│   §1  │   §2  │   §3–§22  │   §3–§22  │   │ §23 │
│ 00 vv │ 01 tt │ id  data… │ id  data… │   │  FF │
└───────┴───────┴───────────┴───────────┴───┴─────┘
```

| Rule | |
|------|-|
| Start block | Always first. Its data byte `vv` is the [version](#1-start-block). |
| Type block | Always present, anywhere after Start. Its data byte `tt` is the [item type](#2-type-block). |
| End block | Always the last byte. |
| Other blocks | Any order. Stat, skill, damage and defense entries may be in any order. Extended Identifications puts its pre-ID entries first; powder entries preserve slot order. Which blocks appear depends on the item type ([registry](#block-registry)). |
| Lengths | None. A block's size follows from its layout, so a decoder must know every block it meets. |
| Optional blocks | Optional blocks (⚪, 🟡) may be absent. Wynntils omits a ⚪ block when it would carry nothing, and always writes a 🟡 block, even when empty. |
| Required blocks | An item missing a required (🟢) block is invalid. |
| Unused blocks | Blocks the item type does not use are ignored, but still have to be read to reach the next block. |
| Duplicates | Each block appears at most once. Wynntils rejects a repeated block, except End, and Name in a crafted item or mount, which it discards. |
| After End | Nothing may follow. Readers should stop at End, as the loop below does. Wynntils instead reads blocks until the bytes run out, treating anything after End as more blocks. |
| Unknown ids | Wynntils rejects an unknown block id, stat id or enum value, except a class (read as None), a shiny stat or a mount color (read as unknown). It reads an Identifications mode other than `0x01` as normal, and a nonzero Mount Stats Max `estimated` byte as `0x01`. |
| Wynntils' order | gear `02 03 04 05 06` · tomes and charms `02 03 05` · crafted gear `07 08 09 0A 0B 0C 04` · crafted consumables `0D 0E 09 0F 0C` · mounts `10 11 12 13 14 15`. Start and Type precede these blocks, and End follows them. Blocks may be omitted as described below. |

A strict version 4 decoder can read the stream as follows:
```
expect 0x00                          Start block id
version ← read u8                    0x03 for version 4; reject anything else in a version-4-only reader
loop:
    id ← read u8
    if id = 0xFF:
        require no remaining bytes
        stop                        End block
    read block `id` using the layout for `version`
    an unknown id is fatal: without a length it cannot be skipped
reject a truncated block or a stream without End
validate duplicate and required blocks for the item type
```

### Block Registry
Every block, and which item types use it in version 4.

🟢 required · 🟡 optional, but Wynntils always writes it · ⚪ optional · blank: not used

| § | Id | Hex | Block | Gear | Tome | Charm | Crafted Gear | Crafted Consumable | Mount |
|---|----|-----|-------|:----:|:----:|:-----:|:------------:|:------------------:|:-----:|
| [1](#1-start-block) | 0 | `0x00` | Start | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| [2](#2-type-block) | 1 | `0x01` | Type | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| [3](#3-name-block) | 2 | `0x02` | Name | 🟢 | 🟢 | 🟢 | | | |
| [4](#4-identifications-block) | 3 | `0x03` | Identifications | ⚪ | ⚪ | ⚪ | | | |
| [5](#5-powder-block) | 4 | `0x04` | Powder | ⚪ | | | ⚪ | | |
| [6](#6-rerolls-block) | 5 | `0x05` | Rerolls | ⚪ | ⚪ | ⚪ | | | |
| [7](#7-shiny-block) | 6 | `0x06` | Shiny | ⚪ | | | | | |
| [8](#8-custom-gear-type-block) | 7 | `0x07` | Custom Gear Type | | | | 🟢 | | |
| [9](#9-durability-block) | 8 | `0x08` | Durability | | | | 🟢 | | |
| [10](#10-requirements-block) | 9 | `0x09` | Requirements | | | | 🟢 | 🟢 | |
| [11](#11-damage-block) | 10 | `0x0A` | Damage | | | | ⚪ | | |
| [12](#12-defense-block) | 11 | `0x0B` | Defense | | | | ⚪ | | |
| [13](#13-custom-identifications-block) | 12 | `0x0C` | Custom Identifications | | | | 🟡 | 🟡 | |
| [14](#14-custom-consumable-type-block) | 13 | `0x0D` | Custom Consumable Type | | | | | 🟢 | |
| [15](#15-uses-block) | 14 | `0x0E` | Uses | | | | | 🟢 | |
| [16](#16-effects-block) | 15 | `0x0F` | Effects | | | | | 🟡 | |
| [17](#17-mount-type-block) | 16 | `0x10` | Mount Type | | | | | | 🟢 |
| [18](#18-mount-potential-block) | 17 | `0x11` | Mount Potential | | | | | | 🟢 |
| [19](#19-mount-color-block) | 18 | `0x12` | Mount Color | | | | | | 🟡 |
| [20](#20-mount-energy-block) | 19 | `0x13` | Mount Energy | | | | | | 🟢 |
| [21](#21-mount-stats-block) | 20 | `0x14` | Mount Stats | | | | | | 🟢 |
| [22](#22-mount-stats-max-block) | 21 | `0x15` | Mount Stats Max | | | | | | 🟢 |
| [23](#23-end-block) | 255 | `0xFF` | End | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |

### Data Types
Every field in this document is one of these types.

| Type | Size | Encoding | Used for |
|------|------|----------|----------|
| `u8` | 1 byte | Unsigned, `0x00`–`0xFF` | Block ids, counts, enum values, small numbers |
| `varint` | 1–10 bytes | Zigzag (64-bit), then 7 bits per byte, lowest bits first; the high bit means "another byte follows" | Numbers that may be large or negative |
| `string` | length + 1 bytes | ASCII characters, then `0x00` | The Name block |
| bits | packed | Most significant bit first, padded to a whole byte with `0` bits | The Powder block |

Zigzag maps a signed integer `n` to `(n << 1) ^ (n >> 63)`. The result is written in groups of 7 bits, least significant group first, with the high bit set on every byte except the last.

### Elements
[Powder](#5-powder-block), [Requirements](#10-requirements-block), [Damage](#11-damage-block) and [Defense](#12-defense-block) refer to elements, and to the skills that go with them, by these ids.

| Id | Hex | Element | Skill |
|---:|-----|---------|-------|
| 0 | `0x00` | Earth | Strength |
| 1 | `0x01` | Thunder | Dexterity |
| 2 | `0x02` | Water | Intelligence |
| 3 | `0x03` | Fire | Defence |
| 4 | `0x04` | Air | Agility |
| 5 | `0x05` | Neutral (Damage only) | |

### Text Encoding
Bytes are turned into characters from the Unicode private use areas, two bytes per character.

| Bytes | Character | Range |
|-------|-----------|-------|
| `hi lo` (any pair except `FF FE` and `FF FF`) | `U+F0000 + (hi << 8) + lo` | `U+F0000`–`U+FFFFD` |
| `FF FE` | `U+100000` | |
| `FF FF` | `U+100001` | |
| a single trailing byte `b` (odd byte count) | `U+100000 + (b << 8) + 0xEE` | `U+1000EE`–`U+10FFEE` |

When decoding, a character at or above `U+100000` whose lowest byte is `0xEE` is a single padded byte, and `U+100000` and `U+100001` are the two special pairs. No other Area-B code point is ever written, so treat any other as invalid. A padded character can only be the last one.

**Base64.** For storage that never passes through chat, the same bytes are written as standard base64 ([RFC 4648](https://datatracker.ietf.org/doc/html/rfc4648#section-4), with `=` padding). Wynntils does this for its item record and build loadouts. Base64 has no name suffix, so a crafted item's or mount's name must be stored alongside it.

### Private Use Areas
The characters come from Unicode's two supplementary private use areas, which make up the last two planes of Unicode.

| Area | Plane | Code points |
|------|------:|-------------|
| [Supplementary Private Use Area-A](https://en.wikipedia.org/wiki/Supplementary_Private_Use_Area-A) | 15 | `U+F0000`–`U+FFFFD` |
| [Supplementary Private Use Area-B](https://en.wikipedia.org/wiki/Supplementary_Private_Use_Area-B) | 16 | `U+100000`–`U+10FFFD` |

How Wynntils Encoding fills each area:
```
Area-A · plane 15
┌──────────────────────────┬───────────────────┐
│    U+F0000 … U+FFFFD     │ U+FFFFE · U+FFFFF │
│ byte pairs 00 00 … FF FD │   noncharacters   │
└──────────────────────────┴───────────────────┘

Area-B · plane 16
┌────────────┬────────────┬───────────────────────────┬─────────────────────┐
│  U+100000  │  U+100001  │    U+1000EE … U+10FFEE    │ U+10FFFE · U+10FFFF │
│ pair FF FE │ pair FF FF │ one trailing byte 00 … FF │    noncharacters    │
└────────────┴────────────┴───────────────────────────┴─────────────────────┘
```
Each plane's last two code points are [noncharacters](https://www.unicode.org/faq/private_use.html#nonchar1), which is why `FF FE` and `FF FF` move to Area-B. Minecraft accepts every character in chat except `§`, control characters and DEL, so the string is sent unchanged.

### Sharing in Chat
In chat, the encoded string can be followed by the item's name as plain text:
```
┌──────────────────────┬───────┬───┬──────┬───┐
│    encoded string    │ space │ " │ name │ " │
│  Area-A/B chars × n  │  0x20 │   │ no " │   │
└──────────────────────┴───────┴───┴──────┴───┘
                       └────── optional ──────┘
```

| Item type | Where the name travels |
|-----------|------------------------|
| Gear, Tome, Charm | Inside the encoded data, in the [Name block](#3-name-block). Nothing follows the string. |
| Crafted Gear, Crafted Consumable, Mount | As plain text after the string, if the sender's **Share Item Name** option is on (the default). |

Exactly one space comes before the opening quote, and the name is at least one character and cannot contain a `"`. Plain text is used so that Wynncraft's chat filter checks the name. Nothing inside the encoded data is checked, which is why decoders ignore a Name block in a crafted item or mount. A name after a gear item, tome or charm is ignored.

Wynntils uses the name in a received chat message unchanged, since the chat filter has already passed it. Anywhere else it reads a plain-text name, it first:

| Step | Rule |
|------|------|
| 1 | Rejects the whole name if it contains any character other than `a`–`z`, `A`–`Z`, `0`–`9`, whitespace, and `'` `-` `.` `,` `!` `?` |
| 2 | Trims it and collapses runs of whitespace into a single space |
| 3 | Truncates it to 50 characters |

A decoder reading a name from anywhere Wynncraft's chat filter has not checked should apply the same steps. If there is no usable name, crafted items are named `Crafted` and their [gear type](#8-custom-gear-type-block) or [consumable type](#14-custom-consumable-type-block), and mounts are named after their [mount type](#17-mount-type-block).

**Length in chat.** The encoded characters are outside the Basic Multilingual Plane, so in UTF-16, which Java and Minecraft use, each one is a surrogate pair of two code units. Minecraft limits a chat message to 256 code units, so a message holds at most 128 encoded characters. A name suffix costs its length plus 3 units, for the space and quotes.

**Boundaries.** An item is one unbroken run of encoded characters, with no delimiters. Wynntils reads each run as a single item, so two items sent back to back must be separated by whitespace or other non-encoded text.

# 1. Start Block
Always the first block. Its data byte decides how every other block is read.

| Id | Used by | Size |
|----|---------|------|
| `0` · `0x00` | every item, first | 2 bytes |

## Layout
```
┌──────┬─────────┐
│ 0x00 │ version │
│  id  │    u8   │
└──────┴─────────┘
```

| Field | Type | Values |
|-------|------|--------|
| version | `u8` | `0x03` for version 4 (`VERSION_4` in Wynntils). |

# 2. Type Block
Says what kind of item this is, which decides the other blocks it has.

| Id | Used by | Size |
|----|---------|------|
| `1` · `0x01` | every item | 2 bytes |

## Layout
```
┌──────┬───────────┐
│ 0x01 │ item type │
│  id  │     u8    │
└──────┴───────────┘
```

## Item Types
| Id | Hex | Item type |
|----:|-----|-----------|
| 0 | `0x00` | Gear |
| 1 | `0x01` | Tome |
| 2 | `0x02` | Charm |
| 3 | `0x03` | Crafted Gear |
| 4 | `0x04` | Crafted Consumable |
| 5 | `0x05` | Mount |

The blocks each item type uses are listed in the [registry](#block-registry).

# 3. Name Block
The item's name, used to look up gear, tomes and charms in the item data.

| Id | Used by | Size |
|----|---------|------|
| `2` · `0x02` | Gear 🟢 · Tome 🟢 · Charm 🟢 | name length + 2 bytes |

## Layout
```
┌──────┬───────────┬────────────┐
│ 0x02 │    name   │    0x00    │
│  id  │ ASCII × n │ terminator │
└──────┴───────────┴────────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| name | `string` | The item's name. Wynntils puts it through the same steps as a plain-text name ([Sharing in Chat](#sharing-in-chat)), then looks it up by exact, case-sensitive match in its item data.[^1] An item whose name is not ASCII cannot be encoded. |

## Shiny and Ascended Names
Both are ordinary gear (`0x00`). Ascended gear uses `Ascended <name>` in the Name block. Shiny gear uses a [Shiny block](#7-shiny-block).

The Name block has to match an item data entry exactly, so encoders drop the `Shiny ` prefix. Only the Shiny block marks an item as shiny.

For ordinary gear whose own name contains `Shiny`, that word stays in the Name block.

An ascended mythic has its own entry, `Ascended <name>`, with a higher level requirement and its own stats. Its tooltip shows the plain name, so Wynntils detects it by level: when a mythic's level requirement differs from its entry's, it switches to `Ascended <name>` if that entry's level matches.

Decoders look up the Name block in Wynntils' item data to select the corresponding entry.

## Notes
- Crafted items and mounts do not use this block. Decoders must ignore it for item types `0x03`–`0x05` and take the name from chat instead ([Sharing in Chat](#sharing-in-chat)).
- Because of the plain-text name steps, Wynntils cannot decode an item whose name is longer than 50 characters. Names are truncated before lookup, so an entry whose full name exceeds this limit will not match.

# 4. Identifications Block
The rolled stats of a gear item, tome or charm.

| Id | Used by | Size |
|----|---------|------|
| `3` · `0x03` | Gear ⚪ · Tome ⚪ · Charm ⚪ | variable |

## Layout
```
┌──────┬───────┬──────┬─────────────────┬────────────────┬─────────┐
│ 0x03 │ count │ mode │   pre-ID count  │ pre-ID entries │ entries │
│  id  │   u8  │  u8  │ u8, mode 1 only │  mode 1 only   │ × count │
└──────┴───────┴──────┴─────────────────┴────────────────┴─────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| count | `u8` | Number of rolled stats, i.e. entries. Pre-identified (fixed) stats are not counted. |
| mode | `u8` | `0x00` normal · `0x01` extended. See below. |
| pre-ID count | `u8` | Extended mode only. Number of pre-identified stats. |
| pre-ID entries | | Extended mode only. One per pre-identified stat, before the rolled entries. |
| entries | | One per rolled stat. |

A stat is **pre-identified** when the item data[^1] gives it a single number instead of a `{min, max, raw}` range; that number is its base. A rolled stat's base is its `raw`.

**Modes.** Normal mode is shorter, and relies on the reader's item data for base values. Extended mode also writes the base values and the pre-identified stats, so the item keeps the stats it was shared with, even if it is later rebalanced.

Wynntils shares items in normal mode unless the sender ticks **Extended Encoding** in its sharing options. It always uses extended mode for the items it stores in base64: its item record and build loadouts.

| Mode | Hex | Pre-identified stats | Base values |
|------|-----|----------------------|-------------|
| Normal | `0x00` | Not written; readers restore them from their item data | Not written |
| Extended | `0x01` | Written, with their base | Written in every entry |

**Pre-ID entry** (extended mode):
```
┌─────────┬────────┐
│ stat id │  base  │
│    u8   │ varint │
└─────────┴────────┘
```

**Rolled entry**, normal mode:
```
┌─────────┬────────┬───────┬─────────┐
│ stat id │ value  │ meter │ perfect │
│    u8   │ varint │   u8  │    u8   │
└─────────┴────────┴───────┴─────────┘
```

**Rolled entry**, extended mode:
```
┌─────────┬────────┬────────┬───────┬─────────┐
│ stat id │  base  │ value  │ meter │ perfect │
│    u8   │ varint │ varint │   u8  │    u8   │
└─────────┴────────┴────────┴───────┴─────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| stat id | `u8` | Which stat ([Stat Ids](#stat-ids)). |
| base | `varint` | The stat's base value, as of sharing. |
| value | `varint` | The number shown on the tooltip, except for spell costs (below). |
| meter | `u8` | Numeric meter stage: `0` (zero progress) through `36` (`0x24`, full), with an implicit maximum of 36. `0xFF` means no meter. |
| perfect | `u8` | `0x01` for a perfect internal roll, `0x00` otherwise. Wynntils reads any nonzero value as true. |

Both meter and perfect bytes are always present for rolled entries. A missing meter (`CappedValue.EMPTY`) is written as `0xFF` and decoded back to `CappedValue.EMPTY`. For a present meter, Wynntils writes its current value directly, without rescaling it, and reads it with a maximum of 36. A zero-stage meter (`0x00`) is distinct from a missing meter. Values `0x25`–`0xFE` are outside the documented range; the decoder does not reject them, but encoders should not write them.

**Spell costs.** For the eight spell cost stats, `1stSpellCost`–`4thSpellCost` (`0x00`–`0x03`) and `raw1stSpellCost`–`raw4thSpellCost` (`0x25`–`0x28`), both the base and the value are written with the opposite sign to the item data and the tooltip. Readers flip the item data's base the same way before using it.

**Internal roll.** Each rolled stat has a hidden internal roll that sets its value: `base × roll ÷ 100`, rounded to a whole number, where a result of `0` becomes `1` or `−1` with the base's sign. Rolls run `30`–`130` for a positive base and `70`–`130` for a negative one, or `80`–`115` for the charm stats `leveledXpBonus` and `leveledLootBonus`. Rounding is half away from zero for a positive base and half toward zero for a negative one; for spell costs, use the sign-flipped base and swap the two rules. The perfect roll is the top of the range for a positive base and the bottom for a negative one. Several rolls can give the same value, so only the perfect flag says whether a roll is perfect.

## Stat Ids
Stat ids come from Wynntils' [`id_keys.json`](https://github.com/Wynntils/Static-Storage/blob/main/Reference/id_keys.json), which maps each stat's key to its id. Wynntils downloads it at runtime, and new ids are only ever appended. As of 2026-10-20 it holds 124 ids, `0x00`–`0x7B`.

The keys are the identification keys of Wynntils' item data.[^1] `elementalDefence` (`0x0E`) and `elementalDefense` (`0x0F`) are two spellings of the same stat. Wynntils writes only `0x0E` and rejects `0x0F` as an unknown stat id, so encoders must not write `0x0F`.

## Notes
- Omitted when there is nothing to write: in normal mode when every stat is pre-identified, in extended mode when the item has no stats.

# 5. Powder Block
The powder slots of a gear or crafted gear item, the powders in them, and its powder special.

| Id | Used by | Size |
|----|---------|------|
| `4` · `0x04` | Gear ⚪ · Crafted Gear ⚪ | 4 + ⌈6 × count ÷ 8⌉ bytes, plus 1 if a special is present |

## Layout
```
0x04 · slots:u8 · count:u8 · packed powders · special id:u8 · [special tier:u8]
```

| Field | Type | Description |
|-------|------|-------------|
| slots | `u8` | Number of powder slots. |
| count | `u8` | Number of powder entries. |
| packed powders | bits | 6 bits per powder, most significant bit first, padded with zero bits to a whole byte. |
| special id | `u8` | `0` for no powder special. Always present, even with zero powders. |
| special tier | `u8` | Present only when special id is nonzero. |

Wynntils writes the item's actual powder tiers and powder special for both gear and crafted gear. A powder is `element × 7 + tier`, with tier `1`–`7`. `0` means an empty slot; values `36`–`63` are unused. For a nonzero value, decode with `element = (value − 1) ÷ 7` (integer division) and `tier = (value − 1) mod 7 + 1`.

| Element | Id | Tier 1 | Tier 2 | Tier 3 | Tier 4 | Tier 5 | Tier 6 | Tier 7 |
|---------|---:|-------:|-------:|-------:|-------:|-------:|-------:|-------:|
| Earth | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
| Thunder | 1 | 8 | 9 | 10 | 11 | 12 | 13 | 14 |
| Water | 2 | 15 | 16 | 17 | 18 | 19 | 20 | 21 |
| Fire | 3 | 22 | 23 | 24 | 25 | 26 | 27 | 28 |
| Air | 4 | 29 | 30 | 31 | 32 | 33 | 34 | 35 |

Powder special ids and tiers are resolved through Wynntils' element data, using `PowderSpecialTierInfo.encodingId()` and `tier()`. These ids are separate from element ids. A nonzero special id must be followed by a tier, and the pair must identify a known special; otherwise decoding fails. See [`ElementModel`](../elements/ElementModel.java) for the lookup.

All specials accept tiers `1`–`7`:

| Id | Hex | Special | Item category |
|---:|-----|---------|---------------|
| 1 | `0x01` | Quake | Weapon |
| 2 | `0x02` | Rage | Armor |
| 3 | `0x03` | Chain Lightning | Weapon |
| 4 | `0x04` | Kill Streak | Armor |
| 5 | `0x05` | Curse | Weapon |
| 6 | `0x06` | Concentration | Armor |
| 7 | `0x07` | Courage | Weapon |
| 8 | `0x08` | Endurance | Armor |
| 9 | `0x09` | Wind Prison | Weapon |
| 10 | `0x0A` | Dodge | Armor |

## Notes
- Omitted when the item has no slots and no powders.
- If absent, the decoder uses zero slots, no powders and no special. Gear still has its powder-slot count in item data.
- Encoders should keep count ≤ slots. Wynntils consumes all packed bytes but only retains the first `min(count, slots)` entries, skipping zero entries.

# 6. Rerolls Block
How many times the item has been rerolled.

| Id | Used by | Size |
|----|---------|------|
| `5` · `0x05` | Gear ⚪ · Tome ⚪ · Charm ⚪ | 2 bytes |

## Layout
```
┌──────┬─────────┐
│ 0x05 │ rerolls │
│  id  │    u8   │
└──────┴─────────┘
```

## Notes
- Omitted when the item has not been rerolled.

# 7. Shiny Block
The item's shiny tracker: a counter of one statistic.

| Id | Used by | Size |
|----|---------|------|
| `6` · `0x06` | Gear ⚪ | 4+ bytes |

## Layout
```
┌──────┬───────────────┬─────────┬────────┐
│ 0x06 │ shiny stat id │ rerolls │ value  │
│  id  │       u8      │    u8   │ varint │
└──────┴───────────────┴─────────┴────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| shiny stat id | `u8` | What is being counted. `0x00` is reserved for unknown. |
| rerolls | `u8` | How many times the shiny stat has been rerolled. |
| value | `varint` | The count. |

**Shiny stats**, from Wynntils' [`shiny_stats.json`](https://github.com/Wynntils/Static-Storage/blob/main/Data-Storage/shiny_stats.json):

| Id | Hex | Shiny stat |
|---:|-----|------------|
| 1 | `0x01` | Mobs Killed |
| 2 | `0x02` | Raids Won |
| 3 | `0x03` | Chests Opened |
| 4 | `0x04` | Wars Won |
| 5 | `0x05` | Players Killed |
| 6 | `0x06` | Dungeons Won |
| 7 | `0x07` | Deaths |
| 8 | `0x08` | Lootruns Completed |
| 9 | `0x09` | Boss Altars Won |
| 10 | `0x0A` | Guild Raids Won |
| 11 | `0x0B` | World Events Won |
| 12 | `0x0C` | Major World Events Won |

# 8. Custom Gear Type Block
What kind of gear a crafted item is.

| Id | Used by | Size |
|----|---------|------|
| `7` · `0x07` | Crafted Gear 🟢 | 2 bytes |

## Layout
```
┌──────┬───────────┐
│ 0x07 │ gear type │
│  id  │     u8    │
└──────┴───────────┘
```

| Id | Hex | Gear type | | Id | Hex | Gear type |
|---:|-----|-----------|-|---:|-----|-----------|
| 0 | `0x00` | Spear | | 7 | `0x07` | Necklace |
| 1 | `0x01` | Wand | | 8 | `0x08` | Helmet |
| 2 | `0x02` | Dagger | | 9 | `0x09` | Chestplate |
| 3 | `0x03` | Bow | | 10 | `0x0A` | Leggings |
| 4 | `0x04` | Relik | | 11 | `0x0B` | Boots |
| 5 | `0x05` | Ring | | 12 | `0x0C` | Weapon (fallback) |
| 6 | `0x06` | Bracelet | | 13 | `0x0D` | Accessory (fallback) |

# 9. Durability Block
A crafted gear item's durability.

| Id | Used by | Size |
|----|---------|------|
| `8` · `0x08` | Crafted Gear 🟢 | 3+ bytes |

## Layout
```
┌──────┬────────┬─────────┐
│ 0x08 │  max   │ current │
│  id  │ varint │  varint │
└──────┴────────┴─────────┘
```

# 10. Requirements Block
A crafted item's level, class and skill point requirements.

| Id | Used by | Size |
|----|---------|------|
| `9` · `0x09` | Crafted Gear 🟢 · Crafted Consumable 🟢 | 4+ bytes |

## Layout
```
┌──────┬───────┬───────┬─────────────┬───────────────┐
│ 0x09 │ level │ class │ skill count │     skills    │
│  id  │   u8  │   u8  │      u8     │ × skill count │
└──────┴───────┴───────┴─────────────┴───────────────┘
```

**Skill entry:**
```
┌─────────┬────────┐
│ element │ points │
│    u8   │ varint │
└─────────┴────────┘
```
The skill is given by its [element](#elements).

**Classes:**

| Id | Hex | Class |
|---:|-----|-------|
| 0 | `0x00` | None |
| 1 | `0x01` | Mage |
| 2 | `0x02` | Archer |
| 3 | `0x03` | Warrior |
| 4 | `0x04` | Assassin |
| 5 | `0x05` | Shaman |

## Notes
- Crafted consumables only use the level, with no class and no skills.

# 11. Damage Block
A crafted weapon's damage.

| Id | Used by | Size |
|----|---------|------|
| `10` · `0x0A` | Crafted Gear ⚪ | variable |

## Layout
```
┌──────┬────────┬──────────────┬──────────────┬────────────────┐
│ 0x0A │  DPS   │ attack speed │ damage count │    damages     │
│  id  │ varint │      u8      │      u8      │ × damage count │
└──────┴────────┴──────────────┴──────────────┴────────────────┘
```

**Damage entry:**
```
┌─────────────┬────────┬────────┐
│ damage type │  min   │  max   │
│      u8     │ varint │ varint │
└─────────────┴────────┴────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| DPS | `varint` | Damage per second, as shown on the tooltip. |
| attack speed | `u8` | See below. |
| damage type | `u8` | An [element](#elements), or `0x05` for Neutral. |
| min, max | `varint` | The damage range. |

**Attack speeds:**

| Id | Hex | Attack speed |
|---:|-----|--------------|
| 0 | `0x00` | Super Fast |
| 1 | `0x01` | Very Fast |
| 2 | `0x02` | Fast |
| 3 | `0x03` | Normal |
| 4 | `0x04` | Slow |
| 5 | `0x05` | Very Slow |
| 6 | `0x06` | Super Slow |

## Notes
- Omitted for items with no DPS, attack speed or damages. When written, it needs an attack speed from the table above: Wynntils will not encode a Damage block without one, and rejects an unknown one.

# 12. Defense Block
A crafted item's health and elemental defenses.

| Id | Used by | Size |
|----|---------|------|
| `11` · `0x0B` | Crafted Gear ⚪ | variable |

## Layout
```
┌──────┬────────┬───────────────┬─────────────────┐
│ 0x0B │ health │ defense count │     defenses    │
│  id  │ varint │       u8      │ × defense count │
└──────┴────────┴───────────────┴─────────────────┘
```

**Defense entry:**
```
┌─────────┬─────────┐
│ element │ defense │
│    u8   │  varint │
└─────────┴─────────┘
```

## Notes
- Omitted for items with no health and no defenses.

# 13. Custom Identifications Block
A crafted item's stats. Unlike [Identifications](#4-identifications-block), there is no item data to look them up in, so every stat is written in full.

| Id | Used by | Size |
|----|---------|------|
| `12` · `0x0C` | Crafted Gear 🟡 · Crafted Consumable 🟡 | variable |

## Layout
```
┌──────┬───────┬─────────┐
│ 0x0C │ count │ entries │
│  id  │   u8  │ × count │
└──────┴───────┴─────────┘
```

**Entry** (the same as a normal-mode [Identifications](#4-identifications-block) entry, including its meter, perfect marker and spell cost sign):
```
┌─────────┬────────┬───────┬─────────┐
│ stat id │ value  │ meter │ perfect │
│    u8   │ varint │   u8  │    u8   │
└─────────┴────────┴───────┴─────────┘
```

## Notes
- Wynntils always writes this block for crafted items, with a count of `0` if there are no stats.

# 14. Custom Consumable Type Block
What kind of consumable a crafted item is.

| Id | Used by | Size |
|----|---------|------|
| `13` · `0x0D` | Crafted Consumable 🟢 | 2 bytes |

## Layout
```
┌──────┬─────────────────┐
│ 0x0D │ consumable type │
│  id  │        u8       │
└──────┴─────────────────┘
```

| Id | Hex | Consumable type |
|---:|-----|-----------------|
| 0 | `0x00` | Potion |
| 1 | `0x01` | Food |
| 2 | `0x02` | Scroll |
| 3 | `0x03` | Consumable (fallback) |

# 15. Uses Block
How many uses a crafted consumable has left.

| Id | Used by | Size |
|----|---------|------|
| `14` · `0x0E` | Crafted Consumable 🟢 | 3 bytes |

## Layout
```
┌──────┬───────────┬─────┐
│ 0x0E │ remaining │ max │
│  id  │     u8    │  u8 │
└──────┴───────────┴─────┘
```

# 16. Effects Block
A crafted consumable's effects.

| Id | Used by | Size |
|----|---------|------|
| `15` · `0x0F` | Crafted Consumable 🟡 | variable |

## Layout
```
┌──────┬───────┬─────────┐
│ 0x0F │ count │ effects │
│  id  │   u8  │ × count │
└──────┴───────┴─────────┘
```

**Effect entry:**
```
┌────────┬────────┐
│ effect │ value  │
│   u8   │ varint │
└────────┴────────┘
```

| Id | Hex | Effect |
|---:|-----|--------|
| 0 | `0x00` | Heal |
| 1 | `0x01` | Mana |
| 2 | `0x02` | Duration (seconds) |

## Notes
- Wynntils always writes this block for crafted consumables, with a count of `0` if there are no effects.

# 17. Mount Type Block
What kind of mount this is.

| Id | Used by | Size |
|----|---------|------|
| `16` · `0x10` | Mount 🟢 | 2 bytes |

## Layout
```
┌──────┬────────────┐
│ 0x10 │ mount type │
│  id  │     u8     │
└──────┴────────────┘
```

| Id | Hex | Mount type |
|---:|-----|------------|
| 0 | `0x00` | Horse |
| 1 | `0x01` | Wyvern |
| 2 | `0x02` | Adasaur |

# 18. Mount Potential Block
The mount's potential.

| Id | Used by | Size |
|----|---------|------|
| `17` · `0x11` | Mount 🟢 | 2+ bytes |

## Layout
```
┌──────┬───────────┐
│ 0x11 │ potential │
│  id  │   varint  │
└──────┴───────────┘
```

## Notes
- Tooltips show potentials of 1000 and more in thousands. Wynntils writes the displayed potential multiplied by 1000, although the real potential can be up to `99` higher ([Mount Checks](#mount-checks)).

# 19. Mount Color Block
The mount's two colors.

| Id | Used by | Size |
|----|---------|------|
| `18` · `0x12` | Mount 🟡 | 3 bytes |

## Layout
```
┌──────┬─────────┬───────────┐
│ 0x12 │ primary │ secondary │
│  id  │    u8   │     u8    │
└──────┴─────────┴───────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| primary, secondary | `u8` | Color ids from Wynntils' mount color data. `0x00` is reserved for unknown. |

Color ids come from Wynntils' [`mount_colors.json`](https://github.com/Wynntils/Static-Storage/blob/main/Data-Storage/mount_colors.json), which also lists the mount types and slots each color is allowed in. Wynntils downloads it at runtime. As of 2026-10-20 it holds 56 colors, `0x01`–`0x38`: each mount type allows 10 primary and 10 secondary colors, and four of them (Sable, Dusk, Ash and Rose) are each allowed in two slots.

## Notes
- Wynntils always writes this block for mounts, with `0x00` for a color it could not read.

# 20. Mount Energy Block
The mount's current and maximum energy.

| Id | Used by | Size |
|----|---------|------|
| `19` · `0x13` | Mount 🟢 | 3+ bytes |

## Layout
```
┌──────┬─────────┬────────┐
│ 0x13 │ current │  max   │
│  id  │  varint │ varint │
└──────┴─────────┴────────┘
```

# 21. Mount Stats Block
Each of the mount's stats, with its current value and cap.

| Id | Used by | Size |
|----|---------|------|
| `20` · `0x14` | Mount 🟢 | variable |

## Layout
```
┌──────┬───────┬─────────┐
│ 0x14 │ count │  stats  │
│  id  │   u8  │ × count │
└──────┴───────┴─────────┘
```

**Stat entry:**
```
┌──────┬─────────┬────────┐
│ stat │ current │  cap   │
│  u8  │  varint │ varint │
└──────┴─────────┴────────┘
```

The current value and cap describe the stat at its current training level. Its overall maximum is stored in [Mount Stats Max](#22-mount-stats-max-block).

| Id | Hex | Stat |
|---:|-----|------|
| 0 | `0x00` | Speed |
| 1 | `0x01` | Acceleration |
| 2 | `0x02` | Jump Height (not Wyverns) |
| 3 | `0x03` | Altitude (Wyverns only) |
| 4 | `0x04` | Energy |
| 5 | `0x05` | Handling |
| 6 | `0x06` | Toughness |
| 7 | `0x07` | Boost |
| 8 | `0x08` | Training |

# 22. Mount Stats Max Block
The highest value each of the mount's stats can reach.

| Id | Used by | Size |
|----|---------|------|
| `21` · `0x15` | Mount 🟢 | variable |

## Layout
```
┌──────┬────────────┬───────┬─────────┐
│ 0x15 │ estimated  │ count │  stats  │
│  id  │ u8, 0 or 1 │   u8  │ × count │
└──────┴────────────┴───────┴─────────┘
```

**Stat entry:**
```
┌──────┬────────┐
│ stat │  max   │
│  u8  │ varint │
└──────┴────────┘
```

| Field | Type | Description |
|-------|------|-------------|
| estimated | `u8` | `0x01` if the maximums were estimated. Tooltips do not always show a stat's maximum; when they do not, it is estimated from the stat's meter. |
| stat | `u8` | A stat id from [Mount Stats](#21-mount-stats-block). |
| max | `varint` | The stat's maximum. |

## Mount Checks
Wynntils rejects a mount unless:

- its potential is `0`–`80000`;
- [Mount Stats](#21-mount-stats-block) and Mount Stats Max each hold exactly the 8 stats that apply to its mount type;
- each current value and cap is `0`–`10000`, and each stat's current ≤ cap ≤ max;
- its [colors](#19-mount-color-block) are allowed for its mount type (`0x00`, unknown, is always allowed);
- its maximums fit its potential: below a potential of `1000` they add up to at most the potential; from `1000` ([Mount Potential](#18-mount-potential-block)), to between the potential and the potential + `99`. If the maximums are estimated, allow `50` more above, and from `1000` also `50` less below.

All ranges are inclusive.

# 23. End Block
Marks the end of the item.

| Id | Used by | Size |
|----|---------|------|
| `255` · `0xFF` | every item, last | 1 byte |

## Layout
```
┌──────┐
│ 0xFF │
│  id  │
└──────┘
```

## Notes
- Has no data, and must be the last byte. Wynntils rejects an item without one.
- Because `FF` is always the last byte, an item with an odd byte count always ends in the padded character `U+10FFEE` ([Text Encoding](#text-encoding)). With an even byte count, `FF` shares the last character with the byte before it, so `U+10FFEE` cannot be used to find where an item ends.

[^1]: Wynntils' item data: [`gear.json`](https://github.com/Wynntils/Static-Storage/blob/main/Reference/gear.json), [`tomes.json`](https://github.com/Wynntils/Static-Storage/blob/main/Reference/tomes.json) and [`charms.json`](https://github.com/Wynntils/Static-Storage/blob/main/Reference/charms.json).
