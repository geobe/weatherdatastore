# (Raspberry Pi) Weather Data Store
Retrieve and store a trace of weather and irradiation data to get a data base 
for analyzing stability and quality of forecasts. Data is stored in an H2 
database. Program architecture takes limited resources of a Raspberry Pi into 
account. For installing and running the project on a raspi, see my github
project [Java To Pi With Gradle and Idea](https://github.com/geobe/Java2PiWithIdea#java-to-pi-with-gradle-and-idea).

## Data Sources
German Weather Service (DWD - Deutscher Wetterdienst) provides an hourly forecast
for the next ten days for some 5000 weather stations as XML files in two forms.
MOSMIX_L is updated four times a day and contains more then 100 weather parameters.
MOSMIX_S is updated 24 times a day about 20 minutes after every hour.

![Persistent Data Model for tracked data](/src/main/resources/images/trackeddata.png)

