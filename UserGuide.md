# User Guide

## Overview

JAR Goggle NG is a desktop utility for **searching inside `.jar` and `.zip` archives**. It scans archive *entry names* (e.g., class paths, resources) and helps you:

* Locate where a class exists across many JARs
* Explore archive contents quickly
* Iteratively refine searches using previous results

It does **not** decompile bytecode or inspect method bodies.

---

## Key Features

* Multiple search modes: **Substring, Exact, Glob**
* Search scopes:

  * All configured roots
  * Current results only
  * Results + roots
* Result operations:

  * Replace
  * Intersect
  * Union
  * Subtract
* Progress tracking with **cancel**
* **Tabbed results**
* Tree view: **Archive → Matching entries**
* Persistent **last-used paths**

---

## Installation & Running

### Requirements

* Java 21 installed
* Windows/macOS/Linux

### Build from source

```bash
mvn clean package
```

### Run

```bash
java -jar target/jar-goggle-ng-2.0.0.jar
```

On Windows, you can typically **double-click the JAR**.

---

## Interface Overview

### Main Components

* **Search Bar**: Enter class name or path
* **Search Mode Selector**: Choose matching behavior
* **Scope Selector**: Choose where to search
* **Action Selector**: Define how results combine
* **Progress Bar**: Shows search progress
* **Cancel Button**: Stops the search
* **Result Tabs**: Each search produces a new tab
* **Tree View**:

  * Top level: archive file
  * Children: matching entries

---

## Adding Search Locations

You must define **roots** (directories or archives).

Typical workflow:

1. Click **Add Folder** or **Add File**
2. Select:

   * Folder(s) containing JARs
   * Individual `.jar` / `.zip` files
3. Paths are saved automatically for next launch

---

## Performing a Search

### Step-by-step

1. Enter a query in the search box
   Example:

   ```
   org.apache.commons.lang3.StringUtils
   ```

2. Choose a **Search Mode**

3. Choose a **Scope**

4. Choose a **Result Operation**

5. Click **Search**

A new result tab will appear when complete.

---

## Search Modes

### 1. Substring

Matches if the entry name contains the query.

Example:

```
Query: StringUtils
Matches: org/apache/.../StringUtils.class
```

---

### 2. Exact

Matches exact path or class.

* Automatically supports `.class` suffix

Example:

```
Query: org/apache/commons/lang3/StringUtils
Matches:
- org/apache/commons/lang3/StringUtils.class
```

---

### 3. Glob (Wildcard)

Uses standard glob patterns:

| Pattern | Meaning          |
| ------- | ---------------- |
| `*`     | any characters   |
| `?`     | single character |

Example:

```
org/apache/**/String*.class
```

---

## Search Scope

### All Roots

Search everything you configured.

---

### Current Result Set Only

Search only within archives found in the active tab.

Use this to **narrow results**.

---

### Results + Roots

Search both:

* existing results
* full root set

Use this to **expand search context**.

---

## Result Operations

These define how new results combine with the current tab.

### Replace

Discard old results and show only new ones.

---

### Intersect

Keep only archives present in both:

* current results
* new search

---

### Union

Combine both result sets.

---

### Subtract

Remove archives found in new search from current results.

---

## Progress & Cancellation

* Progress bar shows overall progress
* Click **Cancel** to stop the search early
* Partial results may still appear

---

## Working with Results

### Tabs

Each search creates a new tab:

* Tabs are independent
* You can switch between them freely

---

### Tree View

Structure:

```
archive.jar
 ├── com/example/Foo.class
 ├── META-INF/MANIFEST.MF
```

---

### Typical Workflow Example

1. Search:

   ```
   org.springframework
   ```

   → many results

2. Narrow:

   * Scope: Current Results
   * Query:

     ```
     context
     ```

3. Intersect:
   → finds archives containing both

---

## Tips

* Use **Substring** for exploration
* Use **Exact** for precise class lookup
* Use **Glob** for structured patterns
* Convert class names mentally:

  ```
  java.lang.String → java/lang/String
  ```

  (the app handles this automatically)

---

## Limitations

* Does not inspect:

  * method bodies
  * bytecode
  * embedded strings
* Searches only **entry names**
* Very large directory trees may take time

---

## Troubleshooting

### App doesn’t open

* Ensure Java 21 is installed:

  ```bash
  java -version
  ```

### Double-click doesn’t work (Windows)

* Associate `.jar` with Java:

  ```
  Open with → Java Platform SE Binary
  ```

### No results found

* Check:

  * search mode
  * correct path format
  * roots include correct directories

---

## Future Enhancements (Planned)

* Export results (CSV/JSON)
* Per-tab filtering
* Archive preview
* Text search inside files
* Parallel scanning improvements

---

## Summary

JAR Goggle NG is optimized for:

* Fast archive scanning
* Iterative refinement
* Clear visibility into where classes live

Use result tabs + set operations to progressively narrow or expand your search space efficiently.
