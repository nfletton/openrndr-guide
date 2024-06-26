@file:Suppress("UNUSED_EXPRESSION")
@file:Title("Kinect")
@file:ParentTitle("ORX")
@file:Order("120")
@file:URL("ORX/kinect")

package docs.`80_ORX`

import org.openrndr.application
import org.openrndr.dokgen.annotations.*
import org.openrndr.extra.kinect.v1.Kinect1

fun main() {
    @Text """
    # orx-kinect
    
    Provides Kinect support (only Kinect version 1 at the moment and 
    only depth camera).
    Source and extra documentation can be found in the 
    [orx sourcetree](https://github.com/openrndr/orx/tree/master)

    Note: the support is split into several modules:

    * `orx-kinect-common`
    * `orx-kinect-v1`
    * [`orx-kinect-v1-demo`](https://github.com/openrndr/orx/tree/master/orx-jvm/orx-kinect-v1-demo/src/main/kotlin)
    * `orx-kinect-v1-natives-linux-x64`
    * `orx-kinect-v1-natives-macos`
    * `orx-kinect-v1-natives-windows`
    
    ## Prerequisites
    
    Assuming you are working on an 
    [`openrndr-template`](https://github.com/openrndr/openrndr-template) based
    project, all you have to do is enable `orx-kinect-v1` in the `orxFeatures`
    set in `build.gradle.kts` and reimport the gradle project.
    
    ## Using the Kinect depth camera
    """

    @Code
    application {
        configure {
            // default resolution of the Kinect v1 depth camera
            width = 640
            height = 480
        }
        program {
            val kinect = Kinect1()
            val device = kinect.openDevice()
            device.depthCamera.flipH = true // to make a mirror
            device.depthCamera.enabled = true
            extend(kinect)
            extend {
                drawer.image(device.depthCamera.currentFrame)
            }
        }
    }

    @Text 
    """
    Note: depth values are mapped into `0-1` range and stored on a 
    `ColorBuffer` containing only RED color channel.
    
    ## Mirroring depth camera image
    
    ```kotlin 
    kinect.depthCamera.mirror = true 
    ```
    
    ## Using multiple Kinects
    
    The `kinects.startDevice()` can be supplied with device number 
    (`0` by default):
    """

    @Code
    application {
        configure {
            width = 640 * 2
            height = 480
        }
        program {
            val kinect = Kinect1()
            val depthCamera1 = kinect.openDevice(0).depthCamera
            val depthCamera2 = kinect.openDevice(1).depthCamera
            depthCamera1.enabled = true
            depthCamera1.flipH = true
            depthCamera2.enabled = true
            depthCamera2.flipH = true
            extend(kinect)
            extend {
                drawer.image(depthCamera1.currentFrame)
                drawer.image(depthCamera2.currentFrame, depthCamera1.resolution.x.toDouble(), 0.0)
            }
        }
    }

    @Text 
    """
    ## Reacting only to the latest frame from the Kinect camera
    
    Kinect is producing 30 frames per second, while screen refresh rates 
    are usually higher.
    Usually, if the data from the depth camera is processed, it is desired 
    to react to the latest Kinect frame only once:

    ```kotlin
    kinect.depthCamera.getLatestFrame()?.let { frame ->
        myFilter.apply(frame, outputColorBuffer)
    }
    ```
    
    ## Using color map filters

    Raw kinect depth data might be visualized in several ways, the following 
    filters are included:

    * `DepthToGrayscaleMapper`
    * `DepthToColorsZucconi6Mapper` - [Colors of natural light dispersion](https://www.alanzucconi.com/2017/07/15/improving-the-rainbow/) by Alan Zucconi
    * `DepthToColorsTurboMapper` - [Turbo, An Improved Rainbow Colormap for Visualization](https://ai.googleblog.com/2019/08/turbo-improved-rainbow-colormap-for.html) by Google

    [Find a runnable example here](https://github.com/openrndr/orx/blob/master/orx-jvm/orx-kinect-v1-demo/src/main/kotlin/Kinect1Demo03DepthToColorMaps.kt). 

    ## Executing native freenect commands

    This kinect support is built on top of the [freenect](https://github.com/OpenKinect/libfreenect)
    library. Even though the access to freenect is abstracted, it is still possible to execute
    [low level freenect commands](https://github.com/openrndr/orx/blob/master/orx-jvm/orx-kinect-v1-demo/src/main/kotlin/Kinect1Demo07NativeFreenectCommands.kt)
    in the native API.

    """
}