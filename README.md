# UbiManager
<p align="center">
  <img src="https://github.com/asirrosa/UbiManager/assets/143890605/b5f77bce-1231-4655-b478-6e64722e23c2" width="500" height="500" />
</p>


## What Is UbiManager?
Hello there, UbiManager is a simple Android app with 2 functionalities:
- Save the current location of your phone, or search for a location and save it in the list of locations.
- Navigate to a specific location, it could be a saved one or you could search it in the map.

This app is written 100% in Java, and it uses some api services like [Mapbox Java SDK](https://docs.mapbox.com/android/java/guides/) (maps and the search engine) and [Geocoder](https://developer.android.com/reference/android/location/Geocoder) (get the name of the place from the latitude and longitude), it also uses [Sqlite](https://www.sqlite.org/index.html) in order to create a database inside the phone and save the info of the locations. 

Currently the apps language is spanish, although in the future the idea is to implement more languages, if you find it interesting you can build the application yourself using [Android Studio](https://developer.android.com/studio), or if you want to implement some of the code in your app feel free to use it, i tried to do it as easy and clean as possible so anyone can understand it (i know there are comments in spanish in the app, don't mind them).
