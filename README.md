# Jar Goggle NG

Modern Java 21 desktop application for searching JAR/ZIP entry names.

---

## Origin & Acknowledgement

This project is an **independent reimplementation inspired by the original `JAR Goggle` utility**, created by:

**Daniel Destro do Carmo**

The original application introduced a simple and effective approach for locating classes and resources across collections of JAR files.

This project does **not include or reuse any original source code**. It is a clean, from-scratch implementation based solely on the underlying idea and observable behavior of the tool.

---

## Why Reimplement?

The original tool is useful but reflects an earlier era of Java desktop applications. This reimplementation was created to:

- Modernize the codebase for **Java 21**
- Provide a **more responsive UI** with background processing
- Add **search progress visibility and cancellation**
- Support **iterative workflows** (refining or expanding previous searches)
- Introduce **result set operations** (union, intersect, subtract)
- Improve usability with **tabbed results and structured views**

The goal is to preserve the usefulness of the original concept while making it more suitable for current development environments and workflows.

---

## Features

- Java 21 + Maven
- Modernized Swing desktop UI
- Search modes:
  - substring
  - exact path/class
  - wildcard/glob
- Search scopes:
  - all roots
  - current result set only
  - current result set + roots
- Result operations:
  - replace
  - intersect
  - union
  - subtract
- Progress bar with cancel support
- Result tabs with archive → entry tree view
- Persisted last opened paths
- Runnable shaded JAR

---

## Build

```bash
mvn clean package
````

---

## Run

```bash
java -jar target/jar-goggle-ng-2.0.0.jar
```

On Windows, the shaded JAR can typically be double-clicked if `.jar` files are associated with Java.

---



## Disclaimer

This project is not affiliated with or endorsed by the original JarGoggle authors. It is a standalone implementation inspired by the same core idea.

```
