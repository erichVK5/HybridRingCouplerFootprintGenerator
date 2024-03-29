// HybridRingCouplerFootprintGenerator.java v1.0
// Copyright (C) 2015 Erich S. Heinzle, a1039181@gmail.com

//    see LICENSE-gpl-v2.txt for software license
//    see README.txt
//    
//    This program is free software; you can redistribute it and/or
//    modify it under the terms of the GNU General Public License
//    as published by the Free Software Foundation; either version 2
//    of the License, or (at your option) any later version.
//    
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with this program if not, write to the Free Software
//    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
//    
//    HybridRingCouplerFootprintGenerator.java
//    Copyright (C) 2015 Erich S. Heinzle a1039181@gmail.com



import java.lang.Math;
import java.io.*;
import java.util.Locale;
import java.text.*;

public class HybridRingCouplerFootprintGenerator
{

	public static void main(String[] args) throws IOException
	{

		// this should prevent continental locales from encountering commas
		// as decimal points in the mm dimensions generated by the utility
		Locale.setDefault(new Locale("en", "US"));

		// set a few defaults
                long segmentLength = 1000; // 1mm length in microns
                long trackWidth = 220; // microns
		long portTrackWidth = trackWidth; // microns
                double frequencyMhz = 1000;
		double velocityFactor = 1.0;

		// we'll need a flag or two
                boolean finishedLoop = false;
                boolean theOneTrueEDAsuiteGEDA = true;
		boolean portTrackWidthSpecified = false;
                	// obviously, !theOneTrueEDAsuiteGEDA = kicad :-)

		// we now parse arguments parsed via the command line
		if (args.length == 0)
		{
			printUsage();
			System.exit(0);
		}

		for (int counter = 0; counter < args.length; counter++)
		{
			if (args[counter].startsWith("-f"))
			{
				frequencyMhz = Integer.parseInt(args[counter+1]);
				counter++;
			}
                        else if (args[counter].startsWith("-l"))
                        {
                                segmentLength = Long.parseLong(args[counter+1]);
                                counter++;
                        }
                        else if (args[counter].startsWith("-w"))
                        {
                                trackWidth = Long.parseLong(args[counter+1]);
                                counter++;
                        }
			else if (args[counter].startsWith("-v"))
			{
				velocityFactor = Double.parseDouble(args[counter+1]);
				counter++;
				if (velocityFactor > 1.0)
				{
					System.out.println("Velocity factor > 1.0..." +
					" yer cannae change the laws of physics!!");
					velocityFactor = 1.0;
				}
			}
			else if (args[counter].startsWith("-p"))
			{
				portTrackWidth = Long.parseLong(args[counter+1]);
				portTrackWidthSpecified = true;
				counter++;
			}
                        else if (args[counter].startsWith("-k"))
                        {
                                theOneTrueEDAsuiteGEDA = false;
                        }
                        else 
                        {
                                printUsage();
				System.exit(0);
                        }
			
		}

		if (!portTrackWidthSpecified)
		{
			portTrackWidth = trackWidth;
		}

		// some preliminary calculations

		long speedOfLight = 299792458; // metres per second
		double wavelength = speedOfLight/(frequencyMhz); // v = f.lambda
				// by dividing m/s by a frequency in MHz, we get microns
				// this may need tweaking for electrical length 

		// we now correct for velocity factor, i.e. correct for electrical length
		wavelength = wavelength * velocityFactor;

		double quarterWave = wavelength/4; // in microns

                // we figure out the circumference in microns, well close enough
                long circumference = (long)(quarterWave * 6);

                double Radius = (circumference/Math.PI)/2.0;

		// now some preliminaries for the hybrid ring coupler
                // loop, we now sort out an appropriate  end theta, and
		// then appropriate angular step sizes for the loop

		double endTheta = (Math.PI*2.0);
		double oneThirdPI = (Math.PI/3.0);
                double nextTheta = 0;

		// we base segments per loop on the loop circumference
		// trying to have an integer multiple of segments per 2PI
		// radians 
		double segmentsPerLoop = Math.ceil(circumference/segmentLength);

		// we figure out a step size in radians to step around the loop
                // which is 2pi radians divided by number of segments
		double deltaTheta = (2.0 * Math.PI)/segmentsPerLoop;

		// we use x1,y1,x2,y2 as variables for the begining and end coords of line segments
		double x1 = 0;
		double y1 = 0;
		double x2 = 0;
		double y2 = 0;

		long layerNumber = 15; // front for kicad

		String moduleName = frequencyMhz + "MHz_hybrid_ring_coupler";

		String outputFileName = "";
	
		if (theOneTrueEDAsuiteGEDA)
		{
			outputFileName = moduleName + ".fp";
		}
		else //kicad
		{
			outputFileName = moduleName + ".mod";
		}

		System.out.println("Generating " + frequencyMhz + " MHz hybrid ring coupler:" +
			outputFileName);

		System.out.println("Using track width of: " + trackWidth + " microns.");

		File outputFile = new File(outputFileName);

		PrintWriter footprintOutput = new PrintWriter(outputFile);

		String headerString = "";

		if (theOneTrueEDAsuiteGEDA)
		{
			headerString = headerString +
				("Element[\"\" \"HybridRing\"" + 
				" \"\" \"\" 0 0 -1000 -1000 0 100 \"\"]" +
				"(\n");
		}
		else // kicad :-)
		{
                	headerString = headerString +
				"PCBNEW-LibModule-V1  mer 27 mar 2013 20:53:24 CET\n" +
                                "Units mm\n" +
                                "$INDEX\n" +
                                moduleName + "\n" +
                                "$EndINDEX\n" +
                                "$MODULE " + moduleName + "\n" +
                                "Po 0 0 0 15 51534DFF 00000000 ~~\n" +
                                "Li " + moduleName + "\n" +
                                "Cd " + moduleName + "\n" +
                                "Sc 0\n" +
                                "AR\n" +
                                "Op 0 0 0\n" +
                                "T0 0 -4134 600 600 0 120 N V 21 N \"S***\"\n";
		}

		footprintOutput.print(headerString);

                double trackWidthMM = trackWidth/1000.0;

		// and length of trace will allow coil resistance to be calculated
		double cumulativeCoilLengthMM = 0.0;

		double theta = 0.0;

		while (theta < endTheta)
		{
			nextTheta = theta + deltaTheta;	
			// we figure out the coordinates in mm as double variables 
			// gEDA will recognise "XXX.XXmm" as mm
			x1 = ((Radius * Math.cos(theta))/1000.0);
			y1 = ((Radius * Math.sin(theta))/1000.0);
                        x2 = ((Radius * Math.cos(nextTheta))/1000.0);
                        y2 = ((Radius * Math.sin(nextTheta))/1000.0);

                        // we add the segment length to the total coil length 
                        cumulativeCoilLengthMM += calculateSegmentLength(x1, y1, x2, y2);

			// for gEDA we have to produce a pad description of the form
			// Pad[X1 Y1 X2 Y2 Thickness Clearance Mask Name Number SFlags]

                                // for kicad we have to produce
                                // a Draw Segment "DS" string of the form
                                // "DS x1 y1 x2 y2 thickness layer"

			if (theOneTrueEDAsuiteGEDA)
			{
				footprintOutput.print(generateGEDApad(x1, y1, x2, y2, trackWidthMM));
			}
			else // kicad
			{
                                footprintOutput.print(generateKicadPad(x1, y1, x2, y2, trackWidthMM, layerNumber));
				// footprintOutput.format("DS %.3f %.3f %.3f %.3f", x1, y1, x2, y2);
				// footprintOutput.format(" %.3f ", trackWidthMM);
				// footprintOutput.println(layerNumber);
			}
			theta = nextTheta;
		}

		// we now create four input/output ports and then finish off the footprint
		// and we factor in a fatter input/output track width 'portTrackWidth' if specified

		for (int counter = 0; counter < 4; counter ++)
		{
			theta = counter * Math.PI/3.0;
	                x1 = (((Radius + (portTrackWidth - trackWidth)/2) * Math.cos(theta))/1000.0);
                	y1 = (-((Radius + (portTrackWidth - trackWidth)/2) * Math.sin(theta))/1000.0);
                        x2 = (((Radius + (portTrackWidth - trackWidth)/2 + 3000) * Math.cos(theta))/1000.0);
                        y2 = (-((Radius + (portTrackWidth - trackWidth)/2 + 3000) * Math.sin(theta))/1000.0);

			if (theOneTrueEDAsuiteGEDA) // :-)
			{
				footprintOutput.print(generateGEDApad(x1, y1, x2, y2, portTrackWidth/1000.0));
			}
			else // kicad
			{
	                        footprintOutput.print(generateKicadPad(x1, y1, x2, y2, portTrackWidth/1000.0, layerNumber));
			}
		}
                if (theOneTrueEDAsuiteGEDA) // :-)
                {
                        footprintOutput.println(")");
                }
                else // kicad
                {
                        footprintOutput.println("$EndMODULE " + moduleName);
                }


		System.out.println("Frequency of operation (MHz): " + frequencyMhz);
		System.out.println("Velocity factor used: " + velocityFactor);
		System.out.print("Total length of power divider arms (mm): ");
		System.out.format("%.4f\n", cumulativeCoilLengthMM);
		System.out.print("DC resistance of arms assuming copper resistivity = 1.75E-8 ohm.m\n\t35.56 micron copper thickness: ");
		System.out.format("%.4f ohm\n", (1.75E-8*(cumulativeCoilLengthMM/1000.0)/((trackWidthMM/1000.0)*(3.556E-5))));
                System.out.print("\t71.12 micron copper thickness: ");
                System.out.format("%.4f ohm\n", (1.75E-8*(cumulativeCoilLengthMM/1000.0)/((trackWidthMM/1000.0)*(7.112E-5))));

		// and we close the footprint file before finishing up
		footprintOutput.close();
	}

	private static String generateGEDApad(double X1, double Y1, double X2, double Y2, double trackWidthMM)
	{
		DecimalFormat myFormat = new DecimalFormat("#####.###");
		String assembledPad = "Pad[" +
			myFormat.format(X1) + "mm " +
			myFormat.format(Y1) + "mm " +
			myFormat.format(X2) + "mm " +
			myFormat.format(Y2) + "mm " +
                	myFormat.format(trackWidthMM) + "mm " + 
			"0.254mm " + 	// the clearance is 10mil
                	"0 " + 		// solder mask clearance is zero
                	"\"A\" " + 	// name of coil
                	"\"1\" " + 	// coil pad number
                	"\"\"]\n"; 	// name of coil
		return assembledPad;
	}

	private static String generateKicadPad(double X1, double Y1, double X2, double Y2, double trackWidthMM, long layerID)
        {
                DecimalFormat myFormat = new DecimalFormat("#####.###");
		String assembledPad = "DS " +
			myFormat.format(X1) + " " +
			myFormat.format(Y1) + " " +
			myFormat.format(X2) + " " +
			myFormat.format(Y2) + " " +
			myFormat.format(trackWidthMM) + " " +
			layerID + "\n";
                // for kicad we have to produce
                // a Draw Segment "DS" string of the form
                // "DS x1 y1 x2 y2 thickness layer"
		return assembledPad;
	}


	private static double calculateSegmentLength(double xOne, double yOne, double xTwo, double yTwo)
	{
		double lengthSquared = ((xOne - xTwo) * (xOne - xTwo))+((yOne - yTwo) * (yOne - yTwo));
		return Math.sqrt(lengthSquared);
	}

	private static void printUsage()
	{
		System.out.println("\nUsage:\n\n\t" +
			"java HybridRingCouplerFootprintGenerator -option value\n" +
			"\n\t\t-k\t\texport a kicad module, default is geda .fp file\n" +
                        "\n\t\t-f double\tfrequency of operation in Megahertz\n" +
                        "\n\t\t-w long\t\ttrack width in microns\n" +
			"\n\t\t-p long\t\tinput/output port track width in microns" +
			"\n\t\t\t\tdefault: port track width = track width\n" +
			"\n\t\t-v double\tvelocity factor <= 1.0" +
			"\n\t\t\t\tdefault: 1.0\n" +
			"\n\t\t-l long\t\tlength of segment in microns used to approximate a circle\n" +
			"\n\t\t-h\t\tprints this\n\n" +
			"Example usage:\n\n\t" +
			"java HybridRingCouplerFootprintGenerator -f 2400 -w 3000 -l 2000 -k -v 0.75 -p 4000\n\n\t" +
			"generates a hybrid ring coupler with four ports\n\t" +
			"spaced lambda/6 apart, assuming velocity factor of 0.75\n\t" +
			"with track width of 3000 microns, port track width of 4000\n\t" +
			"microns, using segment lengths of 2000 microns to create the\n\t" +
			"ring in a kicad module.\n");
	}
}
