# hawkbit-pi-ledstripe-controller
Controlling WS2812 LED-Stripe via hawkBit


This controller is based on the [Pi-WS2812 Project](https://github.com/entrusc/Pi-WS2812) which is working with a Raspberry Pi V1 with the GPIO PWM port #18.

Each LED of a stripe represents a connected hawkBit device which can be assigned software-modules to it containing a file with JSON content defining the RGB values of the LED.
```
{
 "r" : 0,
 "g" : 0,
 "b" : 128
}
``` 
