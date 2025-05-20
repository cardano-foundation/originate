## Hybrid Scanning App with Ionic/Capacitor

#### Development Server (opens on http://localhost:3003)
```
  nvm use 18
  npm i
  npm run dev
```

Incase of fatal error: 'vips/vips8' file not found:
```
  brew info vips
  brew reinstall vips
  brew install pkg-config glib zlib
  brew install libjpeg-turbo libpng webp
```

#### Mobile
Incase cocoapods is not installed:
```
    brew install cocoapods
```

To build and run on both platforms:
```
  npm run build:cap
  npx cap run ios
  npx cap run android
```

Or open XCode or Android Studio (for debugging):
```
  npm run build:cap
  npx cap open ios
  npx cap open android
```

#### For release
To build and release app:
Android: generate signed apk (select build variants Release)
iOS: incase archive failed: 
  open xcode -> Pods -> Target Support Files -> Pods-App -> Pods-App-frameworks
  change line 44(add -f) source="$(readlink "${source}")" -> source="$(readlink -f "${source}")"