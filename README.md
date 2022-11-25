# Lucee Image Extension

[![Java CI](https://github.com/lucee/extension-image/actions/workflows/main.yml/badge.svg)](https://github.com/lucee/extension-image/actions/workflows/main.yml)

Provides CFML Image functions for Lucee

Docs: https://docs.lucee.org/categories/image.html

Issues: https://luceeserver.atlassian.net/issues/?jql=labels%20%3D%20%22Image%22

## Twelve Monkeys

Version 2.0 includes the [Twelve Monkeys](https://github.com/haraldk/TwelveMonkeys) ImageIO library

## Commercial Image Library support

Version 2.0 supports the following commercial Image Libaries (.jars ) when available in the classpath (i.e /lib dir)

- [JDeli](https://www.idrsolutions.com/jdeli/) 
- [Apose Imaging](https://products.aspose.com/imaging/java/) 

JDeli for example provides support the for HEIC format

## Troubleshooting ##

On a headless Linux server, you will get a `java.lang.reflect.InvocationTargetException` error if you don't have fonts installed

Please refer to [LDEV-2619 various image functions crash on headless linux when font packages aren't installed](https://luceeserver.atlassian.net/browse/LDEV-2619) for solution
