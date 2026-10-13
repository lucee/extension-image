# Lucee Image Extension

[![Java CI](https://github.com/lucee/extension-image/actions/workflows/main.yml/badge.svg)](https://github.com/lucee/extension-image/actions/workflows/main.yml)

Provides CFML Image functions for Lucee

Docs: https://docs.lucee.org/categories/image.html

Issues: https://luceeserver.atlassian.net/issues/?jql=labels%20%3D%20%22Image%22

## Versioning and releases

Works like Lucee core (LDEV-6516): every code change gets its own version number. The version is not edited by hand.
`pom.xml` uses a CI-friendly `${revision}` built from `extension.version.base` (`3.1.0`), a build number and
`extension.version.qualifier` (`-SNAPSHOT`).

* **Every push to `master`** (a merged PR or a direct commit) takes the highest existing build of the base, from the git tags,
  Maven Central and the Central snapshot repository, adds 1, builds, tests, publishes that version (e.g. `3.1.0.12-SNAPSHOT`)
  to the [Central snapshot repository](https://central.sonatype.com/repository/maven-snapshots/) and creates the tag `3.1.0.12`.
  No `[release]` keyword and no "set version" commit is needed.
* Pushes run one after another (workflow concurrency queue), so two quick merges get two different numbers.
* PR builds, forks and runs without the deploy secrets build and test `3.1.0.0-SNAPSHOT` and never use up a number.
  A commit with `[skip ci]` in its message does not start a build, so it does not get a number either.
  Like Lucee core there is no exception for documentation-only changes: they get a new snapshot too.
* **Release candidate or final release**, either way gets the next number:
  * like Lucee core, commit a change of `extension.version.qualifier` in `pom.xml` to `-RC` (release candidate) or to empty
    (final release). That push publishes e.g. `3.1.0.13-RC` to Maven Central. Afterwards commit `-SNAPSHOT` back
    ("new cycle"), otherwise every following push publishes another RC.
  * or run the workflow by hand on `master` (Actions > Run workflow), tick `deploy` and pick `RC`, `BETA`, `ALPHA` or `release`
    as qualifier. That publishes the current code once, without a commit; the pom keeps `-SNAPSHOT`.
* Central keeps snapshots for a limited time only; the git tags keep the numbers unique after a snapshot was removed.
* To start a new line change `extension.version.base`.

## Image Format Support

- WEBP support is now read and write in v2

Dump out `imageFormats( true )` (v2 only) to see which formats / codecs are loaded and available

## Twelve Monkeys

Version 2.0 includes the [Twelve Monkeys](https://github.com/haraldk/TwelveMonkeys) ImageIO library

## Commercial Image Library support

Version 2.0 supports the following commercial Image Libaries (.jars ) when available in Lucee's classpath (i.e lucee's /lib dir)

- [JDeli](https://www.idrsolutions.com/jdeli/) 
- [Apose Imaging](https://products.aspose.com/imaging/java/) 

JDeli for example provides support the for HEIC format and write support for WEBP

## Build and Test

### Build

_General_ extension documentation is [here](https://docs.lucee.org/guides/working-with-source/building-and-testing-extensions.html).

As image tests sometimes require visual confirmation, many tests check for the environment variable `TEST_CLEANUP=FALSE`, which will leave the generated test artifacts in place for visual inspection in your local `lucee-server/context/temp/` directory

#### Mac/Linux

Set up a working directory for Lucee development and clone repositories:

```shell
# create a working directory for lucee development
LUCEE_DEV_BASEDIR=~/projects/lucee-dev
mkdir -p "$LUCEE_DEV_BASEDIR"

# clone lucee, or fork then clone
cd "$LUCEE_DEV_BASEDIR"
git clone https://github.com/lucee/Lucee.git lucee
cd lucee
git checkout 6.0

# clone script runner repo, or fork then clone
cd "$LUCEE_DEV_BASEDIR"
git clone https://github.com/lucee/script-runner.git 

# create a directory to house source for extensions
mkdir "$LUCEE_DEV_BASEDIR/extensions"

# clone this extension, or fork then clone
cd "$LUCEE_DEV_BASEDIR/extensions"
git clone https://github.com/lucee/extension-image.git 
```

This should yield a structure as follows:

```
└── lucee-dev
    ├── extensions
    │   └── extension-image
    ├── lucee
    └── script-runner
```

Run build/tests:

```shell
# from within the images's source root
cd ~/projects/lucee-dev/extensions/extension-image/
# build/test
./build.sh
```

#### Windows

See [general extension documentation](https://docs.lucee.org/guides/working-with-source/building-and-testing-extensions.html).

### Tests

Tests can be found in at least the following locations. (Look for files named with the pattern `Image*.cfc`.)

* [/tests](./tests/)
* https://github.com/lucee/Lucee/tree/6.0/test/functions
* https://github.com/lucee/Lucee/tree/6.0/test/general

## Troubleshooting

On a headless Linux server, you will get a `java.lang.reflect.InvocationTargetException` error if you don't have fonts installed

Please refer to [LDEV-2619 various image functions crash on headless linux when font packages aren't installed](https://luceeserver.atlassian.net/browse/LDEV-2619) for solution

## Codec support (v2)

Since Version 2, you can limit the image codecs being used via `this.image.coder=['Lucee','TwelveMonkeys'];` in your `Application.cfc`

Supported codecs are:

`["JDeli","Gotson","NightMonkeys","OpenizeHeic","Aspose","Lucee","TwelveMonkeys","ImageIO","ApacheImaging"]`

**NightMonkeys** (HEIF/HEIC/AVIF read, JPEG XL read) is bundled but only active on **Java 22+** with native libraries installed (`libheif`, `libjxl`) and JVM flag `--enable-native-access=ALL-UNNAMED`.

**OpenizeHeic** (HEIC/HEIF read, pure Java) is bundled but only active on **Java 11–21**. On Java 22+ it is skipped in favour of NightMonkeys. Note: Openize.HEIC uses HEVC/H.265 — see [Openize license](https://github.com/openize-com/openize-heic-java/blob/main/LICENSE) for patent considerations in commercial use.

To test and profile codec support and performance, there is a new function `ImageCoderInfo(image)`
