git log  --pretty=oneline | tail -n 10 >> log.txt
./gradlew clean
./gradlew assembleRelease
#./gradlew bundleRelease
firebase appdistribution:distribute app/release/app-release.apk  \
--app 1:174656673319:android:23f81df8e8d7885646e002  \
--release-notes-file ./log.txt --groups go