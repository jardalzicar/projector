# Projector

> Smart personal project management from the command line.

Projector is a small Java command-line tool that creates and manages personal projects, with a focus on hardware and software development. One command creates a project folder with a README, a TODO list, a part list, Eagle schematic files and a local Git repository. Further commands keep the project's metadata up to date and save its history to Git.

## Contents

- [Features](#features)
- [Requirements](#requirements)
- [Building](#building)
- [Installation](#installation)
- [Usage](#usage)
- [Generated project](#generated-project)
- [Repository layout](#repository-layout)
- [Development](#development)
- [Known limitations](#known-limitations)
- [Roadmap](#roadmap)
- [Author](#author)

## Features

- **Project scaffolding.** Answer a few questions and get a ready-to-use project folder.
- **Optional components.** Pick only what you need:

  | Component       | Created files                                      |
  | --------------- | -------------------------------------------------- |
  | README          | `README.md`, filled in with the project metadata   |
  | TODO list       | `TODO.md`                                          |
  | Part list       | `part_list/part_list.numbers` (Apple Numbers)      |
  | Eagle project   | `eagle/eagle.epf`, `eagle/<project_name>.sch`      |
  | Git repository  | `.git/`, with an initial commit                    |

- **Metadata tracking.** Name, description, author, version, state and dates are stored in `config.json`. The README and TODO list are updated whenever they change.
- **Git integration.** Save the project or release a new version with a single command, without leaving Projector.

## Requirements

| Purpose          | Requirement                                                                 |
| ---------------- | --------------------------------------------------------------------------- |
| Running          | Java 25 or newer (JRE)                                                      |
| Building         | JDK 25 or newer (the Gradle 9.8.2 wrapper is included)                      |
| Operating system | macOS or Linux. Windows is not supported yet.                               |

Git does not need to be installed. Projector uses the bundled [JGit](https://www.eclipse.org/jgit/) library.

## Building

Projector is built with the Gradle wrapper in `projector_v1/`. The `jar` task produces a single self-contained ("fat") jar that includes all dependencies.

```bash
cd projector_v1
./gradlew jar
```

If your default JDK is older than 25, point `JAVA_HOME` at a JDK 25 installation first, for example `JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew jar` on macOS.

The jar is written to `projector_v1/build/libs/projector_v1-1.0-SNAPSHOT.jar`.

To try Projector without installing it, `./gradlew run` builds it and runs `create-test` in `projector_v1/`. The resulting `test_project/` folder is ignored by Git.

## Installation

The launcher script `bin/projector/projector` expects the jar at `/usr/local/projector/projector_v1.jar`.

1. Copy the launcher directory into place:

   ```bash
   sudo cp -R bin/projector /usr/local/projector
   ```

2. Copy the built jar next to it:

   ```bash
   sudo cp projector_v1/build/libs/projector_v1-1.0-SNAPSHOT.jar /usr/local/projector/projector_v1.jar
   ```

3. Make the `projector` command available on your `PATH`:

   ```bash
   sudo ln -s /usr/local/projector/projector /usr/local/bin/projector
   ```

4. Check the installation:

   ```bash
   projector -v
   ```

## Usage

```
projector <command> [arguments]
```

`create` and `create-test` make a new project folder inside the current directory. Every other project command must be run **from inside a project folder**, the one that contains `config.json`.

| Command                        | Description                                                                 |
| ------------------------------ | --------------------------------------------------------------------------- |
| `create`                       | Interactive wizard: asks for name, description, author and components       |
| `create-test`                  | Creates a demo project called *Test project* with every component           |
| `info`                         | Prints the project's metadata and components                                |
| `save [message]`               | Commits all changes to Git (default message: `User saved`)                  |
| `version <version>`            | Sets a new version number and commits it as `New version: <version>`        |
| `change <property> <value>`    | Changes `name`, `description`, `author` or `state`                          |
| `-h`                           | Shows help                                                                  |
| `-v`                           | Shows the program version                                                   |

`save` and `version` need the Git repository component. Quote values that contain spaces.

### Example session

```bash
projector create
```

```
Enter project name
LED Matrix
Enter project description
8x8 LED matrix driver board
Enter author name
Jaroslav Lzicar
Do you want to create MS Excel part list? Type "Y" for yes, "N" for no.
Y
...
Project created successfully!
```

```bash
cd led_matrix
projector change state "in progress"
projector save "Finished schematic"
projector version 0.1.0
projector info
```

```
Project opened successfully!

PROJECT LED Matrix
===============================
Description: 8x8 LED matrix driver board
Version: 0.1.0
Author: Jaroslav Lzicar
Date created: 28.10.2026
Date last saved: 28.10.2026
State: in progress
Components:
   ExcelPartList: part_list/part_list.numbers
   ReadMe: README.md
   TodoList: TODO.md
   GitRepo: true
   Eagle: eagle/eagle.epf
```

## Generated project

The project folder name is the project name in lower case, with whitespace replaced by underscores. For example, *LED Matrix* becomes `led_matrix/`.

```
led_matrix/
├── .git/
├── config.json
├── README.md
├── TODO.md
├── eagle/
│   ├── eagle.epf
│   └── led_matrix.sch
└── part_list/
    └── part_list.numbers
```

### `config.json`

`config.json` holds the project's metadata and is the file that marks a folder as a Projector project. Dates use the `dd.MM.yyyy` format. New projects start at version `0.0.1` with the state `created`. The state can be any text.

```json
{
  "name": "LED Matrix",
  "description": "8x8 LED matrix driver board",
  "author": "Jaroslav Lzicar",
  "version": "0.1.0",
  "state": "in progress",
  "dateCreated": "28.10.2026",
  "dateLastSaved": "28.10.2026",
  "ReadMe": "README.md",
  "TodoList": "TODO.md",
  "ExcelPartList": "part_list/part_list.numbers",
  "Eagle": "eagle/eagle.epf",
  "GitRepo": "true"
}
```

Each component key is present only if that component was chosen. Its value is the component's file path, or `"true"` for the Git repository. Component paths are fixed: Projector refuses to open a project whose config lists a different path.

## Repository layout

```
projector/
├── bin/projector/projector        # Launcher script (bash)
└── projector_v1/                  # Gradle project
    ├── build.gradle
    └── src/main/
        ├── java/
        │   ├── Controller.java    # Entry point and command dispatch
        │   ├── UserInterface.java # Help, version and Y/N prompts
        │   ├── Project.java       # Creates, opens and updates projects
        │   ├── Component.java     # Base class for all project components
        │   ├── Config.java        # config.json reading and writing
        │   ├── Readme.java        # README.md component
        │   ├── TodoList.java      # TODO.md component
        │   ├── ExcelPartList.java # Part list component
        │   ├── Eagle.java         # Eagle project component
        │   └── GitRepository.java # Git repository component (JGit)
        └── resources/             # Templates copied into new projects
```

### Architecture

`Controller` parses the command line and creates a `Project`. A project owns a list of `Component`s. Each component knows its default path and creates its own files (`createFiles`). It fills them in from a template in `resources/` (`initFiles`). When metadata changes, `Project` saves `config.json` and tells the README and TODO components to update their text.

To add a component:

1. Subclass `Component`, set `defaultPath` and `componentName`, and implement `initFiles()`.
2. Add a prompt for it in `Controller.createNewProject()`.
3. Add it to the list in `Project.applyJSONConfig()` so existing projects can load it.

### Dependencies

| Library                                                         | Version | Used for                  |
| --------------------------------------------------------------- | ------- | ------------------------- |
| [Apache POI](https://poi.apache.org/) (`poi-ooxml`)             | 6.0.0   | Excel part list (planned) |
| [json-simple](https://github.com/fangyidong/json-simple)        | 1.1.1   | `config.json`             |
| [JGit](https://www.eclipse.org/jgit/)                           | 7.8.1   | Git repository            |
| [SLF4J](https://www.slf4j.org/) (`slf4j-nop`)                   | 2.0.18  | Silences JGit's logging   |

## Development

```bash
cd projector_v1
./gradlew build    # compile and package
./gradlew run      # run create-test in projector_v1/
```

There are no automated tests yet. JUnit 4.13.2 is already set up as a test dependency, so tests can go into `src/test/java/`.

## Known limitations

- `change name` updates the metadata, README and TODO list, but does not rename the project folder.
- `version` commits the new version but does not create a Git tag.
- `save` updates the *last saved* date after committing. Saving on a different day therefore leaves `config.json` and `README.md` with uncommitted changes.
- Values containing `$` or `\` make `change` fail when it updates the README or TODO list.
- Running a project command outside a project folder leaves an empty `config.json` behind.
- The part list is copied from an Apple Numbers template, despite being called "MS Excel" in the wizard.
- `-h` lists an `open` command that does not exist and leaves out several commands. `clone` is reserved but not implemented.

## Roadmap

- Clean up the files left behind when opening a project fails
- Git tags for versions
- Configurable Eagle file names
- Graphical user interface
- Windows support

## Author

Jaroslav Lzicar
