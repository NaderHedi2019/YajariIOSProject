# Installing Yajari App on iPhone

## Prerequisites
1. A Mac computer with Xcode installed
2. An Apple ID
3. An iPhone running iOS 14.0 or later
4. A USB cable to connect your iPhone to your Mac

## Steps to Install

1. **Open the Project**
   - Open Xcode
   - Open the `YajariProject/iosApp` folder
   - Wait for Xcode to index the project

2. **Configure Signing**
   - Click on the project in the navigator
   - Select the "iosApp" target
   - Go to the "Signing & Capabilities" tab
   - Check "Automatically manage signing"
   - Select your team (your Apple ID)
   - Update the Bundle Identifier to be unique (e.g., "com.yourname.yajari")

3. **Connect Your iPhone**
   - Connect your iPhone to your Mac using a USB cable
   - Trust the computer on your iPhone if prompted
   - In Xcode, select your iPhone from the device dropdown at the top

4. **Build and Run**
   - Click the Play button (▶️) in Xcode
   - Wait for the build to complete
   - The app will install on your iPhone

5. **Trust the Developer**
   - On your iPhone, go to Settings > General > Device Management
   - Find your Apple ID
   - Tap "Trust [Your Apple ID]"

## Troubleshooting

If you encounter any issues:

1. **Build Errors**
   - Clean the build folder (Xcode > Product > Clean Build Folder)
   - Delete derived data (Xcode > Preferences > Locations > Derived Data > Delete)
   - Try building again

2. **Signing Issues**
   - Make sure you're signed in to Xcode with your Apple ID
   - Check that your Apple ID has a valid developer account
   - Try regenerating the provisioning profile

3. **Installation Issues**
   - Make sure your iPhone is unlocked
   - Check that you've trusted the computer
   - Verify that your iPhone's iOS version is compatible

## Updating the App

To update the app after making changes:

1. Make your changes in Xcode
2. Click the Play button to build and run
3. The app will automatically update on your iPhone

## Notes

- The app will remain installed for 7 days before needing to be reinstalled
- You'll need to repeat the trust process if you reinstall the app
- Make sure to update the `baseURL` in `NetworkManager.swift` with your server's public IP 