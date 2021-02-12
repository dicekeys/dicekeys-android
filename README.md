
# DiceKeys for Android

[![Unit Tests](https://github.com/dicekeys/dicekeys-android/workflows/Unit%20Tests/badge.svg)](https://github.com/dicekeys/dicekeys-android/actions)

```
git clone --recursive https://github.com/dicekeys/dicekeys-android.git
```

### Prerequisites Install CMake and Ninja

#### MacOS

```
brew install cmake
brew install ninja
```

If you haven't already installed Android Studio and the SDK

```
bash scripts/install-android-studio-and-ndk.sh
```

ANDROID_HOME should be set to $HOME/Library/Android/sdk

#### Windows
```
 1. Download and install CMake version >= 3.15.0. https://cmake.org/download/
 1. Download ninja-build. https://github.com/ninja-build/ninja/releases and put it on your PATH
 1. Install Android Studio and the Android SDK in it
 1. set enivironment variable ANDROID_HOME to to $HOME/AppData/Local/Android/Sdk (may not actually be necessary, since I succeeded without doing this on 2020-2-11 - Stuart)

```

## UI Widgets

### DieFaceView
`DieFaceView` uses DieFaceUpright drawable which contains 2 Undoverline drawables.

### Dice Views
`DiceBaseView` is base class for dice views. There are several types of dice views:
* `DiceKeyView`
* `StickerSheetView`
* `StickerTargetSheetView`

### Dice Views Helper Layouts
`TwoDiceViewLayout` is child class of `LinearLayout`. It displays relations between dice views. `TwoDiceViewLayout` has 2 dice properties:
* sourceDiceView
* targetDiceView

When you set both references to sourceDiceView and targetDiceView it will display a connection line of highlighted items with a "hand with a sticker". If you set reference to targetDiceView only it will display a "hand with a sticker" only at highlighted item.

### Other important classes
`DiceSizeModel` describes layouts, sizes, proportions of dice views.