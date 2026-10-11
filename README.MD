# Cagisk

[![Build](https://github.com/cvbyisme/Magisk/actions/workflows/build.yml/badge.svg)](https://github.com/cvbyisme/Magisk/actions/workflows/build.yml)

Cagisk is a **themed build of the Magisk manager app**: same root solution, same
modules, same Zygisk — with a manager you actually enjoy looking at.

It is an unofficial fork of [Magisk](https://github.com/topjohnwu/Magisk) by
John Wu, and it installs as `com.cvby.cagisk`.

#### This is not an officially supported Google Product

## What is different

- **Wallpaper** — pick any image, keep it as the backdrop of the whole app,
  with a blur slider (Android 12+) and a legibility scrim.
- **Custom accent** — choose one of the built-in swatches or let Cagisk derive
  the accent from your wallpaper. An explicit accent overrides the Android 12+
  system (Monet) palette so the color you pick is the color you get.
- **Interface opacity** — background and surface opacity sliders with
  *Opaque / Balanced / Translucent* presets, so dialogs, sheets, cards and the
  navigation bar stay consistent with the rest of the UI.
- **Own identity** — the app is renamed, re-iconed and moved to
  `com.cvby.cagisk`, so it can sit next to the official Magisk app.

No third party libraries were added for any of this: the wallpaper is decoded
with the framework decoder and the accent is extracted with the Material color
utilities the app already ships.

## Install

1. Grab the APK from the latest successful
   [Actions run](https://github.com/cvbyisme/Magisk/actions/workflows/build.yml)
   (artifact `cagisk-apk-<sha>`), or build it yourself (see below).
2. Install it. CI artifacts are **debug signed**, so they cannot update an
   official Magisk install — Cagisk is a separate package (`com.cvby.cagisk`)
   and is meant to be used on its own.
3. Open Cagisk and install/update Magisk from inside the app. Use the binaries
   that ship with this fork: `magiskd` looks up the manager app by package
   name, so the Cagisk app and the Cagisk binaries belong together.

> Do not run the official Magisk app and Cagisk against the same installation at
> the same time — each expects to be the manager of the daemon it was built for.

## Theme customization

Settings → *Customization* → **Theme customization**.

| Setting | What it does |
| --- | --- |
| Enable wallpaper | Shows the picked image behind the interface |
| Choose image | Imports an image into private app storage (nothing else can read it) |
| Wallpaper blur | 0–40 dp, Android 12 and above |
| Wallpaper scrim | Darkens/lightens the wallpaper so text stays readable |
| Accent color | Eight swatches; the first one keeps the default Magisk accent |
| Use wallpaper color | Derives the accent from the image (most chromatic hue wins) |
| Background / Surface opacity | Two sliders plus three presets; only active while a wallpaper is shown |

Values update live while you drag and are persisted when you let go.

## Build

Requirements are the same as upstream Magisk: JDK 25, the Android SDK/NDK
managed by `build.py`, Python 3 and the Rust toolchain.

```sh
# Native binaries + manager app (arm64 only via the CI config)
python build.py -v -c .github/ci.prop native
python build.py -v -c .github/ci.prop app
```

Outputs land in `out/` (`app-debug.apk`, `stub-debug.apk`). The bundled CI
workflow builds exactly this on every push that touches the app, the native
code or the build scripts.

## 中文说明

Cagisk 是 **Magisk 管理器应用的主题增强版**：root 方案、模块、Zygisk 全部照旧，
但界面终于好看了。

- **壁纸**：任意图片作全局底图，可调模糊（Android 12+）与遮罩，保证文字可读。
- **主色**：内置 8 个色块，或直接从壁纸取色；选了主色就覆盖 Android 12+ 的系统取色。
- **界面透明度**：背景与面板两条滑块 + 不透明/均衡/通透预设，对话框、卡片、导航栏
  一起变透明，不会出现「有的透有的不透」。
- **独立身份**：改名、换图标、包名换成 `com.cvby.cagisk`，可与官方 Magisk 共存。

安装：到 [Actions](https://github.com/cvbyisme/Magisk/actions/workflows/build.yml)
下载最新成功构建的 `cagisk-apk-<sha>` 产物安装即可（debug 签名，属独立版本）。
请配合本 fork 自带的 Magisk 二进制使用——`magiskd` 是按包名寻找管理器应用的。

零新增第三方依赖：壁纸用系统解码器，取色复用应用本来就带的 Material 颜色工具。

## Credits

- [Magisk](https://github.com/topjohnwu/Magisk) by John Wu — the upstream
  project this fork is based on. All the hard parts are his.
- The theme layer (`app/apk/src/main/java/com/topjohnwu/magisk/ui/theme`) is
  what this fork adds.

## License

Magisk is licensed under the **GPL-3.0**. Cagisk is a derivative work and stays
under the same license — see [LICENSE](LICENSE). If you redistribute it, keep
the source available and keep the license intact.
