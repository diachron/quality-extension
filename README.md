## Quality-extension
[![Build Status](https://travis-ci.org/diachron/quality-extension.svg?branch=master)](https://travis-ci.org/diachron/quality-extension)

Extension for OpenRefine im context of Task 3.3 (Data cleaning ) WP3

In order to use the OpenRefine quality extension you must follow either of the given bellow instructions.

### Install the extension
1. Download [quality-extension.zip](https://github.com/diachron/quality-extension/blob/master/quality-extension.zip) from the quality-extension project.
2. Follow [OpenRefine instructions](https://github.com/OpenRefine/OpenRefine/wiki/Installing-Extensions) on installing extensions.

### Build the extension
1. Check out the OpenRefine project.

  ```
  git clone https://github.com/OpenRefine/OpenRefine
  ```
2. Check out the quality extension project in the *extension* directory in the OpenRefine project.

  ```
  git clone https://github.com/diachron/quality-extension.git
  ```
3. Add the quality extension clean and build ant targets to the build.xml file in the *extension* directory.

  ```
  <project name="google-refine-extensions" default="build" basedir=".">
    <target name="build">
      <ant dir="quality-extension/" target="build" />
    </target>

    <target name="clean">
      <ant dir="quality-extension/" target="clean" />
    </target>
  </project>
  ```
4. Run ```./refine build``` from the OpenRefine root directory.
5. Run ```./refine```. 
