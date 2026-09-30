# Website

The source of [just-chatting.app](https://just-chatting.app), built by GitHub Pages with Jekyll.
It uses its own theme; there is no external theme to install.

## Layout

| Path | Contents |
|---|---|
| `index.md` | The home page's copy (hero, section titles), in its front matter. |
| `privacy.md` | The privacy policy. Other text pages can be added the same way, with `layout: page`. |
| `_data/features.yml` | The feature cards, and which screenshots they show. |
| `_data/devices.yml` | The devices in the "Fits any screen" section. |
| `_data/downloads.yml` | The download options. |
| `_layouts/` | `default` (the page shell), `home` (the home page) and `page` (text pages). |
| `_includes/` | Reusable pieces: cards, device frames, icons, the logo, the header and footer. |
| `assets/css/main.scss` | The styles, with the light and dark colors at the top. |
| `assets/screenshots/landing/` | The screenshots of the home page, generated from the app (see below). |
| `auth/`, `.well-known/` | The Twitch login callback and Android app links. Not part of the theme; leave as is. |

`assets/badges/` isn't used by the website, but by the repository's main README, which also shows
some of the screenshots from `assets/screenshots/landing/`: renaming or removing their tests
breaks it.

## Updating the screenshots

The screenshots of the home page are Compose screenshot tests, in
`app-android/src/screenshotTest/kotlin/fr/outadoc/justchatting/landing/`:

- `LandingScreenshotTest.kt`: one test per screenshot, each in light and dark themes.
- `LandingFixtures.kt`: the content they show, mirroring the app's demo mode.
- `LandingTheme.kt`, `LandingStatusBar.kt`: the theme, preview sizes and fake status bar.

After a UI change, regenerate them and copy them to the website, from the repository's root:

```sh
scripts/update-landing-screenshots.sh
```

This runs `./gradlew :app-android:updateDebugScreenshotTest`, then copies each image to
`docs/assets/screenshots/landing/`, named after its test:
`LandingDynamicColorsPokeScreenshotTest` in the dark theme becomes `dynamic-colors-poke-dark.png`.
Pass `--skip-gradle` to only copy the images the last run generated.

To use a screenshot on the website, refer to it by that name, without the `-light`/`-dark` suffix
(in `_data/features.yml` or `_data/devices.yml`); the page picks the one matching the visitor's
theme. To add one, add a test to `LandingScreenshotTest.kt` and run the script again.

## Building the site

GitHub Pages builds and deploys the site from `docs/` itself; there is no build step or workflow
to run for that.

To preview it locally, the pinned version of `github-pages` in `Gemfile.lock` needs Ruby 3, so the
simplest is to run it in a container, from the repository's root:

```sh
podman run --rm -p 4000:4000 -v "$PWD/docs":/site:Z -w /site \
  -v jc-jekyll-bundle:/bundle -e BUNDLE_PATH=/bundle \
  docker.io/library/ruby:3.3 \
  bash -c "bundle install && bundle exec jekyll serve --host 0.0.0.0"
```

Then open http://localhost:4000. The site is rebuilt whenever a file changes; changes to
`_config.yml` need a restart. The `jc-jekyll-bundle` volume keeps the installed gems between runs;
remove it with `podman volume rm jc-jekyll-bundle`. Docker works too, with `docker` instead of
`podman`, and without the `:Z` suffix.

With Ruby 3 installed locally, `bundle install` then `bundle exec jekyll serve` from `docs/` works
as well.
