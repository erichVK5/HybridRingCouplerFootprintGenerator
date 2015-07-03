# HybridRingCouplerFootprintGenerator
A Hybrid Ring Coupler Footprint Generator for gEDA and Kicad.


Those wishing to use the utility will need to install the java compiler javac, and a compatible java virtual machine (JVM).

Having cloned the git repository to a local directory, i.e.

	git clone https://github.com/erichVK5/HybridRingCouplerFootprintGenerator

	cd HybridRingCouplerFootprintGenerator

You can then compile the java source code:

	javac HybridRingCouplerFootprintGenerator.java

Tips:

The utility adds input and output pads in the final four lines of the footprint file. Users will need to specify their dimensions to achieve the required stripline impedance relative to the hybrid ring.

Users may wish to experiment with segment lengths to achieve a suitably rounded contour, and dimensions should be verified before sending off designs for fabrication.

The velocity factor of the PCB material will affect the dimensions and electrical length of the hybrid ring.


Usage:

	java HybridRingCouplerFootprintGenerator -option value

		-k			export a kicad module, default is geda .fp file

		-f double	frequency of operation in Megahertz

		-w long		track width in microns

		-p long		input/output port track width in microns
					default: port track width = track width

		-v double	velocity factor <= 1.0
					default: 1.0

		-l long		length of segment in microns used to approximate a circle

		-h			prints this

Example usage:

	java HybridRingCouplerFootprintGenerator -f 2400 -w 3000 -l 2000 -k -v 0.75 -p 4000

	generates a hybrid ring coupler with four ports
	spaced lambda/6 apart, assuming velocity factor of 0.75
	with track width of 3000 microns, port track width of 4000
	microns, using segment lengths of 2000 microns to create the
	ring in a kicad module.

