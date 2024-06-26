@file:Suppress("UNUSED_EXPRESSION")
@file:Title("Basic animation")
@file:ParentTitle("Animation")
@file:Order("50")
@file:URL("animation/basicAnimations")

package docs.`50_Animation`

import org.openrndr.application
import org.openrndr.dokgen.annotations.*


fun main() {

    @Text
    """
    # Basic animation

    From a programmer's perspective, animation works by continuously drawing shapes or images 
    creating the illusion of movement. This is achieved through a draw loop, 
    which is a function that runs repeatedly multiple times per second. 
    Each iteration of the draw loop involves clearing the screen and redrawing all elements in their new positions. 
    By making small changes to the positions, rotations or sizes of shapes in each frame, 
    we can create complex animations and movements.
    
    In this example we draw a circle moving horizontally.
    The `x` variable increases from 0 up to `width`
    in a loop, thanks to the `%` modulo operation. `width` is 640 by default.
    """

    @Code
    application {
        program {
            extend {
                val pixelsPerSecond = 100.0
                val x = (seconds * pixelsPerSecond) % width
                val y = height * 0.5
                drawer.circle(x, y, 100.0)
            }
        }
    }

    @Text
    """
    By modifying the speed while the animation progresses we can enhance
    the animation, making it feel more natural and fluid and
    mimicking the way objects in the real world accelerate or decelerate 
    as they move. 
    
    Let's start with a one second long animation loop:
    """

    @Code
    application {
        program {
            extend {
                val x = (seconds % 1.0) * width
                val y = height * 0.5
                drawer.circle(x, y, 100.0)
            }
        }
    }

    @Text
    """
    Note that the expression `(seconds % 1.0)` is always a value
    between 0.0 and 1.0. What happens if we change that expression to
    `(seconds % 1.0).pow(3.0)` instead? The result is still a value
    between 0.0 and 1.0, but by raising the expression to the power of 3.0
    we obtain more values closer to 0.0 than to 1.0. 
    We no longer have a linear animation, but an `ease in`
    effect: start slowly and accelerate.
    
    To simplify calculating acceleration and deceleration curves 
    for animations we can use easing functions to
    specify the rate of change of a parameter over time.
    
    Fortunately OPENRNDR provides
    [orx-easing](https://github.com/openrndr/orx/tree/master/orx-easing)
    to help with this task.
    """
}