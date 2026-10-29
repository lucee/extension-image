# Lucee Image Extension

[![Java CI](https://github.com/lucee/extension-image/actions/workflows/main.yml/badge.svg)](https://github.com/lucee/extension-image/actions/workflows/main.yml)

Provides CFML Image functions for Lucee

Docs: https://docs.lucee.org/categories/image.html

Issues: https://luceeserver.atlassian.net/issues/?jql=labels%20%3D%20%22Image%22

## Versioning and releases

`pom.xml` holds the next version as a 4-part `-SNAPSHOT` (e.g. `3.1.0.13-SNAPSHOT`). It is not edited by hand.

* **Every push** deploys that `-SNAPSHOT` to the [Central snapshot repository](https://central.sonatype.com/repository/maven-snapshots/)
  and S3, then the workflow commits the next `-SNAPSHOT` (`Set version to ...`), so every commit gets its own build number.
* **Pull requests** only build and test, they never deploy.
* **Release**: run the workflow by hand (Actions > Run workflow) and pick a type:
  * `release` publishes `X.Y.Z.N` to Maven Central and S3
  * `rc`, `beta`, `alpha` publish `X.Y.Z.N-RC` / `-BETA` / `-ALPHA` to S3 only (signed `mvn install`, never Central)
  * `snapshot` deploys the current `-SNAPSHOT` without a bump

  `N` comes from the `-SNAPSHOT` in `pom.xml`; afterwards the workflow commits the next `-SNAPSHOT`.

## Image Format Support

- WEBP support is now read and write in v2

Dump out `imageFormats( true )` (v2 only) to see which formats / codecs are loaded and available

## ImageIO temp file cache

By default the extension switches the Java ImageIO disk cache off (`ImageIO.setUseCache( false )`), so reading and writing images uses memory and does not create temp files ([LDEV-3327](https://luceeserver.atlassian.net/browse/LDEV-3327)). The setting is applied once, before the first ImageIO call made by the extension.

`ImageIO.setUseCache` is **JVM wide**: it applies to everything running in the same JVM, not only to this extension, so every other library and web app on that JVM that uses ImageIO is affected as well.

To turn the cache back on, set the environment variable `LUCEE_IMAGE_USE_CACHE` or the system property `lucee.image.use.cache` to `true`. If both are set, the system property wins. Any other value keeps the cache off.

```shell
# environment variable
export LUCEE_IMAGE_USE_CACHE=true

# or system property
java -Dlucee.image.use.cache=true ...
```

The value is read once, when the extension first uses ImageIO, so a change needs a restart of the JVM.

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
