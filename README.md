# Lucee Image Extension

[![Java CI](https://github.com/lucee/extension-image/actions/workflows/main.yml/badge.svg)](https://github.com/lucee/extension-image/actions/workflows/main.yml)

Provides CFML Image functions for Lucee

Docs: https://docs.lucee.org/categories/image.html

Issues: https://luceeserver.atlassian.net/issues/?jql=labels%20%3D%20%22Image%22

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

As image tests sometimes require visual confirmation, many tests check for the environment variable `TEST_CLEANUP=FALSE`, which will leave the generated test artifacts in place for visual inspection in your local `lucee-server/content/temp` directory

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

`["JDeli","Aspose","Gotson","TwelveMonkeys","ImageIO","Lucee","ApacheImaging","JAI"]`

To test and profile codec support and performance, there is a new function `ImageCoderInfo(image)`
