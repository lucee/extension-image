# Changelog

## 3.0.1.2 (2026-03-26)

- [LDEV-5129](https://luceeserver.atlassian.net/browse/LDEV-5129) remove unused bundled jars: commons-io (CVE-2024-47554), xmpcore, apiguardian, hamcrest, opentest4j

## 3.0.1.1 (2026-03-19)

- [LDEV-6157](https://luceeserver.atlassian.net/browse/LDEV-6157) fix jakarta/javax compat — `<cfimage action="writeToBrowser">` threw `NoSuchMethodError` on Lucee 6 because `touchDestination()` bytecode was bound to jakarta servlet API. Use `eng.getClassUtil().callMethod()` reflection instead.
- CI: build once, test against Lucee 6.2 + 7.0 (stable + snapshot), Java 11 + 21. Deploy gated to master only.

## 3.0.1.0 (2026-03-09)

- Add javax servlet support — dual TLD entries (`tag-class` / `javax-tag-class`) and javax tag classes for Lucee 6 compat (per [LDEV-6120](https://luceeserver.atlassian.net/browse/LDEV-6120))
- **Note:** this release has a broken javax implementation, fixed in 3.0.1.1

## 3.0.0.9 (2026-01-29)

- [LDEV-3967](https://luceeserver.atlassian.net/browse/LDEV-3967) use TwelveMonkeys for image resizing

## 3.0.0.8 (2026-01-28)

- [LDEV-6082](https://luceeserver.atlassian.net/browse/LDEV-6082) avoid logging NPE when metadata is null

## 3.0.0.7 (2026-01-27)

- [LDEV-6081](https://luceeserver.atlassian.net/browse/LDEV-6081) fix resource leaks
- [LDEV-5731](https://luceeserver.atlassian.net/browse/LDEV-5731) make JaiCoder visible and improve logging
- [LDEV-5640](https://luceeserver.atlassian.net/browse/LDEV-5640) migrate to Jakarta EE (Lucee 7+)
- Remove JSPException usage
- Migrate from legacy nexus-staging to central-publishing plugin

## 2.0.0.33 (2026-01-28)

- [LDEV-6082](https://luceeserver.atlassian.net/browse/LDEV-6082) avoid logging NPE when metadata is null (backport)

## 2.0.0.32 (2026-01-27)

- [LDEV-6081](https://luceeserver.atlassian.net/browse/LDEV-6081) fix resource leaks
- [LDEV-5731](https://luceeserver.atlassian.net/browse/LDEV-5731) make JaiCoder visible and improve logging

## 2.0.0.29 (2025-02-25)

- [LDEV-5283](https://luceeserver.atlassian.net/browse/LDEV-5283) remove JAI dependencies
- [LDEV-5640](https://luceeserver.atlassian.net/browse/LDEV-5640) fix imagePaste regression by using local graphics object
- Add Java 21 to CI test matrix

## 2.0.0.26 (2025-01-29)

- [LDEV-5284](https://luceeserver.atlassian.net/browse/LDEV-5284) ImageWrite metadata improvements
- imageFilter flare: add centerX and centerY params
- Avoid NPE when imageFilter parameters is empty
- Add test cases from core

## 2.0.0.23 (2023-12-18)

- [LDEV-4455](https://luceeserver.atlassian.net/browse/LDEV-4455) improve exception for ImageSharpen() with gain argument
