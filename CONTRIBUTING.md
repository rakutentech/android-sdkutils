# Contributing
## Found a Bug?
If you find a bug in the source code, you can help us by submitting an issue to our GitHub Repository. Even better, you can submit a Pull Request with a fix.

## Pull Requests
1. Fork the project
2. Implement feature/fix bug & add test cases
3. Ensure test cases & static analysis runs successfully - run `./gradlew check`
4. Submit a pull request to `master` branch

Please include unit tests where necessary to cover any functionality that is introduced.

## Coding Guidelines
* All features or bug fixes **must be tested** by one or more unit tests/specs
* All public API methods **must be documented** in the KDoc/JavaDoc and potentially in the user guide.
* All Kotlin code must follow [Kotlin's Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).
* All Java code must follow [Google's Java Code style](https://google.github.io/styleguide/javaguide.html), the only exception being that annotations on members or classes may be on the same line (no forced line break).

## Commit messages
Each commit message consists of a header, and an optional body and footer. The header has a special format that includes a type, a subject and an optional ticket number:

```
<type>: <subject> (<ticket-no>)
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory but the `<ticket-no>` part of the header is optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier
to read on GitHub as well as in various git tools.

Footer should contain a [closing reference to an issue](https://help.github.com/articles/closing-issues-via-commit-messages/) if any.

### Revert
If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### Type
Must be one of the following:

* **fix**: A bug fix
* **feat**: A new feature, new UI
* **refactor**: (or **improve:**) A code change that neither fixes a bug nor adds a feature (incl. Swift/Kotlin migration)
* **build**: Changes that affect the build system or external dependencies (example scopes: gradle, fastlane, npm)
* **ci**: Changes to our CI configuration files and scripts (Jenkins, Bitrise, Circle CI, SonarCloud, Mobscansf, etc)
* **docs**: Documentation only changes
* **chore**: Changes that do not affect the meaning of the code (formatting/linting, etc). Changes as a part of routine tasks like **Pre/Post release operation** (version bump + CHANGELOG, merge fix, etc).
* **test(s)**: Adding missing tests or correcting existing tests

### Subject
The subject contains a succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* don't capitalize first letter
* no dot (.) at the end
* an issue reference e.g. SDK-1234 can be added to the end of the subject in parentheses

### Body
Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

### Footer
The footer should reference GitHub issues that this commit **Closes**.

### Examples
#### Bad examples:
* _**Pull request #519:**_ improve: refactor user data handling (SDKCF-3970)
<br>Remove default prefixes added by Bitbucket.

* improve: refactor user data handling (SDKCF-3970) _**(#23)**_
<br>Remove default suffixes added by Github.

* _**Revert "refactor user data handling"**_
<br>Revert should start with `revert:` followed by exact original commit message of reverted commit (without quotes).

* fix: _**fix crash on Android 12**_ (SDKCF-0000)
<br>Commit message should contain some context. In this case - a cause of the crash.

* feat: _**add new build configuration for users that require user data to be stored in another directory and add migration logic with exception of users migration from SDK version 1.0.0**_
<br>Try to fit within 100 characters limit. Use abbreviations if necessary. Focus on the most important change.

#### Good examples:
* improve: refactor user data handling (SDKCF-3970)
* improve: refine URL session configuration, add timeouts (SDKCF-0000)
* docs: update README with gradle information (SDKCF-4388)
* chore: bump RSDKUtils to 2.1.0 (SDKCF-4392)
* chore: prepare 5.0.0 release (SDKCF-4160)
* revert: improve: refine URL session configuration, add timeouts (SDKCF-0000)
<br>This reverts commit 6608beb114610ec0a5e96b77e62a5590753e247e.
* feat: add support for id tracking (SDKCF-4072)
* ci: add mobsfscan config (SDKCF-0000)
* tests: add UI tests (SDKCF-2246)
* tests: fix and refactor integration tests (SDKCF-0000)
* fix: fix startup crash caused by wrong build config (SDKCF-0000)
