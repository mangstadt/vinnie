# vinnie

|     |     |
| --- | --- |
| Continuous Integration: | [![](https://travis-ci.org/mangstadt/vinnie.svg?branch=master)](https://travis-ci.org/mangstadt/vinnie) |
| Code Coverage: | [![codecov.io](http://codecov.io/github/mangstadt/vinnie/coverage.svg?branch=master)](http://codecov.io/github/mangstadt/vinnie?branch=master) |
| Maven Central: | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mangstadt/vinnie/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mangstadt/vinnie) |
| Chat Room: | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mangstadt/vinnie?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) |
| Q & A: | [![Codewake](https://www.codewake.com/badges/ask_question.svg)](https://www.codewake.com/p/vinnie) |
| License: | [![MIT License](https://img.shields.io/badge/License-MIT-red.svg)](https://github.com/mangstadt/vinnie/blob/master/LICENSES) |

vinnie is a lightweight Java library that reads and writes "vobject" data (vCard and iCalendar).  It is used by the [ez-vcard](https://github.com/mangstadt/ez-vcard) and [biweekly](https://github.com/mangstadt/biweekly) projects.

<p align="center"><strong><a href="https://github.com/mangstadt/vinnie/wiki/Downloads">Downloads</a> |
<a href="http://mangstadt.github.io/vinnie/javadocs/latest/index.html">Javadocs</a> |
<a href="#mavengradle">Maven/Gradle</a> | <a href="https://github.com/mangstadt/vinnie/wiki">Documentation</a></strong></p>

# Examples

## Parsing

**Code**

```java
String str =
"BEGIN:VCARD\r\n" +
"VERSION:2.1\r\n" +
"FN:John Doe\r\n" +
"NOTE;QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!\r\n" +
"END:VCARD\r\n";

Reader reader = new StringReader(str);
SyntaxRules rules = SyntaxRules.vcard();
VObjectReader vobjectReader = new VObjectReader(reader, rules);
vobjectReader.parse(new VObjectDataAdapter() {
	private boolean inVCard = false;

	public void onComponentBegin(String name, Context context) {
		if (context.getParentComponents().isEmpty() && "VCARD".equals(name)){
			inVCard = true;
		}
	}

	public void onComponentEnd(String name, Context context) {
		if (context.getParentComponents().isEmpty()) {
			//end of vCard, stop parsing
			context.stop();
		}
	}

	public void onProperty(VObjectProperty property, Context context) {
		if (inVCard) {
			System.out.println(property.getName() + " = " + property.getValue());
		}
	}
});
vobjectReader.close();
```

**Output**

```
FN = John Doe
NOTE = ¡Hola, mundo!
```

## Writing

**Code**

```java
Writer writer = new OutputStreamWriter(System.out);
VObjectWriter vobjectWriter = new VObjectWriter(writer, SyntaxStyle.OLD);

vobjectWriter.writeBeginComponent("VCARD");
vobjectWriter.writeVersion("2.1");
vobjectWriter.writeProperty("FN", "John Doe");

VObjectProperty note = new VObjectProperty("NOTE", "¡Hola, mundo!");
note.getParameters().put(null, "QUOTED-PRINTABLE");
vobjectWriter.writeProperty(note);

vobjectWriter.writeEndComponent("VCARD");
vobjectWriter.close();
```

**Output**

```
BEGIN:VCARD
VERSION:2.1
FN:John Doe
NOTE;QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!
END:VCARD
```

# Features

 * Full ABNF compliance with vCard (versions 2.1, 3.0, and 4.0) and iCalendar (versions 1.0 and 2.0) specifications.
 * Automatic decoding/encoding of quoted-printable data.
 * Streaming API.
 * Extensive unit test coverage.
 * Low Java version requirement (1.5 or above).
 * No dependencies on external libraries.

# Maven/Gradle


**Maven**

```xml
<dependency>
   <groupId>com.github.mangstadt</groupId>
   <artifactId>vinnie</artifactId>
   <version>2.0.1</version>
</dependency>
```

**Gradle**

```
compile 'com.github.mangstadt:vinnie:2.0.1'
```

# Build Instructions

vinnie uses [Maven](http://maven.apache.org/) as its build tool, and adheres to its conventions.

To build the project: `mvn compile`  
To run the unit tests: `mvn test`  
To build a JAR: `mvn package`

# Questions / Feedback

You have some options:

 * Post an [issue](https://github.com/mangstadt/vinnie/issues)
 * [Gitter chat room](https://gitter.im/mangstadt/vinnie)
 * [codewake Q&A forum](https://www.codewake.com/p/vinnie)
 * Email me directly: [mike.angstadt@gmail.com](mailto:mike.angstadt@gmail.com)

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=vinnie&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
