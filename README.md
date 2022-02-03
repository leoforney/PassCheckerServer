#PassCheckerServer

##Description
This project was made for my (High School) senior design project. We designed, implemented, and tested a system that would scan license plates in the parking lot using the companion Android app. This app then streamed the data over an HTTP post request to the server. This project (the server) would then take the input, scan for the plate using openalpr, check the license plate in a MongoDB database, then return if the license plate was parked without a permit. It was very rough and can easily be re-done today using more efficient technology.

##
Pre-requisites:
OpenALPR installed on machine (Windows only): https://github.com/openalpr/openalpr/releases[https://github.com/openalpr/openalpr/releases]
MongoDB Server. Set configuration/params in Spring boot application file before running

To run this project, run

`gradle build` or `./gradlew build` for windows

After that, run the jar file in `build/out`
