# Symplectic Elements Harvester Extensions for VIVO

This is a 

## Prerequisites

You must have the Java Development Kit and Maven 2+ installed.

## Usage

Once you have cloned this repository, start a command line / terminal, and navigate to the 'dependencies' directory. From here, run the following command:

	mvn install

This will configure your local Maven with all of the dependencies that are not in the central Maven repository, along with the core code from the VIVO harvester project.

## Development Environment

Typically, I use IntelliJ IDEA for development (there is a free community edition as well as the commercial release).
Among the many nice features, is direct support for Maven projects. So, once you have installed the dependencies (above), all you need to do is open the pom.xml file from the elements-harvester directory.
This will create a project, providing access to all of the extension code, and setting up all of the dependencies as external libraries.

### Running the Harvest from IntelliJ IDEA.

The harvest script (run-elements.sh) breaks down into a number of components - harvesting Elements, loading a temporary triple-store, creating changesets and applying those changes to VIVO.

It is the first step - harvesting Elements - that you will mostly be working with in development. This step queries the Elements API to retrieve all of the objects, and translates them into VIVO model RDF files.

As such, it is useful to have this step setup for execution within the IDE.

In IntelliJ IDEA, what you should do is:

1. Open the drop-down next to the run / debug buttons in the tool bar.
2. Select 'edit configurations'
3. In the dialog, click the + icon at the top of the tree on the left. Choose 'Application'
4. On the right, change the name to 'ElementsFetch'
5. Set the main class to: uk.co.symplectic.vivoweb.harvester.fetch.ElementsFetch
6. Set program arguemnts to: -X elements.config.xml
7. Set working directory to: <project dir>/example-scripts/example-elements
8. Save this configuration (click OK).

You should now be able to run and/or debug the Elements harvester. You will need to ensure that the "elements.config.xml" file inside example-scripts/example-elements has been configured correctly,
and that you have an Elements instance with a running API endpoint that has been configured for you to access.

When you run this configuration, it will create a 'data' subdirectory within 'example-scripts/example-elements'. Inside the 'data' directory will be 'raw-records' (containing the data as it is retrieved from the API),
and 'translated-records' (containing the VIVO model RDF).

### Developing Translations of Elements to VIVO model

As each installation of Elements will capture data in slightly different ways, the key customization for anyone wanting to implement a VIVO instance with Symplectic Elements will be the translation of the Elements data to the VIVO model.

The Elements API returns records in an XML format, and XSLT is used to convert that to the RDF model.
With IntelliJ IDEA, it is possible to run the XSLT translations directly within the IDE, and even use a step-by-step debugger on the translation (if you have the commercial version).

In order to do so, you should first run an ElementsFetch (to obtain the data/raw-records directory) and then:

1. Open the drop-down next to the run / debug buttons in the tool bar.
2. Select 'Edit configurations'
3. In the dialog, click the + icon at the top of the tree on the left. Choose 'XSLT'
4. On the right, give this configuration a name
5. Set XSLT script file to: <project dir>/example-scripts/example-elements/symplectic-to-vivo.datamap.xsl
6. Set Choose XML input file to: <project dir>/example-scripts/example-elements/data/raw-records/.... (choose a user / publication / relationship file, depending on the translation you are working on)
7. Uncheck the 'Make' checkbox (you don't need to rebuild the code when running the XSLT translation).
8. Save this configuration (click OK).

You will now be able to run this translation, and the results will appear in a 'console' tab.

## Packaging and Deployment

When you are ready to move from your workstation to a server (either test or production), then you will need to package up the Elements harvester extensions, and install them on the server.

Start by opening a command prompt / terminal. Navigate to where you cloned the VIVO harvester project, and into the 'elements-harvester' subdirectory. From here, run:

	mvn clean package
	
Once it finishes executing, a .tar.gz file will be created in the 'target' directory. This file contains everything from the 'bin' and 'example-scripts' directory, along with the compiled Java code.

To install the Elements harvester on a server, first download the full VIVO Harvester 1.5 release package, from: http://sourceforge.net/projects/vivo/files/VIVO%20Harvester/

Extract this package to a directory on your server. Next, extract the elements-harvester .tar.gz, and copy the 'bin' and 'example-scripts' directories into the location where you extracted the VIVO Harvester pacakage.
You want to merge these with the existing directories, so the content of the elements-harvester 'bin' directory is added to the contents of the VIVO harvester 'bin' directory, etc.

Then, go to the example-scripts/example-elements directory. Checking that your elements.config.xml is correctly configured for your Elements instance, and vivo.model.xml is correctly configured for your VIVO instance, you can run:

	./run-elements.sh
	
To perform the harvest, and apply the data to your VIVO instance. For the first run, your VIVO instance should be empty before you start.

Subsequent exections of ./run-elements.sh will perform differential updates - but ONLY if you retain the 'previous-harvest' directory that is created.
If the 'previous-harvest' directory gets removed, then you should start again with a clean VIVO instance.

If you wish to clear down your VIVO instance and start again from scratch, you will need to remove the 'previous-harvest' directory.

## Acknowledgements

The first release of the Elements-VIVO Harvester extensions was developed by Ian Boston, and can be found at: https://github.com/ieb/symplectic-harvester