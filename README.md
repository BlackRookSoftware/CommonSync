# Black Rook Common Sync

Copyright (c) 2009-2019 Black Rook Software. All rights reserved.  
[http://blackrooksoftware.com/projects.htm?name=commonsync](http://blackrooksoftware.com/projects.htm?name=commonsync)  
[https://github.com/BlackRookSoftware/CommonSync](https://github.com/BlackRookSoftware/CommonSync)

### Required Libraries

Black Rook Commons 2.32.0+  
[https://github.com/BlackRookSoftware/Common](https://github.com/BlackRookSoftware/Common)

### Introduction

This library contains some utility classes for passive monitoring and
thread pooling.

### Library

Contained in this release is a series of classes used for creating thread
pools and doing other things with process timings and stats. The javadocs 
contain basic outlines of each package's contents.

NOTE: There was never a version 1.0 of this project - this used to be a part of
Commons 1.0, and is called 2.0 just for the sake of consistency.

### Compiling with Ant

To download the dependencies for this project (if you didn't set that up yourself already), type:

	ant dependencies

A *build.properties* file will be created/appended to with the *dev.base* property set.
	
To compile this library with Apache Ant, type:

	ant compile

To make a JAR of this library, type:

	ant jar

And it will be placed in the *build/jar* directory.

### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
