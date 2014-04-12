GPS data overlay on videos
==========================

The idea came after I bought a [GoPro](http://gopro.com/cameras/hd-hero3-silver-edition) camera and started to record
some of my rides which were also tracked with a GPS device.
For tracking the rides I was using the [Strava](http://www.strava.com) application on my phone
or a [Garmin Edge 510](http://www.dcrainmaker.com/2013/01/garmin-edge-510-in-depth-review.html).
After showing the video to friends I was asked why I'm not showing the speed and altitude information overlaid onto video?
Cool, great idea, I wanted to add all kind of telemetry data acquired via the GPS onto the video, but couldn't find a
software for it, running on my Mac. So I decided the write one :)

## Basic requirements
Small list of wishes to achieve:
* show the video file and the track on the map to help synchronizing (shifting) the gps data to the beginning of the video
* add various gauges (drag'n drop into the video panel) like: speed, cadence, heart rate, altitude, acceleration, temperature, etc.
* once the gauges were added and adjusted (the size and position) allow to save the configuration as a template, so next time
it is easier to just load the template and create the new video
* and the final bit, export to newly created video with the gauges

## Credits
The application is using libraries and images created by other people or organizations:
* [JXMapViewer](http://wiki.openstreetmap.org/wiki/JXMapViewer) to show the map and the track
* [xuggle library](http://www.xuggle.com/) to decode, encode and manipulate video files
* [iconeden](http://www.iconeden.com/icon/category/free) for the beautiful icon packs they offer


## Evolution of the application
Thought that it is a good idea to make some screenshots from time to time to see how the software evolves.
![20140412](https://raw.github.com/peregin/gps-overlay-on-video/master/doc/evolution/20140412.jpg "20140412")
